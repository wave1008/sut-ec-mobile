# UI テスト用 testTag 規約

UI テスト(Compose UI テスト / foundation-tester 実機・シミュ)が要素を安定して特定・assert できるようにするための `Modifier.testTag(...)` 命名規約。**この規約は実装と同期が必要な契約。** 追加・変更したら本ファイルの「登録済みタグ」表も更新する。

## 露出設定(必須・1箇所)

ルート([App.kt](../../composeApp/src/commonMain/kotlin/com/sutec/mobile/App.kt))の最上位 Scaffold に `Modifier.exposeTestTagsAsResourceId()` を設定済み。これで testTag が Android の resource-id / iOS の accessibilityIdentifier としてプラットフォームのアクセシビリティツリーに露出し、foundation-tester から参照できる。**この設定を外すと実機ドライブでタグが見えなくなる。**

`testTagsAsResourceId` は Android 専用 API のため commonMain から直接呼べず、expect/actual で分離している([util/TestTags.kt](../../composeApp/src/commonMain/kotlin/com/sutec/mobile/util/TestTags.kt) = expect、Android actual = `semantics{testTagsAsResourceId=true}`、iOS actual = no-op)。

補足(ビルド構成): Compose Multiplatform 1.11.0 は iosX64(Intel Mac シミュレータ)を非配信のため、composeApp / shared とも iosX64 ターゲットは宣言しない(iosArm64 + iosSimulatorArm64 のみ)。

## 命名スキーム(snake_case + 型プレフィックス)

| 種別 | 形式 | 例 |
|---|---|---|
| ボタン・操作 | `btn_<action>` | `btn_checkout`, `btn_login`, `btn_place_order`, `btn_add_to_cart`, `btn_save` |
| 下タブ | `tab_<name>` | `tab_home`, `tab_cart` |
| テキスト入力 | `field_<name>` | `field_email`, `field_password`, `field_search` |
| フィルタ/選択チップ | `chip_<key>` | `chip_sort_price_asc`, `chip_category_shoes` |
| リスト項目(動的・id 付き) | `<entity>_<card\|row>_<id>` | `product_card_${id}`, `cart_item_${id}`, `order_row_${id}` |
| リスト項目内の操作(動的) | `btn_<action>_<id>` | `btn_wishlist_${id}` |
| 画面ルート | `screen_<name>` | `screen_home`, `screen_cart` |
| assert 対象の重要テキスト | `text_<name>` | `text_cart_total`, `text_order_status` |

規則:
- タグは**言語非依存の固定文字列**。表示ラベル(i18n)を tag に使わない。
- 動的 id には `product.id` / `order.id` 等の**サーバー一意 id** を使う(表示名は使わない)。
- 同一画面内でタグは一意。リスト項目は id 付きで衝突回避。

## セマンティクス(到達性・状態 assert 用)

- **トグル(お気に入り等)**: `contentDescription` を状態で出し分け済みに加え、`Modifier.semantics { stateDescription = ... }` で on/off を assert 可能にする。role は IconButton が Button を付与。
- **フィルタチップ**: `FilterChip` が `selected` セマンティクスを付与するため `isSelected`/`isOn` で assert 可能。tag のみ付ければよい。
- **カードのマージ禁止**: 商品カード等、内部に独立操作(お気に入りボタン)を含む要素に `mergeDescendants = true` を付けない。マージすると内部ボタンがマージ木で単独ノードでなくなり、実機ツリーから tag 参照できなくなる。カード root と内部ボタンにそれぞれ tag を付ける方針。
- マージしてよいのは**内部に操作を持たない純表示グループ**のみ(価格+評価ブロック等)。

## 登録済みタグ(実装と同期)

グローバル(共有コンポーネント):

