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

import org.apache.shardingsphere.infra.binder.context.statement.dml.CopyStatementContext;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.CopyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.opengauss.dml.OpenGaussCopyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLCopyStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingCopySupportedCheckerTest {
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheckWhenTableSegmentForPostgreSQL() {
        PostgreSQLCopyStatement sqlStatement = new PostgreSQLCopyStatement();
        sqlStatement.setTableSegment(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        assertDoesNotThrow(() -> new ShardingCopySupportedChecker().check(rule, mock(), mock(), new CopyStatementContext(sqlStatement, "foo_db")));
    }
    
    @Test
    void assertCheckWhenTableSegmentForOpenGauss() {
        OpenGaussCopyStatement sqlStatement = new OpenGaussCopyStatement();
        sqlStatement.setTableSegment(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        assertDoesNotThrow(() -> new ShardingCopySupportedChecker().check(rule, mock(), mock(), new CopyStatementContext(sqlStatement, "foo_db")));
    }
    
    @Test
    void assertCheckCopyWithShardingTableForPostgreSQL() {
        assertThrows(UnsupportedShardingOperationException.class, () -> assertCheckCopyTable(new PostgreSQLCopyStatement()));
    }
    
    @Test
    void assertCheckCopyWithShardingTableForOpenGauss() {
        assertThrows(UnsupportedShardingOperationException.class, () -> assertCheckCopyTable(new OpenGaussCopyStatement()));
    }
    
    private void assertCheckCopyTable(final CopyStatement sqlStatement) {
        sqlStatement.setTableSegment(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        CopyStatementContext sqlStatementContext = new CopyStatementContext(sqlStatement, "foo_db");
        String tableName = "t_order";
        when(rule.isShardingTable(tableName)).thenReturn(true);
        new ShardingCopySupportedChecker().check(rule, mock(), mock(), sqlStatementContext);
    }
}
