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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingDataSourceNames;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.DatabaseMetaDataResultSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CachedDatabaseMetaDataTest {
    
    private static final String DATA_SOURCE_NAME = "ds";
    
    private static final String TABLE_NAME = "table";
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet resultSet;
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
    
    private CachedDatabaseMetaData cachedDatabaseMetaData;
    
    @Before
    public void setUp() throws SQLException {
        dataSourceMap.put(DATA_SOURCE_NAME, dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(resultSet.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
        cachedDatabaseMetaData = new CachedDatabaseMetaData(databaseMetaData, dataSourceMap, mockShardingRule());
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        ShardingDataSourceNames shardingDataSourceNames = mock(ShardingDataSourceNames.class);
        when(shardingDataSourceNames.getRandomDataSourceName()).thenReturn(DATA_SOURCE_NAME);
        when(shardingDataSourceNames.getRawMasterDataSourceName(DATA_SOURCE_NAME)).thenReturn(DATA_SOURCE_NAME);
        when(result.getShardingDataSourceNames()).thenReturn(shardingDataSourceNames);
        when(result.findTableRule(TABLE_NAME)).thenReturn(Optional.of(new TableRule(DATA_SOURCE_NAME, TABLE_NAME)));
        when(result.getDataNode(TABLE_NAME)).thenReturn(new DataNode(DATA_SOURCE_NAME, TABLE_NAME));
        return result;
    }
    
    @Test
    public void assertGetURL() throws SQLException {
        assertThat(cachedDatabaseMetaData.getURL(), is(databaseMetaData.getURL()));
    }
    
    @Test
    public void assertGetUserName() throws SQLException {
        assertThat(cachedDatabaseMetaData.getUserName(), is(databaseMetaData.getUserName()));
    }
    
    @Test
    public void assertGetDatabaseProductName() throws SQLException {
        assertThat(cachedDatabaseMetaData.getDatabaseProductName(), is(databaseMetaData.getDatabaseProductName()));
    }
    
    @Test
    public void assertGetDatabaseProductVersion() throws SQLException {
        assertThat(cachedDatabaseMetaData.getDatabaseProductVersion(), is(databaseMetaData.getDatabaseProductVersion()));
    }
    
    @Test
    public void assertGetDriverName() throws SQLException {
        assertThat(cachedDatabaseMetaData.getDriverName(), is(databaseMetaData.getDriverName()));
    }
    
    @Test
    public void assertGetDriverVersion() throws SQLException {
        assertThat(cachedDatabaseMetaData.getDriverVersion(), is(databaseMetaData.getDriverVersion()));
    }
    
    @Test
    public void assertGetDriverMajorVersion() {
        assertThat(cachedDatabaseMetaData.getDriverMajorVersion(), is(databaseMetaData.getDriverMajorVersion()));
    }
    
    @Test
    public void assertGetDriverMinorVersion() {
        assertThat(cachedDatabaseMetaData.getDriverMinorVersion(), is(databaseMetaData.getDriverMinorVersion()));
    }
    
    @Test
    public void assertGetDatabaseMajorVersion() throws SQLException {
        assertThat(cachedDatabaseMetaData.getDatabaseMajorVersion(), is(databaseMetaData.getDatabaseMajorVersion()));
    }
    
    @Test
    public void assertGetDatabaseMinorVersion() throws SQLException {
        assertThat(cachedDatabaseMetaData.getDatabaseMinorVersion(), is(databaseMetaData.getDatabaseMinorVersion()));
    }
    
    @Test
    public void assertGetJDBCMajorVersion() throws SQLException {
        assertThat(cachedDatabaseMetaData.getJDBCMajorVersion(), is(databaseMetaData.getJDBCMajorVersion()));
    }
    
    @Test
    public void assertGetJDBCMinorVersion() throws SQLException {
        assertThat(cachedDatabaseMetaData.getJDBCMinorVersion(), is(databaseMetaData.getJDBCMinorVersion()));
    }
    
    @Test
    public void assertAssertIsReadOnly() throws SQLException {
        assertThat(cachedDatabaseMetaData.isReadOnly(), is(databaseMetaData.isReadOnly()));
    }
    
    @Test
    public void assertAllProceduresAreCallable() throws SQLException {
        assertThat(cachedDatabaseMetaData.allProceduresAreCallable(), is(databaseMetaData.allProceduresAreCallable()));
    }
    
    @Test
    public void assertAllTablesAreSelectable() throws SQLException {
        assertThat(cachedDatabaseMetaData.allTablesAreSelectable(), is(databaseMetaData.allTablesAreSelectable()));
    }
    
    @Test
    public void assertNullsAreSortedHigh() throws SQLException {
        assertThat(cachedDatabaseMetaData.nullsAreSortedHigh(), is(databaseMetaData.nullsAreSortedHigh()));
    }
    
    @Test
    public void assertNullsAreSortedLow() throws SQLException {
        assertThat(cachedDatabaseMetaData.nullsAreSortedLow(), is(databaseMetaData.nullsAreSortedLow()));
    }
    
    @Test
    public void assertNullsAreSortedAtStart() throws SQLException {
        assertThat(cachedDatabaseMetaData.nullsAreSortedAtStart(), is(databaseMetaData.nullsAreSortedAtStart()));
    }
    
    @Test
    public void assertNullsAreSortedAtEnd() throws SQLException {
        assertThat(cachedDatabaseMetaData.nullsAreSortedAtEnd(), is(databaseMetaData.nullsAreSortedAtEnd()));
    }
    
    @Test
    public void assertUsesLocalFiles() throws SQLException {
        assertThat(cachedDatabaseMetaData.usesLocalFiles(), is(databaseMetaData.usesLocalFiles()));
    }
    
    @Test
    public void assertUsesLocalFilePerTable() throws SQLException {
        assertThat(cachedDatabaseMetaData.usesLocalFilePerTable(), is(databaseMetaData.usesLocalFilePerTable()));
    }
    
    @Test
    public void assertSupportsMixedCaseIdentifiers() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsMixedCaseIdentifiers(), is(databaseMetaData.supportsMixedCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresUpperCaseIdentifiers() throws SQLException {
        assertThat(cachedDatabaseMetaData.storesUpperCaseIdentifiers(), is(databaseMetaData.storesUpperCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresLowerCaseIdentifiers() throws SQLException {
        assertThat(cachedDatabaseMetaData.storesLowerCaseIdentifiers(), is(databaseMetaData.storesLowerCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresMixedCaseIdentifiers() throws SQLException {
        assertThat(cachedDatabaseMetaData.storesMixedCaseIdentifiers(), is(databaseMetaData.storesMixedCaseIdentifiers()));
    }
    
    @Test
    public void assertSupportsMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsMixedCaseQuotedIdentifiers(), is(databaseMetaData.supportsMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresUpperCaseQuotedIdentifiers() throws SQLException {
        assertThat(cachedDatabaseMetaData.storesUpperCaseQuotedIdentifiers(), is(databaseMetaData.storesUpperCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresLowerCaseQuotedIdentifiers() throws SQLException {
        assertThat(cachedDatabaseMetaData.storesLowerCaseQuotedIdentifiers(), is(databaseMetaData.storesLowerCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(cachedDatabaseMetaData.storesMixedCaseQuotedIdentifiers(), is(databaseMetaData.storesMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertGetIdentifierQuoteString() throws SQLException {
        assertThat(cachedDatabaseMetaData.getIdentifierQuoteString(), is(databaseMetaData.getIdentifierQuoteString()));
    }
    
    @Test
    public void assertGetSQLKeywords() throws SQLException {
        assertThat(cachedDatabaseMetaData.getSQLKeywords(), is(databaseMetaData.getSQLKeywords()));
    }
    
    @Test
    public void assertGetNumericFunctions() throws SQLException {
        assertThat(cachedDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    public void assertGetStringFunctions() throws SQLException {
        assertThat(cachedDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    public void assertGetSystemFunctions() throws SQLException {
        assertThat(cachedDatabaseMetaData.getSystemFunctions(), is(databaseMetaData.getSystemFunctions()));
    }
    
    @Test
    public void assertGetTimeDateFunctions() throws SQLException {
        assertThat(cachedDatabaseMetaData.getTimeDateFunctions(), is(databaseMetaData.getTimeDateFunctions()));
    }
    
    @Test
    public void assertGetSearchStringEscape() throws SQLException {
        assertThat(cachedDatabaseMetaData.getSearchStringEscape(), is(databaseMetaData.getSearchStringEscape()));
    }
    
    @Test
    public void assertGetExtraNameCharacters() throws SQLException {
        assertThat(cachedDatabaseMetaData.getExtraNameCharacters(), is(databaseMetaData.getExtraNameCharacters()));
    }
    
    @Test
    public void assertSupportsAlterTableWithAddColumn() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsAlterTableWithAddColumn(), is(databaseMetaData.supportsAlterTableWithAddColumn()));
    }
    
    @Test
    public void assertSupportsAlterTableWithDropColumn() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsAlterTableWithDropColumn(), is(databaseMetaData.supportsAlterTableWithDropColumn()));
    }
    
    @Test
    public void assertSupportsColumnAliasing() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsColumnAliasing(), is(databaseMetaData.supportsColumnAliasing()));
    }
    
    @Test
    public void assertNullPlusNonNullIsNull() throws SQLException {
        assertThat(cachedDatabaseMetaData.nullPlusNonNullIsNull(), is(databaseMetaData.nullPlusNonNullIsNull()));
    }
    
    @Test
    public void assertSupportsConvert() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsConvert(), is(databaseMetaData.supportsConvert()));
        assertThat(cachedDatabaseMetaData.supportsConvert(Types.INTEGER, Types.FLOAT), is(databaseMetaData.supportsConvert()));
    }
    
    @Test
    public void assertSupportsTableCorrelationNames() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsTableCorrelationNames(), is(databaseMetaData.supportsTableCorrelationNames()));
    }
    
    @Test
    public void assertSupportsDifferentTableCorrelationNames() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsDifferentTableCorrelationNames(), is(databaseMetaData.supportsDifferentTableCorrelationNames()));
    }
    
    @Test
    public void assertSupportsExpressionsInOrderBy() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsExpressionsInOrderBy(), is(databaseMetaData.supportsExpressionsInOrderBy()));
    }
    
    @Test
    public void assertSupportsOrderByUnrelated() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsOrderByUnrelated(), is(databaseMetaData.supportsOrderByUnrelated()));
    }
    
    @Test
    public void assertSupportsGroupBy() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsGroupBy(), is(databaseMetaData.supportsGroupBy()));
    }
    
    @Test
    public void assertSupportsGroupByUnrelated() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsGroupByUnrelated(), is(databaseMetaData.supportsGroupByUnrelated()));
    }
    
    @Test
    public void assertSupportsGroupByBeyondSelect() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsGroupByBeyondSelect(), is(databaseMetaData.supportsGroupByBeyondSelect()));
    }
    
    @Test
    public void assertSupportsLikeEscapeClause() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsLikeEscapeClause(), is(databaseMetaData.supportsLikeEscapeClause()));
    }
    
    @Test
    public void assertSupportsMultipleResultSets() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsMultipleResultSets(), is(databaseMetaData.supportsMultipleResultSets()));
    }
    
    @Test
    public void assertSupportsMultipleTransactions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsMultipleTransactions(), is(databaseMetaData.supportsMultipleTransactions()));
    }
    
    @Test
    public void assertSupportsNonNullableColumns() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsNonNullableColumns(), is(databaseMetaData.supportsNonNullableColumns()));
    }
    
    @Test
    public void assertSupportsMinimumSQLGrammar() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsMinimumSQLGrammar(), is(databaseMetaData.supportsMinimumSQLGrammar()));
    }
    
    @Test
    public void assertSupportsCoreSQLGrammar() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsCoreSQLGrammar(), is(databaseMetaData.supportsCoreSQLGrammar()));
    }
    
    @Test
    public void assertSupportsExtendedSQLGrammar() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsExtendedSQLGrammar(), is(databaseMetaData.supportsExtendedSQLGrammar()));
    }
    
    @Test
    public void assertSupportsANSI92EntryLevelSQL() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsANSI92EntryLevelSQL(), is(databaseMetaData.supportsANSI92EntryLevelSQL()));
    }
    
    @Test
    public void assertSupportsANSI92IntermediateSQL() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsANSI92IntermediateSQL(), is(databaseMetaData.supportsANSI92IntermediateSQL()));
    }
    
    @Test
    public void assertSupportsANSI92FullSQL() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsANSI92FullSQL(), is(databaseMetaData.supportsANSI92FullSQL()));
    }
    
    @Test
    public void assertSupportsIntegrityEnhancementFacility() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsIntegrityEnhancementFacility(), is(databaseMetaData.supportsIntegrityEnhancementFacility()));
    }
    
    @Test
    public void assertSupportsOuterJoins() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsOuterJoins(), is(databaseMetaData.supportsOuterJoins()));
    }
    
    @Test
    public void assertSupportsFullOuterJoins() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsFullOuterJoins(), is(databaseMetaData.supportsFullOuterJoins()));
    }
    
    @Test
    public void assertSupportsLimitedOuterJoins() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsLimitedOuterJoins(), is(databaseMetaData.supportsLimitedOuterJoins()));
    }
    
    @Test
    public void assertGetSchemaTerm() throws SQLException {
        assertThat(cachedDatabaseMetaData.getSchemaTerm(), is(databaseMetaData.getSchemaTerm()));
    }
    
    @Test
    public void assertGetProcedureTerm() throws SQLException {
        assertThat(cachedDatabaseMetaData.getProcedureTerm(), is(databaseMetaData.getProcedureTerm()));
    }
    
    @Test
    public void assertGetCatalogTerm() throws SQLException {
        assertThat(cachedDatabaseMetaData.getCatalogTerm(), is(databaseMetaData.getCatalogTerm()));
    }
    
    @Test
    public void assertAssertIsCatalogAtStart() throws SQLException {
        assertThat(cachedDatabaseMetaData.isCatalogAtStart(), is(databaseMetaData.isCatalogAtStart()));
    }
    
    @Test
    public void assertGetCatalogSeparator() throws SQLException {
        assertThat(cachedDatabaseMetaData.getCatalogSeparator(), is(databaseMetaData.getCatalogSeparator()));
    }
    
    @Test
    public void assertSupportsSchemasInDataManipulation() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSchemasInDataManipulation(), is(databaseMetaData.supportsSchemasInDataManipulation()));
    }
    
    @Test
    public void assertSupportsSchemasInProcedureCalls() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSchemasInProcedureCalls(), is(databaseMetaData.supportsSchemasInProcedureCalls()));
    }
    
    @Test
    public void assertSupportsSchemasInTableDefinitions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSchemasInTableDefinitions(), is(databaseMetaData.supportsSchemasInTableDefinitions()));
    }
    
    @Test
    public void assertSupportsSchemasInIndexDefinitions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSchemasInIndexDefinitions(), is(databaseMetaData.supportsSchemasInIndexDefinitions()));
    }
    
    @Test
    public void assertSupportsSchemasInPrivilegeDefinitions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSchemasInPrivilegeDefinitions(), is(databaseMetaData.supportsSchemasInPrivilegeDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInDataManipulation() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsCatalogsInDataManipulation(), is(databaseMetaData.supportsCatalogsInDataManipulation()));
    }
    
    @Test
    public void assertSupportsCatalogsInProcedureCalls() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsCatalogsInProcedureCalls(), is(databaseMetaData.supportsCatalogsInProcedureCalls()));
    }
    
    @Test
    public void assertSupportsCatalogsInTableDefinitions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsCatalogsInTableDefinitions(), is(databaseMetaData.supportsCatalogsInTableDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInIndexDefinitions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsCatalogsInIndexDefinitions(), is(databaseMetaData.supportsCatalogsInIndexDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInPrivilegeDefinitions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsCatalogsInPrivilegeDefinitions(), is(databaseMetaData.supportsCatalogsInPrivilegeDefinitions()));
    }
    
    @Test
    public void assertSupportsPositionedDelete() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsPositionedDelete(), is(databaseMetaData.supportsPositionedDelete()));
    }
    
    @Test
    public void assertSupportsPositionedUpdate() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsPositionedUpdate(), is(databaseMetaData.supportsPositionedUpdate()));
    }
    
    @Test
    public void assertSupportsSelectForUpdate() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSelectForUpdate(), is(databaseMetaData.supportsSelectForUpdate()));
    }
    
    @Test
    public void assertSupportsStoredProcedures() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsStoredProcedures(), is(databaseMetaData.supportsStoredProcedures()));
    }
    
    @Test
    public void assertSupportsSubqueriesInComparisons() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSubqueriesInComparisons(), is(databaseMetaData.supportsSubqueriesInComparisons()));
    }
    
    @Test
    public void assertSupportsSubqueriesInExists() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSubqueriesInExists(), is(databaseMetaData.supportsSubqueriesInExists()));
    }
    
    @Test
    public void assertSupportsSubqueriesInIns() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSubqueriesInIns(), is(databaseMetaData.supportsSubqueriesInIns()));
    }
    
    @Test
    public void assertSupportsSubqueriesInQuantifieds() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSubqueriesInQuantifieds(), is(databaseMetaData.supportsSubqueriesInQuantifieds()));
    }
    
    @Test
    public void assertSupportsCorrelatedSubqueries() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsCorrelatedSubqueries(), is(databaseMetaData.supportsCorrelatedSubqueries()));
    }
    
    @Test
    public void assertSupportsUnion() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsUnion(), is(databaseMetaData.supportsUnion()));
    }
    
    @Test
    public void assertSupportsUnionAll() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsUnionAll(), is(databaseMetaData.supportsUnionAll()));
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossCommit() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsOpenCursorsAcrossCommit(), is(databaseMetaData.supportsOpenCursorsAcrossCommit()));
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossRollback() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsOpenCursorsAcrossRollback(), is(databaseMetaData.supportsOpenCursorsAcrossRollback()));
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossCommit() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsOpenStatementsAcrossCommit(), is(databaseMetaData.supportsOpenStatementsAcrossCommit()));
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossRollback() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsOpenStatementsAcrossRollback(), is(databaseMetaData.supportsOpenStatementsAcrossRollback()));
    }
    
    @Test
    public void assertGetMaxBinaryLiteralLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxBinaryLiteralLength(), is(databaseMetaData.getMaxBinaryLiteralLength()));
    }
    
    @Test
    public void assertGetMaxCharLiteralLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxCharLiteralLength(), is(databaseMetaData.getMaxCharLiteralLength()));
    }
    
    @Test
    public void assertGetMaxColumnNameLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxColumnNameLength(), is(databaseMetaData.getMaxColumnNameLength()));
    }
    
    @Test
    public void assertGetMaxColumnsInGroupBy() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxColumnsInGroupBy(), is(databaseMetaData.getMaxColumnsInGroupBy()));
    }
    
    @Test
    public void assertGetMaxColumnsInIndex() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxColumnsInIndex(), is(databaseMetaData.getMaxColumnsInIndex()));
    }
    
    @Test
    public void assertGetMaxColumnsInOrderBy() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxColumnsInOrderBy(), is(databaseMetaData.getMaxColumnsInOrderBy()));
    }
    
    @Test
    public void assertGetMaxColumnsInSelect() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxColumnsInSelect(), is(databaseMetaData.getMaxColumnsInSelect()));
    }
    
    @Test
    public void assertGetMaxColumnsInTable() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxColumnsInTable(), is(databaseMetaData.getMaxColumnsInTable()));
    }
    
    @Test
    public void assertGetMaxConnections() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxConnections(), is(databaseMetaData.getMaxConnections()));
    }
    
    @Test
    public void assertGetMaxCursorNameLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxCursorNameLength(), is(databaseMetaData.getMaxCursorNameLength()));
    }
    
    @Test
    public void assertGetMaxIndexLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxIndexLength(), is(databaseMetaData.getMaxIndexLength()));
    }
    
    @Test
    public void assertGetMaxSchemaNameLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxSchemaNameLength(), is(databaseMetaData.getMaxSchemaNameLength()));
    }
    
    @Test
    public void assertGetMaxProcedureNameLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxProcedureNameLength(), is(databaseMetaData.getMaxProcedureNameLength()));
    }
    
    @Test
    public void assertGetMaxCatalogNameLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxCatalogNameLength(), is(databaseMetaData.getMaxCatalogNameLength()));
    }
    
    @Test
    public void assertGetMaxRowSize() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxRowSize(), is(databaseMetaData.getMaxRowSize()));
    }
    
    @Test
    public void assertDoesMaxRowSizeIncludeBlobs() throws SQLException {
        assertThat(cachedDatabaseMetaData.doesMaxRowSizeIncludeBlobs(), is(databaseMetaData.doesMaxRowSizeIncludeBlobs()));
    }
    
    @Test
    public void assertGetMaxStatementLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxStatementLength(), is(databaseMetaData.getMaxStatementLength()));
    }
    
    @Test
    public void assertGetMaxStatements() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxStatements(), is(databaseMetaData.getMaxStatements()));
    }
    
    @Test
    public void assertGetMaxTableNameLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxTableNameLength(), is(databaseMetaData.getMaxTableNameLength()));
    }
    
    @Test
    public void assertGetMaxTablesInSelect() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxTablesInSelect(), is(databaseMetaData.getMaxTablesInSelect()));
    }
    
    @Test
    public void assertGetMaxUserNameLength() throws SQLException {
        assertThat(cachedDatabaseMetaData.getMaxUserNameLength(), is(databaseMetaData.getMaxUserNameLength()));
    }
    
    @Test
    public void assertGetDefaultTransactionIsolation() throws SQLException {
        assertThat(cachedDatabaseMetaData.getDefaultTransactionIsolation(), is(databaseMetaData.getDefaultTransactionIsolation()));
    }
    
    @Test
    public void assertSupportsTransactions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsTransactions(), is(databaseMetaData.supportsTransactions()));
    }
    
    @Test
    public void assertSupportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsDataDefinitionAndDataManipulationTransactions(), is(databaseMetaData.supportsDataDefinitionAndDataManipulationTransactions()));
    }
    
    @Test
    public void assertSupportsDataManipulationTransactionsOnly() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsDataManipulationTransactionsOnly(), is(databaseMetaData.supportsDataManipulationTransactionsOnly()));
    }
    
    @Test
    public void assertDataDefinitionCausesTransactionCommit() throws SQLException {
        assertThat(cachedDatabaseMetaData.dataDefinitionCausesTransactionCommit(), is(databaseMetaData.dataDefinitionCausesTransactionCommit()));
    }
    
    @Test
    public void assertDataDefinitionIgnoredInTransactions() throws SQLException {
        assertThat(cachedDatabaseMetaData.dataDefinitionIgnoredInTransactions(), is(databaseMetaData.dataDefinitionIgnoredInTransactions()));
    }
    
    @Test
    public void assertSupportsBatchUpdates() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsBatchUpdates(), is(databaseMetaData.supportsBatchUpdates()));
    }
    
    @Test
    public void assertSupportsSavepoints() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsSavepoints(), is(databaseMetaData.supportsSavepoints()));
    }
    
    @Test
    public void assertSupportsNamedParameters() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsNamedParameters(), is(databaseMetaData.supportsNamedParameters()));
    }
    
    @Test
    public void assertSupportsMultipleOpenResults() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsMultipleOpenResults(), is(databaseMetaData.supportsMultipleOpenResults()));
    }
    
    @Test
    public void assertSupportsGetGeneratedKeys() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsGetGeneratedKeys(), is(databaseMetaData.supportsGetGeneratedKeys()));
    }
    
    @Test
    public void assertGetResultSetHoldability() throws SQLException {
        assertThat(cachedDatabaseMetaData.getResultSetHoldability(), is(databaseMetaData.getResultSetHoldability()));
    }
    
    @Test
    public void assertGetSQLStateType() throws SQLException {
        assertThat(cachedDatabaseMetaData.getSQLStateType(), is(databaseMetaData.getSQLStateType()));
    }
    
    @Test
    public void assertLocatorsUpdateCopy() throws SQLException {
        assertThat(cachedDatabaseMetaData.locatorsUpdateCopy(), is(databaseMetaData.locatorsUpdateCopy()));
    }
    
    @Test
    public void assertSupportsStatementPooling() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsStatementPooling(), is(databaseMetaData.supportsStatementPooling()));
    }
    
    @Test
    public void assertSupportsStoredFunctionsUsingCallSyntax() throws SQLException {
        assertThat(cachedDatabaseMetaData.supportsStoredFunctionsUsingCallSyntax(), is(databaseMetaData.supportsStoredFunctionsUsingCallSyntax()));
    }
    
    @Test
    public void assertAutoCommitFailureClosesAllResultSets() throws SQLException {
        assertThat(cachedDatabaseMetaData.autoCommitFailureClosesAllResultSets(), is(databaseMetaData.autoCommitFailureClosesAllResultSets()));
    }
    
    @Test
    public void assertGetRowIdLifetime() throws SQLException {
        assertThat(cachedDatabaseMetaData.getRowIdLifetime(), is(databaseMetaData.getRowIdLifetime()));
    }
    
    @Test
    public void assertGeneratedKeyAlwaysReturned() throws SQLException {
        assertThat(cachedDatabaseMetaData.generatedKeyAlwaysReturned(), is(databaseMetaData.generatedKeyAlwaysReturned()));
    }
    
    @Test
    public void assertOwnInsertsAreVisible() {
        assertTrue(cachedDatabaseMetaData.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOwnUpdatesAreVisible() {
        assertTrue(cachedDatabaseMetaData.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOwnDeletesAreVisible() {
        assertTrue(cachedDatabaseMetaData.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersInsertsAreVisible() {
        assertTrue(cachedDatabaseMetaData.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersUpdatesAreVisible() {
        assertTrue(cachedDatabaseMetaData.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersDeletesAreVisible() {
        assertTrue(cachedDatabaseMetaData.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertInsertsAreDetected() {
        assertTrue(cachedDatabaseMetaData.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertUpdatesAreDetected() {
        assertTrue(cachedDatabaseMetaData.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertDeletesAreDetected() {
        assertTrue(cachedDatabaseMetaData.deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetType() {
        assertTrue(cachedDatabaseMetaData.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetConcurrency() {
        assertTrue(cachedDatabaseMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetHoldability() {
        assertTrue(cachedDatabaseMetaData.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));
    }
    
    @Test
    public void assertSupportsTransactionIsolationLevel() {
        assertTrue(cachedDatabaseMetaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE));
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        assertThat(cachedDatabaseMetaData.getConnection(), is(dataSource.getConnection()));
    }
    
    @Test
    public void assertGetSuperTypes() throws SQLException {
        when(databaseMetaData.getSuperTypes("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getSuperTypes("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSuperTables() throws SQLException {
        when(databaseMetaData.getSuperTables("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getSuperTables("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetAttributes() throws SQLException {
        when(databaseMetaData.getAttributes("test", null, null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getAttributes("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetProcedures() throws SQLException {
        when(databaseMetaData.getProcedures("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getProcedures("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetProcedureColumns() throws SQLException {
        when(databaseMetaData.getProcedureColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getProcedureColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTables() throws SQLException {
        when(databaseMetaData.getTables("test", null, "%" + TABLE_NAME + "%", null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getTables("test", null, TABLE_NAME, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSchemas() throws SQLException {
        when(databaseMetaData.getSchemas()).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getSchemas(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSchemasForCatalogAndSchemaPattern() throws SQLException {
        when(databaseMetaData.getSchemas("test", null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getSchemas("test", null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetCatalogs() throws SQLException {
        when(databaseMetaData.getCatalogs()).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getCatalogs(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTableTypes() throws SQLException {
        when(databaseMetaData.getTableTypes()).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getTableTypes(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetColumns() throws SQLException {
        when(databaseMetaData.getColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetColumnPrivileges() throws SQLException {
        when(databaseMetaData.getColumnPrivileges("test", null, null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getColumnPrivileges("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTablePrivileges() throws SQLException {
        when(databaseMetaData.getTablePrivileges("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getTablePrivileges("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetBestRowIdentifier() throws SQLException {
        when(databaseMetaData.getBestRowIdentifier("test", null, null, 1, true)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getBestRowIdentifier("test", null, null, 1, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetVersionColumns() throws SQLException {
        when(databaseMetaData.getVersionColumns("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getVersionColumns("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetPrimaryKeys() throws SQLException {
        when(databaseMetaData.getPrimaryKeys("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getPrimaryKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetImportedKeys() throws SQLException {
        when(databaseMetaData.getImportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getImportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetExportedKeys() throws SQLException {
        when(databaseMetaData.getExportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getExportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetCrossReference() throws SQLException {
        when(databaseMetaData.getCrossReference("test", null, null, null, null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getCrossReference("test", null, null, null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTypeInfo() throws SQLException {
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getTypeInfo(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetIndexInfo() throws SQLException {
        when(databaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetUDTs() throws SQLException {
        when(databaseMetaData.getUDTs("test", null, null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getUDTs("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetClientInfoProperties() throws SQLException {
        when(databaseMetaData.getClientInfoProperties()).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getClientInfoProperties(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetFunctions() throws SQLException {
        when(databaseMetaData.getFunctions("test", null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getFunctions("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetFunctionColumns() throws SQLException {
        when(databaseMetaData.getFunctionColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getFunctionColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetPseudoColumns() throws SQLException {
        when(databaseMetaData.getPseudoColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(cachedDatabaseMetaData.getPseudoColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
}
