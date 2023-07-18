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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
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
 */
@Getter
public abstract class CommonSQLStatementContext implements SQLStatementContext {
    
    private final SQLStatement sqlStatement;
    
    private final TablesContext tablesContext;
    
    private final DatabaseType databaseType;
    
    protected CommonSQLStatementContext(final SQLStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
        databaseType = getDatabaseType(sqlStatement);
        tablesContext = new TablesContext(Collections.emptyList(), databaseType);
    }
    
    private DatabaseType getDatabaseType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "MySQL");
        }
        if (sqlStatement instanceof PostgreSQLStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        }
        if (sqlStatement instanceof OracleStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "Oracle");
        }
        if (sqlStatement instanceof SQLServerStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "SQLServer");
        }
        if (sqlStatement instanceof OpenGaussStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "openGauss");
        }
        if (sqlStatement instanceof SQL92Statement) {
            return TypedSPILoader.getService(DatabaseType.class, "SQL92");
        }
        throw new UnsupportedSQLOperationException(sqlStatement.getClass().getName());
    }
}
