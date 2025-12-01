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

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MaskMergedResultTest {
    
    @Mock
    private MergedResult mergedResult;
    
    @Test
    void assertNext() throws SQLException {
        assertFalse(new MaskMergedResult(mock(ShardingSphereDatabase.class), mock(ShardingSphereMetaData.class), mock(SelectStatementContext.class), mergedResult).next());
    }
    
    @Test
    void assertGetValue() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        assertNull(new MaskMergedResult(database, mock(ShardingSphereMetaData.class), mockSelectStatementContext(), mergedResult).getValue(1, String.class));
    }
    
    @Test
    void assertGetValueWithNull() throws SQLException {
        when(mergedResult.getValue(1, Object.class)).thenReturn("VALUE");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        MaskRule maskRule = mockMaskRule();
        when(database.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        assertThat(new MaskMergedResult(database, mock(ShardingSphereMetaData.class), mockSelectStatementContext(), mergedResult).getValue(1, String.class), is("MASK_VALUE"));
    }
    
    @Test
    void assertGetValueWithoutColumnProjection() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("VALUE");
        assertThat(new MaskMergedResult(mock(ShardingSphereDatabase.class), mock(ShardingSphereMetaData.class), mockSelectStatementContextWithoutColumnProjection(), mergedResult)
                .getValue(1, String.class), is("VALUE"));
        
    }
    
    @Test
    void assertGetValueWithoutMaskTable() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("VALUE");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        MaskRule maskRule = mockMaskRuleTableAbsent();
        when(database.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        assertThat(new MaskMergedResult(database, mock(ShardingSphereMetaData.class), mockSelectStatementContextForMaskTableAbsent(), mergedResult).getValue(1, String.class),
                is("VALUE"));
    }
    
    @Test
    void assertGetValueWithoutMaskAlgorithm() throws SQLException {
        when(mergedResult.getValue(1, String.class)).thenReturn("VALUE");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        MaskRule maskRule = mockMaskAlgorithmAbsent();
        when(database.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        assertThat(new MaskMergedResult(database, mock(ShardingSphereMetaData.class), mockSelectStatementContext(), mergedResult).getValue(1, String.class), is("VALUE"));
    }
    
    @Test
    void assertGetValueWhenOriginalValueIsNull() throws SQLException {
        when(mergedResult.getValue(1, Object.class)).thenReturn(null);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        MaskRule maskRule = mockMaskRule();
        when(database.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        assertNull(new MaskMergedResult(database, mock(ShardingSphereMetaData.class), mockSelectStatementContext(), mergedResult).getValue(1, Object.class));
    }
    
    @SuppressWarnings("unchecked")
    private MaskRule mockMaskRule() {
        MaskAlgorithm<String, String> maskAlgorithm = mock(MaskAlgorithm.class);
        when(maskAlgorithm.mask("VALUE")).thenReturn("MASK_VALUE");
        MaskRule result = mock(MaskRule.class);
        MaskTable maskTable = mock(MaskTable.class);
        when(maskTable.findAlgorithm("foo_column")).thenReturn(Optional.of(maskAlgorithm));
        when(result.findMaskTable("foo_table")).thenReturn(Optional.of(maskTable));
        return result;
    }
    
    private SelectStatementContext mockSelectStatementContext() {
        ColumnProjection columnProjection = mock(ColumnProjection.class, RETURNS_DEEP_STUBS);
        when(columnProjection.getOriginalTable().getValue()).thenReturn("foo_table");
        when(columnProjection.getName().getValue()).thenReturn("foo_column");
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getProjectionsContext().findColumnProjection(1)).thenReturn(Optional.of(columnProjection));
        when(result.findColumnBoundInfo(1)).thenReturn(Optional.of(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("foo_table"), new IdentifierValue("foo_column"), TableSourceType.PHYSICAL_TABLE)));
        return result;
    }
    
    private SelectStatementContext mockSelectStatementContextWithoutColumnProjection() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getProjectionsContext().findColumnProjection(anyInt())).thenReturn(Optional.empty());
        return result;
    }
    
    private MaskRule mockMaskRuleTableAbsent() {
        MaskRule result = mock(MaskRule.class);
        when(result.findMaskTable(anyString())).thenReturn(Optional.empty());
        return result;
    }
    
    private MaskRule mockMaskAlgorithmAbsent() {
        MaskRule result = mock(MaskRule.class);
        MaskTable maskTable = mock(MaskTable.class);
        when(maskTable.findAlgorithm("col")).thenReturn(Optional.empty());
        when(result.findMaskTable("tbl")).thenReturn(Optional.of(maskTable));
        return result;
    }
    
    private SelectStatementContext mockSelectStatementContextForMaskTableAbsent() {
        ColumnProjection columnProjection = mock(ColumnProjection.class, RETURNS_DEEP_STUBS);
        when(columnProjection.getOriginalTable().getValue()).thenReturn("foo_table");
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getProjectionsContext().findColumnProjection(1)).thenReturn(Optional.of(columnProjection));
        when(result.findColumnBoundInfo(1)).thenReturn(Optional.of(new ColumnSegmentBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")),
                new IdentifierValue("foo_table"), new IdentifierValue("foo_column"), TableSourceType.PHYSICAL_TABLE)));
        return result;
    }
    
    @Test
    void assertGetCalendarValue() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(new MaskMergedResult(mock(ShardingSphereDatabase.class), mock(ShardingSphereMetaData.class), mock(SelectStatementContext.class), mergedResult).getCalendarValue(1,
                Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetInputStream() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergedResult.getInputStream(1, "asc")).thenReturn(inputStream);
        assertThat(new MaskMergedResult(mock(ShardingSphereDatabase.class), mock(ShardingSphereMetaData.class), mock(SelectStatementContext.class), mergedResult).getInputStream(1,
                "asc"), is(inputStream));
    }
    
    @Test
    void assertGetCharacterStream() throws SQLException {
        Reader reader = mock(Reader.class);
        when(mergedResult.getCharacterStream(1)).thenReturn(reader);
        assertThat(
                new MaskMergedResult(mock(ShardingSphereDatabase.class), mock(ShardingSphereMetaData.class), mock(SelectStatementContext.class), mergedResult).getCharacterStream(1),
                is(reader));
    }
    
    @Test
    void assertWasNull() throws SQLException {
        assertFalse(new MaskMergedResult(mock(ShardingSphereDatabase.class), mock(ShardingSphereMetaData.class), mock(SelectStatementContext.class), mergedResult).wasNull());
    }
}
