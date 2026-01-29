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

package org.apache.shardingsphere.test.e2e.operation.pipeline.dao.orderitem;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.operation.pipeline.dao.orderitem.sqlbuilder.IntPkOrderItemSQLBuilder;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.AutoIncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.DataSourceExecuteUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public final class IntPkOrderItemDAO {
    
    private final DataSource dataSource;
    
    private final IntPkOrderItemSQLBuilder sqlBuilder;
    
    private final String schemaPrefix;
    
    public IntPkOrderItemDAO(final DataSource dataSource, final DatabaseType databaseType, final String schemaName) {
        this.dataSource = dataSource;
        sqlBuilder = DatabaseTypedSPILoader.getService(IntPkOrderItemSQLBuilder.class, databaseType);
        schemaPrefix = null == schemaName || schemaName.isEmpty() ? "" : (schemaName + ".");
    }
    
    /**
     * Create order_item table.
     *
     * @throws SQLException SQL exception
     */
    public void createTable() throws SQLException {
        String sql = sqlBuilder.buildCreateTableSQL(schemaPrefix);
        log.info("Create order_item table SQL: {}", sql);
        DataSourceExecuteUtils.execute(dataSource, sql);
    }
    
    /**
     * Batch insert order items.
     *
     * @param recordCount record count
     * @throws SQLException SQL exception
     */
    public void batchInsert(final int recordCount) throws SQLException {
        List<Object[]> paramsList = PipelineCaseHelper.generateOrderItemInsertData(new AutoIncrementKeyGenerateAlgorithm(), recordCount);
        String sql = sqlBuilder.buildPreparedInsertSQL(schemaPrefix);
        log.info("Batch insert order_item SQL: {}, params list size: {}", sql, paramsList.size());
        DataSourceExecuteUtils.executeBatch(dataSource, sql, paramsList);
    }
    
    /**
     * Insert order item.
     *
     * @param itemId item id
     * @param orderId order id
     * @param userId user id
     * @param status status
     * @throws SQLException SQL exception
     */
    public void insert(final long itemId, final long orderId, final int userId, final String status) throws SQLException {
        String sql = sqlBuilder.buildPreparedInsertSQL(schemaPrefix);
        Object[] params = new Object[]{itemId, orderId, userId, status};
        log.info("Insert order_item SQL: {}, params: {}", sql, params);
        DataSourceExecuteUtils.execute(dataSource, sql, params);
    }
}
