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

import org.apache.shardingsphere.encrypt.rewrite.token.generator.impl.EncryptProjectionTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptProjectionTokenGeneratorTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private EncryptProjectionTokenGenerator encryptProjectionTokenGenerator;
    
    @Before
    public void setup() {
        encryptProjectionTokenGenerator = new EncryptProjectionTokenGenerator();
        encryptProjectionTokenGenerator.setEncryptRule(buildEncryptRule());
    }
    
    @Test
    public void assertOwnerExistsMatchTableAliasGenerateSQLTokens() {
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Arrays.asList("doctor", "doctor1"));
        List<SimpleTableSegment> allUniqueTables = buildAllUniqueTables();
        when(sqlStatementContext.getTablesContext().getAllUniqueTables()).thenReturn(allUniqueTables);
        IdentifierValue identifierValue = new IdentifierValue("mobile");
        ColumnSegment columnSegment = new ColumnSegment(0, 0, identifierValue);
        OwnerSegment ownerSegment = new OwnerSegment(0, 0, new IdentifierValue("a"));
        columnSegment.setOwner(ownerSegment);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(columnProjectionSegment));
        Collection<SubstitutableColumnNameToken> tokens = encryptProjectionTokenGenerator.generateSQLTokens(sqlStatementContext);
        assertThat(tokens.size(), is(1));
    }
    
    @Test
    public void assertOwnerExistsMatchTableNameGenerateSQLTokens() {
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Arrays.asList("doctor", "doctor1"));
        List<SimpleTableSegment> allUniqueTables = buildAllUniqueTables(false);
        when(sqlStatementContext.getTablesContext().getAllUniqueTables()).thenReturn(allUniqueTables);
        IdentifierValue identifierValue = new IdentifierValue("mobile");
        ColumnSegment columnSegment = new ColumnSegment(0, 0, identifierValue);
        OwnerSegment ownerSegment = new OwnerSegment(0, 0, new IdentifierValue("doctor"));
        columnSegment.setOwner(ownerSegment);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(columnProjectionSegment));
        Collection<SubstitutableColumnNameToken> tokens = encryptProjectionTokenGenerator.generateSQLTokens(sqlStatementContext);
        assertThat(tokens.size(), is(1));
    }
    
    @Test
    public void assertColumnUnAmbiguousGenerateSQLTokens() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("column `mobile` is ambiguous in encrypt rules");
        ProjectionsSegment projectionsSegment = mock(ProjectionsSegment.class);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getProjections()).thenReturn(projectionsSegment);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Arrays.asList("doctor", "doctor1"));
        List<SimpleTableSegment> allUniqueTables = buildAllUniqueTables();
        when(sqlStatementContext.getTablesContext().getAllUniqueTables()).thenReturn(allUniqueTables);
        IdentifierValue identifierValue = new IdentifierValue("mobile");
        ColumnSegment columnSegment = new ColumnSegment(0, 0, identifierValue);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
        when(projectionsSegment.getProjections()).thenReturn(Collections.singletonList(columnProjectionSegment));
        encryptProjectionTokenGenerator.generateSQLTokens(sqlStatementContext);
    }
    
    private List<SimpleTableSegment> buildAllUniqueTables() {
        return buildAllUniqueTables(true);
    }
    
    private List<SimpleTableSegment> buildAllUniqueTables(final boolean hasAlias) {
        SimpleTableSegment table1 = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        when(table1.getTableName().getIdentifier().getValue()).thenReturn("doctor");
        SimpleTableSegment table2 = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        when(table2.getTableName().getIdentifier().getValue()).thenReturn("doctor1");
        if (hasAlias) {
            when(table1.getAlias()).thenReturn(Optional.of("a"));
            when(table2.getAlias()).thenReturn(Optional.of("b"));
        }
        return Arrays.asList(table1, table2);
    }
    
    private EncryptRule buildEncryptRule() {
        EncryptRule encryptRule = mock(EncryptRule.class);
        EncryptTable encryptTable1 = mock(EncryptTable.class);
        EncryptTable encryptTable2 = mock(EncryptTable.class);
        when(encryptTable1.getLogicColumns()).thenReturn(Collections.singletonList("mobile"));
        when(encryptTable2.getLogicColumns()).thenReturn(Collections.singletonList("mobile"));
        when(encryptRule.findPlainColumn("doctor", "mobile")).thenReturn(Optional.of("mobile"));
        when(encryptRule.findPlainColumn("doctor1", "mobile")).thenReturn(Optional.of("Mobile"));
        when(encryptRule.findEncryptTable("doctor")).thenReturn(Optional.of(encryptTable1));
        when(encryptRule.findEncryptTable("doctor1")).thenReturn(Optional.of(encryptTable2));
        return encryptRule;
    }
}
