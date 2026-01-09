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

import org.apache.calcite.sql.dialect.MssqlSqlDialect;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.calcite.sql.dialect.OracleSqlDialect;
import org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.impl.CustomMySQLSQLDialect;
import org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.impl.CustomPostgreSQLSQLDialect;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SQLDialectFactoryTest {
    
    @Test
    void assertGetSQLDialect() {
        assertThat(SQLDialectFactory.getSQLDialect("MySQL"), is(CustomMySQLSQLDialect.DEFAULT));
        assertThat(SQLDialectFactory.getSQLDialect("H2"), is(CustomMySQLSQLDialect.DEFAULT));
        assertThat(SQLDialectFactory.getSQLDialect("PostgreSQL"), is(CustomPostgreSQLSQLDialect.DEFAULT));
        assertThat(SQLDialectFactory.getSQLDialect("openGauss"), is(CustomPostgreSQLSQLDialect.DEFAULT));
        assertThat(SQLDialectFactory.getSQLDialect("Oracle"), is(OracleSqlDialect.DEFAULT));
        assertThat(SQLDialectFactory.getSQLDialect("SQLServer"), is(MssqlSqlDialect.DEFAULT));
        assertThat(SQLDialectFactory.getSQLDialect("UnknownDB"), is(MysqlSqlDialect.DEFAULT));
    }
}
