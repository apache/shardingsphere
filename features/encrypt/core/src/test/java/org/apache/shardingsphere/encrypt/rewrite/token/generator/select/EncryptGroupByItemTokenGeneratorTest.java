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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.select;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptGroupByItemTokenGeneratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private EncryptGroupByItemTokenGenerator generator;
    
    @BeforeEach
    void setup() {
        generator = new EncryptGroupByItemTokenGenerator(mockEncryptRule());
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.isEncryptColumn("certificate_number")).thenReturn(true);
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(encryptColumn.getAssistedQuery()).thenReturn(Optional.empty());
        when(encryptTable.getEncryptColumn("certificate_number")).thenReturn(encryptColumn);
        when(result.findEncryptTable("t_encrypt")).thenReturn(Optional.of(encryptTable));
        return result;
    }
    
    @Test
    void assertGenerateSQLTokens() {
        assertThat(generator.generateSQLTokens(buildSelectStatementContext()).size(), is(1));
    }
    
    private SelectStatementContext buildSelectStatementContext() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_encrypt")));
        simpleTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ColumnOrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(getColumnSegment(), OrderDirection.ASC, NullsOrderType.FIRST);
        OrderByItem orderByItem = new OrderByItem(columnOrderByItemSegment);
        when(result.getGroupByContext().getItems()).thenReturn(Collections.singleton(orderByItem));
        when(result.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        when(result.getTablesContext()).thenReturn(new TablesContext(Collections.singleton(simpleTableSegment)));
        return result;
    }
    
    private ColumnSegment getColumnSegment() {
        ColumnSegment result = new ColumnSegment(0, 0, new IdentifierValue("certificate_number"));
        TableSegmentBoundInfo tableSegmentBoundInfo = new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db"));
        result.setColumnBoundInfo(new ColumnSegmentBoundInfo(tableSegmentBoundInfo, new IdentifierValue("t_encrypt"), new IdentifierValue("certificate_number"), TableSourceType.TEMPORARY_TABLE));
        result.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        return result;
    }
}
