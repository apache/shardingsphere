table "order1", ["t_order_${0..1}"], tableStrategy(["order_id"], { "t_order_${order_id.longValue() % 2}" })
table "order2", ["t_order_${0..1}"], tableStrategy(["order_id"], {
    "t_order_${(int) (order_id.doubleValue() * 100) % 2}"
})
table "order3", ["t_order_${0..1}"], tableStrategy(["date"], {
    log.info("{}",date.dateValue("yyyyMMdd"))
    switch (date.dateValue("yyyyMMdd")) {
        case {
            it.after(Date.parse("yyyyMMdd", "20151001")) && it.before(Date.parse("yyyyMMdd", "20151102"))
        }: return "t_order_0"
        case {
            it.after(Date.parse("yyyyMMdd", "20151101")) && it.before(Date.parse("yyyyMMdd", "20151202"))
        }: return "t_order_1"
    }
})

table "order4", ["t_order_201510", "t_order_201511"], tableStrategy(["date"], {
    "t_order_${date.toString("yyyyMM")}"
})

table "order5", ["t_order_0", "t_order_1"], tableStrategy(["date"], {
    switch (date.dateValue()) {
        case {
            it.after(Date.parse("yyyyMMdd", "20151001")) && it.before(Date.parse("yyyyMMdd", "20151102"))
        }: return "t_order_0"
        case {
            it.after(Date.parse("yyyyMMdd", "20151101")) && it.before(Date.parse("yyyyMMdd", "20151202"))
        }: return "t_order_1"
    }
})
table "order6", ["t_order_0", "t_order_1"], tableStrategy(["order_id"], {
})

table "order7", ["t_order_0", "t_order_1"], tableStrategy(["order_id"], {
    ["t_order_0", "t_order_1"]
})


defaultStrategy databaseStrategy(["order_id"], { "db0" })