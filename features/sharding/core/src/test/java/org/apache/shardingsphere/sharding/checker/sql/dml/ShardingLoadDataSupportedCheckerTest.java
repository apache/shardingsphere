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

package org.apache.shardingsphere.sharding.checker.sql.dml;

import org.apache.shardingsphere.infra.binder.context.statement.TableAvailableSQLStatementContext;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.LoadDataStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingLoadDataSupportedCheckerTest {
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheckWithSingleTable() {
        LoadDataStatement sqlStatement = mock(LoadDataStatement.class);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")));
        when(sqlStatement.getTable()).thenReturn(table);
        assertDoesNotThrow(() -> new ShardingLoadDataSupportedChecker().check(rule, mock(), mock(), new TableAvailableSQLStatementContext(mock(), sqlStatement, Collections.singleton(table))));
    }
    
    @Test
    void assertCheckWithShardingTable() {
        LoadDataStatement sqlStatement = mock(LoadDataStatement.class);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")));
        when(sqlStatement.getTable()).thenReturn(table);
        when(rule.isShardingTable("foo_tbl")).thenReturn(true);
        assertThrows(UnsupportedShardingOperationException.class,
                () -> new ShardingLoadDataSupportedChecker().check(rule, mock(), mock(), new TableAvailableSQLStatementContext(mock(), sqlStatement, Collections.singleton(table))));
    }
}
