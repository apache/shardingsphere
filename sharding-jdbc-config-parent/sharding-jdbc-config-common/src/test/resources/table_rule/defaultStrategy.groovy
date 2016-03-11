table "order1", ["t_order_0"]

defaultStrategy databaseStrategy(["order_id"], { "db0" })

defaultStrategy tableStrategy(["order_id"], { "t_order_0" })
