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

package org.apache.shardingsphere.sqlfederation.opengauss;

import org.apache.calcite.schema.Function;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.tools.Frameworks;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.DialectSQLFederationFunctionRegister;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.opengauss.impl.OpenGaussSystemFunction;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.postgresql.impl.PostgreSQLSystemFunction;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussSQLFederationFunctionRegisterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DialectSQLFederationFunctionRegister register = DatabaseTypedSPILoader.getService(DialectSQLFederationFunctionRegister.class, databaseType);
    
    @Test
    void assertRegisterPgCatalogFunctionsAndDelegate() {
        SchemaPlus schemaPlus = Frameworks.createRootSchema(true);
        register.registerFunction(schemaPlus, "pg_catalog");
        assertFunction(schemaPlus.getFunctions("gs_password_deadline"), OpenGaussSystemFunction.class, "gsPasswordDeadline");
        assertFunction(schemaPlus.getFunctions("intervaltonum"), OpenGaussSystemFunction.class, "intervalToNum");
        assertFunction(schemaPlus.getFunctions("gs_password_notifyTime"), OpenGaussSystemFunction.class, "gsPasswordNotifyTime");
        assertFunction(schemaPlus.getFunctions("version"), OpenGaussSystemFunction.class, "version");
        assertFunction(schemaPlus.getFunctions("opengauss_version"), OpenGaussSystemFunction.class, "openGaussVersion");
        assertFunction(schemaPlus.getFunctions("pg_table_is_visible"), PostgreSQLSystemFunction.class, "pgTableIsVisible");
        assertFunction(schemaPlus.getFunctions("pg_get_userbyid"), PostgreSQLSystemFunction.class, "pgGetUserById");
    }
    
    @Test
    void assertDelegateOnlyWhenNotPgCatalog() {
        SchemaPlus schemaPlus = Frameworks.createRootSchema(true);
        register.registerFunction(schemaPlus, "public");
        assertThat(schemaPlus.getFunctions("gs_password_deadline").size(), is(0));
        assertThat(schemaPlus.getFunctions("intervaltonum").size(), is(0));
        assertThat(schemaPlus.getFunctions("gs_password_notifyTime").size(), is(0));
        assertThat(schemaPlus.getFunctions("version").size(), is(0));
        assertThat(schemaPlus.getFunctions("opengauss_version").size(), is(0));
        assertThat(schemaPlus.getFunctions("pg_table_is_visible").size(), is(0));
        assertThat(schemaPlus.getFunctions("pg_get_userbyid").size(), is(0));
    }
    
    private void assertFunction(final Collection<Function> functions, final Class<?> expectedClass, final String expectedMethod) {
        assertThat(functions.size(), is(1));
        ScalarFunctionImpl actualFunction = (ScalarFunctionImpl) functions.iterator().next();
        assertTrue(expectedClass.isAssignableFrom(actualFunction.method.getDeclaringClass()));
        assertThat(actualFunction.method.getName(), is(expectedMethod));
    }
}
