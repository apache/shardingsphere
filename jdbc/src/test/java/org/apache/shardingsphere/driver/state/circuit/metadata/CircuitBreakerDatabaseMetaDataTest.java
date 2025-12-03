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

package org.apache.shardingsphere.driver.state.circuit.metadata;

import org.junit.jupiter.api.Test;

import java.sql.DatabaseMetaData;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

class CircuitBreakerDatabaseMetaDataTest {
    
    private final CircuitBreakerDatabaseMetaData metaData = new CircuitBreakerDatabaseMetaData();
    
    @Test
    void assertAllProceduresAreCallable() {
        assertFalse(metaData.allProceduresAreCallable());
    }
    
    @Test
    void assertAllTablesAreSelectable() {
        assertFalse(metaData.allTablesAreSelectable());
    }
    
    @Test
    void assertGetURL() {
        assertNull(metaData.getURL());
    }
    
    @Test
    void assertGetUserName() {
        assertNull(metaData.getUserName());
    }
    
    @Test
    void assertIsReadOnly() {
        assertFalse(metaData.isReadOnly());
    }
    
    @Test
    void assertNullsAreSortedHigh() {
        assertFalse(metaData.nullsAreSortedHigh());
    }
    
    @Test
    void assertNullsAreSortedLow() {
        assertFalse(metaData.nullsAreSortedLow());
    }
    
    @Test
    void assertNullsAreSortedAtStart() {
        assertFalse(metaData.nullsAreSortedAtStart());
    }
    
    @Test
    void assertNullsAreSortedAtEnd() {
        assertFalse(metaData.nullsAreSortedAtEnd());
    }
    
    @Test
    void assertGetDatabaseProductName() {
        assertThat(metaData.getDatabaseProductName(), is("H2"));
    }
    
    @Test
    void assertGetDatabaseProductVersion() {
        assertNull(metaData.getDatabaseProductVersion());
    }
    
    @Test
    void assertGetDriverName() {
        assertNull(metaData.getDriverName());
    }
    
    @Test
    void assertGetDriverVersion() {
        assertNull(metaData.getDriverVersion());
    }
    
    @Test
    void assertGetDriverMajorVersion() {
        assertThat(metaData.getDriverMajorVersion(), is(0));
    }
    
    @Test
    void assertGetDriverMinorVersion() {
        assertThat(metaData.getDriverMinorVersion(), is(0));
    }
    
    @Test
    void assertUsesLocalFiles() {
        assertFalse(metaData.usesLocalFiles());
    }
    
    @Test
    void assertUsesLocalFilePerTable() {
        assertFalse(metaData.usesLocalFilePerTable());
    }
    
    @Test
    void assertSupportsMixedCaseIdentifiers() {
        assertFalse(metaData.supportsMixedCaseIdentifiers());
    }
    
    @Test
    void assertStoresUpperCaseIdentifiers() {
        assertFalse(metaData.storesUpperCaseIdentifiers());
    }
    
    @Test
    void assertStoresLowerCaseIdentifiers() {
        assertFalse(metaData.storesLowerCaseIdentifiers());
    }
    
    @Test
    void assertStoresMixedCaseIdentifiers() {
        assertFalse(metaData.storesMixedCaseIdentifiers());
    }
    
    @Test
    void assertSupportsMixedCaseQuotedIdentifiers() {
        assertFalse(metaData.supportsMixedCaseQuotedIdentifiers());
    }
    
    @Test
    void assertStoresUpperCaseQuotedIdentifiers() {
        assertFalse(metaData.storesUpperCaseQuotedIdentifiers());
    }
    
    @Test
    void assertStoresLowerCaseQuotedIdentifiers() {
        assertFalse(metaData.storesLowerCaseQuotedIdentifiers());
    }
    
    @Test
    void assertStoresMixedCaseQuotedIdentifiers() {
        assertFalse(metaData.storesMixedCaseQuotedIdentifiers());
    }
    
    @Test
    void assertGetIdentifierQuoteString() {
        assertNull(metaData.getIdentifierQuoteString());
    }
    
    @Test
    void assertGetSQLKeywords() {
        assertNull(metaData.getSQLKeywords());
    }
    
