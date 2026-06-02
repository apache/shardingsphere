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

package org.apache.shardingsphere.sqlfederation.compiler.context;

import org.apache.calcite.sql.SqlOperatorTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.MySQLOperatorTable;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.MySQLStringFunctionOperatorTable;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompilerContextFactoryTest {
    
    @Test
    void assertMySQLContextRegistersMySQLStringFunctionOperatorTable() {
        Collection<SqlOperatorTable> actual = CompilerContextFactory.create(Collections.singletonList(createDatabase("MySQL"))).getOperatorTables();
        assertTrue(containsOperatorTable(actual, MySQLOperatorTable.class));
        assertTrue(containsOperatorTable(actual, MySQLStringFunctionOperatorTable.class));
    }
    
    @Test
    void assertMariaDBLikeContextRegistersMySQLStringFunctionOperatorTableViaTrunk() {
        Collection<SqlOperatorTable> actual = CompilerContextFactory.create(Collections.singletonList(createDatabaseWithMySQLTrunk("MariaDB"))).getOperatorTables();
        assertTrue(containsOperatorTable(actual, MySQLStringFunctionOperatorTable.class));
    }
    
    @Test
    void assertDorisLikeContextRegistersMySQLStringFunctionOperatorTableViaTrunk() {
        Collection<SqlOperatorTable> actual = CompilerContextFactory.create(Collections.singletonList(createDatabaseWithMySQLTrunk("Doris"))).getOperatorTables();
        assertTrue(containsOperatorTable(actual, MySQLStringFunctionOperatorTable.class));
    }
    
    @Test
    void assertNonMySQLTrunkContextDoesNotRegisterMySQLStringFunctionOperatorTable() {
        Collection<SqlOperatorTable> actual = CompilerContextFactory.create(Collections.singletonList(createDatabaseWithTrunk("Greenplum", "PostgreSQL"))).getOperatorTables();
        assertFalse(containsOperatorTable(actual, MySQLStringFunctionOperatorTable.class));
    }
    
    @Test
    void assertPostgreSQLContextDoesNotRegisterMySQLStringFunctionOperatorTable() {
        Collection<SqlOperatorTable> actual = CompilerContextFactory.create(Collections.singletonList(createDatabase("PostgreSQL"))).getOperatorTables();
        assertFalse(containsOperatorTable(actual, MySQLStringFunctionOperatorTable.class));
    }
    
    @Test
    void assertOpenGaussContextDoesNotRegisterMySQLStringFunctionOperatorTable() {
        Collection<SqlOperatorTable> actual = CompilerContextFactory.create(Collections.singletonList(createDatabase("openGauss"))).getOperatorTables();
        assertFalse(containsOperatorTable(actual, MySQLStringFunctionOperatorTable.class));
    }
    
    @Test
    void assertOracleContextDoesNotRegisterMySQLStringFunctionOperatorTable() {
        Collection<SqlOperatorTable> actual = CompilerContextFactory.create(Collections.singletonList(createDatabase("Oracle"))).getOperatorTables();
        assertFalse(containsOperatorTable(actual, MySQLStringFunctionOperatorTable.class));
    }
    
    @Test
    void assertEmptyDatabasesFallsBackToDefaultStorageType() {
        Collection<SqlOperatorTable> actual = CompilerContextFactory.create(Collections.emptyList()).getOperatorTables();
        assertTrue(containsOperatorTable(actual, MySQLOperatorTable.class));
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseType) {
        DatabaseType type = TypedSPILoader.getService(DatabaseType.class, databaseType);
        return new ShardingSphereDatabase("foo_db", type, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()),
                Collections.singleton(new ShardingSphereSchema("foo_db", type, Collections.emptyList(), Collections.emptyList())), new ConfigurationProperties(new Properties()));
    }
    
    private ShardingSphereDatabase createDatabaseWithMySQLTrunk(final String databaseTypeName) {
        return createDatabaseWithTrunk(databaseTypeName, "MySQL");
    }
    
    private ShardingSphereDatabase createDatabaseWithTrunk(final String databaseTypeName, final String trunkDatabaseTypeName) {
        DatabaseType trunk = TypedSPILoader.getService(DatabaseType.class, trunkDatabaseTypeName);
        DatabaseType type = mock(DatabaseType.class);
        when(type.getType()).thenReturn(databaseTypeName);
        when(type.getTrunkDatabaseType()).thenReturn(Optional.of(trunk));
        return new ShardingSphereDatabase("foo_db", type, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()),
                Collections.singleton(new ShardingSphereSchema("foo_db", type, Collections.emptyList(), Collections.emptyList())), new ConfigurationProperties(new Properties()));
    }
    
    private boolean containsOperatorTable(final Collection<SqlOperatorTable> tables, final Class<? extends SqlOperatorTable> target) {
        for (SqlOperatorTable each : tables) {
            if (target.isInstance(each)) {
                return true;
            }
        }
        return false;
    }
}
