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

package org.apache.shardingsphere.encrypt.rewrite.token.generator;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptOrderByItemTokenGeneratorTest {
    
    private final EncryptOrderByItemTokenGenerator generator = new EncryptOrderByItemTokenGenerator();
    
    @BeforeEach
    void setup() {
        generator.setEncryptRule(mockEncryptRule());
        generator.setDatabaseName("db_schema");
        generator.setSchemas(Collections.singletonMap("test", mock(ShardingSphereSchema.class)));
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
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("certificate_number"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        ColumnOrderByItemSegment columnOrderByItemSegment = new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST);
        OrderByItem orderByItem = new OrderByItem(columnOrderByItemSegment);
        when(result.getOrderByContext().getItems()).thenReturn(Collections.singletonList(orderByItem));
        when(result.getGroupByContext().getItems()).thenReturn(Collections.emptyList());
        when(result.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        when(result.getTablesContext()).thenReturn(new TablesContext(Collections.singletonList(simpleTableSegment), DatabaseTypeFactory.get("MySQL")));
        return result;
    }
}
