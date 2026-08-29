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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.altertable.DialectAlterTableOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.column.DialectColumnOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.function.DialectFunctionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.index.DialectIndexOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sql.DialectSQLOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sqlbatch.DialectSQLBatchOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.version.DialectProtocolVersionOption;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AbstractDelegatingDialectDatabaseMetaDataTest {
    
    private final DialectDatabaseMetaData delegate = mock(DialectDatabaseMetaData.class);
    
    private final DialectDatabaseMetaData metaData = new FixtureDelegatingDialectDatabaseMetaData(delegate);
    
    @Test
    void assertDelegate() {
        when(delegate.getQuoteCharacter()).thenReturn(QuoteCharacter.BACK_QUOTE);
        assertThat(metaData.getQuoteCharacter(), is(delegate.getQuoteCharacter()));
        when(delegate.getIdentifierPatternType()).thenReturn(IdentifierPatternType.KEEP_ORIGIN);
        assertThat(metaData.getIdentifierPatternType(), is(delegate.getIdentifierPatternType()));
        when(delegate.getDefaultNullsOrderType()).thenReturn(NullsOrderType.HIGH);
        assertThat(metaData.getDefaultNullsOrderType(), is(delegate.getDefaultNullsOrderType()));
        when(delegate.getDataTypeOption()).thenReturn(mock(DialectDataTypeOption.class));
        assertThat(metaData.getDataTypeOption(), is(delegate.getDataTypeOption()));
        when(delegate.getDriverQuerySystemCatalogOption()).thenReturn(Optional.of(mock(DialectDriverQuerySystemCatalogOption.class)));
        assertThat(metaData.getDriverQuerySystemCatalogOption(), is(delegate.getDriverQuerySystemCatalogOption()));
        when(delegate.getSchemaOption()).thenReturn(mock(DialectSchemaOption.class));
        assertThat(metaData.getSchemaOption(), is(delegate.getSchemaOption()));
        when(delegate.getColumnOption()).thenReturn(mock(DialectColumnOption.class));
        assertThat(metaData.getColumnOption(), is(delegate.getColumnOption()));
        when(delegate.getIndexOption()).thenReturn(mock(DialectIndexOption.class));
        assertThat(metaData.getIndexOption(), is(delegate.getIndexOption()));
        when(delegate.getConnectionOption()).thenReturn(mock(DialectConnectionOption.class));
        assertThat(metaData.getConnectionOption(), is(delegate.getConnectionOption()));
        when(delegate.getTransactionOption()).thenReturn(mock(DialectTransactionOption.class));
        assertThat(metaData.getTransactionOption(), is(delegate.getTransactionOption()));
        when(delegate.getJoinOption()).thenReturn(mock(DialectJoinOption.class));
        assertThat(metaData.getJoinOption(), is(delegate.getJoinOption()));
        when(delegate.getPaginationOption()).thenReturn(mock(DialectPaginationOption.class));
        assertThat(metaData.getPaginationOption(), is(delegate.getPaginationOption()));
        when(delegate.getGeneratedKeyOption()).thenReturn(Optional.of(mock(DialectGeneratedKeyOption.class)));
        assertThat(metaData.getGeneratedKeyOption(), is(delegate.getGeneratedKeyOption()));
        when(delegate.getAlterTableOption()).thenReturn(Optional.of(mock(DialectAlterTableOption.class)));
        assertThat(metaData.getAlterTableOption(), is(delegate.getAlterTableOption()));
        when(delegate.getSQLBatchOption()).thenReturn(mock(DialectSQLBatchOption.class));
        assertThat(metaData.getSQLBatchOption(), is(delegate.getSQLBatchOption()));
        when(delegate.getSQLOption()).thenReturn(mock(DialectSQLOption.class));
        assertThat(metaData.getSQLOption(), is(delegate.getSQLOption()));
        when(delegate.getProtocolVersionOption()).thenReturn(mock(DialectProtocolVersionOption.class));
        assertThat(metaData.getProtocolVersionOption(), is(delegate.getProtocolVersionOption()));
        when(delegate.getFunctionOption()).thenReturn(mock(DialectFunctionOption.class));
        assertThat(metaData.getFunctionOption(), is(delegate.getFunctionOption()));
        when(delegate.isTableVariableIdentifier("@MyTableVar", QuoteCharacter.NONE)).thenReturn(true);
        assertTrue(metaData.isTableVariableIdentifier("@MyTableVar", QuoteCharacter.NONE));
    }
    
    @Test
    void assertRejectNullDelegate() {
        assertThrows(NullPointerException.class, () -> new FixtureDelegatingDialectDatabaseMetaData(null));
    }
    
    @Test
    void assertBranchIdentity() {
        assertThat(metaData.getDatabaseType(), is("BRANCH"));
        assertThat(metaData.getType().getType(), is("BRANCH"));
        verifyNoInteractions(delegate);
    }
    
    private static final class FixtureDelegatingDialectDatabaseMetaData extends AbstractDelegatingDialectDatabaseMetaData {
        
        private FixtureDelegatingDialectDatabaseMetaData(final DialectDatabaseMetaData delegate) {
            super(delegate);
        }
        
        @Override
        public String getDatabaseType() {
            return "BRANCH";
        }
    }
}
