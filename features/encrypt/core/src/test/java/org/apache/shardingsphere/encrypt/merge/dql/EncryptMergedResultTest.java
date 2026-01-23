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

package org.apache.shardingsphere.encrypt.merge.dql;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.exception.data.DecryptFailedException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.column.item.CipherColumnItem;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereArgumentVerifyMatchers.deepEq;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EncryptMergedResultTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SelectStatementContext selectStatementContext;
    
    @Mock
    private MergedResult mergedResult;
    
    @Test
    void assertNext() throws SQLException {
        assertFalse(new EncryptMergedResult(mock(), mock(), selectStatementContext, mergedResult).next());
    }
    
    @Test
    void assertGetValueWithoutColumnProjection() throws SQLException {
        when(selectStatementContext.findColumnBoundInfo(1)).thenReturn(Optional.empty());
        when(mergedResult.getValue(1, String.class)).thenReturn("foo_value");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        assertThat(new EncryptMergedResult(database, mock(), selectStatementContext, mergedResult).getValue(1, String.class), is("foo_value"));
    }
    
    @Test
    void assertGetValueWithoutEncryptTable() throws SQLException {
        ColumnSegmentBoundInfo columnSegmentBoundInfo = new ColumnSegmentBoundInfo(new IdentifierValue("foo_col"));
        when(selectStatementContext.findColumnBoundInfo(1)).thenReturn(Optional.of(columnSegmentBoundInfo));
        EncryptRule rule = mockRule(mock(EncryptAlgorithm.class));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(), mock(), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        when(mergedResult.getValue(1, String.class)).thenReturn("foo_value");
        assertThat(new EncryptMergedResult(database, mock(), selectStatementContext, mergedResult).getValue(1, String.class), is("foo_value"));
    }
    
    @Test
    void assertGetValueWithoutEncryptColumn() throws SQLException {
        ColumnSegmentBoundInfo columnSegmentBoundInfo = new ColumnSegmentBoundInfo(new IdentifierValue("bar_col"));
        when(selectStatementContext.findColumnBoundInfo(1)).thenReturn(Optional.of(columnSegmentBoundInfo));
        EncryptRule rule = mockRule(mock(EncryptAlgorithm.class));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(), mock(), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        when(mergedResult.getValue(1, String.class)).thenReturn("foo_value");
        assertThat(new EncryptMergedResult(database, mock(), selectStatementContext, mergedResult).getValue(1, String.class), is("foo_value"));
    }
    
    @Test
    void assertGetValueWithEncryptColumn() throws SQLException {
        ColumnSegmentBoundInfo columnSegmentBoundInfo = new ColumnSegmentBoundInfo(
                new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")), new IdentifierValue("foo_tbl"), new IdentifierValue("foo_col"),
                TableSourceType.PHYSICAL_TABLE);
        when(selectStatementContext.findColumnBoundInfo(1)).thenReturn(Optional.of(columnSegmentBoundInfo));
        when(selectStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.of("foo_schema"));
        EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        when(encryptAlgorithm.decrypt(eq("foo_value"), deepEq(new AlgorithmSQLContext("foo_db", "foo_schema", "foo_tbl", "foo_col")))).thenReturn("foo_decrypted_value");
        EncryptRule rule = mockRule(encryptAlgorithm);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock());
        when(mergedResult.getValue(1, Object.class)).thenReturn("foo_value");
        assertThat(new EncryptMergedResult(database, metaData, selectStatementContext, mergedResult).getValue(1, String.class), is("foo_decrypted_value"));
    }
    
    @Test
    void assertGetValueFailed() throws SQLException {
        ColumnSegmentBoundInfo columnSegmentBoundInfo = new ColumnSegmentBoundInfo(
                new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")), new IdentifierValue("foo_tbl"), new IdentifierValue("foo_col"),
                TableSourceType.PHYSICAL_TABLE);
        when(selectStatementContext.findColumnBoundInfo(1)).thenReturn(Optional.of(columnSegmentBoundInfo));
        when(selectStatementContext.getTablesContext().getSchemaName()).thenReturn(Optional.of("foo_schema"));
        EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);
        when(encryptAlgorithm.decrypt(eq("foo_value"), deepEq(new AlgorithmSQLContext("foo_db", "foo_schema", "foo_tbl", "foo_col")))).thenThrow(new RuntimeException("Test failed"));
        EncryptRule rule = mockRule(encryptAlgorithm);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock());
        when(mergedResult.getValue(1, Object.class)).thenReturn("foo_value");
        assertThrows(DecryptFailedException.class, () -> new EncryptMergedResult(database, metaData, selectStatementContext, mergedResult).getValue(1, String.class));
    }
    
    private EncryptRule mockRule(final EncryptAlgorithm encryptAlgorithm) {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(encryptTable.isEncryptColumn("foo_col")).thenReturn(true);
        EncryptColumn encryptColumn = new EncryptColumn("foo_col", new CipherColumnItem("foo_cipher_col", encryptAlgorithm));
        when(encryptTable.getEncryptColumn("foo_col")).thenReturn(encryptColumn);
        when(result.findEncryptTable("foo_tbl")).thenReturn(Optional.of(encryptTable));
        when(result.getEncryptTable("foo_tbl")).thenReturn(encryptTable);
        return result;
    }
    
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    void assertGetCalendarValue() throws SQLException {
        Calendar calendar = Calendar.getInstance();
        when(mergedResult.getCalendarValue(1, Date.class, calendar)).thenReturn(new Date(0L));
        assertThat(new EncryptMergedResult(mock(), mock(), selectStatementContext, mergedResult).getCalendarValue(1, Date.class, calendar), is(new Date(0L)));
    }
    
    @Test
    void assertGetInputStream() throws SQLException {
        InputStream inputStream = mock(InputStream.class);
        when(mergedResult.getInputStream(1, "asc")).thenReturn(inputStream);
        assertThat(new EncryptMergedResult(mock(), mock(), selectStatementContext, mergedResult).getInputStream(1, "asc"), is(inputStream));
    }
    
    @Test
    void assertGetCharacterStream() throws SQLException {
        Reader reader = mock(Reader.class);
        when(mergedResult.getCharacterStream(1)).thenReturn(reader);
        assertThat(new EncryptMergedResult(mock(), mock(), selectStatementContext, mergedResult).getCharacterStream(1), is(reader));
    }
    
    @Test
    void assertWasNull() throws SQLException {
        assertFalse(new EncryptMergedResult(mock(), mock(), selectStatementContext, mergedResult).wasNull());
    }
}
