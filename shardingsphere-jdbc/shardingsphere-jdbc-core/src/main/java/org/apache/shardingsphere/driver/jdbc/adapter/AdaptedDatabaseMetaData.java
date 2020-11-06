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

package org.apache.shardingsphere.driver.jdbc.adapter;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.schema.model.datasource.CachedDatabaseMetaData;

import java.sql.DatabaseMetaData;
import java.sql.RowIdLifetime;

/**
 * Adapted database meta data.
 */
@RequiredArgsConstructor
public abstract class AdaptedDatabaseMetaData extends WrapperAdapter implements DatabaseMetaData {
    
    private final CachedDatabaseMetaData cachedDatabaseMetaData;
    
    @Override
    public final String getURL() {
        return cachedDatabaseMetaData.getUrl();
    }
    
    @Override
    public final String getUserName() {
        return cachedDatabaseMetaData.getUserName();
    }
    
    @Override
    public final String getDatabaseProductName() {
        return cachedDatabaseMetaData.getDatabaseProductName();
    }
    
    @Override
    public final String getDatabaseProductVersion() {
        return cachedDatabaseMetaData.getDatabaseProductVersion();
    }
    
    @Override
    public final String getDriverName() {
        return cachedDatabaseMetaData.getDriverName();
    }
    
    @Override
    public final String getDriverVersion() {
        return cachedDatabaseMetaData.getDriverVersion();
    }
    
    @Override
    public final int getDriverMajorVersion() {
        return cachedDatabaseMetaData.getDriverMajorVersion();
    }
    
    @Override
    public final int getDriverMinorVersion() {
        return cachedDatabaseMetaData.getDriverMinorVersion();
    }
    
    @Override
    public final int getDatabaseMajorVersion() {
        return cachedDatabaseMetaData.getDatabaseMajorVersion();
    }
    
    @Override
    public final int getDatabaseMinorVersion() {
        return cachedDatabaseMetaData.getDatabaseMinorVersion();
    }
    
    @Override
    public final int getJDBCMajorVersion() {
        return cachedDatabaseMetaData.getJdbcMajorVersion();
    }
    
    @Override
    public final int getJDBCMinorVersion() {
        return cachedDatabaseMetaData.getJdbcMinorVersion();
    }
    
    @Override
    public final boolean isReadOnly() {
        return cachedDatabaseMetaData.isReadOnly();
    }
    
    @Override
    public final boolean allProceduresAreCallable() {
        return cachedDatabaseMetaData.isAllProceduresAreCallable();
    }
    
    @Override
    public final boolean allTablesAreSelectable() {
        return cachedDatabaseMetaData.isAllTablesAreSelectable();
    }
    
    @Override
    public final boolean nullsAreSortedHigh() {
        return cachedDatabaseMetaData.isNullsAreSortedHigh();
    }
    
    @Override
    public final boolean nullsAreSortedLow() {
        return cachedDatabaseMetaData.isNullsAreSortedLow();
    }
    
    @Override
    public final boolean nullsAreSortedAtStart() {
        return cachedDatabaseMetaData.isNullsAreSortedAtStart();
    }
    
    @Override
    public final boolean nullsAreSortedAtEnd() {
        return cachedDatabaseMetaData.isNullsAreSortedAtEnd();
    }
    
    @Override
    public final boolean usesLocalFiles() {
        return cachedDatabaseMetaData.isUsesLocalFiles();
    }
    
    @Override
    public final boolean usesLocalFilePerTable() {
        return cachedDatabaseMetaData.isUsesLocalFilePerTable();
    }
    
    @Override
    public final boolean supportsMixedCaseIdentifiers() {
        return cachedDatabaseMetaData.isStoresMixedCaseIdentifiers();
    }
    
    @Override
    public final boolean storesUpperCaseIdentifiers() {
        return cachedDatabaseMetaData.isStoresUpperCaseIdentifiers();
    }
    
    @Override
    public final boolean storesLowerCaseIdentifiers() {
        return cachedDatabaseMetaData.isStoresLowerCaseIdentifiers();
    }
    
    @Override
    public final boolean storesMixedCaseIdentifiers() {
        return cachedDatabaseMetaData.isStoresMixedCaseIdentifiers();
    }
    
    @Override
    public final boolean supportsMixedCaseQuotedIdentifiers() {
        return cachedDatabaseMetaData.isSupportsMixedCaseQuotedIdentifiers();
    }
    
