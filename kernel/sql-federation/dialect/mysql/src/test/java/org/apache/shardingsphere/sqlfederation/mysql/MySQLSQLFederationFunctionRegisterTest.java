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

package org.apache.shardingsphere.sqlfederation.mysql;

import org.apache.calcite.config.Lex;
import org.apache.calcite.runtime.SqlFunctions;
import org.apache.calcite.schema.Function;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.DialectSQLFederationFunctionRegister;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.impl.MySQLBinFunction;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.impl.MySQLDatabaseFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLSQLFederationFunctionRegisterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectSQLFederationFunctionRegister register = DatabaseTypedSPILoader.getService(DialectSQLFederationFunctionRegister.class, databaseType);
    
    @Test
    void assertRegisterFunction() {
        SchemaPlus schemaPlus = Frameworks.createRootSchema(true);
        register.registerFunction(schemaPlus, "foo_database");
        assertFunction(schemaPlus.getFunctions("bin"), MySQLBinFunction.class, "bin");
        assertFunction(schemaPlus.getFunctions("atan"), SqlFunctions.class, "atan2");
        assertFunction(schemaPlus.getFunctions("atan2"), SqlFunctions.class, "atan");
        assertDatabaseFunction(schemaPlus.getFunctions("database"));
    }
    
    private void assertFunction(final Collection<Function> functions, final Class<?> expectedClass, final String expectedMethod) {
        assertThat(functions.size(), is(1));
        ScalarFunctionImpl actualFunction = (ScalarFunctionImpl) functions.iterator().next();
        assertTrue(expectedClass.isAssignableFrom(actualFunction.method.getDeclaringClass()));
        assertThat(actualFunction.method.getName(), is(expectedMethod));
    }
    
    private void assertDatabaseFunction(final Collection<Function> functions) {
        assertThat(functions.size(), is(1));
        assertThat(functions.iterator().next(), isA(MySQLDatabaseFunction.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"SELECT DATABASE()", "SELECT {fn DATABASE()}", "SELECT 1 FROM (SELECT 1) AS t WHERE 'foo_database' = DATABASE()"})
    void assertDatabaseFunctionCanBeValidated(final String sql) {
        SchemaPlus schemaPlus = Frameworks.createRootSchema(true);
        register.registerFunction(schemaPlus, "foo_database");
        try (Planner planner = Frameworks.getPlanner(Frameworks.newConfigBuilder().parserConfig(SqlParser.config().withLex(Lex.MYSQL)).defaultSchema(schemaPlus).build())) {
            assertDoesNotThrow(() -> planner.validate(planner.parse(sql)));
        }
    }
}
