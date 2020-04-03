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
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.MasterSlaveConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.MasterSlaveRuntimeContext;
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
public final class MasterSlaveDatabaseMetaDataTest {
    
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
    private MasterSlaveConnection masterSlaveConnection;
    
    @Mock
    private MasterSlaveRuntimeContext masterSlaveRuntimeContext;
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
    
    private MasterSlaveDatabaseMetaData masterSlaveDatabaseMetaData;
    
    @Before
    public void setUp() throws SQLException {
        dataSourceMap.put(DATA_SOURCE_NAME, dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(resultSet.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
        CachedDatabaseMetaData cachedDatabaseMetaData = new CachedDatabaseMetaData(databaseMetaData);
        when(masterSlaveConnection.getCachedConnections()).thenReturn(LinkedHashMultimap.create());
        when(masterSlaveConnection.getConnection(anyString())).thenReturn(connection);
        when(masterSlaveConnection.getDataSourceMap()).thenReturn(dataSourceMap);
        when(masterSlaveConnection.getRuntimeContext()).thenReturn(masterSlaveRuntimeContext);
        when(masterSlaveRuntimeContext.getCachedDatabaseMetaData()).thenReturn(cachedDatabaseMetaData);
        when(masterSlaveRuntimeContext.getRule()).thenReturn(mockMasterSlaveRule());
        masterSlaveDatabaseMetaData = new MasterSlaveDatabaseMetaData(masterSlaveConnection);
    }
    
    private MasterSlaveRule mockMasterSlaveRule() {
        return new MasterSlaveRule("master", DATA_SOURCE_NAME, Collections.singletonList(DATA_SOURCE_NAME), null);
    }
    
    @Test
    public void assertGetURL() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getURL(), is(databaseMetaData.getURL()));
    }
    
