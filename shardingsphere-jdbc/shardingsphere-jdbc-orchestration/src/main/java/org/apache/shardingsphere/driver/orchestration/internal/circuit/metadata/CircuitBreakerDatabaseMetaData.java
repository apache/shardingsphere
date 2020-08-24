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

package org.apache.shardingsphere.driver.orchestration.internal.circuit.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;

/**
 * Circuit breaker metadata.
 */
public final class CircuitBreakerDatabaseMetaData implements DatabaseMetaData {
    
    @Override
    public boolean allProceduresAreCallable() {
        return false;
    }
    
    @Override
    public boolean allTablesAreSelectable() {
        return false;
    }
    
    @Override
    public String getURL() {
        return null;
    }
    
    @Override
    public String getUserName() {
        return null;
    }
    
    @Override
    public boolean isReadOnly() {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedHigh() {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedLow() {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtStart() {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtEnd() {
        return false;
    }
    
    @Override
    public String getDatabaseProductName() {
        return "H2";
    }
    
    @Override
    public String getDatabaseProductVersion() {
        return null;
    }
    
    @Override
    public String getDriverName() {
        return null;
    }
    
    @Override
    public String getDriverVersion() {
        return null;
    }
    
    @Override
    public int getDriverMajorVersion() {
        return 0;
    }
    
    @Override
    public int getDriverMinorVersion() {
        return 0;
    }
    
    @Override
    public boolean usesLocalFiles() {
        return false;
    }
    
    @Override
    public boolean usesLocalFilePerTable() {
        return false;
    }
    
    @Override
    public boolean supportsMixedCaseIdentifiers() {
        return false;
    }
    
    @Override
    public boolean storesUpperCaseIdentifiers() {
        return false;
    }
    
    @Override
    public boolean storesLowerCaseIdentifiers() {
        return false;
    }
    
    @Override
    public boolean storesMixedCaseIdentifiers() {
        return false;
    }
    
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() {
        return false;
    }
    
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() {
        return false;
    }
    
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() {
        return false;
    }
    
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() {
        return false;
    }
    
    @Override
    public String getIdentifierQuoteString() {
        return null;
    }
    
    @Override
    public String getSQLKeywords() {
        return null;
    }
    
    @Override
    public String getNumericFunctions() {
        return null;
    }
    
    @Override
    public String getStringFunctions() {
        return null;
    }
    
    @Override
    public String getSystemFunctions() {
        return null;
    }
    
    @Override
    public String getTimeDateFunctions() {
        return null;
    }
    
    @Override
    public String getSearchStringEscape() {
        return null;
    }
    
    @Override
    public String getExtraNameCharacters() {
        return null;
    }
    
    @Override
    public boolean supportsAlterTableWithAddColumn() {
        return false;
    }
    
    @Override
    public boolean supportsAlterTableWithDropColumn() {
        return false;
    }
    
    @Override
    public boolean supportsColumnAliasing() {
        return false;
    }
    
    @Override
    public boolean nullPlusNonNullIsNull() {
        return false;
    }
    
    @Override
    public boolean supportsConvert() {
        return false;
    }
    
    @Override
    public boolean supportsConvert(final int fromType, final int toType) {
        return false;
    }
    
    @Override
    public boolean supportsTableCorrelationNames() {
        return false;
    }
    
    @Override
    public boolean supportsDifferentTableCorrelationNames() {
        return false;
    }
    
    @Override
    public boolean supportsExpressionsInOrderBy() {
        return false;
    }
    
    @Override
    public boolean supportsOrderByUnrelated() {
        return false;
    }
    
    @Override
    public boolean supportsGroupBy() {
        return false;
    }
    
    @Override
    public boolean supportsGroupByUnrelated() {
        return false;
    }
    
    @Override
    public boolean supportsGroupByBeyondSelect() {
        return false;
    }
    
    @Override
    public boolean supportsLikeEscapeClause() {
        return false;
    }
    
    @Override
    public boolean supportsMultipleResultSets() {
        return false;
    }
    
    @Override
    public boolean supportsMultipleTransactions() {
        return false;
    }
    
    @Override
    public boolean supportsNonNullableColumns() {
        return false;
    }
    
    @Override
    public boolean supportsMinimumSQLGrammar() {
        return false;
    }
    
    @Override
    public boolean supportsCoreSQLGrammar() {
        return false;
    }
    
    @Override
    public boolean supportsExtendedSQLGrammar() {
        return false;
    }
    
    @Override
    public boolean supportsANSI92EntryLevelSQL() {
        return false;
    }
    
    @Override
    public boolean supportsANSI92IntermediateSQL() {
        return false;
    }
    
    @Override
    public boolean supportsANSI92FullSQL() {
        return false;
    }
    
    @Override
    public boolean supportsIntegrityEnhancementFacility() {
        return false;
    }
    
    @Override
    public boolean supportsOuterJoins() {
        return false;
    }
    
    @Override
    public boolean supportsFullOuterJoins() {
        return false;
    }
    
    @Override
    public boolean supportsLimitedOuterJoins() {
        return false;
    }
    
    @Override
    public String getSchemaTerm() {
        return null;
    }
    
    @Override
    public String getProcedureTerm() {
        return null;
    }
    
    @Override
    public String getCatalogTerm() {
        return null;
    }
    
    @Override
    public boolean isCatalogAtStart() {
        return false;
    }
    
    @Override
    public String getCatalogSeparator() {
        return null;
    }
    
    @Override
    public boolean supportsSchemasInDataManipulation() {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInProcedureCalls() {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInTableDefinitions() {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInIndexDefinitions() {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInDataManipulation() {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInProcedureCalls() {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInIndexDefinitions() {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() {
        return false;
    }
    
    @Override
    public boolean supportsPositionedDelete() {
        return false;
    }
    
    @Override
    public boolean supportsPositionedUpdate() {
        return false;
    }
    
    @Override
    public boolean supportsSelectForUpdate() {
        return false;
    }
    
    @Override
    public boolean supportsStoredProcedures() {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInComparisons() {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInExists() {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInIns() {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInQuantifieds() {
        return false;
    }
    
    @Override
    public boolean supportsCorrelatedSubqueries() {
        return false;
    }
    
    @Override
    public boolean supportsUnion() {
        return false;
    }
    
    @Override
    public boolean supportsUnionAll() {
        return false;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        return false;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        return false;
    }
    
    @Override
    public int getMaxBinaryLiteralLength() {
        return 0;
    }
    
    @Override
    public int getMaxCharLiteralLength() {
        return 0;
    }
    
    @Override
    public int getMaxColumnNameLength() {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInGroupBy() {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInIndex() {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInOrderBy() {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInSelect() {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInTable() {
        return 0;
    }
    
    @Override
    public int getMaxConnections() {
        return 0;
    }
    
    @Override
    public int getMaxCursorNameLength() {
        return 0;
    }
    
    @Override
    public int getMaxIndexLength() {
        return 0;
    }
    
    @Override
    public int getMaxSchemaNameLength() {
        return 0;
    }
    
    @Override
    public int getMaxProcedureNameLength() {
        return 0;
    }
    
    @Override
    public int getMaxCatalogNameLength() {
        return 0;
    }
    
    @Override
    public int getMaxRowSize() {
        return 0;
    }
    
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
        return false;
    }
    
    @Override
    public int getMaxStatementLength() {
        return 0;
    }
    
    @Override
    public int getMaxStatements() {
        return 0;
    }
    
    @Override
    public int getMaxTableNameLength() {
        return 0;
    }
    
    @Override
    public int getMaxTablesInSelect() {
        return 0;
    }
    
    @Override
    public int getMaxUserNameLength() {
        return 0;
    }
    
    @Override
    public int getDefaultTransactionIsolation() {
        return 0;
    }
    
    @Override
    public boolean supportsTransactions() {
        return false;
    }
    
    @Override
    public boolean supportsTransactionIsolationLevel(final int level) {
        return false;
    }
    
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        return false;
    }
    
    @Override
    public boolean supportsDataManipulationTransactionsOnly() {
        return false;
    }
    
    @Override
    public boolean dataDefinitionCausesTransactionCommit() {
        return false;
    }
    
    @Override
    public boolean dataDefinitionIgnoredInTransactions() {
        return false;
    }
    
    @Override
    public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) {
        return null;
    }
    
    @Override
    public ResultSet getSchemas() {
        return null;
    }
    
    @Override
    public ResultSet getSchemas(final String catalog, final String schemaPattern) {
        return null;
    }
    
    @Override
    public ResultSet getCatalogs() {
        return null;
    }
    
    @Override
    public ResultSet getTableTypes() {
        return null;
    }
    
    @Override
    public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) {
        return null;
    }
    
    @Override
    public ResultSet getVersionColumns(final String catalog, final String schema, final String table) {
        return null;
    }
    
    @Override
    public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) {
        return null;
    }
    
    @Override
    public ResultSet getImportedKeys(final String catalog, final String schema, final String table) {
        return null;
    }
    
    @Override
    public ResultSet getExportedKeys(final String catalog, final String schema, final String table) {
        return null;
    }
    
    @Override
    public ResultSet getCrossReference(final String parentCatalog, final String parentSchema, final String parentTable, final String foreignCatalog, 
                                       final String foreignSchema, final String foreignTable) {
        return null;
    }
    
    @Override
    public ResultSet getTypeInfo() {
        return null;
    }
    
    @Override
    public ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) {
        return null;
    }
    
    @Override
    public boolean supportsResultSetType(final int type) {
        return false;
    }
    
    @Override
    public boolean supportsResultSetConcurrency(final int type, final int concurrency) {
        return false;
    }
    
    @Override
    public boolean ownUpdatesAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean ownDeletesAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean ownInsertsAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean othersUpdatesAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean othersDeletesAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean othersInsertsAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean updatesAreDetected(final int type) {
        return false;
    }
    
    @Override
    public boolean deletesAreDetected(final int type) {
        return false;
    }
    
    @Override
    public boolean insertsAreDetected(final int type) {
        return false;
    }
    
    @Override
    public boolean supportsBatchUpdates() {
        return false;
    }
    
    @Override
    public ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) {
        return null;
    }
    
    @Override
    public Connection getConnection() {
        return null;
    }
    
    @Override
    public boolean supportsSavepoints() {
        return false;
    }
    
    @Override
    public boolean supportsNamedParameters() {
        return false;
    }
    
    @Override
    public boolean supportsMultipleOpenResults() {
        return false;
    }
    
    @Override
    public boolean supportsGetGeneratedKeys() {
        return false;
    }
    
    @Override
    public ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) {
        return null;
    }
    
    @Override
    public boolean supportsResultSetHoldability(final int holdability) {
        return false;
    }
    
    @Override
    public int getResultSetHoldability() {
        return 0;
    }
    
    @Override
    public int getDatabaseMajorVersion() {
        return 0;
    }
    
    @Override
    public int getDatabaseMinorVersion() {
        return 0;
    }
    
    @Override
    public int getJDBCMajorVersion() {
        return 0;
    }
    
    @Override
    public int getJDBCMinorVersion() {
        return 0;
    }
    
    @Override
    public int getSQLStateType() {
        return DatabaseMetaData.sqlStateSQL;
    }
    
    @Override
    public boolean locatorsUpdateCopy() {
        return false;
    }
    
    @Override
    public boolean supportsStatementPooling() {
        return false;
    }
    
    @Override
    public RowIdLifetime getRowIdLifetime() {
        return null;
    }
    
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() {
        return false;
    }
    
    @Override
    public boolean autoCommitFailureClosesAllResultSets() {
        return false;
    }
    
    @Override
    public ResultSet getClientInfoProperties() {
        return null;
    }
    
    @Override
    public ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) {
        return null;
    }
    
    @Override
    public ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) {
        return null;
    }
    
    @Override
    public boolean generatedKeyAlwaysReturned() {
        return false;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
}
