#!/usr/bin/env python3
# Openverse で埋まらなかったスロットを Pollinations(AI生成)で補完し、ローカル保存する。
# 実写を優先しつつ、欠品ゼロを保証するためのフォールバック。
import json, os, sys, time, urllib.parse, urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
IMG_DIR = os.path.join(HERE, "images")
MAP = os.path.join(HERE, "imgmap.json")
UA = "sut-ec-mobile-mock/1.0"
items = json.load(open(MAP, encoding="utf-8"))

def get(url, timeout=90):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    return urllib.request.urlopen(req, timeout=timeout).read()

filled = 0
for it in items:
    pid, kw, cnt = it["id"], it["keyword"], it["count"]
    for slot in range(1, cnt + 1):
        dest = os.path.join(IMG_DIR, f"{pid}-{slot}.jpg")
        if os.path.exists(dest) and os.path.getsize(dest) > 5000:
            continue
        prompt = f"{kw}, product photo, white background, studio lighting, centered, high detail"
        enc = urllib.parse.quote(prompt)
        seed = (abs(hash(pid)) % 100000) + slot
        url = f"https://image.pollinations.ai/prompt/{enc}?width=600&height=600&nologo=true&seed={seed}"
        # レート制限対策: 最大4回リトライ + 指数バックオフ、成功まで粘る。
        ok = False
        for attempt in range(4):
            try:
                b = get(url, 120)
                if len(b) > 5000:
                    open(dest, "wb").write(b); filled += 1; ok = True
                    print(f"  filled {os.path.basename(dest)} ({len(b)}B)", flush=True)
                    break
                else:
                    print(f"  small {pid}-{slot} attempt {attempt}", flush=True)
            except Exception as e:
                print(f"  fail {pid}-{slot} attempt {attempt}: {str(e)[:50]}", flush=True)
            time.sleep(4 * (attempt + 1))
        if not ok:
            print(f"  GIVEUP {pid}-{slot}", flush=True)
        time.sleep(2)
print(f"FILLED {filled}")
