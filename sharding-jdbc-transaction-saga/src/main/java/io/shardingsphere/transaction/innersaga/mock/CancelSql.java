package io.shardingsphere.transaction.innersaga.mock;

import io.shardingsphere.core.routing.SQLUnit;

import java.util.List;

/**
 * cancel sql generator mock implement
 *
 * @author yangyi
 */

public class CancelSql {

    private final static String UPDATE1 = "UPDATE t_order_0 SET status='UPDATE_1' WHERE user_id=0 AND order_id=1000";
    private final static String UPDATE2 = "UPDATE t_order_0 SET not_existed_column=1 WHERE user_id=1 AND order_id=1000";
    private final static String UPDATE3 = "UPDATE t_order_0 SET status='UPDATE_2' WHERE user_id=0 AND order_id=1000";

    private final static String DELETE1 = "UPDATE t_order_0 SET status='INIT' WHERE user_id=0 AND order_id=1000";
    private final static String DELETE2 = "UPDATE t_order_0 SET not_existed_column=0 WHERE user_id=1 AND order_id=1000";
    private final static String DELETE3 = "UPDATE t_order_0 SET status='UPDATE_1' WHERE user_id=0 AND order_id=1000";

    public static String getCancelSql(String confirm) {
        if (UPDATE1.equals(confirm)) {
            return DELETE1;
        }
        if (UPDATE2.equals(confirm)) {
            return DELETE2;
        }
        if (UPDATE3.equals(confirm)) {
            return DELETE3;
        }

        return "";

    }

    public static String getCancelSql(SQLUnit sqlUnit) {
        String sql = sqlUnit.getSql();
        List<List<Object>> list = sqlUnit.getParameterSets();
        for (List<Object> subList : list) {
            for (Object parameter : subList) {
                sql = sql.replace("?", parameter.toString());
            }
        }
        return getCancelSql(sql);
    }

}
