/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.internal.jdbc.metadata;

import org.junit.Test;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public final class CircuitBreakerDatabaseMetaDataTest {
    
    private CircuitBreakerDatabaseMetaData metaData = new CircuitBreakerDatabaseMetaData();
    
    @Test
    public void assertAllProceduresAreCallable() throws Exception {
        assertFalse(metaData.allProceduresAreCallable());
    }
    
    @Test
    public void assertAllTablesAreSelectable() throws Exception {
        assertFalse(metaData.allTablesAreSelectable());
    }
    
    @Test
    public void assertGetURL() throws Exception {
        assertNull(metaData.getURL());
    }
    
    @Test
    public void assertGetUserName() throws Exception {
        assertNull(metaData.getUserName());
    }
    
    @Test
    public void assertIsReadOnly() throws Exception {
        assertFalse(metaData.isReadOnly());
    }
    
    @Test
    public void assertNullsAreSortedHigh() throws Exception {
        assertFalse(metaData.isReadOnly());
    }
    
    @Test
    public void assertNullsAreSortedLow() throws SQLException {
        assertFalse(metaData.nullsAreSortedLow());
    }
    
    @Test
    public void assertNullsAreSortedAtStart() throws SQLException {
        assertFalse(metaData.nullsAreSortedAtStart());
    }
    
    @Test
    public void assertNullsAreSortedAtEnd() throws SQLException {
        assertFalse(metaData.nullsAreSortedAtEnd());
    }
    
    @Test
    public void assertGetDatabaseProductName() throws SQLException {
        assertThat(metaData.getDatabaseProductName(), is("H2"));
    }
    
    @Test
    public void assertGetDatabaseProductVersion() throws SQLException {
        assertNull(metaData.getDatabaseProductVersion());
    }
    
    @Test
    public void assertGetDriverName() throws SQLException {
        assertNull(metaData.getDriverName());
    }
    
    @Test
    public void assertGetDriverVersion() throws SQLException {
        assertNull(metaData.getDriverVersion());
    }
    
    @Test
    public void assertGetDriverMajorVersion() {
        assertThat(metaData.getDriverMajorVersion(), is(0));
    }
    
    @Test
    public void assertGetDriverMinorVersion() {
        assertThat(metaData.getDriverMinorVersion(), is(0));
    }
    
    @Test
    public void assertUsesLocalFiles() throws SQLException {
        assertFalse(metaData.usesLocalFiles());
    }
    
    @Test
    public void assertUsesLocalFilePerTable() throws SQLException {
        assertFalse(metaData.usesLocalFilePerTable());
    }
    
    @Test
    public void assertSupportsMixedCaseIdentifiers() throws SQLException {
        assertFalse(metaData.supportsMixedCaseIdentifiers());
    }
    
    @Test
    public void assertStoresUpperCaseIdentifiers() throws SQLException {
        assertFalse(metaData.storesUpperCaseIdentifiers());
    }
    
    @Test
    public void assertStoresLowerCaseIdentifiers() throws SQLException {
        assertFalse(metaData.storesUpperCaseIdentifiers());
    }
    
    @Test
    public void assertStoresMixedCaseIdentifiers() throws SQLException {
        assertFalse(metaData.storesMixedCaseIdentifiers());
    }
    
    @Test
    public void assertSupportsMixedCaseQuotedIdentifiers() throws SQLException {
        assertFalse(metaData.supportsMixedCaseQuotedIdentifiers());
    }
    
    @Test
    public void assertStoresUpperCaseQuotedIdentifiers() throws SQLException {
        assertFalse(metaData.supportsMixedCaseQuotedIdentifiers());
    }
    
    @Test
    public void assertStoresLowerCaseQuotedIdentifiers() throws SQLException {
        assertFalse(metaData.storesLowerCaseIdentifiers());
    }
    
    @Test
    public void assertStoresMixedCaseQuotedIdentifiers() throws SQLException {
        assertFalse(metaData.storesMixedCaseQuotedIdentifiers());
    }
    
    @Test
    public void assertGetIdentifierQuoteString() throws SQLException {
        assertNull(metaData.getIdentifierQuoteString());
    }
    
    @Test
    public void assertGetSQLKeywords() throws SQLException {
        assertNull(metaData.getSQLKeywords());
    }
    
    @Test
    public void assertGetNumericFunctions() throws SQLException {
        assertNull(metaData.getNumericFunctions());
    }
    
    @Test
    public void assertGetStringFunctions() throws SQLException {
        assertNull(metaData.getStringFunctions());
    }
    
    @Test
    public void assertGetSystemFunctions() throws SQLException {
        assertNull(metaData.getSystemFunctions());
    }
    
    @Test
    public void assertGetTimeDateFunctions() throws SQLException {
        assertNull(metaData.getTimeDateFunctions());
    }
    
    @Test
    public void assertGetSearchStringEscape() throws SQLException {
        assertNull(metaData.getSearchStringEscape());
    }
    
    @Test
    public void assertGetExtraNameCharacters() throws SQLException {
        assertNull(metaData.getExtraNameCharacters());
    }
    
    @Test
    public void assertSupportsAlterTableWithAddColumn() throws SQLException {
        assertFalse(metaData.supportsAlterTableWithAddColumn());
    }
    
    @Test
    public void assertSupportsAlterTableWithDropColumn() throws SQLException {
        assertFalse(metaData.supportsAlterTableWithDropColumn());
    }
    
    @Test
    public void assertSupportsColumnAliasing() throws SQLException {
        assertFalse(metaData.supportsColumnAliasing());
    }
    
    @Test
    public void assertNullPlusNonNullIsNull() throws SQLException {
        assertFalse(metaData.nullPlusNonNullIsNull());
    }
    
    
    @Test
    public void assertSupportsConvert() throws SQLException {
        assertFalse(metaData.supportsConvert());
    }
    
    @Test
    public void assertSupportsConvertWithParameter() throws SQLException {
        assertFalse(metaData.supportsConvert(0, 0));
    }
    
    @Test
    public void assertSupportsTableCorrelationNames() throws SQLException {
        assertFalse(metaData.supportsTableCorrelationNames());
    }
    
    @Test
    public void assertSupportsDifferentTableCorrelationNames() throws SQLException {
        assertFalse(metaData.supportsDifferentTableCorrelationNames());
    }
    
    @Test
    public void assertSupportsExpressionsInOrderBy() throws SQLException {
        assertFalse(metaData.supportsExpressionsInOrderBy());
    }
    
    @Test
    public void assertSupportsOrderByUnrelated() throws SQLException {
        assertFalse(metaData.supportsOrderByUnrelated());
    }
    
    @Test
    public void assertSupportsGroupBy() throws SQLException {
        assertFalse(metaData.supportsGroupBy());
    }
    
    @Test
    public void assertSupportsGroupByUnrelated() throws SQLException {
        assertFalse(metaData.supportsGroupByUnrelated());
    }
    
    @Test
    public void assertSupportsGroupByBeyondSelect() throws SQLException {
        assertFalse(metaData.supportsGroupByBeyondSelect());
    }
    
    @Test
    public void assertSupportsLikeEscapeClause() throws SQLException {
        assertFalse(metaData.supportsLikeEscapeClause());
    }
    
    @Test
    public void assertSupportsMultipleResultSets() throws SQLException {
        assertFalse(metaData.supportsMultipleResultSets());
    }
    
    @Test
    public void assertSupportsMultipleTransactions() throws SQLException {
        assertFalse(metaData.supportsMultipleTransactions());
    }
    
    @Test
    public void assertSupportsNonNullableColumns() throws SQLException {
        assertFalse(metaData.supportsNonNullableColumns());
    }
    
    @Test
    public void assertSupportsMinimumSQLGrammar() throws SQLException {
        assertFalse(metaData.supportsMinimumSQLGrammar());
    }
    
    @Test
    public void assertSupportsCoreSQLGrammar() throws SQLException {
        assertFalse(metaData.supportsCoreSQLGrammar());
    }
    
    @Test
    public void assertSupportsExtendedSQLGrammar() throws SQLException {
        assertFalse(metaData.supportsExtendedSQLGrammar());
    }
    
    @Test
    public void assertSupportsANSI92EntryLevelSQL() throws SQLException {
        assertFalse(metaData.supportsANSI92EntryLevelSQL());
    }
    
    @Test
    public void assertSupportsANSI92IntermediateSQL() throws SQLException {
        assertFalse(metaData.supportsANSI92IntermediateSQL());
    }
    
    @Test
    public void assertSupportsANSI92FullSQL() throws SQLException {
        assertFalse(metaData.supportsANSI92FullSQL());
    }
    
    @Test
    public void assertSupportsIntegrityEnhancementFacility() throws SQLException {
        assertFalse(metaData.supportsIntegrityEnhancementFacility());
    }
    
    @Test
    public void assertSupportsOuterJoins() throws SQLException {
        assertFalse(metaData.supportsOuterJoins());
    }
    
    @Test
    public void assertSupportsFullOuterJoins() throws SQLException {
        assertFalse(metaData.supportsFullOuterJoins());
    }
    
    @Test
    public void assertSupportsLimitedOuterJoins() throws SQLException {
        assertFalse(metaData.supportsLimitedOuterJoins());
    }
    
    @Test
    public void assertGetSchemaTerm() throws SQLException {
        assertNull(metaData.getSchemaTerm());
    }
    
    @Test
    public void assertGetProcedureTerm() throws SQLException {
        assertNull(metaData.getProcedureTerm());
    }
    
    @Test
    public void assertGetCatalogTerm() throws SQLException {
        assertNull(metaData.getCatalogTerm());
    }
    
    @Test
    public void assertIsCatalogAtStart() throws SQLException {
        assertFalse(metaData.isCatalogAtStart());
    }
    
    @Test
    public void assertGetCatalogSeparator() throws SQLException {
        assertNull(metaData.getCatalogSeparator());
    }
    
    @Test
    public void assertSupportsSchemasInDataManipulation() throws SQLException {
        assertFalse(metaData.supportsSchemasInDataManipulation());
    }
    
    @Test
    public void assertSupportsSchemasInProcedureCalls() throws SQLException {
        assertFalse(metaData.supportsSchemasInProcedureCalls());
    }
    
    @Test
    public void assertSupportsSchemasInTableDefinitions() throws SQLException {
        assertFalse(metaData.supportsSchemasInTableDefinitions());
    }
    
    @Test
    public void assertSupportsSchemasInIndexDefinitions() throws SQLException {
        assertFalse(metaData.supportsSchemasInIndexDefinitions());
    }
    
    @Test
    public void assertSupportsSchemasInPrivilegeDefinitions() throws SQLException {
        assertFalse(metaData.supportsSchemasInPrivilegeDefinitions());
    }
    
    @Test
    public void assertSupportsCatalogsInDataManipulation() throws SQLException {
        assertFalse(metaData.supportsCatalogsInDataManipulation());
    }
    
    @Test
    public void assertSupportsCatalogsInProcedureCalls() throws SQLException {
        assertFalse(metaData.supportsCatalogsInProcedureCalls());
    }
    
    @Test
    public void assertSupportsCatalogsInTableDefinitions() throws SQLException {
        assertFalse(metaData.supportsCatalogsInTableDefinitions());
    }
    
    @Test
    public void assertSupportsCatalogsInIndexDefinitions() throws SQLException {
        assertFalse(metaData.supportsCatalogsInIndexDefinitions());
    }
    
    @Test
    public void assertSupportsCatalogsInPrivilegeDefinitions() throws SQLException {
        assertFalse(metaData.supportsCatalogsInPrivilegeDefinitions());
    }
    
    @Test
    public void assertSupportsPositionedDelete() throws SQLException {
        assertFalse(metaData.supportsPositionedDelete());
    }
    
    @Test
    public void assertSupportsPositionedUpdate() throws SQLException {
        assertFalse(metaData.supportsPositionedUpdate());
    }
    
    @Test
    public void assertSupportsSelectForUpdate() throws SQLException {
        assertFalse(metaData.supportsSelectForUpdate());
    }
    
    @Test
    public void assertSupportsStoredProcedures() throws SQLException {
        assertFalse(metaData.supportsStoredProcedures());
    }
    
    @Test
    public void assertSupportsSubqueriesInComparisons() throws SQLException {
        assertFalse(metaData.supportsSubqueriesInComparisons());
    }
    
    @Test
    public void assertSupportsSubqueriesInExists() throws SQLException {
        assertFalse(metaData.supportsSubqueriesInExists());
    }
    
    @Test
    public void assertSupportsSubqueriesInIns() throws SQLException {
        assertFalse(metaData.supportsSubqueriesInIns());
    }
    
    @Test
    public void assertSupportsSubqueriesInQuantifieds() throws SQLException {
        assertFalse(metaData.supportsSubqueriesInQuantifieds());
    }
    
    @Test
    public void assertSupportsCorrelatedSubqueries() throws SQLException {
        assertFalse(metaData.supportsCorrelatedSubqueries());
    }
    
    @Test
    public void assertSupportsUnion() throws SQLException {
        assertFalse(metaData.supportsUnion());
    }
    
    @Test
    public void assertSupportsUnionAll() throws SQLException {
        assertFalse(metaData.supportsUnionAll());
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossCommit() throws SQLException {
        assertFalse(metaData.supportsOpenCursorsAcrossCommit());
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossRollback() throws SQLException {
        assertFalse(metaData.supportsOpenCursorsAcrossRollback());
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossCommit() throws SQLException {
        assertFalse(metaData.supportsOpenStatementsAcrossCommit());
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossRollback() throws SQLException {
        assertFalse(metaData.supportsOpenStatementsAcrossRollback());
    }
    
    @Test
    public void assertGetMaxBinaryLiteralLength() throws SQLException {
        assertThat(metaData.getMaxBinaryLiteralLength(), is(0));
    }
    
    @Test
    public void assertGetMaxCharLiteralLength() throws SQLException {
        assertThat(metaData.getMaxCharLiteralLength(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnNameLength() throws SQLException {
        assertThat(metaData.getMaxColumnNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInGroupBy() throws SQLException {
        assertThat(metaData.getMaxColumnsInGroupBy(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInIndex() throws SQLException {
        assertThat(metaData.getMaxColumnsInIndex(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInOrderBy() throws SQLException {
        assertThat(metaData.getMaxColumnsInOrderBy(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInSelect() throws SQLException {
        assertThat(metaData.getMaxColumnsInSelect(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInTable() throws SQLException {
        assertThat(metaData.getMaxColumnsInTable(), is(0));
    }
    
    @Test
    public void assertGetMaxConnections() throws SQLException {
        assertThat(metaData.getMaxConnections(), is(0));
    }
    
    @Test
    public void assertGetMaxCursorNameLength() throws SQLException {
        assertThat(metaData.getMaxCursorNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxIndexLength() throws SQLException {
        assertThat(metaData.getMaxIndexLength(), is(0));
    }
    
    @Test
    public void assertGetMaxSchemaNameLength() throws SQLException {
        assertThat(metaData.getMaxSchemaNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxProcedureNameLength() throws SQLException {
        assertThat(metaData.getMaxProcedureNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxCatalogNameLength() throws SQLException {
        assertThat(metaData.getMaxCatalogNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxRowSize() throws SQLException {
        assertThat(metaData.getMaxRowSize(), is(0));
    }
    
    @Test
    public void assertDoesMaxRowSizeIncludeBlobs() throws SQLException {
        assertFalse(metaData.doesMaxRowSizeIncludeBlobs());
    }
    
    @Test
    public void assertGetMaxStatementLength() throws SQLException {
        assertThat(metaData.getMaxStatementLength(), is(0));
    }
    
    @Test
    public void assertGetMaxStatements() throws SQLException {
        assertThat(metaData.getMaxStatements(), is(0));
    }
    
    @Test
    public void assertGetMaxTableNameLength() throws SQLException {
        assertThat(metaData.getMaxTableNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxTablesInSelect() throws SQLException {
        assertThat(metaData.getMaxTablesInSelect(), is(0));
    }
    
    @Test
    public void assertGetMaxUserNameLength() throws SQLException {
        assertThat(metaData.getMaxUserNameLength(), is(0));
    }
    
    @Test
    public void assertGetDefaultTransactionIsolation() throws SQLException {
        assertThat(metaData.getDefaultTransactionIsolation(), is(0));
    }
    
    @Test
    public void assertSupportsTransactions() throws SQLException {
        assertFalse(metaData.supportsTransactions());
    }
    
    @Test
    public void assertSupportsTransactionIsolationLevel() throws SQLException {
        assertFalse(metaData.supportsTransactionIsolationLevel(0));
    }
    
    @Test
    public void assertSupportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        assertFalse(metaData.supportsDataDefinitionAndDataManipulationTransactions());
    }
    
    @Test
    public void assertSupportsDataManipulationTransactionsOnly() throws SQLException {
        assertFalse(metaData.supportsDataManipulationTransactionsOnly());
    }
    
    @Test
    public void assertDataDefinitionCausesTransactionCommit() throws SQLException {
        assertFalse(metaData.dataDefinitionCausesTransactionCommit());
    }
    
    @Test
    public void assertDataDefinitionIgnoredInTransactions() throws SQLException {
        assertFalse(metaData.dataDefinitionIgnoredInTransactions());
    }
    
    @Test
    public void assertGetProcedures() throws SQLException {
        assertNull(metaData.getProcedures("", "", ""));
    }
    
    @Test
    public void assertGetProcedureColumns() throws SQLException {
        assertNull(metaData.getProcedureColumns("", "", "", ""));
    }
    
    @Test
    public void assertGetTables() throws SQLException {
        assertNull(metaData.getTables("", "", "", null));
    }
    
    @Test
    public void assertGetSchemas() throws SQLException {
        assertNull(metaData.getSchemas());
    }
    
    
    @Test
    public void assertGetSchemasWithParameter() throws SQLException {
        assertNull(metaData.getSchemas(null, null));
    }
    
    @Test
    public void assertGetCatalogs() throws SQLException {
        assertNull(metaData.getCatalogs());
    }
    
    @Test
    public void assertGetTableTypes() throws SQLException {
        assertNull(metaData.getTableTypes());
    }
    
    @Test
    public void assertGetColumns() throws SQLException {
        assertNull(metaData.getColumns("", "", "", ""));
    }
    
    @Test
    public void assertGetColumnPrivileges() throws SQLException {
        assertNull(metaData.getColumnPrivileges("", "", "", ""));
    }
    
    @Test
    public void assertGetTablePrivileges() throws SQLException {
        assertNull(metaData.getTablePrivileges("", "", ""));
    }
    
    @Test
    public void assertGetBestRowIdentifier() throws SQLException {
        assertNull(metaData.getBestRowIdentifier("", "", "", 0, false));
    }
    
    
    @Test
    public void assertGetVersionColumns() throws SQLException {
        assertNull(metaData.getVersionColumns("", "", ""));
    }
    
    @Test
    public void assertGetPrimaryKeys() throws SQLException {
        assertNull(metaData.getPrimaryKeys("", "", ""));
    }
    
    @Test
    public void assertGetImportedKeys() throws SQLException {
        assertNull(metaData.getImportedKeys("", "", ""));
    }
    
    @Test
    public void assertGetExportedKeys() throws SQLException {
        assertNull(metaData.getExportedKeys("", "", ""));
    }
    
    @Test
    public void assertGetCrossReference() throws SQLException {
        assertNull(metaData.getCrossReference("", "", "", "", "", ""));
    }
    
    @Test
    public void assertGetTypeInfo() throws SQLException {
        assertNull(metaData.getTypeInfo());
    }
    
    @Test
    public void assertGetIndexInfo() throws SQLException {
        assertNull(metaData.getIndexInfo("", "", "", false, false));
    }
    
    @Test
    public void assertSupportsResultSetType() throws SQLException {
        assertFalse(metaData.supportsResultSetType(0));
    }
    
    @Test
    public void assertSupportsResultSetConcurrency() throws SQLException {
        assertFalse(metaData.supportsResultSetConcurrency(0, 0));
    }
    
    @Test
    public void assertOwnUpdatesAreVisible() throws SQLException {
        assertFalse(metaData.ownUpdatesAreVisible(0));
    }
    
    @Test
    public void assertOwnDeletesAreVisible() throws SQLException {
        assertFalse(metaData.ownDeletesAreVisible(0));
    }
    
    @Test
    public void assertOwnInsertsAreVisible() throws SQLException {
        assertFalse(metaData.ownInsertsAreVisible(0));
    }
    
    @Test
    public void assertOthersUpdatesAreVisible() throws SQLException {
        assertFalse(metaData.othersUpdatesAreVisible(0));
    }
    
    @Test
    public void assertOthersDeletesAreVisible() throws SQLException {
        assertFalse(metaData.othersDeletesAreVisible(0));
    }
    
    @Test
    public void assertOthersInsertsAreVisible() throws SQLException {
        assertFalse(metaData.othersInsertsAreVisible(0));
    }
    
    @Test
    public void assertUpdatesAreDetected() throws SQLException {
        assertFalse(metaData.updatesAreDetected(0));
    }
    
    @Test
    public void assertDeletesAreDetected() throws SQLException {
        assertFalse(metaData.deletesAreDetected(0));
    }
    
    @Test
    public void assertInsertsAreDetected() throws SQLException {
        assertFalse(metaData.insertsAreDetected(0));
    }
    
    @Test
    public void assertSupportsBatchUpdates() throws SQLException {
        assertFalse(metaData.insertsAreDetected(0));
    }
    
    @Test
    public void assertGetUDTs() throws SQLException {
        assertNull(metaData.getUDTs("", "", "", null));
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        assertNull(metaData.getConnection());
    }
    
    @Test
    public void assertSupportsSavepoints() throws SQLException {
        assertFalse(metaData.supportsSavepoints());
    }
    
    @Test
    public void assertSupportsNamedParameters() throws SQLException {
        assertFalse(metaData.supportsNamedParameters());
    }
    
    @Test
    public void assertSupportsMultipleOpenResults() throws SQLException {
        assertFalse(metaData.supportsMultipleOpenResults());
    }
    
    @Test
    public void assertSupportsGetGeneratedKeys() throws SQLException {
        assertFalse(metaData.supportsGetGeneratedKeys());
    }
    
    @Test
    public void assertGetSuperTypes() throws SQLException {
        assertNull(metaData.getSuperTypes("", "", ""));
    }
    
    @Test
    public void assertGetSuperTables() throws SQLException {
        assertNull(metaData.getSuperTables("", "", ""));
    }
    
    @Test
    public void assertGetAttributes() throws SQLException {
        assertNull(metaData.getAttributes("", "", "", ""));
    }
    
    @Test
    public void assertSupportsResultSetHoldability() throws SQLException {
        assertFalse(metaData.supportsResultSetHoldability(0));
    }
    
    @Test
    public void assertGetResultSetHoldability() throws SQLException {
        assertThat(metaData.getResultSetHoldability(), is(0));
    }
    
    @Test
    public void assertGetDatabaseMajorVersion() throws SQLException {
        assertThat(metaData.getDatabaseMajorVersion(), is(0));
    }
    
    @Test
    public void assertGetDatabaseMinorVersion() throws SQLException {
        assertThat(metaData.getDatabaseMinorVersion(), is(0));
    }
    
    @Test
    public void assertGetJDBCMajorVersion() throws SQLException {
        assertThat(metaData.getJDBCMajorVersion(), is(0));
    }
    
    @Test
    public void assertGetJDBCMinorVersion() throws SQLException {
        assertThat(metaData.getJDBCMinorVersion(), is(0));
    }
    
    @Test
    public void assertGetSQLStateType() throws SQLException {
        assertThat(metaData.getSQLStateType(), is(DatabaseMetaData.sqlStateSQL));
    }
    
    @Test
    public void assertLocatorsUpdateCopy() throws SQLException {
        assertFalse(metaData.locatorsUpdateCopy());
    }
    
    @Test
    public void assertSupportsStatementPooling() throws SQLException {
        assertFalse(metaData.supportsStatementPooling());
    }
    
    @Test
    public void assertGetRowIdLifetime() throws SQLException {
        assertNull(metaData.getRowIdLifetime());
    }
    
    @Test
    public void assertSupportsStoredFunctionsUsingCallSyntax() throws SQLException {
        assertFalse(metaData.supportsStoredFunctionsUsingCallSyntax());
    }
    
    @Test
    public void assertAutoCommitFailureClosesAllResultSets() throws SQLException {
        assertFalse(metaData.autoCommitFailureClosesAllResultSets());
    }
    
    @Test
    public void assertGetClientInfoProperties() throws SQLException {
        assertNull(metaData.getClientInfoProperties());
    }
    
    @Test
    public void assertGetFunctions() throws SQLException {
        assertNull(metaData.getFunctions("", "", ""));
    }
    
    @Test
    public void assertGetFunctionColumns() throws SQLException {
        assertNull(metaData.getFunctionColumns("", "", "", ""));
    }
    
    @Test
    public void assertGetPseudoColumns() throws SQLException {
        assertNull(metaData.getPseudoColumns("", "", "", ""));
    }
    
    @Test
    public void assertGeneratedKeyAlwaysReturned() throws SQLException {
        assertFalse(metaData.generatedKeyAlwaysReturned());
    }
    
    @Test
    public void assertUnwrap() throws SQLException {
        assertNull(metaData.unwrap(null));
    }
    
    @Test
    public void assertIsWrapperFor() throws SQLException {
        assertFalse(metaData.isWrapperFor(null));
    }
}
