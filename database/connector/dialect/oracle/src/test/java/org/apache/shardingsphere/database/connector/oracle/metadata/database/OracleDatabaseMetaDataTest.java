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

package org.apache.shardingsphere.database.connector.oracle.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.altertable.DialectAlterTableOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.oracle.metadata.database.option.OracleDataTypeOption;
import org.apache.shardingsphere.database.connector.oracle.metadata.database.option.OracleSchemaOption;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleDatabaseMetaDataTest {
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData = DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, TypedSPILoader.getService(DatabaseType.class, "Oracle"));
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(dialectDatabaseMetaData.getQuoteCharacter(), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertGetIdentifierPatternType() {
        assertThat(dialectDatabaseMetaData.getIdentifierPatternType(), is(IdentifierPatternType.UPPER_CASE));
    }
    
    @Test
    void assertGetDefaultNullsOrderType() {
        assertThat(dialectDatabaseMetaData.getDefaultNullsOrderType(), is(NullsOrderType.HIGH));
    }
    
    @Test
    void assertGetDataTypeOption() {
        assertThat(dialectDatabaseMetaData.getDataTypeOption(), isA(OracleDataTypeOption.class));
    }
    
    @Test
    void assertGetSchemaOption() {
        assertThat(dialectDatabaseMetaData.getSchemaOption(), isA(OracleSchemaOption.class));
    }
    
    @Test
    void assertGetIndexOption() {
        assertTrue(dialectDatabaseMetaData.getIndexOption().isSchemaUniquenessLevel());
    }
    
    @Test
    void assertGetConnectionOption() {
        DialectConnectionOption actualConnectionOption = dialectDatabaseMetaData.getConnectionOption();
        assertTrue(actualConnectionOption.isInstanceConnectionAvailable());
        assertFalse(actualConnectionOption.isSupportThreeTierStorageStructure());
    }
    
    @Test
    void assertGetTransactionOption() {
        DialectTransactionOption actualTransactionOption = dialectDatabaseMetaData.getTransactionOption();
        assertFalse(actualTransactionOption.isSupportGlobalCSN());
        assertFalse(actualTransactionOption.isDDLNeedImplicitCommit());
        assertFalse(actualTransactionOption.isSupportAutoCommitInNestedTransaction());
        assertFalse(actualTransactionOption.isSupportDDLInXATransaction());
        assertTrue(actualTransactionOption.isSupportMetaDataRefreshInTransaction());
        assertThat(actualTransactionOption.getDefaultIsolationLevel(), is(Connection.TRANSACTION_READ_COMMITTED));
        assertFalse(actualTransactionOption.isReturnRollbackStatementWhenCommitFailed());
        assertFalse(actualTransactionOption.isAllowCommitAndRollbackOnlyWhenTransactionFailed());
        assertThat(actualTransactionOption.getXaDriverClassNames(), is(Collections.singleton("oracle.jdbc.xa.client.OracleXADataSource")));
    }
    
    @Test
    void assertGetPaginationOption() {
        DialectPaginationOption actualPaginationOption = dialectDatabaseMetaData.getPaginationOption();
        assertTrue(actualPaginationOption.isContainsRowNumber());
        assertThat(actualPaginationOption.getRowNumberColumnName(), is("ROWNUM"));
        assertFalse(actualPaginationOption.isContainsTop());
    }
    
    @Test
    void assertGetAlterTableOption() {
        Optional<DialectAlterTableOption> actualAlterTableOption = dialectDatabaseMetaData.getAlterTableOption();
        assertTrue(actualAlterTableOption.isPresent());
        assertTrue(actualAlterTableOption.get().isSupportMergeDropColumns());
        assertTrue(actualAlterTableOption.get().isContainsParenthesesOnMergeDropColumns());
    }
}
