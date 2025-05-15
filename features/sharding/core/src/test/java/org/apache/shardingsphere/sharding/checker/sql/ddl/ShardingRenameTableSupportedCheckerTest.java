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

import org.apache.shardingsphere.infra.binder.context.statement.ddl.RenameTableStatementContext;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingRenameTableSupportedCheckerTest {
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheckShardingTable() {
        RenameTableStatementContext sqlStatementContext = createRenameTableStatementContext("t_order", "t_user_order");
        when(rule.containsShardingTable(argThat(tableNames -> tableNames.contains("t_order") || tableNames.contains("t_user_order")))).thenReturn(true);
        assertThrows(UnsupportedShardingOperationException.class, () -> new ShardingRenameTableSupportedChecker().check(rule, mock(), mock(), sqlStatementContext));
    }
    
    @Test
    void assertCheckNormalCase() {
        RenameTableStatementContext sqlStatementContext = createRenameTableStatementContext("t_not_sharding_table", "t_not_sharding_table_new");
        assertDoesNotThrow(() -> new ShardingRenameTableSupportedChecker().check(rule, mock(), mock(), sqlStatementContext));
    }
    
    private RenameTableStatementContext createRenameTableStatementContext(final String originTableName, final String newTableName) {
        RenameTableStatement sqlStatement = mock(RenameTableStatement.class);
        RenameTableDefinitionSegment renameTableDefinitionSegment = new RenameTableDefinitionSegment(0, 0);
        renameTableDefinitionSegment.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(originTableName))));
        renameTableDefinitionSegment.setRenameTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(newTableName))));
        when(sqlStatement.getRenameTables()).thenReturn(Collections.singleton(renameTableDefinitionSegment));
        return new RenameTableStatementContext(sqlStatement);
    }
}
