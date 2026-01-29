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

package org.apache.shardingsphere.test.e2e.operation.pipeline.dao.order.small;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.algorithm.keygen.uuid.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.test.e2e.operation.pipeline.dao.order.small.sqlbuilder.StringPkSmallOrderSQLBuilder;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.DataSourceExecuteUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * String PK small order DAO. Small table means the table has few columns.
 */
@Slf4j
public final class StringPkSmallOrderDAO {
    
    private final DataSource dataSource;
    
    private final StringPkSmallOrderSQLBuilder sqlBuilder;
    
    private final String qualifiedTableName;
    
    public StringPkSmallOrderDAO(final DataSource dataSource, final DatabaseType databaseType, final QualifiedTable qualifiedTable) {
        this.dataSource = dataSource;
        this.sqlBuilder = DatabaseTypedSPILoader.getService(StringPkSmallOrderSQLBuilder.class, databaseType);
        this.qualifiedTableName = qualifiedTable.format();
    }
    
    /**
     * Create order table.
     *
     * @throws SQLException SQL exception
     */
    public void createTable() throws SQLException {
        String sql = sqlBuilder.buildCreateTableSQL(qualifiedTableName);
        log.info("Create string pk small order table SQL: {}", sql);
        DataSourceExecuteUtils.execute(dataSource, sql);
    }
    
    /**
     * Batch insert orders.
     *
     * @param recordCount record count
     * @throws SQLException SQL exception
     */
    public void batchInsert(final int recordCount) throws SQLException {
        List<Object[]> paramsList = PipelineCaseHelper.generateSmallOrderInsertData(new UUIDKeyGenerateAlgorithm(), recordCount);
        String sql = sqlBuilder.buildPreparedInsertSQL(qualifiedTableName);
        log.info("Batch insert string pk small order SQL: {}, params list size: {}", sql, paramsList.size());
        DataSourceExecuteUtils.executeBatch(dataSource, sql, paramsList);
    }
    
    /**
     * Insert order.
     *
     * @param orderId order ID
     * @param userId user ID
     * @param status status
     * @throws SQLException SQL exception
     */
    public void insert(final String orderId, final int userId, final String status) throws SQLException {
        String sql = sqlBuilder.buildPreparedInsertSQL(qualifiedTableName);
        Object[] params = new Object[]{orderId, userId, status};
        log.info("Insert string pk small order SQL: {}, params: {}", sql, params);
        DataSourceExecuteUtils.execute(dataSource, sql, params);
    }
}
