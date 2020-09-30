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

import com.google.common.collect.LinkedHashMultimap;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.DatabaseMetaDataResultSet;
import org.apache.shardingsphere.infra.context.schema.SchemaContext;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.runtime.CachedDatabaseMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
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
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSphereDatabaseMetaDataTest {
    
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
    private ShardingSphereConnection shardingSphereConnection;
    
    @Mock
    private SchemaContexts schemaContexts;
    
    private final Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
    
    private ShardingSphereDatabaseMetaData shardingSphereDatabaseMetaData;
    
    @Before
    public void setUp() throws SQLException {
        dataSourceMap.put(DATA_SOURCE_NAME, dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(resultSet.getMetaData()).thenReturn(mock(ResultSetMetaData.class));
        CachedDatabaseMetaData cachedDatabaseMetaData = new CachedDatabaseMetaData(databaseMetaData);
        when(shardingSphereConnection.getCachedConnections()).thenReturn(LinkedHashMultimap.create());
        when(shardingSphereConnection.getConnection(anyString())).thenReturn(connection);
        when(shardingSphereConnection.getDataSourceMap()).thenReturn(dataSourceMap);
        when(shardingSphereConnection.getSchemaContexts()).thenReturn(schemaContexts);
        SchemaContext schemaContext = mock(SchemaContext.class, RETURNS_DEEP_STUBS);
        when(schemaContexts.getDefaultSchemaContext()).thenReturn(schemaContext);
        when(schemaContext.getRuntimeContext().getCachedDatabaseMetaData()).thenReturn(cachedDatabaseMetaData);
        when(schemaContext.getSchema().getRules()).thenReturn(Collections.singletonList(mockShardingRule()));
        shardingSphereDatabaseMetaData = new ShardingSphereDatabaseMetaData(shardingSphereConnection);
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration shardingTableRuleConfiguration = new ShardingTableRuleConfiguration(TABLE_NAME, DATA_SOURCE_NAME + "." + TABLE_NAME);
        ruleConfig.setTables(Collections.singletonList(shardingTableRuleConfiguration));
        return new ShardingRule(ruleConfig, Collections.singletonList(DATA_SOURCE_NAME));
    }
    
    @Test
    public void assertGetURL() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getURL(), is(databaseMetaData.getURL()));
    }
    
    @Test
    public void assertGetUserName() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getUserName(), is(databaseMetaData.getUserName()));
    }
    
    @Test
    public void assertGetDatabaseProductName() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDatabaseProductName(), is(databaseMetaData.getDatabaseProductName()));
    }
    
    @Test
    public void assertGetDatabaseProductVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDatabaseProductVersion(), is(databaseMetaData.getDatabaseProductVersion()));
    }
    
    @Test
    public void assertGetDriverName() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDriverName(), is(databaseMetaData.getDriverName()));
    }
    
    @Test
    public void assertGetDriverVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDriverVersion(), is(databaseMetaData.getDriverVersion()));
    }
    
    @Test
    public void assertGetDriverMajorVersion() {
        assertThat(shardingSphereDatabaseMetaData.getDriverMajorVersion(), is(databaseMetaData.getDriverMajorVersion()));
    }
    
    @Test
    public void assertGetDriverMinorVersion() {
        assertThat(shardingSphereDatabaseMetaData.getDriverMinorVersion(), is(databaseMetaData.getDriverMinorVersion()));
    }
    
    @Test
    public void assertGetDatabaseMajorVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDatabaseMajorVersion(), is(databaseMetaData.getDatabaseMajorVersion()));
    }
    
    @Test
    public void assertGetDatabaseMinorVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDatabaseMinorVersion(), is(databaseMetaData.getDatabaseMinorVersion()));
    }
    
    @Test
    public void assertGetJDBCMajorVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getJDBCMajorVersion(), is(databaseMetaData.getJDBCMajorVersion()));
    }
    
    @Test
    public void assertGetJDBCMinorVersion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getJDBCMinorVersion(), is(databaseMetaData.getJDBCMinorVersion()));
    }
    
    @Test
    public void assertAssertIsReadOnly() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.isReadOnly(), is(databaseMetaData.isReadOnly()));
    }
    
    @Test
    public void assertAllProceduresAreCallable() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.allProceduresAreCallable(), is(databaseMetaData.allProceduresAreCallable()));
    }
    
    @Test
    public void assertAllTablesAreSelectable() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.allTablesAreSelectable(), is(databaseMetaData.allTablesAreSelectable()));
    }
    
    @Test
    public void assertNullsAreSortedHigh() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullsAreSortedHigh(), is(databaseMetaData.nullsAreSortedHigh()));
    }
    
    @Test
    public void assertNullsAreSortedLow() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullsAreSortedLow(), is(databaseMetaData.nullsAreSortedLow()));
    }
    
    @Test
    public void assertNullsAreSortedAtStart() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullsAreSortedAtStart(), is(databaseMetaData.nullsAreSortedAtStart()));
    }
    
    @Test
    public void assertNullsAreSortedAtEnd() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullsAreSortedAtEnd(), is(databaseMetaData.nullsAreSortedAtEnd()));
    }
    
    @Test
    public void assertUsesLocalFiles() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.usesLocalFiles(), is(databaseMetaData.usesLocalFiles()));
    }
    
    @Test
    public void assertUsesLocalFilePerTable() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.usesLocalFilePerTable(), is(databaseMetaData.usesLocalFilePerTable()));
    }
    
    @Test
    public void assertSupportsMixedCaseIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMixedCaseIdentifiers(), is(databaseMetaData.supportsMixedCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresUpperCaseIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesUpperCaseIdentifiers(), is(databaseMetaData.storesUpperCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresLowerCaseIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesLowerCaseIdentifiers(), is(databaseMetaData.storesLowerCaseIdentifiers()));
    }
    
    @Test
    public void assertStoresMixedCaseIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesMixedCaseIdentifiers(), is(databaseMetaData.storesMixedCaseIdentifiers()));
    }
    
    @Test
    public void assertSupportsMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMixedCaseQuotedIdentifiers(), is(databaseMetaData.supportsMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresUpperCaseQuotedIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesUpperCaseQuotedIdentifiers(), is(databaseMetaData.storesUpperCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresLowerCaseQuotedIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesLowerCaseQuotedIdentifiers(), is(databaseMetaData.storesLowerCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertStoresMixedCaseQuotedIdentifiers() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.storesMixedCaseQuotedIdentifiers(), is(databaseMetaData.storesMixedCaseQuotedIdentifiers()));
    }
    
    @Test
    public void assertGetIdentifierQuoteString() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getIdentifierQuoteString(), is(databaseMetaData.getIdentifierQuoteString()));
    }
    
    @Test
    public void assertGetSQLKeywords() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSQLKeywords(), is(databaseMetaData.getSQLKeywords()));
    }
    
    @Test
    public void assertGetNumericFunctions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    public void assertGetStringFunctions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getNumericFunctions(), is(databaseMetaData.getNumericFunctions()));
    }
    
    @Test
    public void assertGetSystemFunctions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSystemFunctions(), is(databaseMetaData.getSystemFunctions()));
    }
    
    @Test
    public void assertGetTimeDateFunctions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getTimeDateFunctions(), is(databaseMetaData.getTimeDateFunctions()));
    }
    
    @Test
    public void assertGetSearchStringEscape() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSearchStringEscape(), is(databaseMetaData.getSearchStringEscape()));
    }
    
    @Test
    public void assertGetExtraNameCharacters() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getExtraNameCharacters(), is(databaseMetaData.getExtraNameCharacters()));
    }
    
    @Test
    public void assertSupportsAlterTableWithAddColumn() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsAlterTableWithAddColumn(), is(databaseMetaData.supportsAlterTableWithAddColumn()));
    }
    
    @Test
    public void assertSupportsAlterTableWithDropColumn() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsAlterTableWithDropColumn(), is(databaseMetaData.supportsAlterTableWithDropColumn()));
    }
    
    @Test
    public void assertSupportsColumnAliasing() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsColumnAliasing(), is(databaseMetaData.supportsColumnAliasing()));
    }
    
    @Test
    public void assertNullPlusNonNullIsNull() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.nullPlusNonNullIsNull(), is(databaseMetaData.nullPlusNonNullIsNull()));
    }
    
    @Test
    public void assertSupportsConvert() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsConvert(), is(databaseMetaData.supportsConvert()));
        assertThat(shardingSphereDatabaseMetaData.supportsConvert(Types.INTEGER, Types.FLOAT), is(databaseMetaData.supportsConvert()));
    }
    
    @Test
    public void assertSupportsTableCorrelationNames() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsTableCorrelationNames(), is(databaseMetaData.supportsTableCorrelationNames()));
    }
    
    @Test
    public void assertSupportsDifferentTableCorrelationNames() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsDifferentTableCorrelationNames(), is(databaseMetaData.supportsDifferentTableCorrelationNames()));
    }
    
    @Test
    public void assertSupportsExpressionsInOrderBy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsExpressionsInOrderBy(), is(databaseMetaData.supportsExpressionsInOrderBy()));
    }
    
    @Test
    public void assertSupportsOrderByUnrelated() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOrderByUnrelated(), is(databaseMetaData.supportsOrderByUnrelated()));
    }
    
    @Test
    public void assertSupportsGroupBy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsGroupBy(), is(databaseMetaData.supportsGroupBy()));
    }
    
    @Test
    public void assertSupportsGroupByUnrelated() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsGroupByUnrelated(), is(databaseMetaData.supportsGroupByUnrelated()));
    }
    
    @Test
    public void assertSupportsGroupByBeyondSelect() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsGroupByBeyondSelect(), is(databaseMetaData.supportsGroupByBeyondSelect()));
    }
    
    @Test
    public void assertSupportsLikeEscapeClause() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsLikeEscapeClause(), is(databaseMetaData.supportsLikeEscapeClause()));
    }
    
    @Test
    public void assertSupportsMultipleResultSets() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMultipleResultSets(), is(databaseMetaData.supportsMultipleResultSets()));
    }
    
    @Test
    public void assertSupportsMultipleTransactions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMultipleTransactions(), is(databaseMetaData.supportsMultipleTransactions()));
    }
    
    @Test
    public void assertSupportsNonNullableColumns() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsNonNullableColumns(), is(databaseMetaData.supportsNonNullableColumns()));
    }
    
    @Test
    public void assertSupportsMinimumSQLGrammar() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMinimumSQLGrammar(), is(databaseMetaData.supportsMinimumSQLGrammar()));
    }
    
    @Test
    public void assertSupportsCoreSQLGrammar() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCoreSQLGrammar(), is(databaseMetaData.supportsCoreSQLGrammar()));
    }
    
    @Test
    public void assertSupportsExtendedSQLGrammar() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsExtendedSQLGrammar(), is(databaseMetaData.supportsExtendedSQLGrammar()));
    }
    
    @Test
    public void assertSupportsANSI92EntryLevelSQL() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsANSI92EntryLevelSQL(), is(databaseMetaData.supportsANSI92EntryLevelSQL()));
    }
    
    @Test
    public void assertSupportsANSI92IntermediateSQL() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsANSI92IntermediateSQL(), is(databaseMetaData.supportsANSI92IntermediateSQL()));
    }
    
    @Test
    public void assertSupportsANSI92FullSQL() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsANSI92FullSQL(), is(databaseMetaData.supportsANSI92FullSQL()));
    }
    
    @Test
    public void assertSupportsIntegrityEnhancementFacility() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsIntegrityEnhancementFacility(), is(databaseMetaData.supportsIntegrityEnhancementFacility()));
    }
    
    @Test
    public void assertSupportsOuterJoins() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOuterJoins(), is(databaseMetaData.supportsOuterJoins()));
    }
    
    @Test
    public void assertSupportsFullOuterJoins() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsFullOuterJoins(), is(databaseMetaData.supportsFullOuterJoins()));
    }
    
    @Test
    public void assertSupportsLimitedOuterJoins() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsLimitedOuterJoins(), is(databaseMetaData.supportsLimitedOuterJoins()));
    }
    
    @Test
    public void assertGetSchemaTerm() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSchemaTerm(), is(databaseMetaData.getSchemaTerm()));
    }
    
    @Test
    public void assertGetProcedureTerm() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getProcedureTerm(), is(databaseMetaData.getProcedureTerm()));
    }
    
    @Test
    public void assertGetCatalogTerm() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getCatalogTerm(), is(databaseMetaData.getCatalogTerm()));
    }
    
    @Test
    public void assertAssertIsCatalogAtStart() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.isCatalogAtStart(), is(databaseMetaData.isCatalogAtStart()));
    }
    
    @Test
    public void assertGetCatalogSeparator() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getCatalogSeparator(), is(databaseMetaData.getCatalogSeparator()));
    }
    
    @Test
    public void assertSupportsSchemasInDataManipulation() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInDataManipulation(), is(databaseMetaData.supportsSchemasInDataManipulation()));
    }
    
    @Test
    public void assertSupportsSchemasInProcedureCalls() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInProcedureCalls(), is(databaseMetaData.supportsSchemasInProcedureCalls()));
    }
    
    @Test
    public void assertSupportsSchemasInTableDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInTableDefinitions(), is(databaseMetaData.supportsSchemasInTableDefinitions()));
    }
    
    @Test
    public void assertSupportsSchemasInIndexDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInIndexDefinitions(), is(databaseMetaData.supportsSchemasInIndexDefinitions()));
    }
    
    @Test
    public void assertSupportsSchemasInPrivilegeDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSchemasInPrivilegeDefinitions(), is(databaseMetaData.supportsSchemasInPrivilegeDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInDataManipulation() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInDataManipulation(), is(databaseMetaData.supportsCatalogsInDataManipulation()));
    }
    
    @Test
    public void assertSupportsCatalogsInProcedureCalls() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInProcedureCalls(), is(databaseMetaData.supportsCatalogsInProcedureCalls()));
    }
    
    @Test
    public void assertSupportsCatalogsInTableDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInTableDefinitions(), is(databaseMetaData.supportsCatalogsInTableDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInIndexDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInIndexDefinitions(), is(databaseMetaData.supportsCatalogsInIndexDefinitions()));
    }
    
    @Test
    public void assertSupportsCatalogsInPrivilegeDefinitions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCatalogsInPrivilegeDefinitions(), is(databaseMetaData.supportsCatalogsInPrivilegeDefinitions()));
    }
    
    @Test
    public void assertSupportsPositionedDelete() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsPositionedDelete(), is(databaseMetaData.supportsPositionedDelete()));
    }
    
    @Test
    public void assertSupportsPositionedUpdate() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsPositionedUpdate(), is(databaseMetaData.supportsPositionedUpdate()));
    }
    
    @Test
    public void assertSupportsSelectForUpdate() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSelectForUpdate(), is(databaseMetaData.supportsSelectForUpdate()));
    }
    
    @Test
    public void assertSupportsStoredProcedures() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsStoredProcedures(), is(databaseMetaData.supportsStoredProcedures()));
    }
    
    @Test
    public void assertSupportsSubqueriesInComparisons() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSubqueriesInComparisons(), is(databaseMetaData.supportsSubqueriesInComparisons()));
    }
    
    @Test
    public void assertSupportsSubqueriesInExists() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSubqueriesInExists(), is(databaseMetaData.supportsSubqueriesInExists()));
    }
    
    @Test
    public void assertSupportsSubqueriesInIns() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSubqueriesInIns(), is(databaseMetaData.supportsSubqueriesInIns()));
    }
    
    @Test
    public void assertSupportsSubqueriesInQuantifieds() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSubqueriesInQuantifieds(), is(databaseMetaData.supportsSubqueriesInQuantifieds()));
    }
    
    @Test
    public void assertSupportsCorrelatedSubqueries() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsCorrelatedSubqueries(), is(databaseMetaData.supportsCorrelatedSubqueries()));
    }
    
    @Test
    public void assertSupportsUnion() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsUnion(), is(databaseMetaData.supportsUnion()));
    }
    
    @Test
    public void assertSupportsUnionAll() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsUnionAll(), is(databaseMetaData.supportsUnionAll()));
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossCommit() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOpenCursorsAcrossCommit(), is(databaseMetaData.supportsOpenCursorsAcrossCommit()));
    }
    
    @Test
    public void assertSupportsOpenCursorsAcrossRollback() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOpenCursorsAcrossRollback(), is(databaseMetaData.supportsOpenCursorsAcrossRollback()));
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossCommit() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOpenStatementsAcrossCommit(), is(databaseMetaData.supportsOpenStatementsAcrossCommit()));
    }
    
    @Test
    public void assertSupportsOpenStatementsAcrossRollback() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsOpenStatementsAcrossRollback(), is(databaseMetaData.supportsOpenStatementsAcrossRollback()));
    }
    
    @Test
    public void assertGetMaxBinaryLiteralLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxBinaryLiteralLength(), is(databaseMetaData.getMaxBinaryLiteralLength()));
    }
    
    @Test
    public void assertGetMaxCharLiteralLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxCharLiteralLength(), is(databaseMetaData.getMaxCharLiteralLength()));
    }
    
    @Test
    public void assertGetMaxColumnNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnNameLength(), is(databaseMetaData.getMaxColumnNameLength()));
    }
    
    @Test
    public void assertGetMaxColumnsInGroupBy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInGroupBy(), is(databaseMetaData.getMaxColumnsInGroupBy()));
    }
    
    @Test
    public void assertGetMaxColumnsInIndex() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInIndex(), is(databaseMetaData.getMaxColumnsInIndex()));
    }
    
    @Test
    public void assertGetMaxColumnsInOrderBy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInOrderBy(), is(databaseMetaData.getMaxColumnsInOrderBy()));
    }
    
    @Test
    public void assertGetMaxColumnsInSelect() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInSelect(), is(databaseMetaData.getMaxColumnsInSelect()));
    }
    
    @Test
    public void assertGetMaxColumnsInTable() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxColumnsInTable(), is(databaseMetaData.getMaxColumnsInTable()));
    }
    
    @Test
    public void assertGetMaxConnections() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxConnections(), is(databaseMetaData.getMaxConnections()));
    }
    
    @Test
    public void assertGetMaxCursorNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxCursorNameLength(), is(databaseMetaData.getMaxCursorNameLength()));
    }
    
    @Test
    public void assertGetMaxIndexLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxIndexLength(), is(databaseMetaData.getMaxIndexLength()));
    }
    
    @Test
    public void assertGetMaxSchemaNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxSchemaNameLength(), is(databaseMetaData.getMaxSchemaNameLength()));
    }
    
    @Test
    public void assertGetMaxProcedureNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxProcedureNameLength(), is(databaseMetaData.getMaxProcedureNameLength()));
    }
    
    @Test
    public void assertGetMaxCatalogNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxCatalogNameLength(), is(databaseMetaData.getMaxCatalogNameLength()));
    }
    
    @Test
    public void assertGetMaxRowSize() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxRowSize(), is(databaseMetaData.getMaxRowSize()));
    }
    
    @Test
    public void assertDoesMaxRowSizeIncludeBlobs() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.doesMaxRowSizeIncludeBlobs(), is(databaseMetaData.doesMaxRowSizeIncludeBlobs()));
    }
    
    @Test
    public void assertGetMaxStatementLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxStatementLength(), is(databaseMetaData.getMaxStatementLength()));
    }
    
    @Test
    public void assertGetMaxStatements() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxStatements(), is(databaseMetaData.getMaxStatements()));
    }
    
    @Test
    public void assertGetMaxTableNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxTableNameLength(), is(databaseMetaData.getMaxTableNameLength()));
    }
    
    @Test
    public void assertGetMaxTablesInSelect() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxTablesInSelect(), is(databaseMetaData.getMaxTablesInSelect()));
    }
    
    @Test
    public void assertGetMaxUserNameLength() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getMaxUserNameLength(), is(databaseMetaData.getMaxUserNameLength()));
    }
    
    @Test
    public void assertGetDefaultTransactionIsolation() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getDefaultTransactionIsolation(), is(databaseMetaData.getDefaultTransactionIsolation()));
    }
    
    @Test
    public void assertSupportsTransactions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsTransactions(), is(databaseMetaData.supportsTransactions()));
    }
    
    @Test
    public void assertSupportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsDataDefinitionAndDataManipulationTransactions(), is(databaseMetaData.supportsDataDefinitionAndDataManipulationTransactions()));
    }
    
    @Test
    public void assertSupportsDataManipulationTransactionsOnly() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsDataManipulationTransactionsOnly(), is(databaseMetaData.supportsDataManipulationTransactionsOnly()));
    }
    
    @Test
    public void assertDataDefinitionCausesTransactionCommit() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.dataDefinitionCausesTransactionCommit(), is(databaseMetaData.dataDefinitionCausesTransactionCommit()));
    }
    
    @Test
    public void assertDataDefinitionIgnoredInTransactions() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.dataDefinitionIgnoredInTransactions(), is(databaseMetaData.dataDefinitionIgnoredInTransactions()));
    }
    
    @Test
    public void assertSupportsBatchUpdates() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsBatchUpdates(), is(databaseMetaData.supportsBatchUpdates()));
    }
    
    @Test
    public void assertSupportsSavepoints() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsSavepoints(), is(databaseMetaData.supportsSavepoints()));
    }
    
    @Test
    public void assertSupportsNamedParameters() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsNamedParameters(), is(databaseMetaData.supportsNamedParameters()));
    }
    
    @Test
    public void assertSupportsMultipleOpenResults() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsMultipleOpenResults(), is(databaseMetaData.supportsMultipleOpenResults()));
    }
    
    @Test
    public void assertSupportsGetGeneratedKeys() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsGetGeneratedKeys(), is(databaseMetaData.supportsGetGeneratedKeys()));
    }
    
    @Test
    public void assertGetResultSetHoldability() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getResultSetHoldability(), is(databaseMetaData.getResultSetHoldability()));
    }
    
    @Test
    public void assertGetSQLStateType() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getSQLStateType(), is(databaseMetaData.getSQLStateType()));
    }
    
    @Test
    public void assertLocatorsUpdateCopy() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.locatorsUpdateCopy(), is(databaseMetaData.locatorsUpdateCopy()));
    }
    
    @Test
    public void assertSupportsStatementPooling() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsStatementPooling(), is(databaseMetaData.supportsStatementPooling()));
    }
    
    @Test
    public void assertSupportsStoredFunctionsUsingCallSyntax() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.supportsStoredFunctionsUsingCallSyntax(), is(databaseMetaData.supportsStoredFunctionsUsingCallSyntax()));
    }
    
    @Test
    public void assertAutoCommitFailureClosesAllResultSets() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.autoCommitFailureClosesAllResultSets(), is(databaseMetaData.autoCommitFailureClosesAllResultSets()));
    }
    
    @Test
    public void assertGetRowIdLifetime() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getRowIdLifetime(), is(databaseMetaData.getRowIdLifetime()));
    }
    
    @Test
    public void assertGeneratedKeyAlwaysReturned() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.generatedKeyAlwaysReturned(), is(databaseMetaData.generatedKeyAlwaysReturned()));
    }
    
    @Test
    public void assertOwnInsertsAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.ownInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOwnUpdatesAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.ownUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOwnDeletesAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.ownDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersInsertsAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.othersInsertsAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersUpdatesAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.othersUpdatesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertOthersDeletesAreVisible() {
        assertTrue(shardingSphereDatabaseMetaData.othersDeletesAreVisible(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertInsertsAreDetected() {
        assertTrue(shardingSphereDatabaseMetaData.insertsAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertUpdatesAreDetected() {
        assertTrue(shardingSphereDatabaseMetaData.updatesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertDeletesAreDetected() {
        assertTrue(shardingSphereDatabaseMetaData.deletesAreDetected(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetType() {
        assertTrue(shardingSphereDatabaseMetaData.supportsResultSetType(ResultSet.TYPE_FORWARD_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetConcurrency() {
        assertTrue(shardingSphereDatabaseMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
    }
    
    @Test
    public void assertSupportsResultSetHoldability() {
        assertTrue(shardingSphereDatabaseMetaData.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));
    }
    
    @Test
    public void assertSupportsTransactionIsolationLevel() {
        assertTrue(shardingSphereDatabaseMetaData.supportsTransactionIsolationLevel(Connection.TRANSACTION_NONE));
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        assertThat(shardingSphereDatabaseMetaData.getConnection(), is(dataSource.getConnection()));
    }
    
    @Test
    public void assertGetSuperTypes() throws SQLException {
        when(databaseMetaData.getSuperTypes("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getSuperTypes("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSuperTables() throws SQLException {
        when(databaseMetaData.getSuperTables("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getSuperTables("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetAttributes() throws SQLException {
        when(databaseMetaData.getAttributes("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getAttributes("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetProcedures() throws SQLException {
        when(databaseMetaData.getProcedures("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getProcedures("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetProcedureColumns() throws SQLException {
        when(databaseMetaData.getProcedureColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getProcedureColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTables() throws SQLException {
        when(databaseMetaData.getTables("test", null, "%" + TABLE_NAME + "%", null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getTables("test", null, TABLE_NAME, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSchemas() throws SQLException {
        when(databaseMetaData.getSchemas()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getSchemas(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetSchemasForCatalogAndSchemaPattern() throws SQLException {
        when(databaseMetaData.getSchemas("test", null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getSchemas("test", null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetCatalogs() throws SQLException {
        when(databaseMetaData.getCatalogs()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getCatalogs(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTableTypes() throws SQLException {
        when(databaseMetaData.getTableTypes()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getTableTypes(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetColumns() throws SQLException {
        when(databaseMetaData.getColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetColumnPrivileges() throws SQLException {
        when(databaseMetaData.getColumnPrivileges("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getColumnPrivileges("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTablePrivileges() throws SQLException {
        when(databaseMetaData.getTablePrivileges("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getTablePrivileges("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetBestRowIdentifier() throws SQLException {
        when(databaseMetaData.getBestRowIdentifier("test", null, null, 1, true)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getBestRowIdentifier("test", null, null, 1, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetVersionColumns() throws SQLException {
        when(databaseMetaData.getVersionColumns("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getVersionColumns("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetPrimaryKeys() throws SQLException {
        when(databaseMetaData.getPrimaryKeys("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getPrimaryKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetImportedKeys() throws SQLException {
        when(databaseMetaData.getImportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getImportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetExportedKeys() throws SQLException {
        when(databaseMetaData.getExportedKeys("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getExportedKeys("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetCrossReference() throws SQLException {
        when(databaseMetaData.getCrossReference("test", null, null, null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getCrossReference("test", null, null, null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetTypeInfo() throws SQLException {
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getTypeInfo(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetIndexInfo() throws SQLException {
        when(databaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getIndexInfo("test", null, TABLE_NAME, true, true), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetUDTs() throws SQLException {
        when(databaseMetaData.getUDTs("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getUDTs("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetClientInfoProperties() throws SQLException {
        when(databaseMetaData.getClientInfoProperties()).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getClientInfoProperties(), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetFunctions() throws SQLException {
        when(databaseMetaData.getFunctions("test", null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getFunctions("test", null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetFunctionColumns() throws SQLException {
        when(databaseMetaData.getFunctionColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getFunctionColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
    
    @Test
    public void assertGetPseudoColumns() throws SQLException {
        when(databaseMetaData.getPseudoColumns("test", null, null, null)).thenReturn(resultSet);
        assertThat(shardingSphereDatabaseMetaData.getPseudoColumns("test", null, null, null), instanceOf(DatabaseMetaDataResultSet.class));
    }
}
