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
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.hint.SQLHintExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.OracleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.SQL92Statement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;

import java.util.Collections;
import java.util.Optional;

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
            return DatabaseTypeRegistry.getActualDatabaseType("MySQL");
        }
        if (sqlStatement instanceof PostgreSQLStatement) {
            return DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL");
        }
        if (sqlStatement instanceof OracleStatement) {
            return DatabaseTypeRegistry.getActualDatabaseType("Oracle");
        }
        if (sqlStatement instanceof SQLServerStatement) {
            return DatabaseTypeRegistry.getActualDatabaseType("SQLServer");
        }
        if (sqlStatement instanceof SQL92Statement) {
            return DatabaseTypeRegistry.getActualDatabaseType("SQL92");
        }
        if (sqlStatement instanceof OpenGaussStatement) {
            return DatabaseTypeRegistry.getActualDatabaseType("openGauss");
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getName());
    }
    
    /**
     * Find hint data source name.
     *
     * @return dataSource name
     */
    public Optional<String> findHintDataSourceName() {
        return sqlHintExtractor.findHintDataSourceName();
    }
    
    /**
     * Judge whether is hint routed to write data source or not.
     *
     * @return whether is hint routed to write data source or not
     */
    public boolean isHintWriteRouteOnly() {
        return sqlHintExtractor.isHintWriteRouteOnly();
    }
}
