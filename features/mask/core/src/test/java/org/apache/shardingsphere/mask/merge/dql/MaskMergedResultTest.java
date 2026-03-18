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

package org.apache.shardingsphere.mask.merge.dql;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.rule.MaskTable;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaskMergedResultTest {
    
    @Test
    void assertNext() throws SQLException {
        assertFalse(new MaskMergedResult(mock(), mock(), mock(), mock()).next());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetValueArguments")
    void assertGetValue(final String name, final MergedResult mergedResult, final ShardingSphereDatabase database, final ShardingSphereMetaData metaData,
                        final SelectStatementContext selectStatementContext, final Class<?> valueType, final Object expectedValue) throws SQLException {
        assertThat(new MaskMergedResult(database, metaData, selectStatementContext, mergedResult).getValue(1, valueType), is(expectedValue));
    }
    
    @Test
    void assertGetCalendarValue() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(new MaskMergedResult(mock(), mock(), mock(), mergedResult).getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetInputStream() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        InputStream inputStream = mock(InputStream.class);
        when(mergedResult.getInputStream(1, "asc")).thenReturn(inputStream);
        assertThat(new MaskMergedResult(mock(), mock(), mock(), mergedResult).getInputStream(1, "asc"), is(inputStream));
    }
    
    @Test
    void assertGetCharacterStream() throws SQLException {
        MergedResult mergedResult = mock(MergedResult.class);
        Reader reader = mock(Reader.class);
        when(mergedResult.getCharacterStream(1)).thenReturn(reader);
        assertThat(new MaskMergedResult(mock(), mock(), mock(), mergedResult).getCharacterStream(1), is(reader));
    }
    
    @Test
    void assertWasNull() throws SQLException {
        assertFalse(new MaskMergedResult(mock(), mock(), mock(), mock()).wasNull());
    }
    
    private static Stream<Arguments> assertGetValueArguments() throws SQLException {
        return Stream.of(
                Arguments.of("without column bound info returns merged value", mockMergedResultWithValue(String.class, "VALUE"),
                        mock(ShardingSphereDatabase.class), mock(ShardingSphereMetaData.class), mockSelectStatementContextWithoutColumnBoundInfo(), String.class, "VALUE"),
                Arguments.of("without mask rule returns merged value", mockMergedResultWithValue(String.class, "VALUE"),
                        mockDatabaseWithoutMaskRule(), mock(ShardingSphereMetaData.class), mockSelectStatementContextWithColumnBoundInfo(), String.class, "VALUE"),
                Arguments.of("without mask table returns merged value", mockMergedResultWithValue(String.class, "VALUE"),
                        mockDatabaseWithoutMaskTable(), mock(ShardingSphereMetaData.class), mockSelectStatementContextWithColumnBoundInfo(), String.class, "VALUE"),
                Arguments.of("without mask algorithm on second lookup returns merged value", mockMergedResultWithValue(String.class, "VALUE"),
                        mockDatabaseWithoutMaskAlgorithmOnSecondLookup(), mock(ShardingSphereMetaData.class), mockSelectStatementContextWithColumnBoundInfo(), String.class, "VALUE"),
                Arguments.of("null original value returns null", mockMergedResultWithValue(Object.class, null),
                        mockDatabaseWithMaskRule(), mock(ShardingSphereMetaData.class), mockSelectStatementContextWithColumnBoundInfo(), Object.class, null),
                Arguments.of("mask algorithm applies to non-null value", mockMergedResultWithValue(Object.class, "VALUE"),
                        mockDatabaseWithMaskRule(), mock(ShardingSphereMetaData.class), mockSelectStatementContextWithColumnBoundInfo(), String.class, "MASK_VALUE"),
                Arguments.of("database resolved from metadata returns merged value", mockMergedResultWithValue(String.class, "VALUE"),
                        mock(ShardingSphereDatabase.class), mockMetaDataWithDatabase(), mockSelectStatementContextWithColumnBoundInfo(), String.class, "VALUE"));
    }
    
    private static MergedResult mockMergedResultWithValue(final Class<?> valueType, final Object value) throws SQLException {
        MergedResult result = mock(MergedResult.class);
        when(result.getValue(1, valueType)).thenReturn(value);
        return result;
    }
    
    private static ShardingSphereDatabase mockDatabaseWithoutMaskRule() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.empty());
        return result;
    }
    
    private static ShardingSphereDatabase mockDatabaseWithoutMaskTable() {
        MaskRule maskRule = mock(MaskRule.class);
        when(maskRule.findMaskTable("foo_tbl")).thenReturn(Optional.empty());
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ShardingSphereDatabase mockDatabaseWithoutMaskAlgorithmOnSecondLookup() {
        MaskAlgorithm maskAlgorithm = mock(MaskAlgorithm.class);
        MaskTable firstMaskTable = mock(MaskTable.class);
        when(firstMaskTable.findAlgorithm("foo_col")).thenReturn(Optional.of(maskAlgorithm));
        MaskTable secondMaskTable = mock(MaskTable.class);
        when(secondMaskTable.findAlgorithm("foo_col")).thenReturn(Optional.empty());
        MaskRule maskRule = mock(MaskRule.class);
        when(maskRule.findMaskTable("foo_tbl")).thenReturn(Optional.of(firstMaskTable), Optional.of(secondMaskTable));
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        return result;
    }
    
    private static ShardingSphereDatabase mockDatabaseWithMaskRule() {
        MaskRule maskRule = mockMaskRuleWithAlgorithm();
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static MaskRule mockMaskRuleWithAlgorithm() {
        MaskAlgorithm maskAlgorithm = mock(MaskAlgorithm.class);
        when(maskAlgorithm.mask("VALUE")).thenReturn("MASK_VALUE");
        MaskTable maskTable = mock(MaskTable.class);
        when(maskTable.findAlgorithm("foo_col")).thenReturn(Optional.of(maskAlgorithm));
        MaskRule result = mock(MaskRule.class);
        when(result.findMaskTable("foo_tbl")).thenReturn(Optional.of(maskTable));
        return result;
    }
    
    private static ShardingSphereMetaData mockMetaDataWithDatabase() {
        ShardingSphereDatabase database = mockDatabaseWithoutMaskRule();
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    private static SelectStatementContext mockSelectStatementContextWithoutColumnBoundInfo() {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.findColumnBoundInfo(1)).thenReturn(Optional.empty());
        return result;
    }
    
    private static SelectStatementContext mockSelectStatementContextWithColumnBoundInfo() {
        SelectStatementContext result = mock(SelectStatementContext.class);
        ColumnSegmentBoundInfo columnSegmentBoundInfo = new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("foo_tbl"), new IdentifierValue("foo_col"), TableSourceType.PHYSICAL_TABLE);
        when(result.findColumnBoundInfo(1)).thenReturn(Optional.of(columnSegmentBoundInfo));
        return result;
    }
}
