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

package org.apache.shardingsphere.infra.binder.context.statement;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.clickhouse.ClickHouseStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.DorisStatement;
import org.apache.shardingsphere.sql.parser.statement.hive.HiveStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.OracleStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.statement.presto.PrestoStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.SQL92Statement;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.SQLServerStatement;

/**
 * Common SQL statement context.
 */
@Getter
public abstract class CommonSQLStatementContext implements SQLStatementContext {
    
    private final SQLStatement sqlStatement;
    
    private final DatabaseType databaseType;
    
    protected CommonSQLStatementContext(final SQLStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
        databaseType = getDatabaseType(sqlStatement);
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
        if (sqlStatement instanceof ClickHouseStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "ClickHouse");
        }
        if (sqlStatement instanceof DorisStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "Doris");
        }
        if (sqlStatement instanceof HiveStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "Hive");
        }
        if (sqlStatement instanceof PrestoStatement) {
            return TypedSPILoader.getService(DatabaseType.class, "Presto");
        }
        if (sqlStatement instanceof SQL92Statement) {
            return TypedSPILoader.getService(DatabaseType.class, "SQL92");
        }
        throw new UnsupportedSQLOperationException(sqlStatement.getClass().getName());
    }
}