| tag | 場所 | 備考 |
|---|---|---|
| `btn_back` | AppTopBar | 戻る |
| `tab_<name>` | AppBottomBar | home/search/cart/wishlist/account |
| `btn_qty_decrement` / `btn_qty_increment` | QuantityStepper | 数量増減 |
| `btn_wishlist_${id}` | ProductCard / ProductRow | カード内トグル(id 付き) |
| `product_card_${id}` | ProductCard root | 一覧カード |
| `product_row_${id}` | ProductRow root | 一覧行 |
| `view_loading` | StateViews.LoadingState | 読み込み中 |
| `view_error` / `btn_retry` | StateViews.ErrorState | 失敗表示 + 再試行 |
| `btn_wishlist_toggle` | ProductDetailScreen | 単一商品トグル(id 不要) |

EmptyState は `actionTestTag` 引数でアクションボタンに tag を付与できる。SearchField / AppFilterChip / AppButtons は `modifier` を内部の実体へ forward するので、呼び出し側が `Modifier.testTag(...)` を渡してタグ付けする。

画面別タグ(各画面実装時に追記):

| tag | 場所 | 備考 |
|---|---|---|
| `screen_catalog` | CatalogScreen | 画面ルート |
| `btn_search` | CatalogScreen | 検索画面を開く |
| `chip_sort_${option.name.lowercase()}` | CatalogScreen | ソートチップ(relevance/price_asc/price_desc/rating/newest) |
| `screen_search` | SearchScreen | 画面ルート |
| `field_search` | SearchScreen | SearchField |
| `chip_category_${category.id}` | SearchScreen | カテゴリ絞り込みチップ |
| `chip_price_${preset.name.lowercase()}` | SearchScreen | 価格帯チップ(under_1000/r1000_5000/r5000_20000/over_20000) |
| `chip_sort` | SearchScreen | ソートチップ(単一・タップで循環) |
| `screen_product_detail` | ProductDetailScreen | 画面ルート |
| `btn_add_to_cart` | ProductDetailScreen | カートに追加 |
| `text_price` | ProductDetailScreen | 価格表示 |
| `screen_wishlist` | WishlistScreen | 画面ルート |
| `screen_home` | HomeScreen | 画面ルート |
| `screen_login` / `field_email` / `field_password` / `btn_login` / `btn_goto_signup` | LoginScreen | ログイン |
| `screen_signup` / `field_name` / `field_email` / `field_password` / `btn_signup` / `btn_goto_login` | SignupScreen | 新規登録 |
| `screen_cart` / `cart_item_${id}` / `btn_remove_${id}` / `text_cart_total` / `btn_checkout` | CartScreen | カート |
| `screen_checkout` / `address_row_${id}` / `payment_row_${id}` / `text_order_total` / `btn_place_order` | CheckoutScreen | チェックアウト |
| `screen_order_confirmation` / `text_order_id` / `btn_view_order` / `btn_continue_shopping` | OrderConfirmationScreen | 注文完了 |
| `screen_orders` / `order_row_${id}` | OrdersScreen | 注文履歴 |
| `screen_order_detail` / `text_order_status` | OrderDetailScreen | 注文詳細(status は OrderStatusChip 呼出側 modifier で付与) |
| `screen_account` / `btn_orders` / `btn_addresses` / `btn_payments` / `btn_login` / `btn_logout` / `btn_toggle_language_ja` / `btn_toggle_language_en` | AccountScreen | アカウント。MenuRow に `testTag` 引数を追加 |
| `screen_addresses` / `address_row_${id}` / `btn_add_address` / `btn_edit_${id}` / `btn_delete_${id}` | AddressesScreen | 住所一覧 |
| `screen_address_edit` / `field_full_name` / `field_postal_code` / `field_prefecture` / `field_city` / `field_address_line` / `field_address_line2` / `field_phone` / `btn_save` | AddressEditScreen | 住所編集 |
| `screen_payment_methods` / `payment_row_${id}` / `btn_add_payment` / `btn_edit_${id}` / `btn_delete_${id}` | PaymentMethodsScreen | 支払い方法一覧 |
| `screen_payment_edit` / `field_card_brand` / `field_card_number` / `field_card_holder` / `field_expiry_month` / `field_expiry_year` / `btn_save` | PaymentEditScreen | 支払い編集 |