    @Test
    void assertGetNumericFunctions() {
        assertNull(metaData.getNumericFunctions());
    }
    
    @Test
    void assertGetStringFunctions() {
        assertNull(metaData.getStringFunctions());
    }
    
    @Test
    void assertGetSystemFunctions() {
        assertNull(metaData.getSystemFunctions());
    }
    
    @Test
    void assertGetTimeDateFunctions() {
        assertNull(metaData.getTimeDateFunctions());
    }
    
    @Test
    void assertGetSearchStringEscape() {
        assertNull(metaData.getSearchStringEscape());
    }
    
    @Test
    void assertGetExtraNameCharacters() {
        assertNull(metaData.getExtraNameCharacters());
    }
    
    @Test
    void assertSupportsAlterTableWithAddColumn() {
        assertFalse(metaData.supportsAlterTableWithAddColumn());
    }
    
    @Test
    void assertSupportsAlterTableWithDropColumn() {
        assertFalse(metaData.supportsAlterTableWithDropColumn());
    }
    
    @Test
    void assertSupportsColumnAliasing() {
        assertFalse(metaData.supportsColumnAliasing());
    }
    
    @Test
    void assertNullPlusNonNullIsNull() {
        assertFalse(metaData.nullPlusNonNullIsNull());
    }
    
    @Test
    void assertSupportsConvert() {
        assertFalse(metaData.supportsConvert());
    }
    
    @Test
    void assertSupportsConvertWithParameter() {
        assertFalse(metaData.supportsConvert(0, 0));
    }
    
    @Test
    void assertSupportsTableCorrelationNames() {
        assertFalse(metaData.supportsTableCorrelationNames());
    }
    
    @Test
    void assertSupportsDifferentTableCorrelationNames() {
        assertFalse(metaData.supportsDifferentTableCorrelationNames());
    }
    
    @Test
    void assertSupportsExpressionsInOrderBy() {
        assertFalse(metaData.supportsExpressionsInOrderBy());
    }
    
    @Test
    void assertSupportsOrderByUnrelated() {
        assertFalse(metaData.supportsOrderByUnrelated());
    }
    
    @Test
    void assertSupportsGroupBy() {
        assertFalse(metaData.supportsGroupBy());
    }
    
    @Test
    void assertSupportsGroupByUnrelated() {
        assertFalse(metaData.supportsGroupByUnrelated());
    }
    
    @Test
    void assertSupportsGroupByBeyondSelect() {
        assertFalse(metaData.supportsGroupByBeyondSelect());
    }
    
    @Test
    void assertSupportsLikeEscapeClause() {
        assertFalse(metaData.supportsLikeEscapeClause());
    }
    
    @Test
    void assertSupportsMultipleResultSets() {
        assertFalse(metaData.supportsMultipleResultSets());
    }
    
    @Test
    void assertSupportsMultipleTransactions() {
        assertFalse(metaData.supportsMultipleTransactions());
    }
    
    @Test
    void assertSupportsNonNullableColumns() {
        assertFalse(metaData.supportsNonNullableColumns());
    }
    
    @Test
    void assertSupportsMinimumSQLGrammar() {
        assertFalse(metaData.supportsMinimumSQLGrammar());
    }
    
    @Test
    void assertSupportsCoreSQLGrammar() {
        assertFalse(metaData.supportsCoreSQLGrammar());
    }
    
    @Test
    void assertSupportsExtendedSQLGrammar() {
        assertFalse(metaData.supportsExtendedSQLGrammar());
    }
    
    @Test
    void assertSupportsANSI92EntryLevelSQL() {
        assertFalse(metaData.supportsANSI92EntryLevelSQL());
    }
    
    @Test
    void assertSupportsANSI92IntermediateSQL() {
        assertFalse(metaData.supportsANSI92IntermediateSQL());
    }
    
    @Test
    void assertSupportsANSI92FullSQL() {
        assertFalse(metaData.supportsANSI92FullSQL());
    }
    
    @Test
    void assertSupportsIntegrityEnhancementFacility() {
        assertFalse(metaData.supportsIntegrityEnhancementFacility());
    }
    