    @Override
    public final boolean storesUpperCaseQuotedIdentifiers() {
        return cachedDatabaseMetaData.isStoresUpperCaseQuotedIdentifiers();
    }
    
    @Override
    public final boolean storesLowerCaseQuotedIdentifiers() {
        return cachedDatabaseMetaData.isStoresLowerCaseQuotedIdentifiers();
    }
    
    @Override
    public final boolean storesMixedCaseQuotedIdentifiers() {
        return cachedDatabaseMetaData.isSupportsMixedCaseQuotedIdentifiers();
    }
    
    @Override
    public final String getIdentifierQuoteString() {
        return cachedDatabaseMetaData.getIdentifierQuoteString();
    }
    
    @Override
    public final String getSQLKeywords() {
        return cachedDatabaseMetaData.getSqlKeywords();
    }
    
    @Override
    public final String getNumericFunctions() {
        return cachedDatabaseMetaData.getNumericFunctions();
    }
    
    @Override
    public final String getStringFunctions() {
        return cachedDatabaseMetaData.getStringFunctions();
    }
    
    @Override
    public final String getSystemFunctions() {
        return cachedDatabaseMetaData.getSystemFunctions();
    }
    
    @Override
    public final String getTimeDateFunctions() {
        return cachedDatabaseMetaData.getTimeDateFunctions();
    }
    
    @Override
    public final String getSearchStringEscape() {
        return cachedDatabaseMetaData.getSearchStringEscape();
    }
    
    @Override
    public final String getExtraNameCharacters() {
        return cachedDatabaseMetaData.getExtraNameCharacters();
    }
    
    @Override
    public final boolean supportsAlterTableWithAddColumn() {
        return cachedDatabaseMetaData.isSupportsAlterTableWithAddColumn();
    }
    
    @Override
    public final boolean supportsAlterTableWithDropColumn() {
        return cachedDatabaseMetaData.isSupportsAlterTableWithDropColumn();
    }
    
    @Override
    public final boolean supportsColumnAliasing() {
        return cachedDatabaseMetaData.isSupportsColumnAliasing();
    }
    
    @Override
    public final boolean nullPlusNonNullIsNull() {
        return cachedDatabaseMetaData.isNullPlusNonNullIsNull();
    }
    
    @Override
    public final boolean supportsConvert() {
        return cachedDatabaseMetaData.isSupportsConvert();
    }
    
    @Override
    public final boolean supportsConvert(final int fromType, final int toType) {
        return cachedDatabaseMetaData.isSupportsConvert();
    }
    
    @Override
    public final boolean supportsTableCorrelationNames() {
        return cachedDatabaseMetaData.isSupportsTableCorrelationNames();
    }
    
    @Override
    public final boolean supportsDifferentTableCorrelationNames() {
        return cachedDatabaseMetaData.isSupportsDifferentTableCorrelationNames();
    }
    
    @Override
    public final boolean supportsExpressionsInOrderBy() {
        return cachedDatabaseMetaData.isSupportsExpressionsInOrderBy();
    }
    
    @Override
    public final boolean supportsOrderByUnrelated() {
        return cachedDatabaseMetaData.isSupportsOrderByUnrelated();
    }
    
    @Override
    public final boolean supportsGroupBy() {
        return cachedDatabaseMetaData.isSupportsGroupBy();
    }
    
    @Override
    public final boolean supportsGroupByUnrelated() {
        return cachedDatabaseMetaData.isSupportsGroupByUnrelated();
    }
    
    @Override
    public final boolean supportsGroupByBeyondSelect() {
        return cachedDatabaseMetaData.isSupportsGroupByBeyondSelect();
    }
    
    @Override
    public final boolean supportsLikeEscapeClause() {
        return cachedDatabaseMetaData.isSupportsLikeEscapeClause();
    }
    
    @Override
    public final boolean supportsMultipleResultSets() {
        return cachedDatabaseMetaData.isSupportsMultipleResultSets();
    }
    
    @Override
    public final boolean supportsMultipleTransactions() {
        return cachedDatabaseMetaData.isSupportsMultipleTransactions();
    }
    
    @Override
    public final boolean supportsNonNullableColumns() {
        return cachedDatabaseMetaData.isSupportsNonNullableColumns();
    }
    
    @Override
    public final boolean supportsMinimumSQLGrammar() {
        return cachedDatabaseMetaData.isSupportsMinimumSQLGrammar();
    }
    
