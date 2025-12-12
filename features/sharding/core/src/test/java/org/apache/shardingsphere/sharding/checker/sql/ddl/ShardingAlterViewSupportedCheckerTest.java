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
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.exception.metadata.EngagedViewException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingAlterViewSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertPreValidateAlterView() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        AlterViewStatement sqlStatement = new AlterViewStatement(databaseType);
        sqlStatement.setView(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_view"))));
        sqlStatement.setSelect(selectStatement);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        AlterViewStatementContext sqlStatementContext = new AlterViewStatementContext(metaData, sqlStatement, "foo_db");
        when(rule.isShardingTable("t_order")).thenReturn(false);
        assertDoesNotThrow(() -> new ShardingAlterViewSupportedChecker().check(rule, mock(), mock(), sqlStatementContext));
    }
    
    @Test
    void assertPreValidateAlterViewWithShardingTable() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        AlterViewStatement sqlStatement = mock(AlterViewStatement.class);
        when(sqlStatement.getView()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_view"))));
        when(sqlStatement.getSelect()).thenReturn(Optional.of(selectStatement));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        AlterViewStatementContext sqlStatementContext = new AlterViewStatementContext(metaData, sqlStatement, "foo_db");
        when(rule.isShardingTable("t_order")).thenReturn(true);
        assertThrows(EngagedViewException.class, () -> new ShardingAlterViewSupportedChecker().check(rule, mock(), mock(), sqlStatementContext));
    }
    
    @Test
    void assertPreValidateAlterRenamedView() {
        AlterViewStatement sqlStatement = mock(AlterViewStatement.class);
        when(sqlStatement.getView()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_view"))));
        when(sqlStatement.getRenameView()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_new")))));
        AlterViewStatementContext sqlStatementContext = new AlterViewStatementContext(mock(ShardingSphereMetaData.class), sqlStatement, "foo_db");
        assertDoesNotThrow(() -> new ShardingAlterViewSupportedChecker().check(rule, mock(), mock(), sqlStatementContext));
    }
}
