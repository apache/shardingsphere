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
import org.apache.shardingsphere.test.e2e.operation.pipeline.dao.orderitem.sqlbuilder.OrderItemSQLBuilder;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.AutoIncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.DataSourceExecuteUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public final class OrderItemDAO {
    
    private final DataSource dataSource;
    
    private final OrderItemSQLBuilder sqlBuilder;
    
    public OrderItemDAO(final DataSource dataSource, final DatabaseType databaseType) {
        this.dataSource = dataSource;
        sqlBuilder = DatabaseTypedSPILoader.getService(OrderItemSQLBuilder.class, databaseType);
    }
    
    /**
     * Create order_item table.
     *
     * @throws SQLException SQL exception
     */
    public void createTable() throws SQLException {
        DataSourceExecuteUtils.execute(dataSource, sqlBuilder.buildCreateTableSQL());
    }
    
    /**
     * Batch insert order items.
     *
     * @param insertRows insert rows
     * @throws SQLException SQL exception
     */
    public void batchInsert(final int insertRows) throws SQLException {
        List<Object[]> params = PipelineCaseHelper.generateOrderItemInsertData(new AutoIncrementKeyGenerateAlgorithm(), insertRows);
        DataSourceExecuteUtils.execute(dataSource, sqlBuilder.buildPreparedInsertSQL(), params);
    }
}