    @Override
    public final boolean supportsCoreSQLGrammar() {
        return cachedDatabaseMetaData.isSupportsCoreSQLGrammar();
    }
    
    @Override
    public final boolean supportsExtendedSQLGrammar() {
        return cachedDatabaseMetaData.isSupportsExtendedSQLGrammar();
    }
    
    @Override
    public final boolean supportsANSI92EntryLevelSQL() {
        return cachedDatabaseMetaData.isSupportsANSI92EntryLevelSQL();
    }
    
    @Override
    public final boolean supportsANSI92IntermediateSQL() {
        return cachedDatabaseMetaData.isSupportsANSI92IntermediateSQL();
    }
    
    @Override
    public final boolean supportsANSI92FullSQL() {
        return cachedDatabaseMetaData.isSupportsANSI92FullSQL();
    }
    
    @Override
    public final boolean supportsIntegrityEnhancementFacility() {
        return cachedDatabaseMetaData.isSupportsIntegrityEnhancementFacility();
    }
    
    @Override
    public final boolean supportsOuterJoins() {
        return cachedDatabaseMetaData.isSupportsOuterJoins();
    }
    
    @Override
    public final boolean supportsFullOuterJoins() {
        return cachedDatabaseMetaData.isSupportsFullOuterJoins();
    }
    
    @Override
    public final boolean supportsLimitedOuterJoins() {
        return cachedDatabaseMetaData.isSupportsLimitedOuterJoins();
    }
    
    @Override
    public final String getSchemaTerm() {
        return cachedDatabaseMetaData.getSchemaTerm();
    }
    
    @Override
    public final String getProcedureTerm() {
        return cachedDatabaseMetaData.getProcedureTerm();
    }
    
    @Override
    public final String getCatalogTerm() {
        return cachedDatabaseMetaData.getCatalogTerm();
    }
    
    @Override
    public final boolean isCatalogAtStart() {
        return cachedDatabaseMetaData.isCatalogAtStart();
    }
    
    @Override
    public final String getCatalogSeparator() {
        return cachedDatabaseMetaData.getCatalogSeparator();
    }
    
    @Override
    public final boolean supportsSchemasInDataManipulation() {
        return cachedDatabaseMetaData.isSupportsSchemasInDataManipulation();
    }
    
    @Override
    public final boolean supportsSchemasInProcedureCalls() {
        return cachedDatabaseMetaData.isSupportsSchemasInProcedureCalls();
    }
    
    @Override
    public final boolean supportsSchemasInTableDefinitions() {
        return cachedDatabaseMetaData.isSupportsSchemasInTableDefinitions();
    }
    
    @Override
    public final boolean supportsSchemasInIndexDefinitions() {
        return cachedDatabaseMetaData.isSupportsSchemasInIndexDefinitions();
    }
    
    @Override
    public final boolean supportsSchemasInPrivilegeDefinitions() {
        return cachedDatabaseMetaData.isSupportsSchemasInPrivilegeDefinitions();
    }
    
    @Override
    public final boolean supportsCatalogsInDataManipulation() {
        return cachedDatabaseMetaData.isSupportsCatalogsInDataManipulation();
    }
    
    @Override
    public final boolean supportsCatalogsInProcedureCalls() {
        return cachedDatabaseMetaData.isSupportsCatalogsInProcedureCalls();
    }
    
    @Override
    public final boolean supportsCatalogsInTableDefinitions() {
        return cachedDatabaseMetaData.isSupportsCatalogsInTableDefinitions();
    }
    
    @Override
    public final boolean supportsCatalogsInIndexDefinitions() {
        return cachedDatabaseMetaData.isSupportsCatalogsInIndexDefinitions();
    }
    
    @Override
    public final boolean supportsCatalogsInPrivilegeDefinitions() {
        return cachedDatabaseMetaData.isSupportsCatalogsInPrivilegeDefinitions();
    }
    
    @Override
    public final boolean supportsPositionedDelete() {
        return cachedDatabaseMetaData.isSupportsPositionedDelete();
    }
    
    @Override
    public final boolean supportsPositionedUpdate() {
        return cachedDatabaseMetaData.isSupportsPositionedUpdate();
    }
    
    @Override
    public final boolean supportsSelectForUpdate() {
        return cachedDatabaseMetaData.isSupportsSelectForUpdate();
    }
    