    @Test
    void assertSupportsOuterJoins() {
        assertFalse(metaData.supportsOuterJoins());
    }
    
    @Test
    void assertSupportsFullOuterJoins() {
        assertFalse(metaData.supportsFullOuterJoins());
    }
    
    @Test
    void assertSupportsLimitedOuterJoins() {
        assertFalse(metaData.supportsLimitedOuterJoins());
    }
    
    @Test
    void assertGetSchemaTerm() {
        assertNull(metaData.getSchemaTerm());
    }
    
    @Test
    void assertGetProcedureTerm() {
        assertNull(metaData.getProcedureTerm());
    }
    
    @Test
    void assertGetCatalogTerm() {
        assertNull(metaData.getCatalogTerm());
    }
    
    @Test
    void assertIsCatalogAtStart() {
        assertFalse(metaData.isCatalogAtStart());
    }
    
    @Test
    void assertGetCatalogSeparator() {
        assertNull(metaData.getCatalogSeparator());
    }
    
    @Test
    void assertSupportsSchemasInDataManipulation() {
        assertFalse(metaData.supportsSchemasInDataManipulation());
    }
    
    @Test
    void assertSupportsSchemasInProcedureCalls() {
        assertFalse(metaData.supportsSchemasInProcedureCalls());
    }
    
    @Test
    void assertSupportsSchemasInTableDefinitions() {
        assertFalse(metaData.supportsSchemasInTableDefinitions());
    }
    
    @Test
    void assertSupportsSchemasInIndexDefinitions() {
        assertFalse(metaData.supportsSchemasInIndexDefinitions());
    }
    
    @Test
    void assertSupportsSchemasInPrivilegeDefinitions() {
        assertFalse(metaData.supportsSchemasInPrivilegeDefinitions());
    }
    
    @Test
    void assertSupportsCatalogsInDataManipulation() {
        assertFalse(metaData.supportsCatalogsInDataManipulation());
    }
    
    @Test
    void assertSupportsCatalogsInProcedureCalls() {
        assertFalse(metaData.supportsCatalogsInProcedureCalls());
    }
    
    @Test
    void assertSupportsCatalogsInTableDefinitions() {
        assertFalse(metaData.supportsCatalogsInTableDefinitions());
    }
    
    @Test
    void assertSupportsCatalogsInIndexDefinitions() {
        assertFalse(metaData.supportsCatalogsInIndexDefinitions());
    }
    
    @Test
    void assertSupportsCatalogsInPrivilegeDefinitions() {
        assertFalse(metaData.supportsCatalogsInPrivilegeDefinitions());
    }
    
    @Test
    void assertSupportsPositionedDelete() {
        assertFalse(metaData.supportsPositionedDelete());
    }
    
    @Test
    void assertSupportsPositionedUpdate() {
        assertFalse(metaData.supportsPositionedUpdate());
    }
    
    @Test
    void assertSupportsSelectForUpdate() {
        assertFalse(metaData.supportsSelectForUpdate());
    }
    
    @Test
    void assertSupportsStoredProcedures() {
        assertFalse(metaData.supportsStoredProcedures());
    }
    
    @Test
    void assertSupportsSubqueriesInComparisons() {
        assertFalse(metaData.supportsSubqueriesInComparisons());
    }
    
    @Test
    void assertSupportsSubqueriesInExists() {
        assertFalse(metaData.supportsSubqueriesInExists());
    }
    
    @Test
    void assertSupportsSubqueriesInIns() {
        assertFalse(metaData.supportsSubqueriesInIns());
    }
    
    @Test
    void assertSupportsSubqueriesInQuantifieds() {
        assertFalse(metaData.supportsSubqueriesInQuantifieds());
    }
    
    @Test
    void assertSupportsCorrelatedSubqueries() {
        assertFalse(metaData.supportsCorrelatedSubqueries());
    }
    
    @Test
    void assertSupportsUnion() {
        assertFalse(metaData.supportsUnion());
    }
    
    @Test
    void assertSupportsUnionAll() {
        assertFalse(metaData.supportsUnionAll());
    }
    
