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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.projection;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptProjectionTokenGeneratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private EncryptProjectionTokenGenerator generator;
    
    @BeforeEach
    void setup() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        generator = new EncryptProjectionTokenGenerator(Collections.emptyList(), databaseType, mockEncryptRule());
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        EncryptTable encryptTable1 = mock(EncryptTable.class);
        EncryptTable encryptTable2 = mock(EncryptTable.class);
        when(result.findEncryptTable("doctor")).thenReturn(Optional.of(encryptTable1));
        when(result.findEncryptTable("doctor1")).thenReturn(Optional.of(encryptTable2));
        when(encryptTable1.isEncryptColumn("mobile")).thenReturn(true);
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(encryptColumn.getAssistedQuery()).thenReturn(Optional.empty());
        when(encryptTable1.getEncryptColumn("mobile")).thenReturn(encryptColumn);
        when(result.findEncryptTable("t_order").isPresent()).thenReturn(true);
        when(result.getEncryptTable("t_order").isEncryptColumn("order_id")).thenReturn(true);
        return result;
    }
    
    @Test
    void assertGenerateSQLTokensWhenOwnerMatchTableAlias() {
        SimpleTableSegment doctorTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor")));
        doctorTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("mobile"));
        column.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")), new IdentifierValue("doctor"),
                new IdentifierValue("mobile"), TableSourceType.PHYSICAL_TABLE));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        ProjectionsSegment projections = mock(ProjectionsSegment.class);
        when(projections.getProjections()).thenReturn(Collections.singletonList(new ColumnProjectionSegment(column)));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSubqueryType()).thenReturn(null);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projections);
        when(sqlStatementContext.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        SimpleTableSegment doctorOneTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor1")));
        when(sqlStatementContext.getTablesContext()).thenReturn(new TablesContext(Arrays.asList(doctorTable, doctorOneTable)));
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singleton(new ColumnProjection("a", "mobile", null, databaseType)));
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokensWhenOwnerMatchTableAliasForSameTable() {
        SimpleTableSegment doctorTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor")));
        doctorTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("mobile"));
        column.setColumnBoundInfo(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_db")), new IdentifierValue("doctor"),
                new IdentifierValue("mobile"), TableSourceType.PHYSICAL_TABLE));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        ProjectionsSegment projections = mock(ProjectionsSegment.class);
        when(projections.getProjections()).thenReturn(Collections.singletonList(new ColumnProjectionSegment(column)));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSubqueryType()).thenReturn(null);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projections);
        when(sqlStatementContext.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        SimpleTableSegment sameDoctorTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor")));
        when(sqlStatementContext.getTablesContext()).thenReturn(new TablesContext(Arrays.asList(doctorTable, sameDoctorTable)));
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singleton(new ColumnProjection("a", "mobile", null, databaseType)));
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokensWhenOwnerMatchTableName() {
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("mobile"));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue("doctor")));
        ProjectionsSegment projections = mock(ProjectionsSegment.class);
        when(projections.getProjections()).thenReturn(Collections.singletonList(new ColumnProjectionSegment(column)));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSubqueryType()).thenReturn(null);
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projections);
        when(sqlStatementContext.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        SimpleTableSegment doctorTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor")));
        SimpleTableSegment doctorOneTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor1")));
        when(sqlStatementContext.getTablesContext()).thenReturn(new TablesContext(Arrays.asList(doctorTable, doctorOneTable)));
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singleton(new ColumnProjection("doctor", "mobile", null, databaseType)));
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
    }
}
