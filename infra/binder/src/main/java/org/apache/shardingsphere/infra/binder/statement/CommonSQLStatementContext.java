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

package org.apache.shardingsphere.infra.binder.statement;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.hint.SQLHintExtractor;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.OracleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.SQL92Statement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;

import java.util.Collections;

/**
 * Common SQL statement context.
 * 
 * @param <T> type of SQL statement
 */
@Getter
public class CommonSQLStatementContext<T extends SQLStatement> implements SQLStatementContext<T> {
    
    private final T sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final DatabaseType databaseType;
    
    private final SQLHintExtractor sqlHintExtractor;
    
    public CommonSQLStatementContext(final T sqlStatement) {
        this.sqlStatement = sqlStatement;
        databaseType = getDatabaseType(sqlStatement);
        tablesContext = new TablesContext(Collections.emptyList(), databaseType);
        sqlHintExtractor = new SQLHintExtractor(sqlStatement);
    }
    
    private DatabaseType getDatabaseType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLStatement) {
            return DatabaseTypeFactory.getInstance("MySQL");
        }
        if (sqlStatement instanceof PostgreSQLStatement) {
            return DatabaseTypeFactory.getInstance("PostgreSQL");
        }
        if (sqlStatement instanceof OracleStatement) {
            return DatabaseTypeFactory.getInstance("Oracle");
        }
        if (sqlStatement instanceof SQLServerStatement) {
            return DatabaseTypeFactory.getInstance("SQLServer");
        }
        if (sqlStatement instanceof SQL92Statement) {
            return DatabaseTypeFactory.getInstance("SQL92");
        }
        if (sqlStatement instanceof OpenGaussStatement) {
            return DatabaseTypeFactory.getInstance("openGauss");
        }
        throw new UnsupportedSQLOperationException(sqlStatement.getClass().getName());
    }
    
    /**
     * Judge whether is hint routed to write data source or not.
     *
     * @return whether is hint routed to write data source or not
     */
    public boolean isHintWriteRouteOnly() {
        return sqlHintExtractor.isHintWriteRouteOnly();
    }
    
    /**
     * Get hint sharding database value.
     *
     * @param tableName table name
     * @return sharding database value
     */
    public int getHintShardingDatabaseValue(final String tableName) {
        return sqlHintExtractor.getHintShardingDatabaseValue(tableName);
    }
    
    /**
     * Get hint sharding table value.
     *
     * @param tableName table name
     * @return sharding table value
     */
    public int getHintShardingTableValue(final String tableName) {
        return sqlHintExtractor.getHintShardingTableValue(tableName);
    }
    
    /**
     * Judge contains hint sharding databases value or not.
     *
     * @param tableName table name
     * @return contains hint sharding databases value or not
     */
    public boolean containsHintShardingDatabaseValue(final String tableName) {
        return sqlHintExtractor.containsHintShardingDatabaseValue(tableName);
    }
    
    /**
     * Judge contains hint sharding table value or not.
     *
     * @param tableName table name
     * @return Contains hint sharding table value or not
     */
    public boolean containsHintShardingTableValue(final String tableName) {
        return sqlHintExtractor.containsHintShardingTableValue(tableName);
    }
    
    /**
     * Judge contains hint sharding value or not.
     *
     * @param tableName table name
     * @return Contains hint sharding value or not
     */
    public boolean containsHintShardingValue(final String tableName) {
        return containsHintShardingDatabaseValue(tableName) || containsHintShardingTableValue(tableName);
    }
}
