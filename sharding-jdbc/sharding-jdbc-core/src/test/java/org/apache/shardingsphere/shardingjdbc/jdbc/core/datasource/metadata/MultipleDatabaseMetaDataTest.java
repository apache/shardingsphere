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

import com.google.common.collect.LinkedHashMultimap;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContext;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MultipleDatabaseMetaDataTest {
    
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
    
    @Mock
    private ShardingConnection shardingConnection;
    
    @Mock
    private RuntimeContext runtimeContext;
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
    
    private MultipleDatabaseMetaData multipleDatabaseMetaData;
    
    @Before
    public void setUp() throws SQLException {
        dataSourceMap.put(DATA_SOURCE_NAME, dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(resultSet.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
        CachedDatabaseMetaData cachedDatabaseMetaData = new CachedDatabaseMetaData(databaseMetaData);
        when(shardingConnection.getCachedConnections()).thenReturn(LinkedHashMultimap.create());
        when(shardingConnection.getConnection(anyString())).thenReturn(connection);
        when(shardingConnection.getDataSourceMap()).thenReturn(dataSourceMap);
        when(shardingConnection.getRuntimeContext()).thenReturn(runtimeContext);
        when(runtimeContext.getCachedDatabaseMetaData()).thenReturn(cachedDatabaseMetaData);
        when(runtimeContext.getRules()).thenReturn(Collections.singletonList(mockShardingRule()));
        multipleDatabaseMetaData = new MultipleDatabaseMetaData(shardingConnection);
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(TABLE_NAME, DATA_SOURCE_NAME + "." + TABLE_NAME);
        ruleConfig.setTableRuleConfigs(Collections.singletonList(tableRuleConfiguration));
        return new ShardingRule(ruleConfig, Collections.singletonList(DATA_SOURCE_NAME));
    }
    
    @Test
    public void assertGetURL() throws SQLException {
        assertThat(multipleDatabaseMetaData.getURL(), is(databaseMetaData.getURL()));
    }
    
    @Test
    public void assertGetUserName() throws SQLException {
        assertThat(multipleDatabaseMetaData.getUserName(), is(databaseMetaData.getUserName()));
    }
    
    @Test
    public void assertGetDatabaseProductName() throws SQLException {
        assertThat(multipleDatabaseMetaData.getDatabaseProductName(), is(databaseMetaData.getDatabaseProductName()));
    }
    
    @Test
    public void assertGetDatabaseProductVersion() throws SQLException {
        assertThat(multipleDatabaseMetaData.getDatabaseProductVersion(), is(databaseMetaData.getDatabaseProductVersion()));
    }
    
    @Test
    public void assertGetDriverName() throws SQLException {
        assertThat(multipleDatabaseMetaData.getDriverName(), is(databaseMetaData.getDriverName()));
    }
    
    @Test
    public void assertGetDriverVersion() throws SQLException {
        assertThat(multipleDatabaseMetaData.getDriverVersion(), is(databaseMetaData.getDriverVersion()));
    }
    
    @Test
    public void assertGetDriverMajorVersion() {
        assertThat(multipleDatabaseMetaData.getDriverMajorVersion(), is(databaseMetaData.getDriverMajorVersion()));
    }
    
    @Test
    public void assertGetDriverMinorVersion() {
        assertThat(multipleDatabaseMetaData.getDriverMinorVersion(), is(databaseMetaData.getDriverMinorVersion()));
    }
    
    @Test
    public void assertGetDatabaseMajorVersion() throws SQLException {
        assertThat(multipleDatabaseMetaData.getDatabaseMajorVersion(), is(databaseMetaData.getDatabaseMajorVersion()));
    }
    
    @Test
    public void assertGetDatabaseMinorVersion() throws SQLException {
        assertThat(multipleDatabaseMetaData.getDatabaseMinorVersion(), is(databaseMetaData.getDatabaseMinorVersion()));
    }
    
    @Test
    public void assertGetJDBCMajorVersion() throws SQLException {
        assertThat(multipleDatabaseMetaData.getJDBCMajorVersion(), is(databaseMetaData.getJDBCMajorVersion()));
    }
    
    @Test
    public void assertGetJDBCMinorVersion() throws SQLException {
        assertThat(multipleDatabaseMetaData.getJDBCMinorVersion(), is(databaseMetaData.getJDBCMinorVersion()));
    }
    
    @Test
    public void assertAssertIsReadOnly() throws SQLException {
        assertThat(multipleDatabaseMetaData.isReadOnly(), is(databaseMetaData.isReadOnly()));
    }
    
    @Test
    public void assertAllProceduresAreCallable() throws SQLException {
        assertThat(multipleDatabaseMetaData.allProceduresAreCallable(), is(databaseMetaData.allProceduresAreCallable()));
    }
    
    @Test
    public void assertAllTablesAreSelectable() throws SQLException {
        assertThat(multipleDatabaseMetaData.allTablesAreSelectable(), is(databaseMetaData.allTablesAreSelectable()));
    }
    
    @Test
    public void assertNullsAreSortedHigh() throws SQLException {
        assertThat(multipleDatabaseMetaData.nullsAreSortedHigh(), is(databaseMetaData.nullsAreSortedHigh()));
    }
    
    @Test
    public void assertNullsAreSortedLow() throws SQLException {
        assertThat(multipleDatabaseMetaData.nullsAreSortedLow(), is(databaseMetaData.nullsAreSortedLow()));
    }
    
    @Test
    public void assertNullsAreSortedAtStart() throws SQLException {
        assertThat(multipleDatabaseMetaData.nullsAreSortedAtStart(), is(databaseMetaData.nullsAreSortedAtStart()));
    }
    
    @Test
    public void assertNullsAreSortedAtEnd() throws SQLException {
        assertThat(multipleDatabaseMetaData.nullsAreSortedAtEnd(), is(databaseMetaData.nullsAreSortedAtEnd()));
    }
    
    @Test
    public void assertUsesLocalFiles() throws SQLException {
        assertThat(multipleDatabaseMetaData.usesLocalFiles(), is(databaseMetaData.usesLocalFiles()));
    }
    
    @Test
    public void assertUsesLocalFilePerTable() throws SQLException {
        assertThat(multipleDatabaseMetaData.usesLocalFilePerTable(), is(databaseMetaData.usesLocalFilePerTable()));
    }
    
    @Test
    public void assertSupportsMixedCaseIdentifiers() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsMixedCaseIdentifiers(), is(databaseMetaData.supportsMixedCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresUpperCaseIdentifiers() throws SQLException {
        assertThat(multipleDatabaseMetaData.storesUpperCaseIdentifiers(), is(databaseMetaData.storesUpperCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresLowerCaseIdentifiers() throws SQLException {
        assertThat(multipleDatabaseMetaData.storesLowerCaseIdentifiers(), is(databaseMetaData.storesLowerCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresMixedCaseIdentifiers() throws SQLException {
        assertThat(multipleDatabaseMetaData.storesMixedCaseIdentifiers(), is(databaseMetaData.storesMixedCaseIdentifiers()));
    }
    
    @Test
    public void assertSupportsMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsMixedCaseQuotedIdentifiers(), is(databaseMetaData.supportsMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresUpperCaseQuotedIdentifiers() throws SQLException {
        assertThat(multipleDatabaseMetaData.storesUpperCaseQuotedIdentifiers(), is(databaseMetaData.storesUpperCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresLowerCaseQuotedIdentifiers() throws SQLException {
        assertThat(multipleDatabaseMetaData.storesLowerCaseQuotedIdentifiers(), is(databaseMetaData.storesLowerCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(multipleDatabaseMetaData.storesMixedCaseQuotedIdentifiers(), is(databaseMetaData.storesMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertGetIdentifierQuoteString() throws SQLException {
        assertThat(multipleDatabaseMetaData.getIdentifierQuoteString(), is(databaseMetaData.getIdentifierQuoteString()));
    }
    
    @Test
    public void assertGetSQLKeywords() throws SQLException {
        assertThat(multipleDatabaseMetaData.getSQLKeywords(), is(databaseMetaData.getSQLKeywords()));
    }
    
    @Test
    public void assertGetNumericFunctions() throws SQLException {
        assertThat(multipleDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    public void assertGetStringFunctions() throws SQLException {
        assertThat(multipleDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    public void assertGetSystemFunctions() throws SQLException {
        assertThat(multipleDatabaseMetaData.getSystemFunctions(), is(databaseMetaData.getSystemFunctions()));
    }
    
    @Test
    public void assertGetTimeDateFunctions() throws SQLException {
        assertThat(multipleDatabaseMetaData.getTimeDateFunctions(), is(databaseMetaData.getTimeDateFunctions()));
    }
    
    @Test
    public void assertGetSearchStringEscape() throws SQLException {
        assertThat(multipleDatabaseMetaData.getSearchStringEscape(), is(databaseMetaData.getSearchStringEscape()));
    }
    
    @Test
    public void assertGetExtraNameCharacters() throws SQLException {
        assertThat(multipleDatabaseMetaData.getExtraNameCharacters(), is(databaseMetaData.getExtraNameCharacters()));
    }
    
    @Test
    public void assertSupportsAlterTableWithAddColumn() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsAlterTableWithAddColumn(), is(databaseMetaData.supportsAlterTableWithAddColumn()));
    }
    
    @Test
    public void assertSupportsAlterTableWithDropColumn() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsAlterTableWithDropColumn(), is(databaseMetaData.supportsAlterTableWithDropColumn()));
    }
    
    @Test
    public void assertSupportsColumnAliasing() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsColumnAliasing(), is(databaseMetaData.supportsColumnAliasing()));
    }
    
    @Test
    public void assertNullPlusNonNullIsNull() throws SQLException {
        assertThat(multipleDatabaseMetaData.nullPlusNonNullIsNull(), is(databaseMetaData.nullPlusNonNullIsNull()));
    }
    
    @Test
    public void assertSupportsConvert() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsConvert(), is(databaseMetaData.supportsConvert()));
        assertThat(multipleDatabaseMetaData.supportsConvert(Types.INTEGER, Types.FLOAT), is(databaseMetaData.supportsConvert()));
    }
    
    @Test
    public void assertSupportsTableCorrelationNames() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsTableCorrelationNames(), is(databaseMetaData.supportsTableCorrelationNames()));
    }
    
    @Test
    public void assertSupportsDifferentTableCorrelationNames() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsDifferentTableCorrelationNames(), is(databaseMetaData.supportsDifferentTableCorrelationNames()));
    }
    
    @Test
    public void assertSupportsExpressionsInOrderBy() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsExpressionsInOrderBy(), is(databaseMetaData.supportsExpressionsInOrderBy()));
    }
    
    @Test
    public void assertSupportsOrderByUnrelated() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsOrderByUnrelated(), is(databaseMetaData.supportsOrderByUnrelated()));
    }
    
    @Test
    public void assertSupportsGroupBy() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsGroupBy(), is(databaseMetaData.supportsGroupBy()));
    }
    
    @Test
    public void assertSupportsGroupByUnrelated() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsGroupByUnrelated(), is(databaseMetaData.supportsGroupByUnrelated()));
    }
    
    @Test
    public void assertSupportsGroupByBeyondSelect() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsGroupByBeyondSelect(), is(databaseMetaData.supportsGroupByBeyondSelect()));
    }
    
    @Test
    public void assertSupportsLikeEscapeClause() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsLikeEscapeClause(), is(databaseMetaData.supportsLikeEscapeClause()));
    }
    
    @Test
    public void assertSupportsMultipleResultSets() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsMultipleResultSets(), is(databaseMetaData.supportsMultipleResultSets()));
    }
    
    @Test
    public void assertSupportsMultipleTransactions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsMultipleTransactions(), is(databaseMetaData.supportsMultipleTransactions()));
    }
    
    @Test
    public void assertSupportsNonNullableColumns() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsNonNullableColumns(), is(databaseMetaData.supportsNonNullableColumns()));
    }
    
    @Test
    public void assertSupportsMinimumSQLGrammar() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsMinimumSQLGrammar(), is(databaseMetaData.supportsMinimumSQLGrammar()));
    }
    
    @Test
    public void assertSupportsCoreSQLGrammar() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsCoreSQLGrammar(), is(databaseMetaData.supportsCoreSQLGrammar()));
    }
    
    @Test
    public void assertSupportsExtendedSQLGrammar() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsExtendedSQLGrammar(), is(databaseMetaData.supportsExtendedSQLGrammar()));
    }
    
    @Test
    public void assertSupportsANSI92EntryLevelSQL() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsANSI92EntryLevelSQL(), is(databaseMetaData.supportsANSI92EntryLevelSQL()));
    }
    
    @Test
    public void assertSupportsANSI92IntermediateSQL() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsANSI92IntermediateSQL(), is(databaseMetaData.supportsANSI92IntermediateSQL()));
    }
    
    @Test
    public void assertSupportsANSI92FullSQL() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsANSI92FullSQL(), is(databaseMetaData.supportsANSI92FullSQL()));
    }
    
    @Test
    public void assertSupportsIntegrityEnhancementFacility() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsIntegrityEnhancementFacility(), is(databaseMetaData.supportsIntegrityEnhancementFacility()));
    }
    
    @Test
    public void assertSupportsOuterJoins() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsOuterJoins(), is(databaseMetaData.supportsOuterJoins()));
    }
    
    @Test
    public void assertSupportsFullOuterJoins() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsFullOuterJoins(), is(databaseMetaData.supportsFullOuterJoins()));
    }
    
    @Test
    public void assertSupportsLimitedOuterJoins() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsLimitedOuterJoins(), is(databaseMetaData.supportsLimitedOuterJoins()));
    }
    
    @Test
    public void assertGetSchemaTerm() throws SQLException {
        assertThat(multipleDatabaseMetaData.getSchemaTerm(), is(databaseMetaData.getSchemaTerm()));
    }
    
    @Test
    public void assertGetProcedureTerm() throws SQLException {
        assertThat(multipleDatabaseMetaData.getProcedureTerm(), is(databaseMetaData.getProcedureTerm()));
    }
    
    @Test
    public void assertGetCatalogTerm() throws SQLException {
        assertThat(multipleDatabaseMetaData.getCatalogTerm(), is(databaseMetaData.getCatalogTerm()));
    }
    
    @Test
    public void assertAssertIsCatalogAtStart() throws SQLException {
        assertThat(multipleDatabaseMetaData.isCatalogAtStart(), is(databaseMetaData.isCatalogAtStart()));
    }
    
    @Test
    public void assertGetCatalogSeparator() throws SQLException {
        assertThat(multipleDatabaseMetaData.getCatalogSeparator(), is(databaseMetaData.getCatalogSeparator()));
    }
    
    @Test
    public void assertSupportsSchemasInDataManipulation() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSchemasInDataManipulation(), is(databaseMetaData.supportsSchemasInDataManipulation()));
    }
    
    @Test
    public void assertSupportsSchemasInProcedureCalls() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSchemasInProcedureCalls(), is(databaseMetaData.supportsSchemasInProcedureCalls()));
    }
    
    @Test
    public void assertSupportsSchemasInTableDefinitions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSchemasInTableDefinitions(), is(databaseMetaData.supportsSchemasInTableDefinitions()));
    }
    
    @Test
    public void assertSupportsSchemasInIndexDefinitions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSchemasInIndexDefinitions(), is(databaseMetaData.supportsSchemasInIndexDefinitions()));
    }
    
    @Test
    public void assertSupportsSchemasInPrivilegeDefinitions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSchemasInPrivilegeDefinitions(), is(databaseMetaData.supportsSchemasInPrivilegeDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInDataManipulation() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsCatalogsInDataManipulation(), is(databaseMetaData.supportsCatalogsInDataManipulation()));
    }
    
    @Test
    public void assertSupportsCatalogsInProcedureCalls() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsCatalogsInProcedureCalls(), is(databaseMetaData.supportsCatalogsInProcedureCalls()));
    }
    
    @Test
    public void assertSupportsCatalogsInTableDefinitions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsCatalogsInTableDefinitions(), is(databaseMetaData.supportsCatalogsInTableDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInIndexDefinitions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsCatalogsInIndexDefinitions(), is(databaseMetaData.supportsCatalogsInIndexDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInPrivilegeDefinitions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsCatalogsInPrivilegeDefinitions(), is(databaseMetaData.supportsCatalogsInPrivilegeDefinitions()));
    }
    
    @Test
    public void assertSupportsPositionedDelete() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsPositionedDelete(), is(databaseMetaData.supportsPositionedDelete()));
    }
    
    @Test
    public void assertSupportsPositionedUpdate() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsPositionedUpdate(), is(databaseMetaData.supportsPositionedUpdate()));
    }
    
    @Test
    public void assertSupportsSelectForUpdate() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSelectForUpdate(), is(databaseMetaData.supportsSelectForUpdate()));
    }
    
    @Test
    public void assertSupportsStoredProcedures() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsStoredProcedures(), is(databaseMetaData.supportsStoredProcedures()));
    }
    
    @Test
    public void assertSupportsSubqueriesInComparisons() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSubqueriesInComparisons(), is(databaseMetaData.supportsSubqueriesInComparisons()));
    }
    
    @Test
    public void assertSupportsSubqueriesInExists() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSubqueriesInExists(), is(databaseMetaData.supportsSubqueriesInExists()));
    }
    
    @Test
    public void assertSupportsSubqueriesInIns() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSubqueriesInIns(), is(databaseMetaData.supportsSubqueriesInIns()));
    }
    
    @Test
    public void assertSupportsSubqueriesInQuantifieds() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSubqueriesInQuantifieds(), is(databaseMetaData.supportsSubqueriesInQuantifieds()));
    }
    
    @Test
    public void assertSupportsCorrelatedSubqueries() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsCorrelatedSubqueries(), is(databaseMetaData.supportsCorrelatedSubqueries()));
    }
    
    @Test
    public void assertSupportsUnion() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsUnion(), is(databaseMetaData.supportsUnion()));
    }
    
    @Test
    public void assertSupportsUnionAll() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsUnionAll(), is(databaseMetaData.supportsUnionAll()));
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossCommit() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsOpenCursorsAcrossCommit(), is(databaseMetaData.supportsOpenCursorsAcrossCommit()));
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossRollback() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsOpenCursorsAcrossRollback(), is(databaseMetaData.supportsOpenCursorsAcrossRollback()));
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossCommit() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsOpenStatementsAcrossCommit(), is(databaseMetaData.supportsOpenStatementsAcrossCommit()));
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossRollback() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsOpenStatementsAcrossRollback(), is(databaseMetaData.supportsOpenStatementsAcrossRollback()));
    }
    
    @Test
    public void assertGetMaxBinaryLiteralLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxBinaryLiteralLength(), is(databaseMetaData.getMaxBinaryLiteralLength()));
    }
    
    @Test
    public void assertGetMaxCharLiteralLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxCharLiteralLength(), is(databaseMetaData.getMaxCharLiteralLength()));
    }
    
    @Test
    public void assertGetMaxColumnNameLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxColumnNameLength(), is(databaseMetaData.getMaxColumnNameLength()));
    }
    
    @Test
    public void assertGetMaxColumnsInGroupBy() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxColumnsInGroupBy(), is(databaseMetaData.getMaxColumnsInGroupBy()));
    }
    
    @Test
    public void assertGetMaxColumnsInIndex() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxColumnsInIndex(), is(databaseMetaData.getMaxColumnsInIndex()));
    }
    
    @Test
    public void assertGetMaxColumnsInOrderBy() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxColumnsInOrderBy(), is(databaseMetaData.getMaxColumnsInOrderBy()));
    }
    
    @Test
    public void assertGetMaxColumnsInSelect() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxColumnsInSelect(), is(databaseMetaData.getMaxColumnsInSelect()));
    }
    
    @Test
    public void assertGetMaxColumnsInTable() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxColumnsInTable(), is(databaseMetaData.getMaxColumnsInTable()));
    }
    
    @Test
    public void assertGetMaxConnections() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxConnections(), is(databaseMetaData.getMaxConnections()));
    }
    
    @Test
    public void assertGetMaxCursorNameLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxCursorNameLength(), is(databaseMetaData.getMaxCursorNameLength()));
    }
    
    @Test
    public void assertGetMaxIndexLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxIndexLength(), is(databaseMetaData.getMaxIndexLength()));
    }
    
    @Test
    public void assertGetMaxSchemaNameLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxSchemaNameLength(), is(databaseMetaData.getMaxSchemaNameLength()));
    }
    
    @Test
    public void assertGetMaxProcedureNameLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxProcedureNameLength(), is(databaseMetaData.getMaxProcedureNameLength()));
    }
    
    @Test
    public void assertGetMaxCatalogNameLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxCatalogNameLength(), is(databaseMetaData.getMaxCatalogNameLength()));
    }
    
    @Test
    public void assertGetMaxRowSize() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxRowSize(), is(databaseMetaData.getMaxRowSize()));
    }
    
    @Test
    public void assertDoesMaxRowSizeIncludeBlobs() throws SQLException {
        assertThat(multipleDatabaseMetaData.doesMaxRowSizeIncludeBlobs(), is(databaseMetaData.doesMaxRowSizeIncludeBlobs()));
    }
    
    @Test
    public void assertGetMaxStatementLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxStatementLength(), is(databaseMetaData.getMaxStatementLength()));
    }
    
    @Test
    public void assertGetMaxStatements() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxStatements(), is(databaseMetaData.getMaxStatements()));
    }
    
    @Test
    public void assertGetMaxTableNameLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxTableNameLength(), is(databaseMetaData.getMaxTableNameLength()));
    }
    
    @Test
    public void assertGetMaxTablesInSelect() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxTablesInSelect(), is(databaseMetaData.getMaxTablesInSelect()));
    }
    
    @Test
    public void assertGetMaxUserNameLength() throws SQLException {
        assertThat(multipleDatabaseMetaData.getMaxUserNameLength(), is(databaseMetaData.getMaxUserNameLength()));
    }
    
    @Test
    public void assertGetDefaultTransactionIsolation() throws SQLException {
        assertThat(multipleDatabaseMetaData.getDefaultTransactionIsolation(), is(databaseMetaData.getDefaultTransactionIsolation()));
    }
    
    @Test
    public void assertSupportsTransactions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsTransactions(), is(databaseMetaData.supportsTransactions()));
    }
    
    @Test
    public void assertSupportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsDataDefinitionAndDataManipulationTransactions(), is(databaseMetaData.supportsDataDefinitionAndDataManipulationTransactions()));
    }
    
    @Test
    public void assertSupportsDataManipulationTransactionsOnly() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsDataManipulationTransactionsOnly(), is(databaseMetaData.supportsDataManipulationTransactionsOnly()));
    }
    
    @Test
    public void assertDataDefinitionCausesTransactionCommit() throws SQLException {
        assertThat(multipleDatabaseMetaData.dataDefinitionCausesTransactionCommit(), is(databaseMetaData.dataDefinitionCausesTransactionCommit()));
    }
    
    @Test
    public void assertDataDefinitionIgnoredInTransactions() throws SQLException {
        assertThat(multipleDatabaseMetaData.dataDefinitionIgnoredInTransactions(), is(databaseMetaData.dataDefinitionIgnoredInTransactions()));
    }
    
    @Test
    public void assertSupportsBatchUpdates() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsBatchUpdates(), is(databaseMetaData.supportsBatchUpdates()));
    }
    
    @Test
    public void assertSupportsSavepoints() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsSavepoints(), is(databaseMetaData.supportsSavepoints()));
    }
    
    @Test
    public void assertSupportsNamedParameters() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsNamedParameters(), is(databaseMetaData.supportsNamedParameters()));
    }
    
    @Test
    public void assertSupportsMultipleOpenResults() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsMultipleOpenResults(), is(databaseMetaData.supportsMultipleOpenResults()));
    }
    
    @Test
    public void assertSupportsGetGeneratedKeys() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsGetGeneratedKeys(), is(databaseMetaData.supportsGetGeneratedKeys()));
    }
    
    @Test
    public void assertGetResultSetHoldability() throws SQLException {
        assertThat(multipleDatabaseMetaData.getResultSetHoldability(), is(databaseMetaData.getResultSetHoldability()));
    }
    
    @Test
    public void assertGetSQLStateType() throws SQLException {
        assertThat(multipleDatabaseMetaData.getSQLStateType(), is(databaseMetaData.getSQLStateType()));
    }
    
    @Test
    public void assertLocatorsUpdateCopy() throws SQLException {
        assertThat(multipleDatabaseMetaData.locatorsUpdateCopy(), is(databaseMetaData.locatorsUpdateCopy()));
    }
    
    @Test
    public void assertSupportsStatementPooling() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsStatementPooling(), is(databaseMetaData.supportsStatementPooling()));
    }
    
    @Test
    public void assertSupportsStoredFunctionsUsingCallSyntax() throws SQLException {
        assertThat(multipleDatabaseMetaData.supportsStoredFunctionsUsingCallSyntax(), is(databaseMetaData.supportsStoredFunctionsUsingCallSyntax()));
    }
    
    @Test
    public void assertAutoCommitFailureClosesAllResultSets() throws SQLException {
        assertThat(multipleDatabaseMetaData.autoCommitFailureClosesAllResultSets(), is(databaseMetaData.autoCommitFailureClosesAllResultSets()));
    }
    
    @Test
    public void assertGetRowIdLifetime() throws SQLException {
        assertThat(multipleDatabaseMetaData.getRowIdLifetime(), is(databaseMetaData.getRowIdLifetime()));
    }
    
    @Test
    public void assertGeneratedKeyAlwaysReturned() throws SQLException {
        assertThat(multipleDatabaseMetaData.generatedKeyAlwaysReturned(), is(databaseMetaData.generatedKeyAlwaysReturned()));
    }
    
    @Test
    public void assertOwnInsertsAreVisible() {
        assertTrue(multipleDatabaseMetaData.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOwnUpdatesAreVisible() {
        assertTrue(multipleDatabaseMetaData.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOwnDeletesAreVisible() {
        assertTrue(multipleDatabaseMetaData.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersInsertsAreVisible() {
        assertTrue(multipleDatabaseMetaData.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersUpdatesAreVisible() {
        assertTrue(multipleDatabaseMetaData.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersDeletesAreVisible() {
        assertTrue(multipleDatabaseMetaData.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertInsertsAreDetected() {
        assertTrue(multipleDatabaseMetaData.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertUpdatesAreDetected() {
        assertTrue(multipleDatabaseMetaData.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertDeletesAreDetected() {
        assertTrue(multipleDatabaseMetaData.deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetType() {
        assertTrue(multipleDatabaseMetaData.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetConcurrency() {
        assertTrue(multipleDatabaseMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetHoldability() {
        assertTrue(multipleDatabaseMetaData.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));
    }
    
    @Test
    public void assertSupportsTransactionIsolationLevel() {
        assertTrue(multipleDatabaseMetaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE));
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        assertThat(multipleDatabaseMetaData.getConnection(), is(dataSource.getConnection()));
    }
    
    @Test
    public void assertGetSuperTypes() throws SQLException {
        when(databaseMetaData.getSuperTypes("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getSuperTypes("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSuperTables() throws SQLException {
        when(databaseMetaData.getSuperTables("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getSuperTables("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetAttributes() throws SQLException {
        when(databaseMetaData.getAttributes("test", null, null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getAttributes("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetProcedures() throws SQLException {
        when(databaseMetaData.getProcedures("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getProcedures("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetProcedureColumns() throws SQLException {
        when(databaseMetaData.getProcedureColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getProcedureColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTables() throws SQLException {
        when(databaseMetaData.getTables("test", null, "%" + TABLE_NAME + "%", null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getTables("test", null, TABLE_NAME, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSchemas() throws SQLException {
        when(databaseMetaData.getSchemas()).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getSchemas(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSchemasForCatalogAndSchemaPattern() throws SQLException {
        when(databaseMetaData.getSchemas("test", null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getSchemas("test", null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetCatalogs() throws SQLException {
        when(databaseMetaData.getCatalogs()).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getCatalogs(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTableTypes() throws SQLException {
        when(databaseMetaData.getTableTypes()).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getTableTypes(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetColumns() throws SQLException {
        when(databaseMetaData.getColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetColumnPrivileges() throws SQLException {
        when(databaseMetaData.getColumnPrivileges("test", null, null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getColumnPrivileges("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTablePrivileges() throws SQLException {
        when(databaseMetaData.getTablePrivileges("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getTablePrivileges("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetBestRowIdentifier() throws SQLException {
        when(databaseMetaData.getBestRowIdentifier("test", null, null, 1, true)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getBestRowIdentifier("test", null, null, 1, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetVersionColumns() throws SQLException {
        when(databaseMetaData.getVersionColumns("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getVersionColumns("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetPrimaryKeys() throws SQLException {
        when(databaseMetaData.getPrimaryKeys("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getPrimaryKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetImportedKeys() throws SQLException {
        when(databaseMetaData.getImportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getImportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetExportedKeys() throws SQLException {
        when(databaseMetaData.getExportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getExportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetCrossReference() throws SQLException {
        when(databaseMetaData.getCrossReference("test", null, null, null, null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getCrossReference("test", null, null, null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTypeInfo() throws SQLException {
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getTypeInfo(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetIndexInfo() throws SQLException {
        when(databaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetUDTs() throws SQLException {
        when(databaseMetaData.getUDTs("test", null, null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getUDTs("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetClientInfoProperties() throws SQLException {
        when(databaseMetaData.getClientInfoProperties()).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getClientInfoProperties(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetFunctions() throws SQLException {
        when(databaseMetaData.getFunctions("test", null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getFunctions("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetFunctionColumns() throws SQLException {
        when(databaseMetaData.getFunctionColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getFunctionColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetPseudoColumns() throws SQLException {
        when(databaseMetaData.getPseudoColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(multipleDatabaseMetaData.getPseudoColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
}
