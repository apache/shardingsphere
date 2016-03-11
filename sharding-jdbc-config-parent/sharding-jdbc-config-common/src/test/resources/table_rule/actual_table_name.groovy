table "order_1", ["db0.t_order_${[1, 3]}", "db1.t_order_${[2, 4]}"]

table "order_2", ["t_order_${1..3}", "t_order_bak"]

table "order_3", ["table_${1..3}${null}${[]}${["", "_bak"]}${[]}"]

table "order_4", ["table_${1}"]
