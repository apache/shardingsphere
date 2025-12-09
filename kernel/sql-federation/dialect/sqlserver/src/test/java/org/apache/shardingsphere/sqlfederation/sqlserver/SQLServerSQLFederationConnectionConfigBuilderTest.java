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

package org.apache.shardingsphere.sqlfederation.sqlserver;

import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.context.connection.config.DialectSQLFederationConnectionConfigBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class SQLServerSQLFederationConnectionConfigBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
    
    private final DialectSQLFederationConnectionConfigBuilder builder = DatabaseTypedSPILoader.getService(DialectSQLFederationConnectionConfigBuilder.class, databaseType);
    
    @Test
    void assertBuild() {
        CalciteConnectionConfig actualConfig = builder.build();
        assertThat(actualConfig.lex(), is(Lex.SQL_SERVER));
        assertThat(actualConfig.conformance(), is(SqlConformanceEnum.SQL_SERVER_2008));
        assertNull(actualConfig.fun(SqlOperatorTable.class, null));
        assertThat(actualConfig.caseSensitive(), is(Lex.SQL_SERVER.caseSensitive));
    }
}