    @Test
    void assertSupportsOpenCursorsAcrossCommit() {
        assertFalse(metaData.supportsOpenCursorsAcrossCommit());
    }
    
    @Test
    void assertSupportsOpenCursorsAcrossRollback() {
        assertFalse(metaData.supportsOpenCursorsAcrossRollback());
    }
    
    @Test
    void assertSupportsOpenStatementsAcrossCommit() {
        assertFalse(metaData.supportsOpenStatementsAcrossCommit());
    }
    
    @Test
    void assertSupportsOpenStatementsAcrossRollback() {
        assertFalse(metaData.supportsOpenStatementsAcrossRollback());
    }
    
    @Test
    void assertGetMaxBinaryLiteralLength() {
        assertThat(metaData.getMaxBinaryLiteralLength(), is(0));
    }
    
    @Test
    void assertGetMaxCharLiteralLength() {
        assertThat(metaData.getMaxCharLiteralLength(), is(0));
    }
    
    @Test
    void assertGetMaxColumnNameLength() {
        assertThat(metaData.getMaxColumnNameLength(), is(0));
    }
    
    @Test
    void assertGetMaxColumnsInGroupBy() {
        assertThat(metaData.getMaxColumnsInGroupBy(), is(0));
    }
    
    @Test
    void assertGetMaxColumnsInIndex() {
        assertThat(metaData.getMaxColumnsInIndex(), is(0));
    }
    
    @Test
    void assertGetMaxColumnsInOrderBy() {
        assertThat(metaData.getMaxColumnsInOrderBy(), is(0));
    }
    
    @Test
    void assertGetMaxColumnsInSelect() {
        assertThat(metaData.getMaxColumnsInSelect(), is(0));
    }
    
    @Test
    void assertGetMaxColumnsInTable() {
        assertThat(metaData.getMaxColumnsInTable(), is(0));
    }
    
    @Test
    void assertGetMaxConnections() {
        assertThat(metaData.getMaxConnections(), is(0));
    }
    
    @Test
    void assertGetMaxCursorNameLength() {
        assertThat(metaData.getMaxCursorNameLength(), is(0));
    }
    
    @Test
    void assertGetMaxIndexLength() {
        assertThat(metaData.getMaxIndexLength(), is(0));
    }
    
    @Test
    void assertGetMaxSchemaNameLength() {
        assertThat(metaData.getMaxSchemaNameLength(), is(0));
    }
    
    @Test
    void assertGetMaxProcedureNameLength() {
        assertThat(metaData.getMaxProcedureNameLength(), is(0));
    }
    
    @Test
    void assertGetMaxCatalogNameLength() {
        assertThat(metaData.getMaxCatalogNameLength(), is(0));
    }
    
    @Test
    void assertGetMaxRowSize() {
        assertThat(metaData.getMaxRowSize(), is(0));
    }
    
    @Test
    void assertDoesMaxRowSizeIncludeBlobs() {
        assertFalse(metaData.doesMaxRowSizeIncludeBlobs());
    }
    
    @Test
    void assertGetMaxStatementLength() {
        assertThat(metaData.getMaxStatementLength(), is(0));
    }
    
    @Test
    void assertGetMaxStatements() {
        assertThat(metaData.getMaxStatements(), is(0));
    }
    
    @Test
    void assertGetMaxTableNameLength() {
        assertThat(metaData.getMaxTableNameLength(), is(0));
    }
    
    @Test
    void assertGetMaxTablesInSelect() {
        assertThat(metaData.getMaxTablesInSelect(), is(0));
    }
    
    @Test
    void assertGetMaxUserNameLength() {
        assertThat(metaData.getMaxUserNameLength(), is(0));
    }
    
    @Test
    void assertGetDefaultTransactionIsolation() {
        assertThat(metaData.getDefaultTransactionIsolation(), is(0));
    }
    
    @Test
    void assertSupportsTransactions() {
        assertFalse(metaData.supportsTransactions());
    }
    
    @Test
    void assertSupportsTransactionIsolationLevel() {
        assertFalse(metaData.supportsTransactionIsolationLevel(0));
    }
    
