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

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.AssistedQueryColumnItem;
import org.apache.shardingsphere.encrypt.rule.column.item.LikeQueryColumnItem;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptOpenQueryPassThroughSQLTest {
    
    @Test
    void assertParseWithMultipartTableName() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM foo_schema.foo_tbl WHERE foo_col = 1");
        assertThat(actual.getTableName(), is("foo_tbl"));
        assertThat(actual.getSchemaName(), is(Optional.of("foo_schema")));
        assertThat(actual.getRemainder(), is(" WHERE foo_col = 1"));
    }
    
    @Test
    void assertParseWithDelimitedMultipartTableName() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT foo_col FROM [foo_schema].[foo_tbl]");
        assertThat(actual.getTableName(), is("foo_tbl"));
        assertThat(actual.getSchemaName(), is(Optional.of("foo_schema")));
    }
    
    @Test
    void assertParsePreservesPredicateOnEncryptedColumn() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT GroupName FROM dbo.Department WHERE GroupName IS NOT NULL");
        assertThat(actual.getRemainder(), is(" WHERE GroupName IS NOT NULL"));
    }
    
    @Test
    void assertParsePreservesStringLiteralContainingColumnNameInPredicate() {
        EncryptOpenQueryPassThroughSQL actual = EncryptOpenQueryPassThroughSQL.parse("SELECT GroupName FROM dbo.Department WHERE Note = 'GroupName'");
        assertThat(actual.getRemainder(), is(" WHERE Note = 'GroupName'"));
    }
    
    @Test
    void assertRewritePreservesPredicateOnEncryptedColumn() {
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(encryptColumn.getName()).thenReturn("GroupName");
        when(encryptColumn.getCipher().getName()).thenReturn("group_name_cipher");
        when(encryptColumn.getAssistedQuery()).thenReturn(Optional.empty());
        when(encryptColumn.getLikeQuery()).thenReturn(Optional.empty());
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT GroupName FROM dbo.Department WHERE GroupName IS NOT NULL");
        String actual = passThroughSQL.rewrite(Collections.singletonList(encryptColumn));
        assertThat(actual, is("SELECT group_name_cipher FROM dbo.Department WHERE GroupName IS NOT NULL"));
    }
    
    @Test
    void assertRewritePreservesStringLiteralContainingColumnNameInPredicate() {
        EncryptColumn encryptColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        when(encryptColumn.getName()).thenReturn("GroupName");
        when(encryptColumn.getCipher().getName()).thenReturn("group_name_cipher");
        when(encryptColumn.getAssistedQuery()).thenReturn(Optional.empty());
        when(encryptColumn.getLikeQuery()).thenReturn(Optional.empty());
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT GroupName FROM dbo.Department WHERE Note = 'GroupName'");
        String actual = passThroughSQL.rewrite(Collections.singletonList(encryptColumn));
        assertThat(actual, is("SELECT group_name_cipher FROM dbo.Department WHERE Note = 'GroupName'"));
    }
    
    @Test
    void assertFindTableNameWithThreePartTableName() {
        Optional<String> actual = EncryptOpenQueryPassThroughSQL.findTableName("SELECT GroupName FROM db.schema.Department WHERE DepartmentID = 4");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("Department"));
    }
    
    @Test
    void assertFindTableNameWithJoin() {
        Optional<String> actual = EncryptOpenQueryPassThroughSQL.findTableName(
                "SELECT GroupName FROM dbo.Department JOIN dbo.Employee ON Department.DepartmentID = Employee.DepartmentID");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("Department"));
    }
    
    @Test
    void assertParseWithSelectListLiteralExpectsException() {
        assertThrows(UnsupportedEncryptSQLException.class, () -> EncryptOpenQueryPassThroughSQL.parse("SELECT 'GroupName' FROM dbo.Department"));
    }
    
    @Test
    void assertParseWithSelectListExpressionExpectsException() {
        assertThrows(UnsupportedEncryptSQLException.class, () -> EncryptOpenQueryPassThroughSQL.parse("SELECT UPPER(GroupName) FROM dbo.Department"));
    }
    
    @Test
    void assertParseWithSpaceDelimitedIdentifierExpectsException() {
        assertThrows(UnsupportedEncryptSQLException.class, () -> EncryptOpenQueryPassThroughSQL.parse("SELECT GroupName FROM [Human Resources].[Department]"));
    }
    
    @Test
    void assertParseWithThreePartTableNameExpectsException() {
        assertThrows(UnsupportedEncryptSQLException.class, () -> EncryptOpenQueryPassThroughSQL.parse("SELECT GroupName FROM db.schema.Department"));
    }
    
    @Test
    void assertParseWithJoinExpectsException() {
        String passThroughSQL = "SELECT GroupName FROM dbo.Department JOIN dbo.Employee ON Department.DepartmentID = Employee.DepartmentID";
        assertThrows(UnsupportedEncryptSQLException.class, () -> EncryptOpenQueryPassThroughSQL.parse(passThroughSQL));
    }
    
    @Test
    void assertRewriteWithDerivedColumns() {
        EncryptColumn remarkColumn = mock(EncryptColumn.class, RETURNS_DEEP_STUBS);
        AssistedQueryColumnItem assistedQuery = mock(AssistedQueryColumnItem.class);
        LikeQueryColumnItem likeQuery = mock(LikeQueryColumnItem.class);
        when(remarkColumn.getName()).thenReturn("Remark");
        when(remarkColumn.getCipher().getName()).thenReturn("remark_cipher");
        when(assistedQuery.getName()).thenReturn("assisted_query_remark");
        when(likeQuery.getName()).thenReturn("like_query_remark");
        when(remarkColumn.getAssistedQuery()).thenReturn(Optional.of(assistedQuery));
        when(remarkColumn.getLikeQuery()).thenReturn(Optional.of(likeQuery));
        EncryptOpenQueryPassThroughSQL passThroughSQL = EncryptOpenQueryPassThroughSQL.parse("SELECT GroupName, Remark FROM dbo.Department WHERE DepartmentID = 4");
        String actual = passThroughSQL.rewrite(Collections.singletonList(remarkColumn));
        assertThat(actual, is("SELECT GroupName, remark_cipher, assisted_query_remark, like_query_remark FROM dbo.Department WHERE DepartmentID = 4"));
    }
}
