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

import java.sql.DatabaseMetaData;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

/**
 * Adapted database meta data.
 */
@RequiredArgsConstructor
public abstract class AdaptedDatabaseMetaData extends WrapperAdapter implements DatabaseMetaData {
    
    private final DatabaseMetaData databaseMetaData;
    
    @Override
    public final String getURL() throws SQLException {
        return databaseMetaData.getURL();
    }
    
    @Override
    public final String getUserName() throws SQLException {
        return databaseMetaData.getUserName();
    }
    
    @Override
    public final String getDatabaseProductName() throws SQLException {
        return databaseMetaData.getDatabaseProductName();
    }
    
    @Override
    public final String getDatabaseProductVersion() throws SQLException {
        return databaseMetaData.getDatabaseProductVersion();
    }
    
    @Override
    public final String getDriverName() throws SQLException {
        return databaseMetaData.getDriverName();
    }
    
    @Override
    public final String getDriverVersion() throws SQLException {
        return databaseMetaData.getDriverVersion();
    }
    
    @Override
    public final int getDriverMajorVersion() {
        return databaseMetaData.getDriverMajorVersion();
    }
    
    @Override
    public final int getDriverMinorVersion() {
        return databaseMetaData.getDriverMinorVersion();
    }
    
    @Override
    public final int getDatabaseMajorVersion() throws SQLException {
        return databaseMetaData.getDatabaseMajorVersion();
    }
    
    @Override
    public final int getDatabaseMinorVersion() throws SQLException {
        return databaseMetaData.getDatabaseMinorVersion();
    }
    
    @Override
    public final int getJDBCMajorVersion() throws SQLException {
        return databaseMetaData.getJDBCMajorVersion();
    }
    
    @Override
    public final int getJDBCMinorVersion() throws SQLException {
        return databaseMetaData.getJDBCMinorVersion();
    }
    
    @Override
    public final boolean isReadOnly() throws SQLException {
        return databaseMetaData.isReadOnly();
    }
    
    @Override
    public final boolean allProceduresAreCallable() throws SQLException {
        return databaseMetaData.allProceduresAreCallable();
    }
    
    @Override
    public final boolean allTablesAreSelectable() throws SQLException {
        return databaseMetaData.allTablesAreSelectable();
    }
    
    @Override
    public final boolean nullsAreSortedHigh() throws SQLException {
        return databaseMetaData.nullsAreSortedHigh();
    }
    
    @Override
    public final boolean nullsAreSortedLow() throws SQLException {
        return databaseMetaData.nullsAreSortedLow();
    }
    
    @Override
    public final boolean nullsAreSortedAtStart() throws SQLException {
        return databaseMetaData.nullsAreSortedAtStart();
    }
    
    @Override
    public final boolean nullsAreSortedAtEnd() throws SQLException {
        return databaseMetaData.nullsAreSortedAtEnd();
    }
    
    @Override
    public final boolean usesLocalFiles() throws SQLException {
        return databaseMetaData.usesLocalFiles();
    }
    
    @Override
    public final boolean usesLocalFilePerTable() throws SQLException {
        return databaseMetaData.usesLocalFilePerTable();
    }
    
    @Override
    public final boolean supportsMixedCaseIdentifiers() throws SQLException {
        return databaseMetaData.supportsMixedCaseIdentifiers();
    }
    
    @Override
    public final boolean storesUpperCaseIdentifiers() throws SQLException {
        return databaseMetaData.storesUpperCaseIdentifiers();
    }
    
    @Override
    public final boolean storesLowerCaseIdentifiers() throws SQLException {
        return databaseMetaData.storesLowerCaseIdentifiers();
    }
    
    @Override
    public final boolean storesMixedCaseIdentifiers() throws SQLException {
        return databaseMetaData.storesMixedCaseIdentifiers();
    }
    