    @Test
    void assertSupportsDataDefinitionAndDataManipulationTransactions() {
        assertFalse(metaData.supportsDataDefinitionAndDataManipulationTransactions());
    }
    
    @Test
    void assertSupportsDataManipulationTransactionsOnly() {
        assertFalse(metaData.supportsDataManipulationTransactionsOnly());
    }
    
    @Test
    void assertDataDefinitionCausesTransactionCommit() {
        assertFalse(metaData.dataDefinitionCausesTransactionCommit());
    }
    
    @Test
    void assertDataDefinitionIgnoredInTransactions() {
        assertFalse(metaData.dataDefinitionIgnoredInTransactions());
    }
    
    @Test
    void assertGetProcedures() {
        assertNull(metaData.getProcedures("", "", ""));
    }
    
    @Test
    void assertGetProcedureColumns() {
        assertNull(metaData.getProcedureColumns("", "", "", ""));
    }
    
    @Test
    void assertGetTables() {
        assertNull(metaData.getTables("", "", "", null));
    }
    
    @Test
    void assertGetSchemas() {
        assertNull(metaData.getSchemas());
    }
    
    @Test
    void assertGetSchemasWithParameter() {
        assertNull(metaData.getSchemas(null, null));
    }
    
    @Test
    void assertGetCatalogs() {
        assertNull(metaData.getCatalogs());
    }
    
    @Test
    void assertGetTableTypes() {
        assertNull(metaData.getTableTypes());
    }
    
    @Test
    void assertGetColumns() {
        assertNull(metaData.getColumns("", "", "", ""));
    }
    
    @Test
    void assertGetColumnPrivileges() {
        assertNull(metaData.getColumnPrivileges("", "", "", ""));
    }
    
    @Test
    void assertGetTablePrivileges() {
        assertNull(metaData.getTablePrivileges("", "", ""));
    }
    
    @Test
    void assertGetBestRowIdentifier() {
        assertNull(metaData.getBestRowIdentifier("", "", "", 0, false));
    }
    
    @Test
    void assertGetVersionColumns() {
        assertNull(metaData.getVersionColumns("", "", ""));
    }
    
    @Test
    void assertGetPrimaryKeys() {
        assertNull(metaData.getPrimaryKeys("", "", ""));
    }
    
    @Test
    void assertGetImportedKeys() {
        assertNull(metaData.getImportedKeys("", "", ""));
    }
    
    @Test
    void assertGetExportedKeys() {
        assertNull(metaData.getExportedKeys("", "", ""));
    }
    
    @Test
    void assertGetCrossReference() {
        assertNull(metaData.getCrossReference("", "", "", "", "", ""));
    }
    
    @Test
    void assertGetTypeInfo() {
        assertNull(metaData.getTypeInfo());
    }
    
    @Test
    void assertGetIndexInfo() {
        assertNull(metaData.getIndexInfo("", "", "", false, false));
    }
    
    @Test
    void assertSupportsResultSetType() {
        assertFalse(metaData.supportsResultSetType(0));
    }
    
    @Test
    void assertSupportsResultSetConcurrency() {
        assertFalse(metaData.supportsResultSetConcurrency(0, 0));
    }
    
    @Test
    void assertOwnUpdatesAreVisible() {
        assertFalse(metaData.ownUpdatesAreVisible(0));
    }
    
    @Test
    void assertOwnDeletesAreVisible() {
        assertFalse(metaData.ownDeletesAreVisible(0));
    }
    
    @Test
    void assertOwnInsertsAreVisible() {
        assertFalse(metaData.ownInsertsAreVisible(0));
    }
    
    @Test
    void assertOthersUpdatesAreVisible() {
        assertFalse(metaData.othersUpdatesAreVisible(0));
    }
    
    @Test
    void assertOthersDeletesAreVisible() {
        assertFalse(metaData.othersDeletesAreVisible(0));
    }
    
    @Test
    void assertOthersInsertsAreVisible() {
        assertFalse(metaData.othersInsertsAreVisible(0));
    }
    
    @Test
    void assertUpdatesAreDetected() {
        assertFalse(metaData.updatesAreDetected(0));
    }
    
