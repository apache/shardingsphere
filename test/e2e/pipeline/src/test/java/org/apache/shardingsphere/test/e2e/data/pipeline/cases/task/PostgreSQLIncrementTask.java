/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.task;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.BaseIncrementTask;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DataSourceExecuteUtil;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
public final class PostgreSQLIncrementTask extends BaseIncrementTask {
    
    private static final KeyGenerateAlgorithm KEY_GENERATE_ALGORITHM = new SnowflakeKeyGenerateAlgorithm();
    
    private final DataSource dataSource;
    
    private final String schema;
    
    private final String orderTableName;
    
    private final int executeCountLimit;
    
    static {
        KEY_GENERATE_ALGORITHM.init(PropertiesBuilder.build(new Property("max-vibration-offset", "2")));
    }
    
    @Override
    public void run() {
        int executeCount = 0;
        while (executeCount < executeCountLimit && !Thread.currentThread().isInterrupted()) {
            Object orderId = insertOrder();
            if (0 == executeCount % 2) {
                DataSourceExecuteUtil.execute(dataSource, String.format("DELETE FROM %s WHERE order_id = ?", getTableNameWithSchema(orderTableName)), new Object[]{orderId});
            } else {
                updateOrderByPrimaryKey(orderId);
            }
            Object orderItemPrimaryKey = insertOrderItem();
            String updateSql = String.format("UPDATE %s SET status = ? WHERE item_id = ?", getTableNameWithSchema("t_order_item"));
            DataSourceExecuteUtil.execute(dataSource, updateSql, new Object[]{"updated" + Instant.now().getEpochSecond(), orderItemPrimaryKey});
            String deleteSql = String.format("DELETE FROM %s WHERE item_id = ?", getTableNameWithSchema("t_order_item"));
            DataSourceExecuteUtil.execute(dataSource, deleteSql, new Object[]{orderItemPrimaryKey});
            executeCount++;
        }
        log.info("PostgreSQL increment task runnable execute successfully.");
    }
    
    private Object insertOrder() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String status = 0 == random.nextInt() % 2 ? null : "中文测试";
        Object[] orderInsertDate = new Object[]{KEY_GENERATE_ALGORITHM.generateKey(), random.nextInt(0, 6), status};
        String insertSQL = String.format("INSERT INTO %s (order_id,user_id,status) VALUES (?, ?, ?)", getTableNameWithSchema(orderTableName));
        log.info("insert order sql:{}", insertSQL);
        DataSourceExecuteUtil.execute(dataSource, insertSQL, orderInsertDate);
        return orderInsertDate[0];
    }
    
    private Object insertOrderItem() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String status = 0 == random.nextInt() % 2 ? null : "NOT-NULL";
        Object[] orderInsertItemDate = new Object[]{KEY_GENERATE_ALGORITHM.generateKey(), PipelineCaseHelper.generateSnowflakeKey(), random.nextInt(0, 6), status};
        String insertSql = "INSERT INTO %s(item_id,order_id,user_id,status) VALUES(?,?,?,?)";
        DataSourceExecuteUtil.execute(dataSource, String.format(insertSql, getTableNameWithSchema("t_order_item")), orderInsertItemDate);
        return orderInsertItemDate[0];
    }
    
    private void updateOrderByPrimaryKey(final Object primaryKey) {
        Object[] updateData = {"updated" + Instant.now().getEpochSecond(), primaryKey};
        String updateSql = String.format("UPDATE %s SET status = ? WHERE order_id = ?", getTableNameWithSchema(orderTableName));
        DataSourceExecuteUtil.execute(dataSource, String.format(updateSql, getTableNameWithSchema("t_order_item")), updateData);
    }
    
    private String getTableNameWithSchema(final String tableName) {
        return Strings.isNullOrEmpty(schema) ? tableName : String.join(".", schema, tableName);
    }
}
