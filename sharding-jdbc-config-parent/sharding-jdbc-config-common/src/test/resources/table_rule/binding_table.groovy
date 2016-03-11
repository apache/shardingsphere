table "t_order", ["t_order_${1..10}"], databaseStrategy(["order_id"], {
    "db${order_id.longValue() % 2}"
}), tableStrategy(["order_id"], { "t_order_${order_id.longValue() % 10}" })
table "t_order_item", ["t_order_item_${1..10}"]

bind(["t_order", "t_order_item"])
