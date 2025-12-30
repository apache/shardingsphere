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

package org.apache.shardingsphere.database.connector.mariadb.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.mysql.metadata.database.option.MySQLDataTypeOption;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MariaDBDatabaseMetaDataTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MariaDB");
    
    private final DialectDatabaseMetaData metaData = DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType);
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(metaData.getQuoteCharacter(), is(QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    void assertGetIdentifierPatternType() {
        assertThat(metaData.getIdentifierPatternType(), is(IdentifierPatternType.KEEP_ORIGIN));
    }
    
    @Test
    void assertGetDefaultNullsOrderType() {
        assertThat(metaData.getDefaultNullsOrderType(), is(NullsOrderType.LOW));
    }
    
    @Test
    void assertGetDataTypeOption() {
        assertThat(metaData.getDataTypeOption(), isA(MySQLDataTypeOption.class));
    }
    
    @Test
    void assertGetColumnOption() {
        assertFalse(metaData.getColumnOption().isColumnNameEqualsLabelInColumnProjection());
    }
    
    @Test
    void assertGetConnectionOption() {
        DialectConnectionOption actual = metaData.getConnectionOption();
        assertTrue(actual.isInstanceConnectionAvailable());
        assertTrue(actual.isSupportThreeTierStorageStructure());
    }
    
    @Test
    void assertGetTransactionOption() {
        DialectTransactionOption actual = metaData.getTransactionOption();
        assertFalse(actual.isSupportGlobalCSN());
        assertFalse(actual.isDDLNeedImplicitCommit());
        assertTrue(actual.isSupportAutoCommitInNestedTransaction());
        assertFalse(actual.isSupportDDLInXATransaction());
        assertTrue(actual.isSupportMetaDataRefreshInTransaction());
        assertThat(actual.getDefaultIsolationLevel(), is(Connection.TRANSACTION_REPEATABLE_READ));
        assertFalse(actual.isReturnRollbackStatementWhenCommitFailed());
        assertFalse(actual.isAllowCommitAndRollbackOnlyWhenTransactionFailed());
        assertThat(actual.getXaDriverClassNames().size(), is(1));
        assertTrue(actual.getXaDriverClassNames().contains("org.mariadb.jdbc.MariaDbDataSource"));
    }
    
    @Test
    void assertGetJoinOption() {
        DialectJoinOption actual = metaData.getJoinOption();
        assertTrue(actual.isUsingColumnsByProjectionOrder());
        assertTrue(actual.isRightColumnsByFirstOrder());
    }
    
    @Test
    void assertGetGeneratedKeyOption() {
        Optional<DialectGeneratedKeyOption> actual = metaData.getGeneratedKeyOption();
        assertTrue(actual.isPresent());
        assertThat(actual.map(DialectGeneratedKeyOption::getColumnName).orElse(""), is("GENERATED_KEY"));
    }
    
    @Test
    void assertGetProtocolVersionOption() {
        assertThat(metaData.getProtocolVersionOption().getDefaultVersion(), is("5.7.22"));
    }
}