    @Test
    void assertDeletesAreDetected() {
        assertFalse(metaData.deletesAreDetected(0));
    }
    
    @Test
    void assertInsertsAreDetected() {
        assertFalse(metaData.insertsAreDetected(0));
    }
    
    @Test
    void assertSupportsBatchUpdates() {
        assertFalse(metaData.supportsBatchUpdates());
    }
    
    @Test
    void assertGetUDTs() {
        assertNull(metaData.getUDTs("", "", "", null));
    }
    
    @Test
    void assertGetConnection() {
        assertNull(metaData.getConnection());
    }
    
    @Test
    void assertSupportsSavepoints() {
        assertFalse(metaData.supportsSavepoints());
    }
    
    @Test
    void assertSupportsNamedParameters() {
        assertFalse(metaData.supportsNamedParameters());
    }
    
    @Test
    void assertSupportsMultipleOpenResults() {
        assertFalse(metaData.supportsMultipleOpenResults());
    }
    
    @Test
    void assertSupportsGetGeneratedKeys() {
        assertFalse(metaData.supportsGetGeneratedKeys());
    }
    
    @Test
    void assertGetSuperTypes() {
        assertNull(metaData.getSuperTypes("", "", ""));
    }
    
    @Test
    void assertGetSuperTables() {
        assertNull(metaData.getSuperTables("", "", ""));
    }
    
    @Test
    void assertGetAttributes() {
        assertNull(metaData.getAttributes("", "", "", ""));
    }
    
    @Test
    void assertSupportsResultSetHoldability() {
        assertFalse(metaData.supportsResultSetHoldability(0));
    }
    
    @Test
    void assertGetResultSetHoldability() {
        assertThat(metaData.getResultSetHoldability(), is(0));
    }
    
    @Test
    void assertGetDatabaseMajorVersion() {
        assertThat(metaData.getDatabaseMajorVersion(), is(0));
    }
    
    @Test
    void assertGetDatabaseMinorVersion() {
        assertThat(metaData.getDatabaseMinorVersion(), is(0));
    }
    
    @Test
    void assertGetJDBCMajorVersion() {
        assertThat(metaData.getJDBCMajorVersion(), is(0));
    }
    
    @Test
    void assertGetJDBCMinorVersion() {
        assertThat(metaData.getJDBCMinorVersion(), is(0));
    }
    
    @Test
    void assertGetSQLStateType() {
        assertThat(metaData.getSQLStateType(), is(DatabaseMetaData.sqlStateSQL));
    }
    
    @Test
    void assertLocatorsUpdateCopy() {
        assertFalse(metaData.locatorsUpdateCopy());
    }
    
    @Test
    void assertSupportsStatementPooling() {
        assertFalse(metaData.supportsStatementPooling());
    }
    
    @Test
    void assertGetRowIdLifetime() {
        assertNull(metaData.getRowIdLifetime());
    }
    
    @Test
    void assertSupportsStoredFunctionsUsingCallSyntax() {
        assertFalse(metaData.supportsStoredFunctionsUsingCallSyntax());
    }
    
    @Test
    void assertAutoCommitFailureClosesAllResultSets() {
        assertFalse(metaData.autoCommitFailureClosesAllResultSets());
    }
    
    @Test
    void assertGetClientInfoProperties() {
        assertNull(metaData.getClientInfoProperties());
    }
    
    @Test
    void assertGetFunctions() {
        assertNull(metaData.getFunctions("", "", ""));
    }
    
    @Test
    void assertGetFunctionColumns() {
        assertNull(metaData.getFunctionColumns("", "", "", ""));
    }
    
    @Test
    void assertGetPseudoColumns() {
        assertNull(metaData.getPseudoColumns("", "", "", ""));
    }
    
    @Test
    void assertGeneratedKeyAlwaysReturned() {
        assertFalse(metaData.generatedKeyAlwaysReturned());
    }
    
    @Test
    void assertUnwrap() {
        assertNull(metaData.unwrap(null));
    }
    
    @Test
    void assertIsWrapperFor() {
        assertFalse(metaData.isWrapperFor(null));
    }
}
