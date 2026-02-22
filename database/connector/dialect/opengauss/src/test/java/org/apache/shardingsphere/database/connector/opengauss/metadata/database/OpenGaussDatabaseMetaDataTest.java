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

package org.apache.shardingsphere.database.connector.opengauss.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.opengauss.metadata.database.option.OpenGaussDataTypeOption;
import org.apache.shardingsphere.database.connector.opengauss.metadata.database.option.OpenGaussSchemaOption;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussDatabaseMetaDataTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData = DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType);
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(dialectDatabaseMetaData.getQuoteCharacter(), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertGetIdentifierPatternType() {
        assertThat(dialectDatabaseMetaData.getIdentifierPatternType(), is(IdentifierPatternType.LOWER_CASE));
    }
    
    @Test
    void assertGetDefaultNullsOrderType() {
        assertThat(dialectDatabaseMetaData.getDefaultNullsOrderType(), is(NullsOrderType.HIGH));
    }
    
    @Test
    void assertGetDataTypeOption() {
        assertThat(dialectDatabaseMetaData.getDataTypeOption(), isA(OpenGaussDataTypeOption.class));
    }
    
    @Test
    void assertGetDriverQuerySystemCatalogOption() {
        Optional<DialectDriverQuerySystemCatalogOption> actual = dialectDatabaseMetaData.getDriverQuerySystemCatalogOption();
        assertTrue(actual.isPresent());
        assertTrue(actual.map(each -> each.isSystemCatalogQueryExpressions("version()")).orElse(false));
        assertTrue(actual.map(each -> each.isSystemCatalogQueryExpressions("VERSION()")).orElse(false));
        assertTrue(actual.map(each -> each.isSystemTable("PG_DATABASE")).orElse(false));
        assertTrue(actual.map(each -> each.isDatabaseDataTable("PG_DATABASE")).orElse(false));
        assertTrue(actual.map(each -> each.isTableDataTable("PG_TABLES")).orElse(false));
        assertTrue(actual.map(each -> each.isRoleDataTable("PG_ROLES")).orElse(false));
        assertThat(actual.map(DialectDriverQuerySystemCatalogOption::getDatCompatibility).orElse(""), is("PG"));
    }
    
    @Test
    void assertGetSchemaOption() {
        DialectSchemaOption actual = dialectDatabaseMetaData.getSchemaOption();
        assertThat(actual, isA(OpenGaussSchemaOption.class));
        assertTrue(actual.isSchemaAvailable());
        assertThat(actual.getDefaultSchema(), is(Optional.of("public")));
        assertThat(actual.getDefaultSystemSchema(), is(Optional.of("pg_catalog")));
    }
    
    @Test
    void assertGetIndexOption() {
        assertTrue(dialectDatabaseMetaData.getIndexOption().isSchemaUniquenessLevel());
    }
    
    @Test
    void assertGetTransactionOption() {
        DialectTransactionOption actual = dialectDatabaseMetaData.getTransactionOption();
        assertTrue(actual.isSupportGlobalCSN());
        assertFalse(actual.isDDLNeedImplicitCommit());
        assertFalse(actual.isSupportAutoCommitInNestedTransaction());
        assertTrue(actual.isSupportDDLInXATransaction());
        assertFalse(actual.isSupportMetaDataRefreshInTransaction());
        assertThat(actual.getDefaultIsolationLevel(), is(Connection.TRANSACTION_READ_COMMITTED));
        assertTrue(actual.isReturnRollbackStatementWhenCommitFailed());
        assertTrue(actual.isAllowCommitAndRollbackOnlyWhenTransactionFailed());
        assertThat(actual.getXaDriverClassNames().size(), is(1));
        assertTrue(actual.getXaDriverClassNames().contains("org.opengauss.xa.PGXADataSource"));
    }
    
    @Test
    void assertGetProtocolVersionOption() {
        assertThat(dialectDatabaseMetaData.getProtocolVersionOption().getDefaultVersion(), is("9.2.4"));
    }
    
    @Test
    void assertIsCaseSensitive() {
        assertTrue(dialectDatabaseMetaData.isCaseSensitive());
    }
}
