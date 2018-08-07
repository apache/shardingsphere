/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.innersaga.mock;

import io.shardingsphere.core.routing.SQLUnit;

import java.util.List;

/**
 * cancel sql generator mock implement.
 *
 * @author yangyi
 */

public class CancelSql {
    
    /**
     * get compensated sql according to confirm sql.
     *
     * @param confirm confirm sql.
     * @return compensated sql.
     */
    public static String getCancelSql(final String confirm) {
        String update1 = "UPDATE t_order_0 SET status='UPDATE_1' WHERE user_id=0 AND order_id=1000";
        String update2 = "UPDATE t_order_0 SET not_existed_column=1 WHERE user_id=1 AND order_id=1000";
        String update3 = "UPDATE t_order_0 SET status='UPDATE_2' WHERE user_id=0 AND order_id=1000";
        String delete1 = "UPDATE t_order_0 SET status='INIT' WHERE user_id=0 AND order_id=1000";
        String delete2 = "UPDATE t_order_0 SET not_existed_column=0 WHERE user_id=1 AND order_id=1000";
        String delete3 = "UPDATE t_order_0 SET status='UPDATE_1' WHERE user_id=0 AND order_id=1000";
        if (update1.equals(confirm)) {
            return delete1;
        }
        if (update2.equals(confirm)) {
            return delete2;
        }
        if (update3.equals(confirm)) {
            return delete3;
        }
        return "";
    }
    
    /**
     * get compensated sql according to confirm sqlUnit.
     *
     * @param sqlUnit confirm sqlUnit
     * @return ccompensated sql
     */
    public static String getCancelSql(final SQLUnit sqlUnit) {
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
