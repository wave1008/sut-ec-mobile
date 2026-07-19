-- S0: Flyway 配線検証用のメタテーブル。ドメインスキーマは S1 の V2 で追加。
create table if not exists app_meta (
    key   text primary key,
    value text not null
);
insert into app_meta(key, value) values ('schema_stage', 'S0')
on conflict (key) do nothing;
