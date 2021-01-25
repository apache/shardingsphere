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

package org.apache.shardingsphere.driver.governance.internal.circuit.metadata;

import org.junit.Test;

import java.sql.DatabaseMetaData;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class CircuitBreakerDatabaseMetaDataTest {
    
    private final CircuitBreakerDatabaseMetaData metaData = new CircuitBreakerDatabaseMetaData();
    
    @Test
    public void assertAllProceduresAreCallable() {
        assertFalse(metaData.allProceduresAreCallable());
    }
    
    @Test
    public void assertAllTablesAreSelectable() {
        assertFalse(metaData.allTablesAreSelectable());
    }
    
    @Test
    public void assertGetURL() {
        assertNull(metaData.getURL());
    }
    
    @Test
    public void assertGetUserName() {
        assertNull(metaData.getUserName());
    }
    
    @Test
    public void assertIsReadOnly() {
        assertFalse(metaData.isReadOnly());
    }
    
    @Test
    public void assertNullsAreSortedHigh() {
        assertFalse(metaData.isReadOnly());
    }
    
    @Test
    public void assertNullsAreSortedLow() {
        assertFalse(metaData.nullsAreSortedLow());
    }
    
    @Test
    public void assertNullsAreSortedAtStart() {
        assertFalse(metaData.nullsAreSortedAtStart());
    }
    
    @Test
    public void assertNullsAreSortedAtEnd() {
        assertFalse(metaData.nullsAreSortedAtEnd());
    }
    
    @Test
    public void assertGetDatabaseProductName() {
        assertThat(metaData.getDatabaseProductName(), is("H2"));
    }
    
    @Test
    public void assertGetDatabaseProductVersion() {
        assertNull(metaData.getDatabaseProductVersion());
    }
    
    @Test
    public void assertGetDriverName() {
        assertNull(metaData.getDriverName());
    }
    
    @Test
    public void assertGetDriverVersion() {
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
    public void assertUsesLocalFiles() {
        assertFalse(metaData.usesLocalFiles());
    }
    
    @Test
    public void assertUsesLocalFilePerTable() {
        assertFalse(metaData.usesLocalFilePerTable());
    }
    
    @Test
    public void assertSupportsMixedCaseIdentifiers() {
        assertFalse(metaData.supportsMixedCaseIdentifiers());
    }
    
    @Test
    public void assertStoresUpperCaseIdentifiers() {
        assertFalse(metaData.storesUpperCaseIdentifiers());
    }
    
    @Test
    public void assertStoresLowerCaseIdentifiers() {
        assertFalse(metaData.storesUpperCaseIdentifiers());
    }
    
    @Test
    public void assertStoresMixedCaseIdentifiers() {
        assertFalse(metaData.storesMixedCaseIdentifiers());
    }
    
    @Test
    public void assertSupportsMixedCaseQuotedIdentifiers() {
        assertFalse(metaData.supportsMixedCaseQuotedIdentifiers());
    }
    
    @Test
    public void assertStoresUpperCaseQuotedIdentifiers() {
        assertFalse(metaData.supportsMixedCaseQuotedIdentifiers());
    }
    
    @Test
    public void assertStoresLowerCaseQuotedIdentifiers() {
        assertFalse(metaData.storesLowerCaseIdentifiers());
    }
    
    @Test
    public void assertStoresMixedCaseQuotedIdentifiers() {
        assertFalse(metaData.storesMixedCaseQuotedIdentifiers());
    }
    
    @Test
    public void assertGetIdentifierQuoteString() {
        assertNull(metaData.getIdentifierQuoteString());
    }
    
    @Test
    public void assertGetSQLKeywords() {
        assertNull(metaData.getSQLKeywords());
    }
    
    @Test
    public void assertGetNumericFunctions() {
        assertNull(metaData.getNumericFunctions());
    }
    
    @Test
    public void assertGetStringFunctions() {
        assertNull(metaData.getStringFunctions());
    }
    
    @Test
    public void assertGetSystemFunctions() {
        assertNull(metaData.getSystemFunctions());
    }
    
    @Test
    public void assertGetTimeDateFunctions() {
        assertNull(metaData.getTimeDateFunctions());
    }
    
    @Test
    public void assertGetSearchStringEscape() {
        assertNull(metaData.getSearchStringEscape());
    }
    
    @Test
    public void assertGetExtraNameCharacters() {
        assertNull(metaData.getExtraNameCharacters());
    }
    
    @Test
    public void assertSupportsAlterTableWithAddColumn() {
        assertFalse(metaData.supportsAlterTableWithAddColumn());
    }
    
    @Test
    public void assertSupportsAlterTableWithDropColumn() {
        assertFalse(metaData.supportsAlterTableWithDropColumn());
    }
    
    @Test
    public void assertSupportsColumnAliasing() {
        assertFalse(metaData.supportsColumnAliasing());
    }
    
    @Test
    public void assertNullPlusNonNullIsNull() {
        assertFalse(metaData.nullPlusNonNullIsNull());
    }
    
    @Test
    public void assertSupportsConvert() {
        assertFalse(metaData.supportsConvert());
    }
    
    @Test
    public void assertSupportsConvertWithParameter() {
        assertFalse(metaData.supportsConvert(0, 0));
    }
    
    @Test
    public void assertSupportsTableCorrelationNames() {
        assertFalse(metaData.supportsTableCorrelationNames());
    }
    
    @Test
    public void assertSupportsDifferentTableCorrelationNames() {
        assertFalse(metaData.supportsDifferentTableCorrelationNames());
    }
    
    @Test
    public void assertSupportsExpressionsInOrderBy() {
        assertFalse(metaData.supportsExpressionsInOrderBy());
    }
    
    @Test
    public void assertSupportsOrderByUnrelated() {
        assertFalse(metaData.supportsOrderByUnrelated());
    }
    
    @Test
    public void assertSupportsGroupBy() {
        assertFalse(metaData.supportsGroupBy());
    }
    
    @Test
    public void assertSupportsGroupByUnrelated() {
        assertFalse(metaData.supportsGroupByUnrelated());
    }
    
    @Test
    public void assertSupportsGroupByBeyondSelect() {
        assertFalse(metaData.supportsGroupByBeyondSelect());
    }
    
    @Test
    public void assertSupportsLikeEscapeClause() {
        assertFalse(metaData.supportsLikeEscapeClause());
    }
    
    @Test
    public void assertSupportsMultipleResultSets() {
        assertFalse(metaData.supportsMultipleResultSets());
    }
    
    @Test
    public void assertSupportsMultipleTransactions() {
        assertFalse(metaData.supportsMultipleTransactions());
    }
    
    @Test
    public void assertSupportsNonNullableColumns() {
        assertFalse(metaData.supportsNonNullableColumns());
    }
    
    @Test
    public void assertSupportsMinimumSQLGrammar() {
        assertFalse(metaData.supportsMinimumSQLGrammar());
    }
    
    @Test
    public void assertSupportsCoreSQLGrammar() {
        assertFalse(metaData.supportsCoreSQLGrammar());
    }
    
    @Test
    public void assertSupportsExtendedSQLGrammar() {
        assertFalse(metaData.supportsExtendedSQLGrammar());
    }
    
    @Test
    public void assertSupportsANSI92EntryLevelSQL() {
        assertFalse(metaData.supportsANSI92EntryLevelSQL());
    }
    
    @Test
    public void assertSupportsANSI92IntermediateSQL() {
        assertFalse(metaData.supportsANSI92IntermediateSQL());
    }
    
    @Test
    public void assertSupportsANSI92FullSQL() {
        assertFalse(metaData.supportsANSI92FullSQL());
    }
    
    @Test
    public void assertSupportsIntegrityEnhancementFacility() {
        assertFalse(metaData.supportsIntegrityEnhancementFacility());
    }
    
    @Test
    public void assertSupportsOuterJoins() {
        assertFalse(metaData.supportsOuterJoins());
    }
    
    @Test
    public void assertSupportsFullOuterJoins() {
        assertFalse(metaData.supportsFullOuterJoins());
    }
    
    @Test
    public void assertSupportsLimitedOuterJoins() {
        assertFalse(metaData.supportsLimitedOuterJoins());
    }
    
    @Test
    public void assertGetSchemaTerm() {
        assertNull(metaData.getSchemaTerm());
    }
    
    @Test
    public void assertGetProcedureTerm() {
        assertNull(metaData.getProcedureTerm());
    }
    
    @Test
    public void assertGetCatalogTerm() {
        assertNull(metaData.getCatalogTerm());
    }
    
    @Test
    public void assertIsCatalogAtStart() {
        assertFalse(metaData.isCatalogAtStart());
    }
    
    @Test
    public void assertGetCatalogSeparator() {
        assertNull(metaData.getCatalogSeparator());
    }
    
    @Test
    public void assertSupportsSchemasInDataManipulation() {
        assertFalse(metaData.supportsSchemasInDataManipulation());
    }
    
    @Test
    public void assertSupportsSchemasInProcedureCalls() {
        assertFalse(metaData.supportsSchemasInProcedureCalls());
    }
    
    @Test
    public void assertSupportsSchemasInTableDefinitions() {
        assertFalse(metaData.supportsSchemasInTableDefinitions());
    }
    
    @Test
    public void assertSupportsSchemasInIndexDefinitions() {
        assertFalse(metaData.supportsSchemasInIndexDefinitions());
    }
    
    @Test
    public void assertSupportsSchemasInPrivilegeDefinitions() {
        assertFalse(metaData.supportsSchemasInPrivilegeDefinitions());
    }
    
    @Test
    public void assertSupportsCatalogsInDataManipulation() {
        assertFalse(metaData.supportsCatalogsInDataManipulation());
    }
    
    @Test
    public void assertSupportsCatalogsInProcedureCalls() {
        assertFalse(metaData.supportsCatalogsInProcedureCalls());
    }
    
    @Test
    public void assertSupportsCatalogsInTableDefinitions() {
        assertFalse(metaData.supportsCatalogsInTableDefinitions());
    }
    
    @Test
    public void assertSupportsCatalogsInIndexDefinitions() {
        assertFalse(metaData.supportsCatalogsInIndexDefinitions());
    }
    
    @Test
    public void assertSupportsCatalogsInPrivilegeDefinitions() {
        assertFalse(metaData.supportsCatalogsInPrivilegeDefinitions());
    }
    
    @Test
    public void assertSupportsPositionedDelete() {
        assertFalse(metaData.supportsPositionedDelete());
    }
    
    @Test
    public void assertSupportsPositionedUpdate() {
        assertFalse(metaData.supportsPositionedUpdate());
    }
    
    @Test
    public void assertSupportsSelectForUpdate() {
        assertFalse(metaData.supportsSelectForUpdate());
    }
    
    @Test
    public void assertSupportsStoredProcedures() {
        assertFalse(metaData.supportsStoredProcedures());
    }
    
    @Test
    public void assertSupportsSubqueriesInComparisons() {
        assertFalse(metaData.supportsSubqueriesInComparisons());
    }
    
    @Test
    public void assertSupportsSubqueriesInExists() {
        assertFalse(metaData.supportsSubqueriesInExists());
    }
    
    @Test
    public void assertSupportsSubqueriesInIns() {
        assertFalse(metaData.supportsSubqueriesInIns());
    }
    
    @Test
    public void assertSupportsSubqueriesInQuantifieds() {
        assertFalse(metaData.supportsSubqueriesInQuantifieds());
    }
    
    @Test
    public void assertSupportsCorrelatedSubqueries() {
        assertFalse(metaData.supportsCorrelatedSubqueries());
    }
    
    @Test
    public void assertSupportsUnion() {
        assertFalse(metaData.supportsUnion());
    }
    
    @Test
    public void assertSupportsUnionAll() {
        assertFalse(metaData.supportsUnionAll());
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossCommit() {
        assertFalse(metaData.supportsOpenCursorsAcrossCommit());
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossRollback() {
        assertFalse(metaData.supportsOpenCursorsAcrossRollback());
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossCommit() {
        assertFalse(metaData.supportsOpenStatementsAcrossCommit());
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossRollback() {
        assertFalse(metaData.supportsOpenStatementsAcrossRollback());
    }
    
    @Test
    public void assertGetMaxBinaryLiteralLength() {
        assertThat(metaData.getMaxBinaryLiteralLength(), is(0));
    }
    
    @Test
    public void assertGetMaxCharLiteralLength() {
        assertThat(metaData.getMaxCharLiteralLength(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnNameLength() {
        assertThat(metaData.getMaxColumnNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInGroupBy() {
        assertThat(metaData.getMaxColumnsInGroupBy(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInIndex() {
        assertThat(metaData.getMaxColumnsInIndex(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInOrderBy() {
        assertThat(metaData.getMaxColumnsInOrderBy(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInSelect() {
        assertThat(metaData.getMaxColumnsInSelect(), is(0));
    }
    
    @Test
    public void assertGetMaxColumnsInTable() {
        assertThat(metaData.getMaxColumnsInTable(), is(0));
    }
    
    @Test
    public void assertGetMaxConnections() {
        assertThat(metaData.getMaxConnections(), is(0));
    }
    
    @Test
    public void assertGetMaxCursorNameLength() {
        assertThat(metaData.getMaxCursorNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxIndexLength() {
        assertThat(metaData.getMaxIndexLength(), is(0));
    }
    
    @Test
    public void assertGetMaxSchemaNameLength() {
        assertThat(metaData.getMaxSchemaNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxProcedureNameLength() {
        assertThat(metaData.getMaxProcedureNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxCatalogNameLength() {
        assertThat(metaData.getMaxCatalogNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxRowSize() {
        assertThat(metaData.getMaxRowSize(), is(0));
    }
    
    @Test
    public void assertDoesMaxRowSizeIncludeBlobs() {
        assertFalse(metaData.doesMaxRowSizeIncludeBlobs());
    }
    
    @Test
    public void assertGetMaxStatementLength() {
        assertThat(metaData.getMaxStatementLength(), is(0));
    }
    
    @Test
    public void assertGetMaxStatements() {
        assertThat(metaData.getMaxStatements(), is(0));
    }
    
    @Test
    public void assertGetMaxTableNameLength() {
        assertThat(metaData.getMaxTableNameLength(), is(0));
    }
    
    @Test
    public void assertGetMaxTablesInSelect() {
        assertThat(metaData.getMaxTablesInSelect(), is(0));
    }
    
    @Test
    public void assertGetMaxUserNameLength() {
        assertThat(metaData.getMaxUserNameLength(), is(0));
    }
    
    @Test
    public void assertGetDefaultTransactionIsolation() {
        assertThat(metaData.getDefaultTransactionIsolation(), is(0));
    }
    
    @Test
    public void assertSupportsTransactions() {
        assertFalse(metaData.supportsTransactions());
    }
    
    @Test
    public void assertSupportsTransactionIsolationLevel() {
        assertFalse(metaData.supportsTransactionIsolationLevel(0));
    }
    
    @Test
    public void assertSupportsDataDefinitionAndDataManipulationTransactions() {
        assertFalse(metaData.supportsDataDefinitionAndDataManipulationTransactions());
    }
    
    @Test
    public void assertSupportsDataManipulationTransactionsOnly() {
        assertFalse(metaData.supportsDataManipulationTransactionsOnly());
    }
    
    @Test
    public void assertDataDefinitionCausesTransactionCommit() {
        assertFalse(metaData.dataDefinitionCausesTransactionCommit());
    }
    
    @Test
    public void assertDataDefinitionIgnoredInTransactions() {
        assertFalse(metaData.dataDefinitionIgnoredInTransactions());
    }
    
    @Test
    public void assertGetProcedures() {
        assertNull(metaData.getProcedures("", "", ""));
    }
    
    @Test
    public void assertGetProcedureColumns() {
        assertNull(metaData.getProcedureColumns("", "", "", ""));
    }
    
    @Test
    public void assertGetTables() {
        assertNull(metaData.getTables("", "", "", null));
    }
    
    @Test
    public void assertGetSchemas() {
        assertNull(metaData.getSchemas());
    }
    
    @Test
    public void assertGetSchemasWithParameter() {
        assertNull(metaData.getSchemas(null, null));
    }
    
    @Test
    public void assertGetCatalogs() {
        assertNull(metaData.getCatalogs());
    }
    
    @Test
    public void assertGetTableTypes() {
        assertNull(metaData.getTableTypes());
    }
    
    @Test
    public void assertGetColumns() {
        assertNull(metaData.getColumns("", "", "", ""));
    }
    
    @Test
    public void assertGetColumnPrivileges() {
        assertNull(metaData.getColumnPrivileges("", "", "", ""));
    }
    
    @Test
    public void assertGetTablePrivileges() {
        assertNull(metaData.getTablePrivileges("", "", ""));
    }
    
    @Test
    public void assertGetBestRowIdentifier() {
        assertNull(metaData.getBestRowIdentifier("", "", "", 0, false));
    }
    
    @Test
    public void assertGetVersionColumns() {
        assertNull(metaData.getVersionColumns("", "", ""));
    }
    
    @Test
    public void assertGetPrimaryKeys() {
        assertNull(metaData.getPrimaryKeys("", "", ""));
    }
    
    @Test
    public void assertGetImportedKeys() {
        assertNull(metaData.getImportedKeys("", "", ""));
    }
    
    @Test
    public void assertGetExportedKeys() {
        assertNull(metaData.getExportedKeys("", "", ""));
    }
    
    @Test
    public void assertGetCrossReference() {
        assertNull(metaData.getCrossReference("", "", "", "", "", ""));
    }
    
    @Test
    public void assertGetTypeInfo() {
        assertNull(metaData.getTypeInfo());
    }
    
    @Test
    public void assertGetIndexInfo() {
        assertNull(metaData.getIndexInfo("", "", "", false, false));
    }
    
    @Test
    public void assertSupportsResultSetType() {
        assertFalse(metaData.supportsResultSetType(0));
    }
    
    @Test
    public void assertSupportsResultSetConcurrency() {
        assertFalse(metaData.supportsResultSetConcurrency(0, 0));
    }
    
    @Test
    public void assertOwnUpdatesAreVisible() {
        assertFalse(metaData.ownUpdatesAreVisible(0));
    }
    
    @Test
    public void assertOwnDeletesAreVisible() {
        assertFalse(metaData.ownDeletesAreVisible(0));
    }
    
    @Test
    public void assertOwnInsertsAreVisible() {
        assertFalse(metaData.ownInsertsAreVisible(0));
    }
    
    @Test
    public void assertOthersUpdatesAreVisible() {
        assertFalse(metaData.othersUpdatesAreVisible(0));
    }
    
    @Test
    public void assertOthersDeletesAreVisible() {
        assertFalse(metaData.othersDeletesAreVisible(0));
    }
    
    @Test
    public void assertOthersInsertsAreVisible() {
        assertFalse(metaData.othersInsertsAreVisible(0));
    }
    
    @Test
    public void assertUpdatesAreDetected() {
        assertFalse(metaData.updatesAreDetected(0));
    }
    
    @Test
    public void assertDeletesAreDetected() {
        assertFalse(metaData.deletesAreDetected(0));
    }
    
    @Test
    public void assertInsertsAreDetected() {
        assertFalse(metaData.insertsAreDetected(0));
    }
    
    @Test
    public void assertSupportsBatchUpdates() {
        assertFalse(metaData.insertsAreDetected(0));
    }
    
    @Test
    public void assertGetUDTs() {
        assertNull(metaData.getUDTs("", "", "", null));
    }
    
    @Test
    public void assertGetConnection() {
        assertNull(metaData.getConnection());
    }
    
    @Test
    public void assertSupportsSavepoints() {
        assertFalse(metaData.supportsSavepoints());
    }
    
    @Test
    public void assertSupportsNamedParameters() {
        assertFalse(metaData.supportsNamedParameters());
    }
    
    @Test
    public void assertSupportsMultipleOpenResults() {
        assertFalse(metaData.supportsMultipleOpenResults());
    }
    
    @Test
    public void assertSupportsGetGeneratedKeys() {
        assertFalse(metaData.supportsGetGeneratedKeys());
    }
    
    @Test
    public void assertGetSuperTypes() {
        assertNull(metaData.getSuperTypes("", "", ""));
    }
    
    @Test
    public void assertGetSuperTables() {
        assertNull(metaData.getSuperTables("", "", ""));
    }
    
    @Test
    public void assertGetAttributes() {
        assertNull(metaData.getAttributes("", "", "", ""));
    }
    
    @Test
    public void assertSupportsResultSetHoldability() {
        assertFalse(metaData.supportsResultSetHoldability(0));
    }
    
    @Test
    public void assertGetResultSetHoldability() {
        assertThat(metaData.getResultSetHoldability(), is(0));
    }
    
    @Test
    public void assertGetDatabaseMajorVersion() {
        assertThat(metaData.getDatabaseMajorVersion(), is(0));
    }
    
    @Test
    public void assertGetDatabaseMinorVersion() {
        assertThat(metaData.getDatabaseMinorVersion(), is(0));
    }
    
    @Test
    public void assertGetJDBCMajorVersion() {
        assertThat(metaData.getJDBCMajorVersion(), is(0));
    }
    
    @Test
    public void assertGetJDBCMinorVersion() {
        assertThat(metaData.getJDBCMinorVersion(), is(0));
    }
    
    @Test
    public void assertGetSQLStateType() {
        assertThat(metaData.getSQLStateType(), is(DatabaseMetaData.sqlStateSQL));
    }
    
    @Test
    public void assertLocatorsUpdateCopy() {
        assertFalse(metaData.locatorsUpdateCopy());
    }
    
    @Test
    public void assertSupportsStatementPooling() {
        assertFalse(metaData.supportsStatementPooling());
    }
    
    @Test
    public void assertGetRowIdLifetime() {
        assertNull(metaData.getRowIdLifetime());
    }
    
    @Test
    public void assertSupportsStoredFunctionsUsingCallSyntax() {
        assertFalse(metaData.supportsStoredFunctionsUsingCallSyntax());
    }
    
    @Test
    public void assertAutoCommitFailureClosesAllResultSets() {
        assertFalse(metaData.autoCommitFailureClosesAllResultSets());
    }
    
    @Test
    public void assertGetClientInfoProperties() {
        assertNull(metaData.getClientInfoProperties());
    }
    
    @Test
    public void assertGetFunctions() {
        assertNull(metaData.getFunctions("", "", ""));
    }
    
    @Test
    public void assertGetFunctionColumns() {
        assertNull(metaData.getFunctionColumns("", "", "", ""));
    }
    
    @Test
    public void assertGetPseudoColumns() {
        assertNull(metaData.getPseudoColumns("", "", "", ""));
    }
    
    @Test
    public void assertGeneratedKeyAlwaysReturned() {
        assertFalse(metaData.generatedKeyAlwaysReturned());
    }
    
    @Test
    public void assertUnwrap() {
        assertNull(metaData.unwrap(null));
    }
    
    @Test
    public void assertIsWrapperFor() {
        assertFalse(metaData.isWrapperFor(null));
    }
}
