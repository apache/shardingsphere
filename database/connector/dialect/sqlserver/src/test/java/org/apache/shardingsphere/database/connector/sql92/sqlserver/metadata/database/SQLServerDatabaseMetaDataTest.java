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

package org.apache.shardingsphere.database.connector.sql92.sqlserver.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.altertable.DialectAlterTableOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLServerDatabaseMetaDataTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData = DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType);
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(dialectDatabaseMetaData.getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
    }
    
    @Test
    void assertGetIdentifierPatternType() {
        assertThat(dialectDatabaseMetaData.getIdentifierPatternType(), is(IdentifierPatternType.KEEP_ORIGIN));
    }
    
    @Test
    void assertGetDefaultNullsOrderType() {
        assertThat(dialectDatabaseMetaData.getDefaultNullsOrderType(), is(NullsOrderType.LOW));
    }
    
    @Test
    void assertGetSchemaOption() {
        DialectSchemaOption actual = dialectDatabaseMetaData.getSchemaOption();
        assertFalse(actual.isSchemaAvailable());
        assertThat(actual.getDefaultSchema(), is(Optional.of("dbo")));
        assertThat(actual.getDefaultSystemSchema(), is(Optional.empty()));
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
        assertThat(actual.getXaDriverClassNames().size(), is(1));
        assertTrue(actual.getXaDriverClassNames().contains("com.microsoft.sqlserver.jdbc.SQLServerXADataSource"));
    }
    
    @Test
    void assertGetPaginationOption() {
        DialectPaginationOption actual = dialectDatabaseMetaData.getPaginationOption();
        assertTrue(actual.isContainsRowNumber());
        assertThat(actual.getRowNumberColumnName(), is("ROW_NUMBER"));
        assertTrue(actual.isContainsTop());
    }
    
    @Test
    void assertGetAlterTableOption() {
        Optional<DialectAlterTableOption> actual = dialectDatabaseMetaData.getAlterTableOption();
        assertTrue(actual.isPresent());
        assertTrue(actual.map(DialectAlterTableOption::isSupportMergeDropColumns).orElse(false));
        assertFalse(actual.map(DialectAlterTableOption::isContainsParenthesesOnMergeDropColumns).orElse(true));
    }
}
