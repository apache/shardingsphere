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

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DefaultDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

class DialectDatabaseMetaDataTest {
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, CALLS_REAL_METHODS);
    
    @Test
    void assertIsCaseSensitive() {
        assertFalse(dialectDatabaseMetaData.isCaseSensitive());
    }
    
    @Test
    void assertGetDataTypeOption() {
        assertThat(dialectDatabaseMetaData.getDataTypeOption(), isA(DefaultDataTypeOption.class));
    }
    
    @Test
    void assertGetDriverQuerySystemCatalogOption() {
        assertThat(dialectDatabaseMetaData.getDriverQuerySystemCatalogOption(), is(Optional.empty()));
    }
    
    @Test
    void assertGetSchemaOption() {
        DialectSchemaOption actual = dialectDatabaseMetaData.getSchemaOption();
        assertFalse(actual.isSchemaAvailable());
        assertThat(actual.getDefaultSchema(), is(Optional.empty()));
    }
    
    @Test
    void assertGetColumnOption() {
        assertTrue(dialectDatabaseMetaData.getColumnOption().isColumnNameEqualsLabelInColumnProjection());
    }
    
    @Test
    void assertGetIndexOption() {
        assertFalse(dialectDatabaseMetaData.getIndexOption().isSchemaUniquenessLevel());
    }
    
    @Test
    void assertGetConnectionOption() {
        DialectConnectionOption actual = dialectDatabaseMetaData.getConnectionOption();
        assertFalse(actual.isInstanceConnectionAvailable());
        assertFalse(actual.isSupportThreeTierStorageStructure());
    }
    
    @Test
    void assertGetTransactionOption() {
        DialectTransactionOption actual = dialectDatabaseMetaData.getTransactionOption();
        assertFalse(actual.isSupportGlobalCSN());
        assertFalse(actual.isDDLNeedImplicitCommit());
        assertFalse(actual.isSupportAutoCommitInNestedTransaction());
        assertFalse(actual.isSupportDDLInXATransaction());
        assertTrue(actual.isSupportMetaDataRefreshInTransaction());
        assertThat(actual.getDefaultIsolationLevel(), is(Connection.TRANSACTION_READ_COMMITTED));
        assertFalse(actual.isReturnRollbackStatementWhenCommitFailed());
        assertFalse(actual.isAllowCommitAndRollbackOnlyWhenTransactionFailed());
        assertTrue(actual.getXaDriverClassNames().isEmpty());
    }
    
    @Test
    void assertGetJoinOption() {
        DialectJoinOption actual = dialectDatabaseMetaData.getJoinOption();
        assertFalse(actual.isUsingColumnsByProjectionOrder());
        assertFalse(actual.isRightColumnsByFirstOrder());
    }
    
    @Test
    void assertGetPaginationOption() {
        DialectPaginationOption actual = dialectDatabaseMetaData.getPaginationOption();
        assertFalse(actual.isContainsRowNumber());
        assertThat(actual.getRowNumberColumnName(), is(""));
        assertFalse(actual.isContainsTop());
    }
    
    @Test
    void assertGetGeneratedKeyOption() {
        assertThat(dialectDatabaseMetaData.getGeneratedKeyOption(), is(Optional.empty()));
    }
    
    @Test
    void assertGetAlterTableOption() {
        assertThat(dialectDatabaseMetaData.getAlterTableOption(), is(Optional.empty()));
    }
    
    @Test
    void assertGetSQLBatchOption() {
        assertTrue(dialectDatabaseMetaData.getSQLBatchOption().isSupportSQLBatch());
    }
    
    @Test
    void assertGetProtocolVersionOption() {
        assertThat(dialectDatabaseMetaData.getProtocolVersionOption().getDefaultVersion(), is(""));
    }
}
