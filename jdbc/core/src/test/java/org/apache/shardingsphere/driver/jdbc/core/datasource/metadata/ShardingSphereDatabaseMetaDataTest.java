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

package org.apache.shardingsphere.driver.jdbc.core.datasource.metadata;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.DatabaseMetaDataResultSet;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingSphereDatabaseMetaDataTest {
    
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
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereConnection shardingSphereConnection;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    private final Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    private ShardingSphereDatabaseMetaData shardingSphereDatabaseMetaData;
    
    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMap.put(DATA_SOURCE_NAME, dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(resultSet.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
        when(shardingSphereConnection.getDatabaseConnectionManager().getRandomPhysicalDataSourceName()).thenReturn(DATA_SOURCE_NAME);
        when(shardingSphereConnection.getDatabaseConnectionManager().getRandomConnection()).thenReturn(connection);
        when(shardingSphereConnection.getContextManager().getMetaDataContexts()).thenReturn(metaDataContexts);
        when(shardingSphereConnection.getContextManager().getDataSourceMap(DefaultDatabase.LOGIC_NAME)).thenReturn(dataSourceMap);
        when(shardingSphereConnection.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase(shardingSphereConnection.getDatabaseName())).thenReturn(database);
        ShardingRule shardingRule = mockShardingRule();
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(shardingRule));
        shardingSphereDatabaseMetaData = new ShardingSphereDatabaseMetaData(shardingSphereConnection);
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfig = new ShardingTableRuleConfiguration(TABLE_NAME, DATA_SOURCE_NAME + "." + TABLE_NAME);
        ruleConfig.setTables(Collections.singletonList(shardingTableRuleConfig));
        return new ShardingRule(ruleConfig, Collections.singletonList(DATA_SOURCE_NAME), mock(InstanceContext.class));
    }
    
    @Test
    void assertGetURL() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getURL(), is(databaseMetaData.getURL()));
    }
    
    @Test
    void assertGetUserName() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getUserName(), is(databaseMetaData.getUserName()));
    }
    
    @Test
    void assertGetDatabaseProductName() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDatabaseProductName(), is(databaseMetaData.getDatabaseProductName()));
    }
    
    @Test
    void assertGetDatabaseProductVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDatabaseProductVersion(), is(databaseMetaData.getDatabaseProductVersion()));
    }
    
    @Test
    void assertGetDriverName() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDriverName(), is(databaseMetaData.getDriverName()));
    }
    
    @Test
    void assertGetDriverVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDriverVersion(), is(databaseMetaData.getDriverVersion()));
    }
    
    @Test
    void assertGetDriverMajorVersion() {
        assertThat(shardingSphereDatabaseMetaData.getDriverMajorVersion(), is(databaseMetaData.getDriverMajorVersion()));
    }
    
    @Test
    void assertGetDriverMinorVersion() {
        assertThat(shardingSphereDatabaseMetaData.getDriverMinorVersion(), is(databaseMetaData.getDriverMinorVersion()));
    }
    
    @Test
    void assertGetDatabaseMajorVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDatabaseMajorVersion(), is(databaseMetaData.getDatabaseMajorVersion()));
    }
    
    @Test
    void assertGetDatabaseMinorVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDatabaseMinorVersion(), is(databaseMetaData.getDatabaseMinorVersion()));
    }
    
    @Test
    void assertGetJDBCMajorVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getJDBCMajorVersion(), is(databaseMetaData.getJDBCMajorVersion()));
    }
    
    @Test
    void assertGetJDBCMinorVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getJDBCMinorVersion(), is(databaseMetaData.getJDBCMinorVersion()));
    }
    
    @Test
    void assertAssertIsReadOnly() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.isReadOnly(), is(databaseMetaData.isReadOnly()));
    }
    
    @Test
    void assertAllProceduresAreCallable() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.allProceduresAreCallable(), is(databaseMetaData.allProceduresAreCallable()));
    }
    
    @Test
    void assertAllTablesAreSelectable() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.allTablesAreSelectable(), is(databaseMetaData.allTablesAreSelectable()));
    }
    
    @Test
    void assertNullsAreSortedHigh() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullsAreSortedHigh(), is(databaseMetaData.nullsAreSortedHigh()));
    }
    
    @Test
    void assertNullsAreSortedLow() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullsAreSortedLow(), is(databaseMetaData.nullsAreSortedLow()));
    }
    
    @Test
    void assertNullsAreSortedAtStart() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullsAreSortedAtStart(), is(databaseMetaData.nullsAreSortedAtStart()));
    }
    
    @Test
    void assertNullsAreSortedAtEnd() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullsAreSortedAtEnd(), is(databaseMetaData.nullsAreSortedAtEnd()));
    }
    
    @Test
    void assertUsesLocalFiles() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.usesLocalFiles(), is(databaseMetaData.usesLocalFiles()));
    }
    
    @Test
    void assertUsesLocalFilePerTable() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.usesLocalFilePerTable(), is(databaseMetaData.usesLocalFilePerTable()));
    }
    
    @Test
    void assertSupportsMixedCaseIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMixedCaseIdentifiers(), is(databaseMetaData.supportsMixedCaseIdentifiers()));
    }
    
    @Test
    void assertStoresUpperCaseIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesUpperCaseIdentifiers(), is(databaseMetaData.storesUpperCaseIdentifiers()));
    }
    
    @Test
    void assertStoresLowerCaseIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesLowerCaseIdentifiers(), is(databaseMetaData.storesLowerCaseIdentifiers()));
    }
    
    @Test
    void assertStoresMixedCaseIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesMixedCaseIdentifiers(), is(databaseMetaData.storesMixedCaseIdentifiers()));
    }
    
    @Test
    void assertSupportsMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMixedCaseQuotedIdentifiers(), is(databaseMetaData.supportsMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    void assertStoresUpperCaseQuotedIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesUpperCaseQuotedIdentifiers(), is(databaseMetaData.storesUpperCaseQuotedIdentifiers()));
    }
    
    @Test
    void assertStoresLowerCaseQuotedIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesLowerCaseQuotedIdentifiers(), is(databaseMetaData.storesLowerCaseQuotedIdentifiers()));
    }
    
    @Test
    void assertStoresMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesMixedCaseQuotedIdentifiers(), is(databaseMetaData.storesMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    void assertGetIdentifierQuoteString() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getIdentifierQuoteString(), is(databaseMetaData.getIdentifierQuoteString()));
    }
    
    @Test
    void assertGetSQLKeywords() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSQLKeywords(), is(databaseMetaData.getSQLKeywords()));
    }
    
    @Test
    void assertGetNumericFunctions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    void assertGetStringFunctions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    void assertGetSystemFunctions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSystemFunctions(), is(databaseMetaData.getSystemFunctions()));
    }
    
    @Test
    void assertGetTimeDateFunctions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getTimeDateFunctions(), is(databaseMetaData.getTimeDateFunctions()));
    }
    
    @Test
    void assertGetSearchStringEscape() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSearchStringEscape(), is(databaseMetaData.getSearchStringEscape()));
    }
    
    @Test
    void assertGetExtraNameCharacters() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getExtraNameCharacters(), is(databaseMetaData.getExtraNameCharacters()));
    }
    
    @Test
    void assertSupportsAlterTableWithAddColumn() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsAlterTableWithAddColumn(), is(databaseMetaData.supportsAlterTableWithAddColumn()));
    }
    
    @Test
    void assertSupportsAlterTableWithDropColumn() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsAlterTableWithDropColumn(), is(databaseMetaData.supportsAlterTableWithDropColumn()));
    }
    
    @Test
    void assertSupportsColumnAliasing() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsColumnAliasing(), is(databaseMetaData.supportsColumnAliasing()));
    }
    
    @Test
    void assertNullPlusNonNullIsNull() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullPlusNonNullIsNull(), is(databaseMetaData.nullPlusNonNullIsNull()));
    }
    
    @Test
    void assertSupportsConvert() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsConvert(), is(databaseMetaData.supportsConvert()));
        assertThat(shardingSphereDatabaseMetaData.supportsConvert(Types.INTEGER, Types.FLOAT), is(databaseMetaData.supportsConvert()));
    }
    
    @Test
    void assertSupportsTableCorrelationNames() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsTableCorrelationNames(), is(databaseMetaData.supportsTableCorrelationNames()));
    }
    
    @Test
    void assertSupportsDifferentTableCorrelationNames() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsDifferentTableCorrelationNames(), is(databaseMetaData.supportsDifferentTableCorrelationNames()));
    }
    
    @Test
    void assertSupportsExpressionsInOrderBy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsExpressionsInOrderBy(), is(databaseMetaData.supportsExpressionsInOrderBy()));
    }
    
    @Test
    void assertSupportsOrderByUnrelated() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOrderByUnrelated(), is(databaseMetaData.supportsOrderByUnrelated()));
    }
    
    @Test
    void assertSupportsGroupBy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsGroupBy(), is(databaseMetaData.supportsGroupBy()));
    }
    
    @Test
    void assertSupportsGroupByUnrelated() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsGroupByUnrelated(), is(databaseMetaData.supportsGroupByUnrelated()));
    }
    
    @Test
    void assertSupportsGroupByBeyondSelect() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsGroupByBeyondSelect(), is(databaseMetaData.supportsGroupByBeyondSelect()));
    }
    
    @Test
    void assertSupportsLikeEscapeClause() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsLikeEscapeClause(), is(databaseMetaData.supportsLikeEscapeClause()));
    }
    
    @Test
    void assertSupportsMultipleResultSets() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMultipleResultSets(), is(databaseMetaData.supportsMultipleResultSets()));
    }
    
    @Test
    void assertSupportsMultipleTransactions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMultipleTransactions(), is(databaseMetaData.supportsMultipleTransactions()));
    }
    
    @Test
    void assertSupportsNonNullableColumns() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsNonNullableColumns(), is(databaseMetaData.supportsNonNullableColumns()));
    }
    
    @Test
    void assertSupportsMinimumSQLGrammar() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMinimumSQLGrammar(), is(databaseMetaData.supportsMinimumSQLGrammar()));
    }
    
    @Test
    void assertSupportsCoreSQLGrammar() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCoreSQLGrammar(), is(databaseMetaData.supportsCoreSQLGrammar()));
    }
    
    @Test
    void assertSupportsExtendedSQLGrammar() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsExtendedSQLGrammar(), is(databaseMetaData.supportsExtendedSQLGrammar()));
    }
    
    @Test
    void assertSupportsANSI92EntryLevelSQL() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsANSI92EntryLevelSQL(), is(databaseMetaData.supportsANSI92EntryLevelSQL()));
    }
    
    @Test
    void assertSupportsANSI92IntermediateSQL() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsANSI92IntermediateSQL(), is(databaseMetaData.supportsANSI92IntermediateSQL()));
    }
    
    @Test
    void assertSupportsANSI92FullSQL() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsANSI92FullSQL(), is(databaseMetaData.supportsANSI92FullSQL()));
    }
    
    @Test
    void assertSupportsIntegrityEnhancementFacility() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsIntegrityEnhancementFacility(), is(databaseMetaData.supportsIntegrityEnhancementFacility()));
    }
    
    @Test
    void assertSupportsOuterJoins() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOuterJoins(), is(databaseMetaData.supportsOuterJoins()));
    }
    
    @Test
    void assertSupportsFullOuterJoins() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsFullOuterJoins(), is(databaseMetaData.supportsFullOuterJoins()));
    }
    
    @Test
    void assertSupportsLimitedOuterJoins() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsLimitedOuterJoins(), is(databaseMetaData.supportsLimitedOuterJoins()));
    }
    
    @Test
    void assertGetSchemaTerm() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSchemaTerm(), is(databaseMetaData.getSchemaTerm()));
    }
    
    @Test
    void assertGetProcedureTerm() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getProcedureTerm(), is(databaseMetaData.getProcedureTerm()));
    }
    
    @Test
    void assertGetCatalogTerm() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getCatalogTerm(), is(databaseMetaData.getCatalogTerm()));
    }
    
    @Test
    void assertAssertIsCatalogAtStart() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.isCatalogAtStart(), is(databaseMetaData.isCatalogAtStart()));
    }
    
    @Test
    void assertGetCatalogSeparator() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getCatalogSeparator(), is(databaseMetaData.getCatalogSeparator()));
    }
    
    @Test
    void assertSupportsSchemasInDataManipulation() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInDataManipulation(), is(databaseMetaData.supportsSchemasInDataManipulation()));
    }
    
    @Test
    void assertSupportsSchemasInProcedureCalls() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInProcedureCalls(), is(databaseMetaData.supportsSchemasInProcedureCalls()));
    }
    
    @Test
    void assertSupportsSchemasInTableDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInTableDefinitions(), is(databaseMetaData.supportsSchemasInTableDefinitions()));
    }
    
    @Test
    void assertSupportsSchemasInIndexDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInIndexDefinitions(), is(databaseMetaData.supportsSchemasInIndexDefinitions()));
    }
    
    @Test
    void assertSupportsSchemasInPrivilegeDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInPrivilegeDefinitions(), is(databaseMetaData.supportsSchemasInPrivilegeDefinitions()));
    }
    
    @Test
    void assertSupportsCatalogsInDataManipulation() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInDataManipulation(), is(databaseMetaData.supportsCatalogsInDataManipulation()));
    }
    
    @Test
    void assertSupportsCatalogsInProcedureCalls() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInProcedureCalls(), is(databaseMetaData.supportsCatalogsInProcedureCalls()));
    }
    
    @Test
    void assertSupportsCatalogsInTableDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInTableDefinitions(), is(databaseMetaData.supportsCatalogsInTableDefinitions()));
    }
    
    @Test
    void assertSupportsCatalogsInIndexDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInIndexDefinitions(), is(databaseMetaData.supportsCatalogsInIndexDefinitions()));
    }
    
    @Test
    void assertSupportsCatalogsInPrivilegeDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInPrivilegeDefinitions(), is(databaseMetaData.supportsCatalogsInPrivilegeDefinitions()));
    }
    
    @Test
    void assertSupportsPositionedDelete() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsPositionedDelete(), is(databaseMetaData.supportsPositionedDelete()));
    }
    
    @Test
    void assertSupportsPositionedUpdate() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsPositionedUpdate(), is(databaseMetaData.supportsPositionedUpdate()));
    }
    
    @Test
    void assertSupportsSelectForUpdate() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSelectForUpdate(), is(databaseMetaData.supportsSelectForUpdate()));
    }
    
    @Test
    void assertSupportsStoredProcedures() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsStoredProcedures(), is(databaseMetaData.supportsStoredProcedures()));
    }
    
    @Test
    void assertSupportsSubqueriesInComparisons() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSubqueriesInComparisons(), is(databaseMetaData.supportsSubqueriesInComparisons()));
    }
    
    @Test
    void assertSupportsSubqueriesInExists() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSubqueriesInExists(), is(databaseMetaData.supportsSubqueriesInExists()));
    }
    
    @Test
    void assertSupportsSubqueriesInIns() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSubqueriesInIns(), is(databaseMetaData.supportsSubqueriesInIns()));
    }
    
    @Test
    void assertSupportsSubqueriesInQuantifieds() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSubqueriesInQuantifieds(), is(databaseMetaData.supportsSubqueriesInQuantifieds()));
    }
    
    @Test
    void assertSupportsCorrelatedSubqueries() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCorrelatedSubqueries(), is(databaseMetaData.supportsCorrelatedSubqueries()));
    }
    
    @Test
    void assertSupportsUnion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsUnion(), is(databaseMetaData.supportsUnion()));
    }
    
    @Test
    void assertSupportsUnionAll() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsUnionAll(), is(databaseMetaData.supportsUnionAll()));
    }
    
    @Test
    void assertSupportsOpenCursorsAcrossCommit() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOpenCursorsAcrossCommit(), is(databaseMetaData.supportsOpenCursorsAcrossCommit()));
    }
    
    @Test
    void assertSupportsOpenCursorsAcrossRollback() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOpenCursorsAcrossRollback(), is(databaseMetaData.supportsOpenCursorsAcrossRollback()));
    }
    
    @Test
    void assertSupportsOpenStatementsAcrossCommit() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOpenStatementsAcrossCommit(), is(databaseMetaData.supportsOpenStatementsAcrossCommit()));
    }
    
    @Test
    void assertSupportsOpenStatementsAcrossRollback() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOpenStatementsAcrossRollback(), is(databaseMetaData.supportsOpenStatementsAcrossRollback()));
    }
    
    @Test
    void assertGetMaxBinaryLiteralLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxBinaryLiteralLength(), is(databaseMetaData.getMaxBinaryLiteralLength()));
    }
    
    @Test
    void assertGetMaxCharLiteralLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxCharLiteralLength(), is(databaseMetaData.getMaxCharLiteralLength()));
    }
    
    @Test
    void assertGetMaxColumnNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnNameLength(), is(databaseMetaData.getMaxColumnNameLength()));
    }
    
    @Test
    void assertGetMaxColumnsInGroupBy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInGroupBy(), is(databaseMetaData.getMaxColumnsInGroupBy()));
    }
    
    @Test
    void assertGetMaxColumnsInIndex() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInIndex(), is(databaseMetaData.getMaxColumnsInIndex()));
    }
    
    @Test
    void assertGetMaxColumnsInOrderBy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInOrderBy(), is(databaseMetaData.getMaxColumnsInOrderBy()));
    }
    
    @Test
    void assertGetMaxColumnsInSelect() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInSelect(), is(databaseMetaData.getMaxColumnsInSelect()));
    }
    
    @Test
    void assertGetMaxColumnsInTable() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInTable(), is(databaseMetaData.getMaxColumnsInTable()));
    }
    
    @Test
    void assertGetMaxConnections() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxConnections(), is(databaseMetaData.getMaxConnections()));
    }
    
    @Test
    void assertGetMaxCursorNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxCursorNameLength(), is(databaseMetaData.getMaxCursorNameLength()));
    }
    
    @Test
    void assertGetMaxIndexLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxIndexLength(), is(databaseMetaData.getMaxIndexLength()));
    }
    
    @Test
    void assertGetMaxSchemaNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxSchemaNameLength(), is(databaseMetaData.getMaxSchemaNameLength()));
    }
    
    @Test
    void assertGetMaxProcedureNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxProcedureNameLength(), is(databaseMetaData.getMaxProcedureNameLength()));
    }
    
    @Test
    void assertGetMaxCatalogNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxCatalogNameLength(), is(databaseMetaData.getMaxCatalogNameLength()));
    }
    
    @Test
    void assertGetMaxRowSize() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxRowSize(), is(databaseMetaData.getMaxRowSize()));
    }
    
    @Test
    void assertDoesMaxRowSizeIncludeBlobs() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.doesMaxRowSizeIncludeBlobs(), is(databaseMetaData.doesMaxRowSizeIncludeBlobs()));
    }
    
    @Test
    void assertGetMaxStatementLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxStatementLength(), is(databaseMetaData.getMaxStatementLength()));
    }
    
    @Test
    void assertGetMaxStatements() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxStatements(), is(databaseMetaData.getMaxStatements()));
    }
    
    @Test
    void assertGetMaxTableNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxTableNameLength(), is(databaseMetaData.getMaxTableNameLength()));
    }
    
    @Test
    void assertGetMaxTablesInSelect() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxTablesInSelect(), is(databaseMetaData.getMaxTablesInSelect()));
    }
    
    @Test
    void assertGetMaxUserNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxUserNameLength(), is(databaseMetaData.getMaxUserNameLength()));
    }
    
    @Test
    void assertGetDefaultTransactionIsolation() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDefaultTransactionIsolation(), is(databaseMetaData.getDefaultTransactionIsolation()));
    }
    
    @Test
    void assertSupportsTransactions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsTransactions(), is(databaseMetaData.supportsTransactions()));
    }
    
    @Test
    void assertSupportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsDataDefinitionAndDataManipulationTransactions(), is(databaseMetaData.supportsDataDefinitionAndDataManipulationTransactions()));
    }
    
    @Test
    void assertSupportsDataManipulationTransactionsOnly() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsDataManipulationTransactionsOnly(), is(databaseMetaData.supportsDataManipulationTransactionsOnly()));
    }
    
    @Test
    void assertDataDefinitionCausesTransactionCommit() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.dataDefinitionCausesTransactionCommit(), is(databaseMetaData.dataDefinitionCausesTransactionCommit()));
    }
    
    @Test
    void assertDataDefinitionIgnoredInTransactions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.dataDefinitionIgnoredInTransactions(), is(databaseMetaData.dataDefinitionIgnoredInTransactions()));
    }
    
    @Test
    void assertSupportsBatchUpdates() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsBatchUpdates(), is(databaseMetaData.supportsBatchUpdates()));
    }
    
    @Test
    void assertSupportsSavepoints() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSavepoints(), is(databaseMetaData.supportsSavepoints()));
    }
    
    @Test
    void assertSupportsNamedParameters() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsNamedParameters(), is(databaseMetaData.supportsNamedParameters()));
    }
    
    @Test
    void assertSupportsMultipleOpenResults() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMultipleOpenResults(), is(databaseMetaData.supportsMultipleOpenResults()));
    }
    
    @Test
    void assertSupportsGetGeneratedKeys() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsGetGeneratedKeys(), is(databaseMetaData.supportsGetGeneratedKeys()));
    }
    
    @Test
    void assertGetResultSetHoldability() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getResultSetHoldability(), is(databaseMetaData.getResultSetHoldability()));
    }
    
    @Test
    void assertGetSQLStateType() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSQLStateType(), is(databaseMetaData.getSQLStateType()));
    }
    
    @Test
    void assertLocatorsUpdateCopy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.locatorsUpdateCopy(), is(databaseMetaData.locatorsUpdateCopy()));
    }
    
    @Test
    void assertSupportsStatementPooling() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsStatementPooling(), is(databaseMetaData.supportsStatementPooling()));
    }
    
    @Test
    void assertSupportsStoredFunctionsUsingCallSyntax() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsStoredFunctionsUsingCallSyntax(), is(databaseMetaData.supportsStoredFunctionsUsingCallSyntax()));
    }
    
    @Test
    void assertAutoCommitFailureClosesAllResultSets() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.autoCommitFailureClosesAllResultSets(), is(databaseMetaData.autoCommitFailureClosesAllResultSets()));
    }
    
    @Test
    void assertGetRowIdLifetime() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getRowIdLifetime(), is(databaseMetaData.getRowIdLifetime()));
    }
    
    @Test
    void assertGeneratedKeyAlwaysReturned() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.generatedKeyAlwaysReturned(), is(databaseMetaData.generatedKeyAlwaysReturned()));
    }
    
    @Test
    void assertOwnInsertsAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertOwnUpdatesAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertOwnDeletesAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertOthersInsertsAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertOthersUpdatesAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertOthersDeletesAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertInsertsAreDetected() {
        assertTrue(shardingSphereDatabaseMetaData.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertUpdatesAreDetected() {
        assertTrue(shardingSphereDatabaseMetaData.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertDeletesAreDetected() {
        assertTrue(shardingSphereDatabaseMetaData.deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertSupportsResultSetType() {
        assertTrue(shardingSphereDatabaseMetaData.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    void assertSupportsResultSetConcurrency() {
        assertTrue(shardingSphereDatabaseMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    void assertSupportsResultSetHoldability() {
        assertTrue(shardingSphereDatabaseMetaData.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));
    }
    
    @Test
    void assertSupportsTransactionIsolationLevel() {
        assertTrue(shardingSphereDatabaseMetaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE));
    }
    
    @Test
    void assertGetConnection() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getConnection(), is(dataSource.getConnection()));
    }
    
    @Test
    void assertGetSuperTypes() throws SQLException {
        when(databaseMetaData.getSuperTypes("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getSuperTypes("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetSuperTables() throws SQLException {
        when(databaseMetaData.getSuperTables("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getSuperTables("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetAttributes() throws SQLException {
        when(databaseMetaData.getAttributes("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getAttributes("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetProcedures() throws SQLException {
        when(databaseMetaData.getProcedures("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getProcedures("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetProcedureColumns() throws SQLException {
        when(databaseMetaData.getProcedureColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getProcedureColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetTables() throws SQLException {
        when(databaseMetaData.getTables("test", null, "%" + TABLE_NAME + "%", null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getTables("test", null, TABLE_NAME, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetSchemas() throws SQLException {
        when(databaseMetaData.getSchemas()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getSchemas(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetSchemasForCatalogAndSchemaPattern() throws SQLException {
        when(databaseMetaData.getSchemas("test", null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getSchemas("test", null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetCatalogs() throws SQLException {
        when(databaseMetaData.getCatalogs()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getCatalogs(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetTableTypes() throws SQLException {
        when(databaseMetaData.getTableTypes()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getTableTypes(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetColumns() throws SQLException {
        when(databaseMetaData.getColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetColumnPrivileges() throws SQLException {
        when(databaseMetaData.getColumnPrivileges("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getColumnPrivileges("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetTablePrivileges() throws SQLException {
        when(databaseMetaData.getTablePrivileges("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getTablePrivileges("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetBestRowIdentifier() throws SQLException {
        when(databaseMetaData.getBestRowIdentifier("test", null, null, 1, true)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getBestRowIdentifier("test", null, null, 1, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetVersionColumns() throws SQLException {
        when(databaseMetaData.getVersionColumns("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getVersionColumns("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetPrimaryKeys() throws SQLException {
        when(databaseMetaData.getPrimaryKeys("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getPrimaryKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetImportedKeys() throws SQLException {
        when(databaseMetaData.getImportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getImportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetExportedKeys() throws SQLException {
        when(databaseMetaData.getExportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getExportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetCrossReference() throws SQLException {
        when(databaseMetaData.getCrossReference("test", null, null, null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getCrossReference("test", null, null, null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetTypeInfo() throws SQLException {
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getTypeInfo(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetIndexInfo() throws SQLException {
        when(databaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetUDTs() throws SQLException {
        when(databaseMetaData.getUDTs("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getUDTs("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetClientInfoProperties() throws SQLException {
        when(databaseMetaData.getClientInfoProperties()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getClientInfoProperties(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetFunctions() throws SQLException {
        when(databaseMetaData.getFunctions("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getFunctions("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetFunctionColumns() throws SQLException {
        when(databaseMetaData.getFunctionColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getFunctionColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    void assertGetPseudoColumns() throws SQLException {
        when(databaseMetaData.getPseudoColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getPseudoColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
}