    @Override
    public final boolean supportsStoredProcedures() {
        return cachedDatabaseMetaData.isSupportsStoredProcedures();
    }
    
    @Override
    public final boolean supportsSubqueriesInComparisons() {
        return cachedDatabaseMetaData.isSupportsSubqueriesInComparisons();
    }
    
    @Override
    public final boolean supportsSubqueriesInExists() {
        return cachedDatabaseMetaData.isSupportsSubqueriesInExists();
    }
    
    @Override
    public final boolean supportsSubqueriesInIns() {
        return cachedDatabaseMetaData.isSupportsSubqueriesInIns();
    }
    
    @Override
    public final boolean supportsSubqueriesInQuantifieds() {
        return cachedDatabaseMetaData.isSupportsSubqueriesInQuantifieds();
    }
    
    @Override
    public final boolean supportsCorrelatedSubqueries() {
        return cachedDatabaseMetaData.isSupportsCorrelatedSubqueries();
    }
    
    @Override
    public final boolean supportsUnion() {
        return cachedDatabaseMetaData.isSupportsUnion();
    }
    
    @Override
    public final boolean supportsUnionAll() {
        return cachedDatabaseMetaData.isSupportsUnionAll();
    }
    
    @Override
    public final boolean supportsOpenCursorsAcrossCommit() {
        return cachedDatabaseMetaData.isSupportsOpenCursorsAcrossCommit();
    }
    
    @Override
    public final boolean supportsOpenCursorsAcrossRollback() {
        return cachedDatabaseMetaData.isSupportsOpenCursorsAcrossRollback();
    }
    
    @Override
    public final boolean supportsOpenStatementsAcrossCommit() {
        return cachedDatabaseMetaData.isSupportsOpenStatementsAcrossCommit();
    }
    
    @Override
    public final boolean supportsOpenStatementsAcrossRollback() {
        return cachedDatabaseMetaData.isSupportsOpenStatementsAcrossRollback();
    }
    
    @Override
    public final int getMaxBinaryLiteralLength() {
        return cachedDatabaseMetaData.getMaxBinaryLiteralLength();
    }
    
    @Override
    public final int getMaxCharLiteralLength() {
        return cachedDatabaseMetaData.getMaxCharLiteralLength();
    }
    
    @Override
    public final int getMaxColumnNameLength() {
        return cachedDatabaseMetaData.getMaxColumnNameLength();
    }
    
    @Override
    public final int getMaxColumnsInGroupBy() {
        return cachedDatabaseMetaData.getMaxColumnsInGroupBy();
    }
    
    @Override
    public final int getMaxColumnsInIndex() {
        return cachedDatabaseMetaData.getMaxColumnsInIndex();
    }
    
    @Override
    public final int getMaxColumnsInOrderBy() {
        return cachedDatabaseMetaData.getMaxColumnsInOrderBy();
    }
    
    @Override
    public final int getMaxColumnsInSelect() {
        return cachedDatabaseMetaData.getMaxColumnsInSelect();
    }
    
    @Override
    public final int getMaxColumnsInTable() {
        return cachedDatabaseMetaData.getMaxColumnsInTable();
    }
    
    @Override
    public final int getMaxConnections() {
        return cachedDatabaseMetaData.getMaxConnections();
    }
    
    @Override
    public final int getMaxCursorNameLength() {
        return cachedDatabaseMetaData.getMaxCursorNameLength();
    }
    
    @Override
    public final int getMaxIndexLength() {
        return cachedDatabaseMetaData.getMaxIndexLength();
    }
    
    @Override
    public final int getMaxSchemaNameLength() {
        return cachedDatabaseMetaData.getMaxSchemaNameLength();
    }
    
    @Override
    public final int getMaxProcedureNameLength() {
        return cachedDatabaseMetaData.getMaxProcedureNameLength();
    }
    
    @Override
    public final int getMaxCatalogNameLength() {
        return cachedDatabaseMetaData.getMaxCatalogNameLength();
    }
    
    @Override
    public final int getMaxRowSize() {
        return cachedDatabaseMetaData.getMaxRowSize();
    }
    
    @Override
    public final boolean doesMaxRowSizeIncludeBlobs() {
        return cachedDatabaseMetaData.isDoesMaxRowSizeIncludeBlobs();
    }
    
    @Override
    public final int getMaxStatementLength() {
        return cachedDatabaseMetaData.getMaxStatementLength();
    }
    
