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

package org.apache.shardingsphere.encrypt.rewrite.impl;

import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptProjectionTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptProjectionTokenGeneratorTest {
    
    private EncryptProjectionTokenGenerator generator;
    
    @Before
    public void setup() {
        generator = new EncryptProjectionTokenGenerator();
        generator.setEncryptRule(mockEncryptRule());
        generator.setDatabaseName("db_schema");
        generator.setSchemas(Collections.singletonMap("test", mock(ShardingSphereSchema.class)));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable1 = mock(EncryptTable.class);
        EncryptTable encryptTable2 = mock(EncryptTable.class);
        when(encryptTable1.getLogicColumns()).thenReturn(Collections.singletonList("mobile"));
        when(encryptTable2.getLogicColumns()).thenReturn(Collections.singletonList("mobile"));
        when(result.findPlainColumn("doctor", "mobile")).thenReturn(Optional.of("mobile"));
        when(result.findPlainColumn("doctor1", "mobile")).thenReturn(Optional.of("Mobile"));
        when(result.findEncryptTable("doctor")).thenReturn(Optional.of(encryptTable1));
        when(result.findEncryptTable("doctor1")).thenReturn(Optional.of(encryptTable2));
        EncryptColumn column = new EncryptColumn("mobile", null, "mobile", null, null);
        when(result.findEncryptColumn("doctor", "mobile")).thenReturn(Optional.of(column));
        return result;
    }
    
    @Test
    public void assertGenerateSQLTokensWhenOwnerMatchTableAlias() {
        SimpleTableSegment doctorTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor")));
        doctorTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("mobile"));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        ProjectionsSegment projections = mock(ProjectionsSegment.class);
        when(projections.getProjections()).thenReturn(Collections.singletonList(new ColumnProjectionSegment(column)));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projections);
        when(sqlStatementContext.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        SimpleTableSegment doctorOneTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor1")));
        when(sqlStatementContext.getTablesContext()).thenReturn(new TablesContext(Arrays.asList(doctorTable, doctorOneTable), DatabaseTypeEngine.getDatabaseType("MySQL")));
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singletonList(new ColumnProjection("a", "mobile", null)));
        Collection<SubstitutableColumnNameToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    public void assertGenerateSQLTokensWhenOwnerMatchTableAliasForSameTable() {
        SimpleTableSegment doctorTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor")));
        doctorTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("mobile"));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        ProjectionsSegment projections = mock(ProjectionsSegment.class);
        when(projections.getProjections()).thenReturn(Collections.singletonList(new ColumnProjectionSegment(column)));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projections);
        when(sqlStatementContext.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        SimpleTableSegment sameDoctorTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor")));
        when(sqlStatementContext.getTablesContext()).thenReturn(new TablesContext(Arrays.asList(doctorTable, sameDoctorTable), DatabaseTypeEngine.getDatabaseType("MySQL")));
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singletonList(new ColumnProjection("a", "mobile", null)));
        Collection<SubstitutableColumnNameToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
    }
    
    @Test
    public void assertGenerateSQLTokensWhenOwnerMatchTableName() {
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("mobile"));
        column.setOwner(new OwnerSegment(0, 0, new IdentifierValue("doctor")));
        ProjectionsSegment projections = mock(ProjectionsSegment.class);
        when(projections.getProjections()).thenReturn(Collections.singletonList(new ColumnProjectionSegment(column)));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projections);
        when(sqlStatementContext.getSubqueryContexts().values()).thenReturn(Collections.emptyList());
        SimpleTableSegment doctorTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor")));
        SimpleTableSegment doctorOneTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("doctor1")));
        when(sqlStatementContext.getTablesContext()).thenReturn(new TablesContext(Arrays.asList(doctorTable, doctorOneTable), DatabaseTypeEngine.getDatabaseType("MySQL")));
        when(sqlStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singletonList(new ColumnProjection("doctor", "mobile", null)));
        Collection<SubstitutableColumnNameToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertThat(actual.size(), is(1));
    }
}
