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

package org.apache.shardingsphere.test.e2e.operation.pipeline.dao.order.large;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.test.e2e.operation.pipeline.dao.order.large.sqlbuilder.IntPkLargeOrderSQLBuilder;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.AutoIncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.DataSourceExecuteUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * Int PK large order DAO. Large table means the table has many columns.
 */
@Slf4j
public final class IntPkLargeOrderDAO {
    
    private final DataSource dataSource;
    
    private final DatabaseType databaseType;
    
    private final IntPkLargeOrderSQLBuilder sqlBuilder;
    
    private final String qualifiedTableName;
    
    public IntPkLargeOrderDAO(final DataSource dataSource, final DatabaseType databaseType, final QualifiedTable qualifiedTable) {
        this.dataSource = dataSource;
        this.databaseType = databaseType;
        this.sqlBuilder = DatabaseTypedSPILoader.getService(IntPkLargeOrderSQLBuilder.class, databaseType);
        this.qualifiedTableName = qualifiedTable.format();
    }
    
    /**
     * Create order table.
     *
     * @throws SQLException SQL exception
     */
    public void createTable() throws SQLException {
        String sql = sqlBuilder.buildCreateTableSQL(qualifiedTableName);
        log.info("Create int pk large order table SQL: {}", sql);
        DataSourceExecuteUtils.execute(dataSource, sql);
    }
    
    /**
     * Batch insert orders.
     *
     * @param recordCount record count
     * @throws SQLException SQL exception
     */
    public void batchInsert(final int recordCount) throws SQLException {
        List<Object[]> paramsList = PipelineCaseHelper.generateOrderInsertData(databaseType, new AutoIncrementKeyGenerateAlgorithm(), recordCount);
        String sql = sqlBuilder.buildPreparedInsertSQL(qualifiedTableName);
        log.info("Batch insert int pk large order SQL: {}, params list size: {}", sql, paramsList.size());
        DataSourceExecuteUtils.execute(dataSource, sql, paramsList);
    }
    
    /**
     * Insert order.
     *
     * @param orderId order ID
     * @param userId user ID
     * @param status status
     * @throws SQLException SQL exception
     */
    public void insert(final long orderId, final int userId, final String status) throws SQLException {
        String sql = sqlBuilder.buildPreparedSimpleInsertSQL(qualifiedTableName);
        Object[] params = new Object[]{orderId, userId, status};
        log.info("Insert int pk large order simple SQL: {}, params: {}", sql, params);
        DataSourceExecuteUtils.execute(dataSource, sql, params);
    }
}
