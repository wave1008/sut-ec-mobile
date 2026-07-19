-- 全ドメインスキーマ。S1(catalog)/S2(auth)/S3(user data) が使う。Exposed の db/Tables.kt と一致必須。
create table categories (
    id         text primary key,
    name_ja    text not null,
    name_en    text not null,
    emoji      text not null,
    sort_order int  not null default 0
);

create table products (
    id              text primary key,
    seq             int  not null,
    name_ja         text not null,
    name_en         text not null,
    brand_ja        text not null,
    brand_en        text not null,
    description_ja  text not null,
    description_en  text not null,
    price_yen       int  not null,
    list_price_yen  int,
    category_id     text not null references categories(id),
    rating          double precision not null,
    review_count    int  not null,
    in_stock        boolean not null default true
);
create index idx_products_category on products(category_id);
create index idx_products_seq on products(seq);

create table product_images (
    id         bigserial primary key,
    product_id text not null references products(id),
    url_path   text not null,
    position   int  not null
);
create index idx_product_images_product on product_images(product_id);

create table product_tags (
    product_id text not null references products(id),
    tag        text not null,
    primary key (product_id, tag)
);

create table reviews (
    id          text primary key,
    product_id  text not null references products(id),
    author_name text not null,
    rating      int  not null,
    title_ja    text not null,
    title_en    text not null,
    body_ja     text not null,
    body_en     text not null,
    date        text not null
);
create index idx_reviews_product on reviews(product_id);

create table users (
    id            uuid primary key,
    name          text not null,
    email         text not null unique,
    password_hash text not null,
    created_at    timestamptz not null default now()
);

create table addresses (
    id          text primary key,
    user_id     uuid not null references users(id),
    full_name   text not null,
    postal_code text not null,
    prefecture  text not null,
    city        text not null,
    line1       text not null,
    line2       text not null default '',
    phone       text not null,
    is_default  boolean not null default false
);
create index idx_addresses_user on addresses(user_id);

create table payment_methods (
    id          text primary key,
    user_id     uuid not null references users(id),
    type        text not null,
    brand       text not null default '',
    last4       text not null default '',
    holder_name text not null default '',
    exp_month   int  not null default 0,
    exp_year    int  not null default 0,
    is_default  boolean not null default false
);
create index idx_payment_methods_user on payment_methods(user_id);

create table carts (
    user_id uuid primary key references users(id)
);

create table cart_items (
    cart_user_id uuid not null references carts(user_id),
    product_id   text not null references products(id),
    quantity     int  not null,
    primary key (cart_user_id, product_id)
);

create table wishlist_items (
    user_id    uuid not null references users(id),
    product_id text not null references products(id),
    primary key (user_id, product_id)
);

create table orders (
    id                    text primary key,
    user_id               uuid not null references users(id),
    status                text not null,
    placed_at             text not null,
    created_at            timestamptz not null default now(),
    subtotal_yen          int  not null,
    shipping_yen          int  not null,
    tax_yen               int  not null,
    shipping_address_json jsonb not null,
    payment_label         text not null
);
create index idx_orders_user on orders(user_id);

create table order_items (
    id           bigserial primary key,
    order_id     text not null references orders(id),
    position     int  not null,
    product_json jsonb not null,
    quantity     int  not null
);
create index idx_order_items_order on order_items(order_id);
