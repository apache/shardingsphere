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

package org.apache.shardingsphere.encrypt.merge.dal.show;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecoratedEncryptShowColumnsMergedResultTest {
    
    @Mock
    private MergedResult mergedResult;
    
    @Test
    void assertNextWithNotHasNext() throws SQLException {
        assertFalse(createDecoratedEncryptShowColumnsMergedResult(mergedResult, mock(EncryptRule.class)).next());
    }
    
    @Test
    void assertNextWithHasNext() throws SQLException {
        when(mergedResult.next()).thenReturn(true);
        assertTrue(createDecoratedEncryptShowColumnsMergedResult(mergedResult, mock(EncryptRule.class)).next());
    }
    
    @Test
    void assertNextWithAssistedQuery() throws SQLException {
        when(mergedResult.next()).thenReturn(true).thenReturn(false);
        when(mergedResult.getValue(1, String.class)).thenReturn("user_id_assisted");
        assertFalse(createDecoratedEncryptShowColumnsMergedResult(mergedResult, mockEncryptRule()).next());
    }
    
    @Test
    void assertNextWithLikeQuery() throws SQLException {
        when(mergedResult.next()).thenReturn(true).thenReturn(false);
        when(mergedResult.getValue(1, String.class)).thenReturn("user_id_like");
        assertFalse(createDecoratedEncryptShowColumnsMergedResult(mergedResult, mockEncryptRule()).next());
    }
    
    @Test
    void assertNextWithLikeQueryAndMultiColumns() throws SQLException {
        when(mergedResult.next()).thenReturn(true, true, true, false);
        when(mergedResult.getValue(1, String.class)).thenReturn("user_id_like", "order_id", "content");
        DecoratedEncryptShowColumnsMergedResult actual = createDecoratedEncryptShowColumnsMergedResult(mergedResult, mockEncryptRule());
        assertTrue(actual.next());
        assertTrue(actual.next());
        assertFalse(actual.next());
    }
    
    @Test
    void assertGetValueWithCipherColumn() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("user_id_cipher");
        assertThat(createDecoratedEncryptShowColumnsMergedResult(mergedResult, mockEncryptRule()).getValue(1, String.class), is("user_id"));
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(result.findEncryptTable("t_encrypt")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.isAssistedQueryColumn("user_id_assisted")).thenReturn(true);
        when(encryptTable.isLikeQueryColumn("user_id_like")).thenReturn(true);
        when(encryptTable.isCipherColumn("user_id_cipher")).thenReturn(true);
        when(encryptTable.getLogicColumnByCipherColumn("user_id_cipher")).thenReturn("user_id");
        return result;
    }
    
    @Test
    void assertGetValueWithOtherColumn() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("user_id_assisted");
        assertThat(createDecoratedEncryptShowColumnsMergedResult(mergedResult, mock(EncryptRule.class)).getValue(1, String.class), is("user_id_assisted"));
    }
    
    @Test
    void assertGetValueWithOtherIndex() throws SQLException {
        when(mergedResult.getValue(2, String.class)).thenReturn("order_id");
        assertThat(createDecoratedEncryptShowColumnsMergedResult(mergedResult, mock(EncryptRule.class)).getValue(2, String.class), is("order_id"));
    }
    
    @Test
    void assertWasNull() throws SQLException {
        assertFalse(createDecoratedEncryptShowColumnsMergedResult(mergedResult, mock(EncryptRule.class)).wasNull());
    }
    
    @Test
    void assertGetCalendarValue() {
        assertThrows(SQLFeatureNotSupportedException.class,
                () -> createDecoratedEncryptShowColumnsMergedResult(mergedResult, mock(EncryptRule.class)).getCalendarValue(1, Date.class, Calendar.getInstance()));
    }
    
    @Test
    void assertGetInputStream() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> createDecoratedEncryptShowColumnsMergedResult(mergedResult, mock(EncryptRule.class)).getInputStream(1, "asc"));
    }
    
    private DecoratedEncryptShowColumnsMergedResult createDecoratedEncryptShowColumnsMergedResult(final MergedResult mergedResult, final EncryptRule encryptRule) {
        ShowColumnsStatementContext showColumnsStatementContext = mock(ShowColumnsStatementContext.class);
        when(showColumnsStatementContext.getAllTables()).thenReturn(Collections.singletonList(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_encrypt")))));
        return new DecoratedEncryptShowColumnsMergedResult(mergedResult, showColumnsStatementContext, encryptRule);
    }
}
