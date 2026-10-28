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

package org.apache.shardingsphere.infra.executor.checker;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PushDownSQLExecutionCheckEngineTest {
    
    @ParameterizedTest(name = "{0}: storage types {1}, execution units {2}")
    @MethodSource("getAllowedSelects")
    void assertCheckWithAllowedSelect(final String protocolType, final Collection<String> storageTypes, final Collection<Integer> routedUnits) {
        ShardingSphereDatabase database = createDatabase(protocolType, storageTypes);
        ExecutionContext executionContext = createExecutionContext(mock(SelectStatement.class), routedUnits);
        assertDoesNotThrow(() -> new PushDownSQLExecutionCheckEngine().check(database, executionContext));
    }
    
    private ShardingSphereDatabase createDatabase(final String protocolType, final Collection<String> storageTypes) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn(protocolType);
        when(result.getProtocolType()).thenReturn(databaseType);
        Map<String, StorageUnit> storageUnits = new HashMap<>(storageTypes.size(), 1F);
        int index = 0;
        for (String each : storageTypes) {
            StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
            when(storageUnit.getStorageType().getType()).thenReturn(each);
            storageUnits.put("foo_" + index++, storageUnit);
        }
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
    
    private ExecutionContext createExecutionContext(final SQLStatement sqlStatement, final Collection<Integer> routedUnits) {
        ExecutionContext result = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatementContext().getSqlStatement()).thenReturn(sqlStatement);
        Collection<ExecutionUnit> executionUnits = new ArrayList<>(routedUnits.size());
        for (Integer each : routedUnits) {
            executionUnits.add(new ExecutionUnit("foo_" + each, new SQLUnit("SELECT id FROM foo_table", Collections.emptyList())));
        }
        when(result.getExecutionUnits()).thenReturn(executionUnits);
        return result;
    }
    
    private static Stream<Arguments> getAllowedSelects() {
        return Stream.of(
                Arguments.of("MySQL", Arrays.asList("MySQL", "PostgreSQL"), Collections.emptyList()),
                Arguments.of("MySQL", Collections.singletonList("MySQL"), Collections.singletonList(0)),
                Arguments.of("MySQL", Arrays.asList("MySQL", "MySQL"), Arrays.asList(0, 1)),
                Arguments.of("PostgreSQL", Arrays.asList("MySQL", "PostgreSQL", "MySQL"), Arrays.asList(0, 2)),
                Arguments.of("MySQL", Arrays.asList("MySQL", "PostgreSQL"), Arrays.asList(1, 1)),
                Arguments.of("PostgreSQL", Arrays.asList("Oracle", "Oracle"), Arrays.asList(0, 1)));
    }
    
    @ParameterizedTest(name = "{0} and {1}")
    @MethodSource("getRejectedStorageTypes")
    void assertCheckWithMixedStorageTypes(final String firstStorageType, final String secondStorageType) {
        ShardingSphereDatabase database = createDatabase("MySQL", Arrays.asList(firstStorageType, secondStorageType));
        ExecutionContext executionContext = createExecutionContext(mock(SelectStatement.class), Arrays.asList(0, 1));
        SQLException actual = assertThrows(SQLException.class, () -> new PushDownSQLExecutionCheckEngine().check(database, executionContext));
        assertThat(actual.getMessage(), is("Unsupported SQL operation: SELECT across different storage database types in push down execution."));
        assertThat(actual.getSQLState(), is("0A000"));
        assertThat(actual.getErrorCode(), is(30001));
    }
    
    private static Stream<Arguments> getRejectedStorageTypes() {
        return Stream.of(Arguments.of("MySQL", "PostgreSQL"), Arguments.of("MySQL", "MariaDB"), Arguments.of("Oracle", "SQLServer"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getNonSelectStatements")
    void assertCheckWithNonSelect(final SQLStatement sqlStatement) {
        ShardingSphereDatabase database = createDatabase("MySQL", Arrays.asList("MySQL", "PostgreSQL"));
        ExecutionContext executionContext = createExecutionContext(sqlStatement, Arrays.asList(0, 1));
        assertDoesNotThrow(() -> new PushDownSQLExecutionCheckEngine().check(database, executionContext));
    }
    
    private static Stream<SQLStatement> getNonSelectStatements() {
        return Stream.of(mock(InsertStatement.class), mock(CreateTableStatement.class), mock(CommitStatement.class));
    }
    
}