    @Override
    public final boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return databaseMetaData.supportsMixedCaseQuotedIdentifiers();
    }
    
    @Override
    public final boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return databaseMetaData.storesUpperCaseQuotedIdentifiers();
    }
    
    @Override
    public final boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return databaseMetaData.storesLowerCaseQuotedIdentifiers();
    }
    
    @Override
    public final boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return databaseMetaData.storesMixedCaseQuotedIdentifiers();
    }
    
    @Override
    public final String getIdentifierQuoteString() throws SQLException {
        return databaseMetaData.getIdentifierQuoteString();
    }
    
    @Override
    public final String getSQLKeywords() throws SQLException {
        return databaseMetaData.getSQLKeywords();
    }
    
    @Override
    public final String getNumericFunctions() throws SQLException {
        return databaseMetaData.getNumericFunctions();
    }
    
    @Override
    public final String getStringFunctions() throws SQLException {
        return databaseMetaData.getStringFunctions();
    }
    
    @Override
    public final String getSystemFunctions() throws SQLException {
        return databaseMetaData.getSystemFunctions();
    }
    
    @Override
    public final String getTimeDateFunctions() throws SQLException {
        return databaseMetaData.getTimeDateFunctions();
    }
    
    @Override
    public final String getSearchStringEscape() throws SQLException {
        return databaseMetaData.getSearchStringEscape();
    }
    
    @Override
    public final String getExtraNameCharacters() throws SQLException {
        return databaseMetaData.getExtraNameCharacters();
    }
    
    @Override
    public final boolean supportsAlterTableWithAddColumn() throws SQLException {
        return databaseMetaData.supportsAlterTableWithAddColumn();
    }
    
    @Override
    public final boolean supportsAlterTableWithDropColumn() throws SQLException {
        return databaseMetaData.supportsAlterTableWithDropColumn();
    }
    
    @Override
    public final boolean supportsColumnAliasing() throws SQLException {
        return databaseMetaData.supportsColumnAliasing();
    }
    
    @Override
    public final boolean nullPlusNonNullIsNull() throws SQLException {
        return databaseMetaData.nullPlusNonNullIsNull();
    }
    
    @Override
    public final boolean supportsConvert() throws SQLException {
        return databaseMetaData.supportsConvert();
    }
    
    @Override
    public final boolean supportsConvert(final int fromType, final int toType) throws SQLException {
        return databaseMetaData.supportsConvert(fromType, toType);
    }
    
    @Override
    public final boolean supportsTableCorrelationNames() throws SQLException {
        return databaseMetaData.supportsTableCorrelationNames();
    }
    
    @Override
    public final boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return databaseMetaData.supportsDifferentTableCorrelationNames();
    }
    
    @Override
    public final boolean supportsExpressionsInOrderBy() throws SQLException {
        return databaseMetaData.supportsExpressionsInOrderBy();
    }
    
    @Override
    public final boolean supportsOrderByUnrelated() throws SQLException {
        return databaseMetaData.supportsOrderByUnrelated();
    }
    
    @Override
    public final boolean supportsGroupBy() throws SQLException {
        return databaseMetaData.supportsGroupBy();
    }
    
    @Override
    public final boolean supportsGroupByUnrelated() throws SQLException {
        return databaseMetaData.supportsGroupByUnrelated();
    }
    
    @Override
    public final boolean supportsGroupByBeyondSelect() throws SQLException {
        return databaseMetaData.supportsGroupByBeyondSelect();
    }
    
    @Override
    public final boolean supportsLikeEscapeClause() throws SQLException {
        return databaseMetaData.supportsLikeEscapeClause();
    }
    
    @Override
    public final boolean supportsMultipleResultSets() throws SQLException {
        return databaseMetaData.supportsMultipleResultSets();
    }
    
    @Override
    public final boolean supportsMultipleTransactions() throws SQLException {
        return databaseMetaData.supportsMultipleTransactions();
    }
    
    @Override
    public final boolean supportsNonNullableColumns() throws SQLException {
        return databaseMetaData.supportsNonNullableColumns();
    }
    
    @Override
    public final boolean supportsMinimumSQLGrammar() throws SQLException {
        return databaseMetaData.supportsMinimumSQLGrammar();
    }
    
    @Override
    public final boolean supportsCoreSQLGrammar() throws SQLException {
        return databaseMetaData.supportsCoreSQLGrammar();
    }
    
    @Override
    public final boolean supportsExtendedSQLGrammar() throws SQLException {
        return databaseMetaData.supportsExtendedSQLGrammar();
    }
    
    @Override
    public final boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return databaseMetaData.supportsANSI92EntryLevelSQL();
    }
    
    @Override
    public final boolean supportsANSI92IntermediateSQL() throws SQLException {
        return databaseMetaData.supportsANSI92IntermediateSQL();
    }
    
    @Override
    public final boolean supportsANSI92FullSQL() throws SQLException {
        return databaseMetaData.supportsANSI92FullSQL();
    }
    
    @Override
    public final boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return databaseMetaData.supportsIntegrityEnhancementFacility();
    }
    
    @Override
    public final boolean supportsOuterJoins() throws SQLException {
        return databaseMetaData.supportsOuterJoins();
    }
    
    @Override
    public final boolean supportsFullOuterJoins() throws SQLException {
        return databaseMetaData.supportsFullOuterJoins();
    }
    
    @Override
    public final boolean supportsLimitedOuterJoins() throws SQLException {
        return databaseMetaData.supportsLimitedOuterJoins();
    }
    
    @Override
    public final String getSchemaTerm() throws SQLException {
        return databaseMetaData.getSchemaTerm();
    }
    
    @Override
    public final String getProcedureTerm() throws SQLException {
        return databaseMetaData.getProcedureTerm();
    }
    
    @Override
    public final String getCatalogTerm() throws SQLException {
        return databaseMetaData.getCatalogTerm();
    }
    
    @Override
    public final boolean isCatalogAtStart() throws SQLException {
        return databaseMetaData.isCatalogAtStart();
    }
    
    @Override
    public final String getCatalogSeparator() throws SQLException {
        return databaseMetaData.getCatalogSeparator();
    }
    
    @Override
    public final boolean supportsSchemasInDataManipulation() throws SQLException {
        return databaseMetaData.supportsSchemasInDataManipulation();
    }
    
    @Override
    public final boolean supportsSchemasInProcedureCalls() throws SQLException {
        return databaseMetaData.supportsSchemasInProcedureCalls();
    }
    
    @Override
    public final boolean supportsSchemasInTableDefinitions() throws SQLException {
        return databaseMetaData.supportsSchemasInTableDefinitions();
    }
    
    @Override
    public final boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return databaseMetaData.supportsSchemasInIndexDefinitions();
    }
    
    @Override
    public final boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return databaseMetaData.supportsSchemasInPrivilegeDefinitions();
    }
    
    @Override
    public final boolean supportsCatalogsInDataManipulation() throws SQLException {
        return databaseMetaData.supportsCatalogsInDataManipulation();
    }
    
    @Override
    public final boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return databaseMetaData.supportsCatalogsInProcedureCalls();
    }
    
    @Override
    public final boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return databaseMetaData.supportsCatalogsInTableDefinitions();
    }
    
    @Override
    public final boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return databaseMetaData.supportsCatalogsInIndexDefinitions();
    }
    
    @Override
    public final boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return databaseMetaData.supportsCatalogsInPrivilegeDefinitions();
    }
    
    @Override
    public final boolean supportsPositionedDelete() throws SQLException {
        return databaseMetaData.supportsPositionedDelete();
    }
    
    @Override
    public final boolean supportsPositionedUpdate() throws SQLException {
        return databaseMetaData.supportsPositionedUpdate();
    }
    
    @Override
    public final boolean supportsSelectForUpdate() throws SQLException {
        return databaseMetaData.supportsSelectForUpdate();
    }
    
    @Override
    public final boolean supportsStoredProcedures() throws SQLException {
        return databaseMetaData.supportsStoredProcedures();
    }
    
    @Override
    public final boolean supportsSubqueriesInComparisons() throws SQLException {
        return databaseMetaData.supportsSubqueriesInComparisons();
    }
    
    @Override
    public final boolean supportsSubqueriesInExists() throws SQLException {
        return databaseMetaData.supportsSubqueriesInExists();
    }
    
    @Override
    public final boolean supportsSubqueriesInIns() throws SQLException {
        return databaseMetaData.supportsSubqueriesInIns();
    }
    
    @Override
    public final boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return databaseMetaData.supportsSubqueriesInQuantifieds();
    }
    
    @Override
    public final boolean supportsCorrelatedSubqueries() throws SQLException {
        return databaseMetaData.supportsCorrelatedSubqueries();
    }
    
    @Override
    public final boolean supportsUnion() throws SQLException {
        return databaseMetaData.supportsUnion();
    }
    
    @Override
    public final boolean supportsUnionAll() throws SQLException {
        return databaseMetaData.supportsUnionAll();
    }
    
    @Override
    public final boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return databaseMetaData.supportsOpenCursorsAcrossCommit();
    }
    
    @Override
    public final boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return databaseMetaData.supportsOpenCursorsAcrossRollback();
    }
    
    @Override
    public final boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return databaseMetaData.supportsOpenStatementsAcrossCommit();
    }
    
    @Override
    public final boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return databaseMetaData.supportsOpenStatementsAcrossRollback();
    }
    
    @Override
    public final int getMaxBinaryLiteralLength() throws SQLException {
        return databaseMetaData.getMaxBinaryLiteralLength();
    }
    
    @Override
    public final int getMaxCharLiteralLength() throws SQLException {
        return databaseMetaData.getMaxCharLiteralLength();
    }
    
    @Override
    public final int getMaxColumnNameLength() throws SQLException {
        return databaseMetaData.getMaxColumnNameLength();
    }
    
    @Override
    public final int getMaxColumnsInGroupBy() throws SQLException {
        return databaseMetaData.getMaxColumnsInGroupBy();
    }
    
    @Override
    public final int getMaxColumnsInIndex() throws SQLException {
        return databaseMetaData.getMaxColumnsInIndex();
    }
    
    @Override
    public final int getMaxColumnsInOrderBy() throws SQLException {
        return databaseMetaData.getMaxColumnsInOrderBy();
    }
    
    @Override
    public final int getMaxColumnsInSelect() throws SQLException {
        return databaseMetaData.getMaxColumnsInSelect();
    }
    
    @Override
    public final int getMaxColumnsInTable() throws SQLException {
        return databaseMetaData.getMaxColumnsInTable();
    }
    
    @Override
    public final int getMaxConnections() throws SQLException {
        return databaseMetaData.getMaxConnections();
    }
    
    @Override
    public final int getMaxCursorNameLength() throws SQLException {
        return databaseMetaData.getMaxCursorNameLength();
    }
    
    @Override
    public final int getMaxIndexLength() throws SQLException {
        return databaseMetaData.getMaxIndexLength();
    }
    
    @Override
    public final int getMaxSchemaNameLength() throws SQLException {
        return databaseMetaData.getMaxSchemaNameLength();
    }
    
    @Override
    public final int getMaxProcedureNameLength() throws SQLException {
        return databaseMetaData.getMaxProcedureNameLength();
    }
    
    @Override
    public final int getMaxCatalogNameLength() throws SQLException {
        return databaseMetaData.getMaxCatalogNameLength();
    }
    
    @Override
    public final int getMaxRowSize() throws SQLException {
        return databaseMetaData.getMaxRowSize();
    }
    
    @Override
    public final boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return databaseMetaData.doesMaxRowSizeIncludeBlobs();
    }
    
    @Override
    public final int getMaxStatementLength() throws SQLException {
        return databaseMetaData.getMaxStatementLength();
    }
    
    @Override
    public final int getMaxStatements() throws SQLException {
        return databaseMetaData.getMaxStatements();
    }
    
    @Override
    public final int getMaxTableNameLength() throws SQLException {
        return databaseMetaData.getMaxTableNameLength();
    }
    
    @Override
    public final int getMaxTablesInSelect() throws SQLException {
        return databaseMetaData.getMaxTablesInSelect();
    }
    
    @Override
    public final int getMaxUserNameLength() throws SQLException {
        return databaseMetaData.getMaxUserNameLength();
    }
    
    @Override
    public final int getDefaultTransactionIsolation() throws SQLException {
        return databaseMetaData.getDefaultTransactionIsolation();
    }
    
    @Override
    public final boolean supportsTransactions() throws SQLException {
        return databaseMetaData.supportsTransactions();
    }
    
    @Override
    public final boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return databaseMetaData.supportsDataDefinitionAndDataManipulationTransactions();
    }
    
    @Override
    public final boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return databaseMetaData.supportsDataManipulationTransactionsOnly();
    }
    
    @Override
    public final boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return databaseMetaData.dataDefinitionCausesTransactionCommit();
    }
    
    @Override
    public final boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return databaseMetaData.dataDefinitionIgnoredInTransactions();
    }
    
    @Override
    public final boolean supportsBatchUpdates() throws SQLException {
        return databaseMetaData.supportsBatchUpdates();
    }
    
    @Override
    public final boolean supportsSavepoints() throws SQLException {
        return databaseMetaData.supportsSavepoints();
    }
    
    @Override
    public final boolean supportsNamedParameters() throws SQLException {
        return databaseMetaData.supportsNamedParameters();
    }
    
    @Override
    public final boolean supportsMultipleOpenResults() throws SQLException {
        return databaseMetaData.supportsMultipleOpenResults();
    }
    
    @Override
    public final boolean supportsGetGeneratedKeys() throws SQLException {
        return databaseMetaData.supportsGetGeneratedKeys();
    }
    
    @Override
    public final int getResultSetHoldability() throws SQLException {
        return databaseMetaData.getResultSetHoldability();
    }
    
    @Override
    public final int getSQLStateType() throws SQLException {
        return databaseMetaData.getSQLStateType();
    }
    
    @Override
    public final boolean locatorsUpdateCopy() throws SQLException {
        return databaseMetaData.locatorsUpdateCopy();
    }
    
    @Override
    public final boolean supportsStatementPooling() throws SQLException {
        return databaseMetaData.supportsStatementPooling();
    }
    
    @Override
    public final boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return databaseMetaData.supportsStoredFunctionsUsingCallSyntax();
    }
    
    @Override
    public final boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return databaseMetaData.autoCommitFailureClosesAllResultSets();
    }
    
    @Override
    public final RowIdLifetime getRowIdLifetime() throws SQLException {
        return databaseMetaData.getRowIdLifetime();
    }
    
    @Override
    public final boolean generatedKeyAlwaysReturned() throws SQLException {
        return databaseMetaData.generatedKeyAlwaysReturned();
    }
    
    @Override
    public final boolean ownInsertsAreVisible(final int type) throws SQLException {
        return databaseMetaData.ownInsertsAreVisible(type);
    }
    
    @Override
    public final boolean ownUpdatesAreVisible(final int type) throws SQLException {
        return databaseMetaData.ownUpdatesAreVisible(type);
    }
    
    @Override
    public final boolean ownDeletesAreVisible(final int type) throws SQLException {
        return databaseMetaData.ownDeletesAreVisible(type);
    }
    
    @Override
    public final boolean othersInsertsAreVisible(final int type) throws SQLException {
        return databaseMetaData.othersInsertsAreVisible(type);
    }
    
    @Override
    public final boolean othersUpdatesAreVisible(final int type) throws SQLException {
        return databaseMetaData.othersUpdatesAreVisible(type);
    }
    
    @Override
    public final boolean othersDeletesAreVisible(final int type) throws SQLException {
        return databaseMetaData.othersDeletesAreVisible(type);
    }
    
    @Override
    public final boolean insertsAreDetected(final int type) throws SQLException {
        return databaseMetaData.insertsAreDetected(type);
    }
    
    @Override
    public final boolean updatesAreDetected(final int type) throws SQLException {
        return databaseMetaData.updatesAreDetected(type);
    }
    
    @Override
    public final boolean deletesAreDetected(final int type) throws SQLException {
        return databaseMetaData.deletesAreDetected(type);
    }
    
    @Override
    public final boolean supportsResultSetType(final int type) throws SQLException {
        return databaseMetaData.supportsResultSetType(type);
    }
    
    @Override
    public final boolean supportsResultSetConcurrency(final int type, final int concurrency) throws SQLException {
        return databaseMetaData.supportsResultSetConcurrency(type, concurrency);
    }
    
    @Override
    public final boolean supportsResultSetHoldability(final int holdability) throws SQLException {
        return databaseMetaData.supportsResultSetHoldability(holdability);
    }
    
    @Override
    public final boolean supportsTransactionIsolationLevel(final int level) throws SQLException {
        return databaseMetaData.supportsTransactionIsolationLevel(level);
    }
}
