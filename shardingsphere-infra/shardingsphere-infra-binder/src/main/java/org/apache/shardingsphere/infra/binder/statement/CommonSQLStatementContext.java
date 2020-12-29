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
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
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
    
    public CommonSQLStatementContext(final T sqlStatement) {
        this.sqlStatement = sqlStatement;
        this.databaseType = initDatabaseType(sqlStatement);
        tablesContext = new TablesContext(Collections.emptyList());
    }

    private DatabaseType initDatabaseType(final SQLStatement sqlStatement) {
        DatabaseType databaseType = null;
        if (sqlStatement instanceof MySQLStatement) {
            databaseType = DatabaseTypeRegistry.getActualDatabaseType("MySQL");
        } else if (sqlStatement instanceof PostgreSQLStatement) {
            databaseType = DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL");
        } else if (sqlStatement instanceof OracleStatement) {
            databaseType = DatabaseTypeRegistry.getActualDatabaseType("Oracle");
        } else if (sqlStatement instanceof SQLServerStatement) {
            databaseType = DatabaseTypeRegistry.getActualDatabaseType("SQLServer");
        } else if (sqlStatement instanceof SQL92Statement) {
            databaseType = DatabaseTypeRegistry.getActualDatabaseType("SQL92");
        }
        return databaseType;
    }
}
