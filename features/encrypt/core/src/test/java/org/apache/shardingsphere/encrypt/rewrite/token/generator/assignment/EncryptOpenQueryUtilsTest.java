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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptOpenQueryUtilsTest {
    
    @Test
    void assertIsOpenQueryFunctionTable() {
        assertTrue(EncryptOpenQueryUtils.isOpenQueryFunctionTable(createOpenQueryTableSegment("SELECT foo_col FROM foo_schema.foo_tbl")));
        assertFalse(EncryptOpenQueryUtils.isOpenQueryFunctionTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")))));
    }
    
    @Test
    void assertFindEncryptTable() {
        EncryptRule rule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(rule.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
        Optional<EncryptTable> actual = EncryptOpenQueryUtils.findEncryptTable(rule, createOpenQueryTableSegment("SELECT foo_col FROM foo_schema.foo_tbl"));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(encryptTable));
        assertFalse(EncryptOpenQueryUtils.findEncryptTable(rule, createOpenQueryTableSegment("SELECT foo_col FROM foo_schema.bar_tbl")).isPresent());
    }
    
    @Test
    void assertFindSchemaName() {
        Optional<String> actual = EncryptOpenQueryUtils.findSchemaName(createOpenQueryTableSegment("SELECT foo_col FROM foo_schema.foo_tbl"));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
        assertFalse(EncryptOpenQueryUtils.findSchemaName(createOpenQueryTableSegment("SELECT foo_col FROM foo_tbl")).isPresent());
    }
    
    @Test
    void assertFindEncryptTableWithDelimitedMultipartTableName() {
        EncryptRule rule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(rule.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
        Optional<EncryptTable> actual = EncryptOpenQueryUtils.findEncryptTable(rule, createOpenQueryTableSegment("SELECT foo_col FROM [foo_schema].[foo_tbl]"));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(encryptTable));
    }
    
    @Test
    void assertFindEncryptTableWithDoubleQuotedMultipartTableName() {
        EncryptRule rule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(rule.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
        Optional<EncryptTable> actual = EncryptOpenQueryUtils.findEncryptTable(rule, createOpenQueryTableSegment("SELECT foo_col FROM \"foo_schema\".\"foo_tbl\""));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(encryptTable));
    }
    
    @Test
    void assertFindEncryptTableWithThreePartUnrelatedTableReturnsEmpty() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.findEncryptTable("Employee")).thenReturn(Optional.empty());
        assertFalse(EncryptOpenQueryUtils.findEncryptTable(rule, createOpenQueryTableSegment("SELECT col FROM db.schema.Employee")).isPresent());
    }
    
    @Test
    void assertFindEncryptTableWithJoinUnrelatedTableReturnsEmpty() {
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.findEncryptTable("Employee")).thenReturn(Optional.empty());
        assertFalse(EncryptOpenQueryUtils.findEncryptTable(rule,
                createOpenQueryTableSegment("SELECT col FROM dbo.Employee JOIN dbo.Department ON Employee.DepartmentID = Department.DepartmentID")).isPresent());
    }
    
    @Test
    void assertFindEncryptTableWithCommaTableSourcesStillDiscoversEncryptTable() {
        EncryptRule rule = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(rule.findEncryptTable("Department")).thenReturn(Optional.of(encryptTable));
        Optional<EncryptTable> actual = EncryptOpenQueryUtils.findEncryptTable(rule,
                createOpenQueryTableSegment("SELECT GroupName FROM dbo.Department, dbo.Other"));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(encryptTable));
    }
    
    private FunctionTableSegment createOpenQueryTableSegment(final String openQuerySQL) {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "OPENQUERY", "OPENQUERY (foo_server, '" + openQuerySQL + "')");
        functionSegment.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("foo_server")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, openQuerySQL));
        return new FunctionTableSegment(0, 0, functionSegment);
    }
}
