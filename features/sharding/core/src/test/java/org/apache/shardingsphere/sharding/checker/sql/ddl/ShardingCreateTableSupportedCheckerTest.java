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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingCreateTableSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheck() {
        CreateTableStatement sqlStatement = new CreateTableStatement(databaseType);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("foo_tbl"))));
        sqlStatement.buildAttributes();
        assertThrows(TableExistsException.class, () -> assertCheck(sqlStatement));
    }

    private void assertCheck(final CreateTableStatement sqlStatement) {
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        new ShardingCreateTableSupportedChecker().check(rule, database, schema, sqlStatementContext);
    }

    @Test
    void assertCheckIfNotExists() {
        CreateTableStatement sqlStatement = new CreateTableStatement(databaseType);
        sqlStatement.setIfNotExists(true);
        sqlStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("foo_tbl"))));
        sqlStatement.buildAttributes();
        assertCheckIfNotExists(sqlStatement);
    }

    private void assertCheckIfNotExists(final CreateTableStatement sqlStatement) {
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        new ShardingCreateTableSupportedChecker().check(rule, database, mock(), sqlStatementContext);
    }
}
