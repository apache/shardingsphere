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

package org.apache.shardingsphere.single.checker.sql.schema;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.single.exception.DropNotEmptySchemaException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleDropSchemaSupportedCheckerTest {
    
    @Mock
    private SingleRule rule;
    
    private final SingleDropSchemaSupportedChecker checker = new SingleDropSchemaSupportedChecker();
    
    @Test
    void assertIsCheckWithDropSchemaStatement() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DropSchemaStatement.class));
        assertTrue(checker.isCheck(sqlStatementContext));
    }
    
    @Test
    void assertIsCheckWithNonDropSchemaStatement() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        assertFalse(checker.isCheck(sqlStatementContext));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkCases")
    void assertCheck(final String scenario, final String schemaName, final boolean containsCascade, final boolean schemaExisted,
                     final boolean schemaEmpty, final Class<? extends Throwable> expectedThrowableType) {
        ShardingSphereDatabase database = mockDatabase(schemaName, schemaExisted, containsCascade, schemaEmpty);
        SQLStatementContext sqlStatementContext = createSQLStatementContext(schemaName, containsCascade);
        if (null == expectedThrowableType) {
            assertDoesNotThrow(() -> checker.check(rule, database, mock(ShardingSphereSchema.class), sqlStatementContext));
        } else {
            assertThrows(expectedThrowableType, () -> checker.check(rule, database, mock(ShardingSphereSchema.class), sqlStatementContext));
        }
    }
    
    private static Stream<Arguments> checkCases() {
        return Stream.of(
                Arguments.of("not empty schema without cascade should throw", "foo_schema", false, true, false, DropNotEmptySchemaException.class),
                Arguments.of("not existed schema should throw", "not_existed_schema", true, false, false, SchemaNotFoundException.class),
                Arguments.of("not empty schema with cascade should pass", "foo_schema", true, true, false, null),
                Arguments.of("empty schema without cascade should pass", "empty_schema", false, true, true, null));
    }
    
    private ShardingSphereDatabase mockDatabase(final String schemaName, final boolean schemaExisted, final boolean containsCascade, final boolean schemaEmpty) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        if (!schemaExisted) {
            return result;
        }
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        if (!containsCascade) {
            when(schema.getAllTables()).thenReturn(schemaEmpty ? Collections.emptyList() : Collections.singleton(mock(ShardingSphereTable.class)));
        }
        when(result.getSchema(schemaName)).thenReturn(schema);
        return result;
    }
    
    private SQLStatementContext createSQLStatementContext(final String schemaName, final boolean containsCascade) {
        SQLStatementContext result = mock(SQLStatementContext.class);
        DropSchemaStatement dropSchemaStatement = mock(DropSchemaStatement.class);
        when(dropSchemaStatement.isContainsCascade()).thenReturn(containsCascade);
        when(dropSchemaStatement.getSchemaNames()).thenReturn(Collections.singleton(new IdentifierValue(schemaName)));
        when(result.getSqlStatement()).thenReturn(dropSchemaStatement);
        return result;
    }
}