    @Test
    public void assertGetUserName() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getUserName(), is(databaseMetaData.getUserName()));
    }
    
    @Test
    public void assertGetDatabaseProductName() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getDatabaseProductName(), is(databaseMetaData.getDatabaseProductName()));
    }
    
    @Test
    public void assertGetDatabaseProductVersion() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getDatabaseProductVersion(), is(databaseMetaData.getDatabaseProductVersion()));
    }
    
    @Test
    public void assertGetDriverName() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getDriverName(), is(databaseMetaData.getDriverName()));
    }
    
    @Test
    public void assertGetDriverVersion() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getDriverVersion(), is(databaseMetaData.getDriverVersion()));
    }
    
    @Test
    public void assertGetDriverMajorVersion() {
        assertThat(masterSlaveDatabaseMetaData.getDriverMajorVersion(), is(databaseMetaData.getDriverMajorVersion()));
    }
    
    @Test
    public void assertGetDriverMinorVersion() {
        assertThat(masterSlaveDatabaseMetaData.getDriverMinorVersion(), is(databaseMetaData.getDriverMinorVersion()));
    }
    
    @Test
    public void assertGetDatabaseMajorVersion() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getDatabaseMajorVersion(), is(databaseMetaData.getDatabaseMajorVersion()));
    }
    
    @Test
    public void assertGetDatabaseMinorVersion() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getDatabaseMinorVersion(), is(databaseMetaData.getDatabaseMinorVersion()));
    }
    
    @Test
    public void assertGetJDBCMajorVersion() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getJDBCMajorVersion(), is(databaseMetaData.getJDBCMajorVersion()));
    }
    
    @Test
    public void assertGetJDBCMinorVersion() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getJDBCMinorVersion(), is(databaseMetaData.getJDBCMinorVersion()));
    }
    
    @Test
    public void assertAssertIsReadOnly() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.isReadOnly(), is(databaseMetaData.isReadOnly()));
    }
    
    @Test
    public void assertAllProceduresAreCallable() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.allProceduresAreCallable(), is(databaseMetaData.allProceduresAreCallable()));
    }
    
    @Test
    public void assertAllTablesAreSelectable() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.allTablesAreSelectable(), is(databaseMetaData.allTablesAreSelectable()));
    }
    
    @Test
    public void assertNullsAreSortedHigh() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.nullsAreSortedHigh(), is(databaseMetaData.nullsAreSortedHigh()));
    }
    
    @Test
    public void assertNullsAreSortedLow() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.nullsAreSortedLow(), is(databaseMetaData.nullsAreSortedLow()));
    }
    
    @Test
    public void assertNullsAreSortedAtStart() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.nullsAreSortedAtStart(), is(databaseMetaData.nullsAreSortedAtStart()));
    }
    
    @Test
    public void assertNullsAreSortedAtEnd() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.nullsAreSortedAtEnd(), is(databaseMetaData.nullsAreSortedAtEnd()));
    }
    
    @Test
    public void assertUsesLocalFiles() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.usesLocalFiles(), is(databaseMetaData.usesLocalFiles()));
    }
    
    @Test
    public void assertUsesLocalFilePerTable() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.usesLocalFilePerTable(), is(databaseMetaData.usesLocalFilePerTable()));
    }
    
    @Test
    public void assertSupportsMixedCaseIdentifiers() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsMixedCaseIdentifiers(), is(databaseMetaData.supportsMixedCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresUpperCaseIdentifiers() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.storesUpperCaseIdentifiers(), is(databaseMetaData.storesUpperCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresLowerCaseIdentifiers() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.storesLowerCaseIdentifiers(), is(databaseMetaData.storesLowerCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresMixedCaseIdentifiers() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.storesMixedCaseIdentifiers(), is(databaseMetaData.storesMixedCaseIdentifiers()));
    }
    
    @Test
    public void assertSupportsMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsMixedCaseQuotedIdentifiers(), is(databaseMetaData.supportsMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresUpperCaseQuotedIdentifiers() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.storesUpperCaseQuotedIdentifiers(), is(databaseMetaData.storesUpperCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresLowerCaseQuotedIdentifiers() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.storesLowerCaseQuotedIdentifiers(), is(databaseMetaData.storesLowerCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.storesMixedCaseQuotedIdentifiers(), is(databaseMetaData.storesMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertGetIdentifierQuoteString() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getIdentifierQuoteString(), is(databaseMetaData.getIdentifierQuoteString()));
    }
    
    @Test
    public void assertGetSQLKeywords() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getSQLKeywords(), is(databaseMetaData.getSQLKeywords()));
    }
    
    @Test
    public void assertGetNumericFunctions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    public void assertGetStringFunctions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    public void assertGetSystemFunctions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getSystemFunctions(), is(databaseMetaData.getSystemFunctions()));
    }
    
    @Test
    public void assertGetTimeDateFunctions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getTimeDateFunctions(), is(databaseMetaData.getTimeDateFunctions()));
    }
    
    @Test
    public void assertGetSearchStringEscape() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getSearchStringEscape(), is(databaseMetaData.getSearchStringEscape()));
    }
    
    @Test
    public void assertGetExtraNameCharacters() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getExtraNameCharacters(), is(databaseMetaData.getExtraNameCharacters()));
    }
    
    @Test
    public void assertSupportsAlterTableWithAddColumn() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsAlterTableWithAddColumn(), is(databaseMetaData.supportsAlterTableWithAddColumn()));
    }
    
    @Test
    public void assertSupportsAlterTableWithDropColumn() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsAlterTableWithDropColumn(), is(databaseMetaData.supportsAlterTableWithDropColumn()));
    }
    
    @Test
    public void assertSupportsColumnAliasing() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsColumnAliasing(), is(databaseMetaData.supportsColumnAliasing()));
    }
    
    @Test
    public void assertNullPlusNonNullIsNull() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.nullPlusNonNullIsNull(), is(databaseMetaData.nullPlusNonNullIsNull()));
    }
    
    @Test
    public void assertSupportsConvert() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsConvert(), is(databaseMetaData.supportsConvert()));
        assertThat(masterSlaveDatabaseMetaData.supportsConvert(Types.INTEGER, Types.FLOAT), is(databaseMetaData.supportsConvert()));
    }
    
    @Test
    public void assertSupportsTableCorrelationNames() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsTableCorrelationNames(), is(databaseMetaData.supportsTableCorrelationNames()));
    }
    
    @Test
    public void assertSupportsDifferentTableCorrelationNames() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsDifferentTableCorrelationNames(), is(databaseMetaData.supportsDifferentTableCorrelationNames()));
    }
    
    @Test
    public void assertSupportsExpressionsInOrderBy() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsExpressionsInOrderBy(), is(databaseMetaData.supportsExpressionsInOrderBy()));
    }
    
    @Test
    public void assertSupportsOrderByUnrelated() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsOrderByUnrelated(), is(databaseMetaData.supportsOrderByUnrelated()));
    }
    
    @Test
    public void assertSupportsGroupBy() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsGroupBy(), is(databaseMetaData.supportsGroupBy()));
    }
    
    @Test
    public void assertSupportsGroupByUnrelated() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsGroupByUnrelated(), is(databaseMetaData.supportsGroupByUnrelated()));
    }
    
    @Test
    public void assertSupportsGroupByBeyondSelect() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsGroupByBeyondSelect(), is(databaseMetaData.supportsGroupByBeyondSelect()));
    }
    
    @Test
    public void assertSupportsLikeEscapeClause() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsLikeEscapeClause(), is(databaseMetaData.supportsLikeEscapeClause()));
    }
    
    @Test
    public void assertSupportsMultipleResultSets() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsMultipleResultSets(), is(databaseMetaData.supportsMultipleResultSets()));
    }
    
    @Test
    public void assertSupportsMultipleTransactions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsMultipleTransactions(), is(databaseMetaData.supportsMultipleTransactions()));
    }
    
    @Test
    public void assertSupportsNonNullableColumns() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsNonNullableColumns(), is(databaseMetaData.supportsNonNullableColumns()));
    }
    
    @Test
    public void assertSupportsMinimumSQLGrammar() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsMinimumSQLGrammar(), is(databaseMetaData.supportsMinimumSQLGrammar()));
    }
    
    @Test
    public void assertSupportsCoreSQLGrammar() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsCoreSQLGrammar(), is(databaseMetaData.supportsCoreSQLGrammar()));
    }
    
    @Test
    public void assertSupportsExtendedSQLGrammar() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsExtendedSQLGrammar(), is(databaseMetaData.supportsExtendedSQLGrammar()));
    }
    
    @Test
    public void assertSupportsANSI92EntryLevelSQL() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsANSI92EntryLevelSQL(), is(databaseMetaData.supportsANSI92EntryLevelSQL()));
    }
    
    @Test
    public void assertSupportsANSI92IntermediateSQL() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsANSI92IntermediateSQL(), is(databaseMetaData.supportsANSI92IntermediateSQL()));
    }
    
    @Test
    public void assertSupportsANSI92FullSQL() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsANSI92FullSQL(), is(databaseMetaData.supportsANSI92FullSQL()));
    }
    
    @Test
    public void assertSupportsIntegrityEnhancementFacility() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsIntegrityEnhancementFacility(), is(databaseMetaData.supportsIntegrityEnhancementFacility()));
    }
    
    @Test
    public void assertSupportsOuterJoins() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsOuterJoins(), is(databaseMetaData.supportsOuterJoins()));
    }
    
    @Test
    public void assertSupportsFullOuterJoins() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsFullOuterJoins(), is(databaseMetaData.supportsFullOuterJoins()));
    }
    
    @Test
    public void assertSupportsLimitedOuterJoins() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsLimitedOuterJoins(), is(databaseMetaData.supportsLimitedOuterJoins()));
    }
    
    @Test
    public void assertGetSchemaTerm() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getSchemaTerm(), is(databaseMetaData.getSchemaTerm()));
    }
    
    @Test
    public void assertGetProcedureTerm() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getProcedureTerm(), is(databaseMetaData.getProcedureTerm()));
    }
    
    @Test
    public void assertGetCatalogTerm() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getCatalogTerm(), is(databaseMetaData.getCatalogTerm()));
    }
    
    @Test
    public void assertAssertIsCatalogAtStart() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.isCatalogAtStart(), is(databaseMetaData.isCatalogAtStart()));
    }
    
    @Test
    public void assertGetCatalogSeparator() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getCatalogSeparator(), is(databaseMetaData.getCatalogSeparator()));
    }
    
    @Test
    public void assertSupportsSchemasInDataManipulation() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSchemasInDataManipulation(), is(databaseMetaData.supportsSchemasInDataManipulation()));
    }
    
    @Test
    public void assertSupportsSchemasInProcedureCalls() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSchemasInProcedureCalls(), is(databaseMetaData.supportsSchemasInProcedureCalls()));
    }
    
    @Test
    public void assertSupportsSchemasInTableDefinitions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSchemasInTableDefinitions(), is(databaseMetaData.supportsSchemasInTableDefinitions()));
    }
    
    @Test
    public void assertSupportsSchemasInIndexDefinitions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSchemasInIndexDefinitions(), is(databaseMetaData.supportsSchemasInIndexDefinitions()));
    }
    
    @Test
    public void assertSupportsSchemasInPrivilegeDefinitions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSchemasInPrivilegeDefinitions(), is(databaseMetaData.supportsSchemasInPrivilegeDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInDataManipulation() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsCatalogsInDataManipulation(), is(databaseMetaData.supportsCatalogsInDataManipulation()));
    }
    
    @Test
    public void assertSupportsCatalogsInProcedureCalls() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsCatalogsInProcedureCalls(), is(databaseMetaData.supportsCatalogsInProcedureCalls()));
    }
    
    @Test
    public void assertSupportsCatalogsInTableDefinitions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsCatalogsInTableDefinitions(), is(databaseMetaData.supportsCatalogsInTableDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInIndexDefinitions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsCatalogsInIndexDefinitions(), is(databaseMetaData.supportsCatalogsInIndexDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInPrivilegeDefinitions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsCatalogsInPrivilegeDefinitions(), is(databaseMetaData.supportsCatalogsInPrivilegeDefinitions()));
    }
    
    @Test
    public void assertSupportsPositionedDelete() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsPositionedDelete(), is(databaseMetaData.supportsPositionedDelete()));
    }
    
    @Test
    public void assertSupportsPositionedUpdate() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsPositionedUpdate(), is(databaseMetaData.supportsPositionedUpdate()));
    }
    
    @Test
    public void assertSupportsSelectForUpdate() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSelectForUpdate(), is(databaseMetaData.supportsSelectForUpdate()));
    }
    
    @Test
    public void assertSupportsStoredProcedures() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsStoredProcedures(), is(databaseMetaData.supportsStoredProcedures()));
    }
    
    @Test
    public void assertSupportsSubqueriesInComparisons() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSubqueriesInComparisons(), is(databaseMetaData.supportsSubqueriesInComparisons()));
    }
    
    @Test
    public void assertSupportsSubqueriesInExists() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSubqueriesInExists(), is(databaseMetaData.supportsSubqueriesInExists()));
    }
    
    @Test
    public void assertSupportsSubqueriesInIns() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSubqueriesInIns(), is(databaseMetaData.supportsSubqueriesInIns()));
    }
    
    @Test
    public void assertSupportsSubqueriesInQuantifieds() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSubqueriesInQuantifieds(), is(databaseMetaData.supportsSubqueriesInQuantifieds()));
    }
    
    @Test
    public void assertSupportsCorrelatedSubqueries() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsCorrelatedSubqueries(), is(databaseMetaData.supportsCorrelatedSubqueries()));
    }
    
    @Test
    public void assertSupportsUnion() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsUnion(), is(databaseMetaData.supportsUnion()));
    }
    
    @Test
    public void assertSupportsUnionAll() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsUnionAll(), is(databaseMetaData.supportsUnionAll()));
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossCommit() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsOpenCursorsAcrossCommit(), is(databaseMetaData.supportsOpenCursorsAcrossCommit()));
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossRollback() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsOpenCursorsAcrossRollback(), is(databaseMetaData.supportsOpenCursorsAcrossRollback()));
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossCommit() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsOpenStatementsAcrossCommit(), is(databaseMetaData.supportsOpenStatementsAcrossCommit()));
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossRollback() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsOpenStatementsAcrossRollback(), is(databaseMetaData.supportsOpenStatementsAcrossRollback()));
    }
    
    @Test
    public void assertGetMaxBinaryLiteralLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxBinaryLiteralLength(), is(databaseMetaData.getMaxBinaryLiteralLength()));
    }
    
    @Test
    public void assertGetMaxCharLiteralLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxCharLiteralLength(), is(databaseMetaData.getMaxCharLiteralLength()));
    }
    
    @Test
    public void assertGetMaxColumnNameLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxColumnNameLength(), is(databaseMetaData.getMaxColumnNameLength()));
    }
    
    @Test
    public void assertGetMaxColumnsInGroupBy() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxColumnsInGroupBy(), is(databaseMetaData.getMaxColumnsInGroupBy()));
    }
    
    @Test
    public void assertGetMaxColumnsInIndex() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxColumnsInIndex(), is(databaseMetaData.getMaxColumnsInIndex()));
    }
    
    @Test
    public void assertGetMaxColumnsInOrderBy() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxColumnsInOrderBy(), is(databaseMetaData.getMaxColumnsInOrderBy()));
    }
    
    @Test
    public void assertGetMaxColumnsInSelect() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxColumnsInSelect(), is(databaseMetaData.getMaxColumnsInSelect()));
    }
    
    @Test
    public void assertGetMaxColumnsInTable() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxColumnsInTable(), is(databaseMetaData.getMaxColumnsInTable()));
    }
    
    @Test
    public void assertGetMaxConnections() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxConnections(), is(databaseMetaData.getMaxConnections()));
    }
    
    @Test
    public void assertGetMaxCursorNameLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxCursorNameLength(), is(databaseMetaData.getMaxCursorNameLength()));
    }
    
    @Test
    public void assertGetMaxIndexLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxIndexLength(), is(databaseMetaData.getMaxIndexLength()));
    }
    
    @Test
    public void assertGetMaxSchemaNameLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxSchemaNameLength(), is(databaseMetaData.getMaxSchemaNameLength()));
    }
    
    @Test
    public void assertGetMaxProcedureNameLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxProcedureNameLength(), is(databaseMetaData.getMaxProcedureNameLength()));
    }
    
    @Test
    public void assertGetMaxCatalogNameLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxCatalogNameLength(), is(databaseMetaData.getMaxCatalogNameLength()));
    }
    
    @Test
    public void assertGetMaxRowSize() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxRowSize(), is(databaseMetaData.getMaxRowSize()));
    }
    
    @Test
    public void assertDoesMaxRowSizeIncludeBlobs() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.doesMaxRowSizeIncludeBlobs(), is(databaseMetaData.doesMaxRowSizeIncludeBlobs()));
    }
    
    @Test
    public void assertGetMaxStatementLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxStatementLength(), is(databaseMetaData.getMaxStatementLength()));
    }
    
    @Test
    public void assertGetMaxStatements() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxStatements(), is(databaseMetaData.getMaxStatements()));
    }
    
    @Test
    public void assertGetMaxTableNameLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxTableNameLength(), is(databaseMetaData.getMaxTableNameLength()));
    }
    
    @Test
    public void assertGetMaxTablesInSelect() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxTablesInSelect(), is(databaseMetaData.getMaxTablesInSelect()));
    }
    
    @Test
    public void assertGetMaxUserNameLength() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getMaxUserNameLength(), is(databaseMetaData.getMaxUserNameLength()));
    }
    
    @Test
    public void assertGetDefaultTransactionIsolation() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getDefaultTransactionIsolation(), is(databaseMetaData.getDefaultTransactionIsolation()));
    }
    
    @Test
    public void assertSupportsTransactions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsTransactions(), is(databaseMetaData.supportsTransactions()));
    }
    
    @Test
    public void assertSupportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsDataDefinitionAndDataManipulationTransactions(), is(databaseMetaData.supportsDataDefinitionAndDataManipulationTransactions()));
    }
    
    @Test
    public void assertSupportsDataManipulationTransactionsOnly() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsDataManipulationTransactionsOnly(), is(databaseMetaData.supportsDataManipulationTransactionsOnly()));
    }
    
    @Test
    public void assertDataDefinitionCausesTransactionCommit() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.dataDefinitionCausesTransactionCommit(), is(databaseMetaData.dataDefinitionCausesTransactionCommit()));
    }
    
    @Test
    public void assertDataDefinitionIgnoredInTransactions() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.dataDefinitionIgnoredInTransactions(), is(databaseMetaData.dataDefinitionIgnoredInTransactions()));
    }
    
    @Test
    public void assertSupportsBatchUpdates() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsBatchUpdates(), is(databaseMetaData.supportsBatchUpdates()));
    }
    
    @Test
    public void assertSupportsSavepoints() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsSavepoints(), is(databaseMetaData.supportsSavepoints()));
    }
    
    @Test
    public void assertSupportsNamedParameters() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsNamedParameters(), is(databaseMetaData.supportsNamedParameters()));
    }
    
    @Test
    public void assertSupportsMultipleOpenResults() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsMultipleOpenResults(), is(databaseMetaData.supportsMultipleOpenResults()));
    }
    
    @Test
    public void assertSupportsGetGeneratedKeys() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsGetGeneratedKeys(), is(databaseMetaData.supportsGetGeneratedKeys()));
    }
    
    @Test
    public void assertGetResultSetHoldability() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getResultSetHoldability(), is(databaseMetaData.getResultSetHoldability()));
    }
    
    @Test
    public void assertGetSQLStateType() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getSQLStateType(), is(databaseMetaData.getSQLStateType()));
    }
    
    @Test
    public void assertLocatorsUpdateCopy() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.locatorsUpdateCopy(), is(databaseMetaData.locatorsUpdateCopy()));
    }
    
    @Test
    public void assertSupportsStatementPooling() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsStatementPooling(), is(databaseMetaData.supportsStatementPooling()));
    }
    
    @Test
    public void assertSupportsStoredFunctionsUsingCallSyntax() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.supportsStoredFunctionsUsingCallSyntax(), is(databaseMetaData.supportsStoredFunctionsUsingCallSyntax()));
    }
    
    @Test
    public void assertAutoCommitFailureClosesAllResultSets() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.autoCommitFailureClosesAllResultSets(), is(databaseMetaData.autoCommitFailureClosesAllResultSets()));
    }
    
    @Test
    public void assertGetRowIdLifetime() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getRowIdLifetime(), is(databaseMetaData.getRowIdLifetime()));
    }
    
    @Test
    public void assertGeneratedKeyAlwaysReturned() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.generatedKeyAlwaysReturned(), is(databaseMetaData.generatedKeyAlwaysReturned()));
    }
    
    @Test
    public void assertOwnInsertsAreVisible() {
        assertTrue(masterSlaveDatabaseMetaData.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOwnUpdatesAreVisible() {
        assertTrue(masterSlaveDatabaseMetaData.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOwnDeletesAreVisible() {
        assertTrue(masterSlaveDatabaseMetaData.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersInsertsAreVisible() {
        assertTrue(masterSlaveDatabaseMetaData.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersUpdatesAreVisible() {
        assertTrue(masterSlaveDatabaseMetaData.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersDeletesAreVisible() {
        assertTrue(masterSlaveDatabaseMetaData.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertInsertsAreDetected() {
        assertTrue(masterSlaveDatabaseMetaData.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertUpdatesAreDetected() {
        assertTrue(masterSlaveDatabaseMetaData.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertDeletesAreDetected() {
        assertTrue(masterSlaveDatabaseMetaData.deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetType() {
        assertTrue(masterSlaveDatabaseMetaData.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetConcurrency() {
        assertTrue(masterSlaveDatabaseMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetHoldability() {
        assertTrue(masterSlaveDatabaseMetaData.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));
    }
    
    @Test
    public void assertSupportsTransactionIsolationLevel() {
        assertTrue(masterSlaveDatabaseMetaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE));
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        assertThat(masterSlaveDatabaseMetaData.getConnection(), is(dataSource.getConnection()));
    }
    
    @Test
    public void assertGetSuperTypes() throws SQLException {
        when(databaseMetaData.getSuperTypes("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getSuperTypes("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSuperTables() throws SQLException {
        when(databaseMetaData.getSuperTables("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getSuperTables("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetAttributes() throws SQLException {
        when(databaseMetaData.getAttributes("test", null, null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getAttributes("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetProcedures() throws SQLException {
        when(databaseMetaData.getProcedures("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getProcedures("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetProcedureColumns() throws SQLException {
        when(databaseMetaData.getProcedureColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getProcedureColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTables() throws SQLException {
        when(databaseMetaData.getTables("test", null, TABLE_NAME, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getTables("test", null, TABLE_NAME, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSchemas() throws SQLException {
        when(databaseMetaData.getSchemas()).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getSchemas(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSchemasForCatalogAndSchemaPattern() throws SQLException {
        when(databaseMetaData.getSchemas("test", null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getSchemas("test", null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetCatalogs() throws SQLException {
        when(databaseMetaData.getCatalogs()).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getCatalogs(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTableTypes() throws SQLException {
        when(databaseMetaData.getTableTypes()).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getTableTypes(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetColumns() throws SQLException {
        when(databaseMetaData.getColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetColumnPrivileges() throws SQLException {
        when(databaseMetaData.getColumnPrivileges("test", null, null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getColumnPrivileges("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTablePrivileges() throws SQLException {
        when(databaseMetaData.getTablePrivileges("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getTablePrivileges("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetBestRowIdentifier() throws SQLException {
        when(databaseMetaData.getBestRowIdentifier("test", null, null, 1, true)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getBestRowIdentifier("test", null, null, 1, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetVersionColumns() throws SQLException {
        when(databaseMetaData.getVersionColumns("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getVersionColumns("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetPrimaryKeys() throws SQLException {
        when(databaseMetaData.getPrimaryKeys("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getPrimaryKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetImportedKeys() throws SQLException {
        when(databaseMetaData.getImportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getImportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetExportedKeys() throws SQLException {
        when(databaseMetaData.getExportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getExportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetCrossReference() throws SQLException {
        when(databaseMetaData.getCrossReference("test", null, null, null, null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getCrossReference("test", null, null, null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTypeInfo() throws SQLException {
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getTypeInfo(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetIndexInfo() throws SQLException {
        when(databaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetUDTs() throws SQLException {
        when(databaseMetaData.getUDTs("test", null, null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getUDTs("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetClientInfoProperties() throws SQLException {
        when(databaseMetaData.getClientInfoProperties()).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getClientInfoProperties(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetFunctions() throws SQLException {
        when(databaseMetaData.getFunctions("test", null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getFunctions("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetFunctionColumns() throws SQLException {
        when(databaseMetaData.getFunctionColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getFunctionColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetPseudoColumns() throws SQLException {
        when(databaseMetaData.getPseudoColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(masterSlaveDatabaseMetaData.getPseudoColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
}