    @Override
    public final int getMaxStatements() {
        return cachedDatabaseMetaData.getMaxStatements();
    }
    
    @Override
    public final int getMaxTableNameLength() {
        return cachedDatabaseMetaData.getMaxTableNameLength();
    }
    
    @Override
    public final int getMaxTablesInSelect() {
        return cachedDatabaseMetaData.getMaxTablesInSelect();
    }
    
    @Override
    public final int getMaxUserNameLength() {
        return cachedDatabaseMetaData.getMaxUserNameLength();
    }
    
    @Override
    public final int getDefaultTransactionIsolation() {
        return cachedDatabaseMetaData.getDefaultTransactionIsolation();
    }
    
    @Override
    public final boolean supportsTransactions() {
        return cachedDatabaseMetaData.isSupportsTransactions();
    }
    
    @Override
    public final boolean supportsDataDefinitionAndDataManipulationTransactions() {
        return cachedDatabaseMetaData.isSupportsDataDefinitionAndDataManipulationTransactions();
    }
    
    @Override
    public final boolean supportsDataManipulationTransactionsOnly() {
        return cachedDatabaseMetaData.isSupportsDataManipulationTransactionsOnly();
    }
    
    @Override
    public final boolean dataDefinitionCausesTransactionCommit() {
        return cachedDatabaseMetaData.isDataDefinitionCausesTransactionCommit();
    }
    
    @Override
    public final boolean dataDefinitionIgnoredInTransactions() {
        return cachedDatabaseMetaData.isDataDefinitionIgnoredInTransactions();
    }
    
    @Override
    public final boolean supportsBatchUpdates() {
        return cachedDatabaseMetaData.isSupportsBatchUpdates();
    }
    
    @Override
    public final boolean supportsSavepoints() {
        return cachedDatabaseMetaData.isSupportsSavepoints();
    }
    
    @Override
    public final boolean supportsNamedParameters() {
        return cachedDatabaseMetaData.isSupportsNamedParameters();
    }
    
    @Override
    public final boolean supportsMultipleOpenResults() {
        return cachedDatabaseMetaData.isSupportsMultipleOpenResults();
    }
    
    @Override
    public final boolean supportsGetGeneratedKeys() {
        return cachedDatabaseMetaData.isSupportsGetGeneratedKeys();
    }
    
    @Override
    public final int getResultSetHoldability() {
        return cachedDatabaseMetaData.getResultSetHoldability();
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public final int getSQLStateType() {
        return cachedDatabaseMetaData.getSqlStateType();
    }
    
    @Override
    public final boolean locatorsUpdateCopy() {
        return cachedDatabaseMetaData.isLocatorsUpdateCopy();
    }
    
    @Override
    public final boolean supportsStatementPooling() {
        return cachedDatabaseMetaData.isSupportsStatementPooling();
    }
    
    @Override
    public final boolean supportsStoredFunctionsUsingCallSyntax() {
        return cachedDatabaseMetaData.isSupportsStoredFunctionsUsingCallSyntax();
    }
    
    @Override
    public final boolean autoCommitFailureClosesAllResultSets() {
        return cachedDatabaseMetaData.isAutoCommitFailureClosesAllResultSets();
    }
    
    @Override
    public final RowIdLifetime getRowIdLifetime() {
        return cachedDatabaseMetaData.getRowIdLifetime();
    }
    
    @Override
    public final boolean generatedKeyAlwaysReturned() {
        return cachedDatabaseMetaData.isGeneratedKeyAlwaysReturned();
    }
    
    @Override
    public final boolean ownInsertsAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean ownUpdatesAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean ownDeletesAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean othersInsertsAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean othersUpdatesAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean othersDeletesAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean insertsAreDetected(final int type) {
        return true;
    }
    
    @Override
    public final boolean updatesAreDetected(final int type) {
        return true;
    }
    
    @Override
    public final boolean deletesAreDetected(final int type) {
        return true;
    }
    
    @Override
    public final boolean supportsResultSetType(final int type) {
        return true;
    }
    
    @Override
    public final boolean supportsResultSetConcurrency(final int type, final int concurrency) {
        return true;
    }
    
    @Override
    public final boolean supportsResultSetHoldability(final int holdability) {
        return true;
    }
    
    @Override
    public final boolean supportsTransactionIsolationLevel(final int level) {
        return true;
    }
}
