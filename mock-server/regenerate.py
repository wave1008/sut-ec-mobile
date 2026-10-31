#!/usr/bin/env python3
# regen_prompts.json の画像を FLUX.1-schnell(Apache-2.0)のローカル実行で作り直す。macOS + mflux 前提。
#   MFLUX_BIN=<venv>/bin/mflux-generate ./regenerate.py   generated/ に生成(既存はスキップ)
#   ... ./regenerate.py --seed-offset 1 a.jpg b.jpg       指定分だけ別シードで再生成
#   ./regenerate.py --dry-run                             対象とプロンプトの表示のみ
#   ./regenerate.py --apply [a.jpg b.jpg]                 目視確認後、images/ へ上書きし image-provenance.json を更新(既定は全件)
# Pollinations の匿名枠は使わない: 45秒に1枚の制限があり、モデルが sana 固定で透かしも入る。
import json, os, shutil, subprocess, sys, time

HERE = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(HERE, "images")
OUT_DIR = os.path.join(HERE, "generated")
PROMPTS = os.path.join(HERE, "regen_prompts.json")
PROVENANCE = os.path.join(HERE, "image-provenance.json")
MODEL = "dhairyashil/FLUX.1-schnell-mflux-4bit"  # 約9GB, 実行時メモリ約19GB, 1024px で約25秒/枚
VIEWS = ["front view", "three-quarter view", "top-down flat lay", "close-up detail shot"]
STYLE = "realistic e-commerce product photo, soft studio lighting, plain light background, no text, no logo, no watermark"

targets = {}
for pid, it in json.load(open(PROMPTS, encoding="utf-8")).items():
    for slot in it["slots"]:
        targets[f"{pid}-{slot}.jpg"] = f"{it['prompt']}, {VIEWS[slot - 1]}, {STYLE}"

def generate(name, prompt, offset):
    png = os.path.join(OUT_DIR, name[:-4] + ".png")
    # シミュレータ等と GPU を共有すると Metal Internal Error (00000206) が断続的に出る。小バッファ+待機再試行で凌ぐ
    env = {**os.environ, "MLX_MAX_OPS_PER_BUFFER": "1", "MLX_MAX_MB_PER_BUFFER": "100"}
    for attempt in range(6):
        r = subprocess.run([os.environ["MFLUX_BIN"], "--model", MODEL, "--base-model", "schnell", "--steps", "4",
                            "--seed", str(sum(map(ord, name)) + offset), "--width", "1024", "--height", "1024",
                            "--output", png, "--prompt", prompt], env=env, capture_output=True, text=True)
        if r.returncode == 0:
            break
        print(f"  fail {name} attempt {attempt}: {r.stderr[-120:]}", flush=True)
        time.sleep(60)
    else:
        sys.exit(f"GIVEUP {name}")
    subprocess.run(["sips", "-s", "format", "jpeg", "-s", "formatOptions", "85", png, "--out", os.path.join(OUT_DIR, name)],
                   check=True, capture_output=True)  # .jpg 名で配信するため JPEG 化が必須
    os.remove(png)
    print(f"  ok {name}", flush=True)

def apply(names):
    prov = json.load(open(PROVENANCE, encoding="utf-8"))
    by_file = {x["file"]: x for x in prov["served_exempt"]}
    done = names or [n for n in targets if os.path.exists(os.path.join(OUT_DIR, n))]
    for n in done:
        shutil.copyfile(os.path.join(OUT_DIR, n), os.path.join(IMG_DIR, n))
        by_file[n].update(source=f"local:{MODEL}", license="AI-generated (model: Apache-2.0)", author="", url="", **{"class": "exempt"})
    json.dump(prov, open(PROVENANCE, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"APPLIED {len(done)}/{len(targets)}")

if "--dry-run" in sys.argv:
    for n, p in targets.items():
        print(n, p)
    print(f"TARGETS {len(targets)}")
elif "--apply" in sys.argv:
    apply([a for a in sys.argv[1:] if a != "--apply"])
else:
    os.makedirs(OUT_DIR, exist_ok=True)
    args = sys.argv[1:]
    offset = 0
    if "--seed-offset" in args:
        i = args.index("--seed-offset")
        offset = int(args[i + 1])
        del args[i:i + 2]
    names = args or [n for n in targets if not os.path.exists(os.path.join(OUT_DIR, n))]
    for n in names:
        generate(n, targets[n], offset)
    print(f"DONE {len(names)}")
