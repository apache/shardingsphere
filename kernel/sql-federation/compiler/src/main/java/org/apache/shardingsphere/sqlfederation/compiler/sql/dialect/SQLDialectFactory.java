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

package org.apache.shardingsphere.sqlfederation.compiler.sql.dialect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.MssqlSqlDialect;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.calcite.sql.dialect.OracleSqlDialect;
import org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.impl.CustomMySQLSQLDialect;
import org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.impl.CustomPostgreSQLSQLDialect;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL dialect factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLDialectFactory {
    
    private static final Map<String, SqlDialect> SQL_DIALECTS_REGISTRY = new HashMap<>();
    
    static {
        SQL_DIALECTS_REGISTRY.put("H2", CustomMySQLSQLDialect.DEFAULT);
        SQL_DIALECTS_REGISTRY.put("MySQL", CustomMySQLSQLDialect.DEFAULT);
        SQL_DIALECTS_REGISTRY.put("MariaDB", CustomMySQLSQLDialect.DEFAULT);
        SQL_DIALECTS_REGISTRY.put("Oracle", OracleSqlDialect.DEFAULT);
        SQL_DIALECTS_REGISTRY.put("SQLServer", MssqlSqlDialect.DEFAULT);
        SQL_DIALECTS_REGISTRY.put("PostgreSQL", CustomPostgreSQLSQLDialect.DEFAULT);
        SQL_DIALECTS_REGISTRY.put("openGauss", CustomPostgreSQLSQLDialect.DEFAULT);
    }
    
    /**
     * Get SQL dialect.
     *
     * @param databaseType database type
     * @return SQL dialect
     */
    public static SqlDialect getSQLDialect(final String databaseType) {
        return SQL_DIALECTS_REGISTRY.getOrDefault(databaseType, MysqlSqlDialect.DEFAULT);
    }
}
