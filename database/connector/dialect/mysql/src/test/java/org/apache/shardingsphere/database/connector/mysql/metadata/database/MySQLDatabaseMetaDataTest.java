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

package org.apache.shardingsphere.database.connector.mysql.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.explain.DialectExplainOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.index.DialectIndexOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.mysql.metadata.database.option.MySQLDataTypeOption;
import org.apache.shardingsphere.database.connector.mysql.metadata.database.option.MySQLFunctionOption;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLDatabaseMetaDataTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData = DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType);
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(dialectDatabaseMetaData.getQuoteCharacter(), is(QuoteCharacter.BACK_QUOTE));
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
    void assertGetDataTypeOption() {
        assertThat(dialectDatabaseMetaData.getDataTypeOption(), isA(MySQLDataTypeOption.class));
    }
    
    @Test
    void assertGetColumnOption() {
        assertFalse(dialectDatabaseMetaData.getColumnOption().isColumnNameEqualsLabelInColumnProjection());
    }
    
    @Test
    void assertGetSchemaOption() {
        DialectSchemaOption actual = dialectDatabaseMetaData.getSchemaOption();
        assertThat(actual.getSchemaSemantics(), is(DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertFalse(actual.isCrossSchemaQuerySupported());
    }
    
    @Test
    void assertGetIndexOption() {
        DialectIndexOption actual = dialectDatabaseMetaData.getIndexOption();
        assertFalse(actual.isSchemaUniquenessLevel());
        assertThat(actual.getIndexNameMaxLength(), is(Integer.MAX_VALUE));
        assertTrue(actual.isIndexMetaDataSupported());
    }
    
    @Test
    void assertGetConnectionOption() {
        DialectConnectionOption actual = dialectDatabaseMetaData.getConnectionOption();
        assertTrue(actual.isInstanceConnectionAvailable());
        assertTrue(actual.isSupportThreeTierStorageStructure());
    }
    
    @Test
    void assertGetTransactionOption() {
        DialectTransactionOption actual = dialectDatabaseMetaData.getTransactionOption();
        assertFalse(actual.isSupportGlobalCSN());
        assertFalse(actual.isDDLNeedImplicitCommit());
        assertTrue(actual.isSupportAutoCommitInNestedTransaction());
        assertFalse(actual.isSupportDDLInXATransaction());
        assertTrue(actual.isSupportMetaDataRefreshInTransaction());
        assertThat(actual.getDefaultIsolationLevel(), is(Connection.TRANSACTION_REPEATABLE_READ));
        assertFalse(actual.isReturnRollbackStatementWhenCommitFailed());
        assertFalse(actual.isAllowCommitAndRollbackOnlyWhenTransactionFailed());
        assertThat(actual.getXaDriverClassNames().size(), is(2));
        assertTrue(actual.getXaDriverClassNames().contains("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"));
        assertTrue(actual.getXaDriverClassNames().contains("com.mysql.cj.jdbc.MysqlXADataSource"));
        assertTrue(actual.isSupportTransaction());
        assertTrue(actual.isSupportSavepoint());
    }
    
    @Test
    void assertGetJoinOption() {
        DialectJoinOption actual = dialectDatabaseMetaData.getJoinOption();
        assertTrue(actual.isUsingColumnsByProjectionOrder());
        assertTrue(actual.isRightColumnsByFirstOrder());
    }
    
    @Test
    void assertGetGeneratedKeyOption() {
        Optional<DialectGeneratedKeyOption> actual = dialectDatabaseMetaData.getGeneratedKeyOption();
        assertTrue(actual.isPresent());
        assertThat(actual.map(DialectGeneratedKeyOption::getColumnName).orElse(""), is("GENERATED_KEY"));
    }
    
    @Test
    void assertGetProtocolVersionOption() {
        assertThat(dialectDatabaseMetaData.getProtocolVersionOption().getDefaultVersion(), is("5.7.22"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getExplainOptionArguments")
    void assertGetExplainOption(final String name, final String databaseVersion, final boolean expected) {
        DialectExplainOption actual = dialectDatabaseMetaData.getExplainOption();
        assertThat(actual.isExplainAnalyzeSupported(databaseVersion), is(expected));
    }
    
    @Test
    void assertGetFunctionOption() {
        assertThat(dialectDatabaseMetaData.getFunctionOption(), isA(MySQLFunctionOption.class));
    }
    
    private static Stream<Arguments> getExplainOptionArguments() {
        return Stream.of(
                Arguments.of("null version", null, false),
                Arguments.of("major is less than target", "7.0.0", false),
                Arguments.of("major is greater than target", "9.0.0", true),
                Arguments.of("patch is less than target", "8.0.17", false),
                Arguments.of("minor is greater than target", "8.1.0", true),
                Arguments.of("missing patch is less than target", "8.0", false),
                Arguments.of("patch equals target with suffix", " 8.0.18-log ", true),
                Arguments.of("missing minor and patch default to zero", "8", false));
    }
}
