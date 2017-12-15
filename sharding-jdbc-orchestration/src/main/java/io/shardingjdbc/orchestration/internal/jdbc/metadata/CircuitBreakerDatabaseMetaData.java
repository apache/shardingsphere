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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

/**
 * Circuit breaker metadata.
 *
 * @author caohao
 */
public final class CircuitBreakerDatabaseMetaData implements DatabaseMetaData {
    
    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }
    
    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return false;
    }
    
    @Override
    public String getURL() throws SQLException {
        return null;
    }
    
    @Override
    public String getUserName() throws SQLException {
        return null;
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }
    
    @Override
    public String getDatabaseProductName() throws SQLException {
        return "H2";
    }
    
    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return null;
    }
    
    @Override
    public String getDriverName() throws SQLException {
        return null;
    }
    
    @Override
    public String getDriverVersion() throws SQLException {
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
    public boolean usesLocalFiles() throws SQLException {
        return false;
    }
    
    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return null;
    }
    
    @Override
    public String getSQLKeywords() throws SQLException {
        return null;
    }
    
    @Override
    public String getNumericFunctions() throws SQLException {
        return null;
    }
    
    @Override
    public String getStringFunctions() throws SQLException {
        return null;
    }
    
    @Override
    public String getSystemFunctions() throws SQLException {
        return null;
    }
    
    @Override
    public String getTimeDateFunctions() throws SQLException {
        return null;
    }
    
    @Override
    public String getSearchStringEscape() throws SQLException {
        return null;
    }
    
    @Override
    public String getExtraNameCharacters() throws SQLException {
        return null;
    }
    
    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsConvert(final int fromType, final int toType) throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsGroupBy() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return false;
    }
    
    @Override
    public String getSchemaTerm() throws SQLException {
        return null;
    }
    
    @Override
    public String getProcedureTerm() throws SQLException {
        return null;
    }
    
    @Override
    public String getCatalogTerm() throws SQLException {
        return null;
    }
    
    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return false;
    }
    
    @Override
    public String getCatalogSeparator() throws SQLException {
        return null;
    }
    
    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsUnion() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsUnionAll() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }
    
    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxConnections() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxIndexLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxRowSize() throws SQLException {
        return 0;
    }
    
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return false;
    }
    
    @Override
    public int getMaxStatementLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxStatements() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return 0;
    }
    
    @Override
    public boolean supportsTransactions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsTransactionIsolationLevel(final int level) throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }
    
    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    }
    
    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getSchemas() throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getCatalogs() throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getTableTypes() throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getCrossReference(final String parentCatalog, final String parentSchema, final String parentTable, final String foreignCatalog, 
                                       final String foreignSchema, final String foreignTable) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        return null;
    }
    
    @Override
    public boolean supportsResultSetType(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsResultSetConcurrency(final int type, final int concurrency) throws SQLException {
        return false;
    }
    
    @Override
    public boolean ownUpdatesAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean ownDeletesAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean ownInsertsAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean othersUpdatesAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean othersDeletesAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean othersInsertsAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean updatesAreDetected(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean deletesAreDetected(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean insertsAreDetected(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        return null;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }
    
    @Override
    public boolean supportsSavepoints() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public boolean supportsResultSetHoldability(final int holdability) throws SQLException {
        return false;
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }
    
    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return 0;
    }
    
    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return 0;
    }
    
    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 0;
    }
    
    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }
    
    @Override
    public int getSQLStateType() throws SQLException {
        return DatabaseMetaData.sqlStateSQL;
    }
    
    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }
    
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return null;
    }
    
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }
    
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return null;
    }
    
    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
}
