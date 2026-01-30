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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.single.exception.DropNotEmptySchemaException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.schema.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleDropSchemaSupportedCheckerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SingleRule rule;
    
    @Test
    void assertCheckWithoutCascadeSchema() {
        assertThrows(DropNotEmptySchemaException.class, () -> new SingleDropSchemaSupportedChecker().check(rule, mockDatabase(), mock(), createSQLStatementContext("foo_schema", false)));
    }
    
    @Test
    void assertCheckWithNotExistedSchema() {
        ShardingSphereDatabase database = mockDatabase();
        when(database.getSchema("not_existed_schema")).thenReturn(null);
        assertThrows(SchemaNotFoundException.class, () -> new SingleDropSchemaSupportedChecker().check(rule, database, mock(), createSQLStatementContext("not_existed_schema", true)));
    }
    
    @Test
    void assertCheck() {
        assertDoesNotThrow(() -> new SingleDropSchemaSupportedChecker().check(rule, mockDatabase(), mock(), createSQLStatementContext("foo_schema", true)));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", mock(DatabaseType.class),
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE)), Collections.emptyList());
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        return result;
    }
    
    private SQLStatementContext createSQLStatementContext(final String schemaName, final boolean containsCascade) {
        DropSchemaStatement dropSchemaStatement = mock(DropSchemaStatement.class, RETURNS_DEEP_STUBS);
        when(dropSchemaStatement.isContainsCascade()).thenReturn(containsCascade);
        when(dropSchemaStatement.getSchemaNames()).thenReturn(Collections.singleton(new IdentifierValue(schemaName)));
        when(dropSchemaStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        return new CommonSQLStatementContext(dropSchemaStatement);
    }
}
