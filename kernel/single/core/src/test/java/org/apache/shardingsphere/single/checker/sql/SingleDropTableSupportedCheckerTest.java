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

package org.apache.shardingsphere.single.checker.sql;

import org.apache.shardingsphere.infra.binder.context.statement.ddl.DropTableStatementContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.infra.exception.kernel.syntax.UnsupportedDropCascadeTableException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.single.checker.sql.table.SingleDropTableSupportedChecker;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleDropTableSupportedCheckerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SingleRule rule;
    
    @Test
    void assertCheckWithCascade() {
        assertThrows(UnsupportedDropCascadeTableException.class, () -> new SingleDropTableSupportedChecker().check(rule, mockDatabase(), mock(), createSQLStatementContext(true)));
    }
    
    @Test
    void assertCheckWithoutCascade() {
        new SingleDropTableSupportedChecker().check(rule, mockDatabase(), mock(), createSQLStatementContext(false));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema",
                Collections.singleton(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE)), Collections.emptyList());
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        return result;
    }
    
    private DropTableStatementContext createSQLStatementContext(final boolean containsCascade) {
        PostgreSQLDropTableStatement dropSchemaStatement = mock(PostgreSQLDropTableStatement.class, RETURNS_DEEP_STUBS);
        when(dropSchemaStatement.isContainsCascade()).thenReturn(containsCascade);
        when(dropSchemaStatement.getTables()).thenReturn(Collections.emptyList());
        return new DropTableStatementContext(dropSchemaStatement, "foo_db");
    }
}
