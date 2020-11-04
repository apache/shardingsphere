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

package org.apache.shardingsphere.infra.schema.model.datasource;

import lombok.Getter;

import java.sql.DatabaseMetaData;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Cached database meta data.
 */
@Getter
public final class CachedDatabaseMetaData {
    
    private final String url;
    
    private final String userName;
    
    private final String databaseProductName;
    
    private final String databaseProductVersion;
    
    private final String driverName;
    
    private final String driverVersion;
    
    private final int driverMajorVersion;
    
    private final int driverMinorVersion;
    
    private final int databaseMajorVersion;
    
    private final int databaseMinorVersion;
    
    private final int jdbcMajorVersion;
    
    private final int jdbcMinorVersion;
    
    private final boolean isReadOnly;
    
    private final boolean allProceduresAreCallable;
    
    private final boolean allTablesAreSelectable;
    
    private final boolean nullsAreSortedHigh;
    
    private final boolean nullsAreSortedLow;
    
    private final boolean nullsAreSortedAtStart;
    
    private final boolean nullsAreSortedAtEnd;
    
    private final boolean usesLocalFiles;
    
    private final boolean usesLocalFilePerTable;
    
    private final boolean supportsMixedCaseIdentifiers;
    
    private final boolean storesUpperCaseIdentifiers;
    
    private final boolean storesLowerCaseIdentifiers;
    
    private final boolean storesMixedCaseIdentifiers;
    
    private final boolean supportsMixedCaseQuotedIdentifiers;
    
    private final boolean storesUpperCaseQuotedIdentifiers;
    
    private final boolean storesLowerCaseQuotedIdentifiers;
    
    private final boolean storesMixedCaseQuotedIdentifiers;
    
    private final String identifierQuoteString;
    
    private final String sqlKeywords;
    
    private final String numericFunctions;
    
    private final String stringFunctions;
    
    private final String systemFunctions;
    
    private final String timeDateFunctions;
    
    private final String searchStringEscape;
    
    private final String extraNameCharacters;
    
    private final boolean supportsAlterTableWithAddColumn;
    
    private final boolean supportsAlterTableWithDropColumn;
    
    private final boolean supportsColumnAliasing;
    
    private final boolean nullPlusNonNullIsNull;
    
    private final boolean supportsConvert;
    
    private final boolean supportsTableCorrelationNames;
    
    private final boolean supportsDifferentTableCorrelationNames;
    
    private final boolean supportsExpressionsInOrderBy;
    
    private final boolean supportsOrderByUnrelated;
    
    private final boolean supportsGroupBy;
    
    private final boolean supportsGroupByUnrelated;
    
    private final boolean supportsGroupByBeyondSelect;
    
    private final boolean supportsLikeEscapeClause;
    
    private final boolean supportsMultipleResultSets;
    
    private final boolean supportsMultipleTransactions;
    
    private final boolean supportsNonNullableColumns;
    
    private final boolean supportsMinimumSQLGrammar;
    
    private final boolean supportsCoreSQLGrammar;
    
    private final boolean supportsExtendedSQLGrammar;
    
    private final boolean supportsANSI92EntryLevelSQL;
    
    private final boolean supportsANSI92IntermediateSQL;
    
    private final boolean supportsANSI92FullSQL;
    
    private final boolean supportsIntegrityEnhancementFacility;
    
    private final boolean supportsOuterJoins;
    
    private final boolean supportsFullOuterJoins;
    
    private final boolean supportsLimitedOuterJoins;
    
    private final String schemaTerm;
    
    private final String procedureTerm;
    
    private final String catalogTerm;
    
    private final boolean isCatalogAtStart;
    
    private final String catalogSeparator;
    
    private final boolean supportsSchemasInDataManipulation;
    
    private final boolean supportsSchemasInProcedureCalls;
    
    private final boolean supportsSchemasInTableDefinitions;
    
    private final boolean supportsSchemasInIndexDefinitions;
    
    private final boolean supportsSchemasInPrivilegeDefinitions;
    
    private final boolean supportsCatalogsInDataManipulation;
    
    private final boolean supportsCatalogsInProcedureCalls;
    
    private final boolean supportsCatalogsInTableDefinitions;
    
    private final boolean supportsCatalogsInIndexDefinitions;
    
    private final boolean supportsCatalogsInPrivilegeDefinitions;
    
    private final boolean supportsPositionedDelete;
    
    private final boolean supportsPositionedUpdate;
    
    private final boolean supportsSelectForUpdate;
    
    private final boolean supportsStoredProcedures;
    
    private final boolean supportsSubqueriesInComparisons;
    
    private final boolean supportsSubqueriesInExists;
    
    private final boolean supportsSubqueriesInIns;
    
    private final boolean supportsSubqueriesInQuantifieds;
    
    private final boolean supportsCorrelatedSubqueries;
    
    private final boolean supportsUnion;
    
    private final boolean supportsUnionAll;
    
    private final boolean supportsOpenCursorsAcrossCommit;
    
    private final boolean supportsOpenCursorsAcrossRollback;
    
    private final boolean supportsOpenStatementsAcrossCommit;
    
    private final boolean supportsOpenStatementsAcrossRollback;
    
    private final int maxBinaryLiteralLength;
    
    private final int maxCharLiteralLength;
    
    private final int maxColumnNameLength;
    
    private final int maxColumnsInGroupBy;
    
    private final int maxColumnsInIndex;
    
    private final int maxColumnsInOrderBy;
    
    private final int maxColumnsInSelect;
    
    private final int maxColumnsInTable;
    
    private final int maxConnections;
    
    private final int maxCursorNameLength;
    
    private final int maxIndexLength;
    
    private final int maxSchemaNameLength;
    
    private final int maxProcedureNameLength;
    
    private final int maxCatalogNameLength;
    
    private final int maxRowSize;
    
    private final boolean doesMaxRowSizeIncludeBlobs;
    
    private final int maxStatementLength;
    
    private final int maxStatements;
    
    private final int maxTableNameLength;
    
    private final int maxTablesInSelect;
    
    private final int maxUserNameLength;
    
    private final int defaultTransactionIsolation;
    
    private final boolean supportsTransactions;
    
    private final boolean supportsDataDefinitionAndDataManipulationTransactions;
    
    private final boolean supportsDataManipulationTransactionsOnly;
    
    private final boolean dataDefinitionCausesTransactionCommit;
    
    private final boolean dataDefinitionIgnoredInTransactions;
    
    private final boolean supportsBatchUpdates;
    
    private final boolean supportsSavepoints;
    
    private final boolean supportsNamedParameters;
    
    private final boolean supportsMultipleOpenResults;
    
    private final boolean supportsGetGeneratedKeys;
    
    private final int resultSetHoldability;
    
    private final int sqlStateType;
    
    private final boolean locatorsUpdateCopy;
    
    private final boolean supportsStatementPooling;
    
    private final boolean supportsStoredFunctionsUsingCallSyntax;
    
    private final boolean autoCommitFailureClosesAllResultSets;
    
    private final RowIdLifetime rowIdLifetime;
    
    private final boolean generatedKeyAlwaysReturned;
    
    public CachedDatabaseMetaData(final DatabaseMetaData databaseMetaData) throws SQLException {
        url = databaseMetaData.getURL();
        userName = databaseMetaData.getUserName();
        databaseProductName = databaseMetaData.getDatabaseProductName();
        databaseProductVersion = databaseMetaData.getDatabaseProductVersion();
        driverName = databaseMetaData.getDriverName();
        driverVersion = databaseMetaData.getDriverVersion();
        driverMajorVersion = databaseMetaData.getDriverMajorVersion();
        driverMinorVersion = databaseMetaData.getDriverMinorVersion();
        databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
        databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();
        jdbcMajorVersion = databaseMetaData.getJDBCMajorVersion();
        jdbcMinorVersion = databaseMetaData.getJDBCMinorVersion();
        isReadOnly = databaseMetaData.isReadOnly();
        allProceduresAreCallable = databaseMetaData.allProceduresAreCallable();
        allTablesAreSelectable = databaseMetaData.allTablesAreSelectable();
        nullsAreSortedHigh = databaseMetaData.nullsAreSortedHigh();
        nullsAreSortedLow = databaseMetaData.nullsAreSortedLow();
        nullsAreSortedAtStart = databaseMetaData.nullsAreSortedAtStart();
        nullsAreSortedAtEnd = databaseMetaData.nullsAreSortedAtEnd();
        usesLocalFiles = databaseMetaData.usesLocalFiles();
        usesLocalFilePerTable = databaseMetaData.usesLocalFilePerTable();
        supportsMixedCaseIdentifiers = databaseMetaData.supportsMixedCaseIdentifiers();
        storesUpperCaseIdentifiers = databaseMetaData.storesUpperCaseIdentifiers();
        storesLowerCaseIdentifiers = databaseMetaData.storesLowerCaseIdentifiers();
        storesMixedCaseIdentifiers = databaseMetaData.storesMixedCaseIdentifiers();
        supportsMixedCaseQuotedIdentifiers = databaseMetaData.supportsMixedCaseQuotedIdentifiers();
        storesUpperCaseQuotedIdentifiers = databaseMetaData.storesUpperCaseQuotedIdentifiers();
        storesLowerCaseQuotedIdentifiers = databaseMetaData.storesLowerCaseQuotedIdentifiers();
        storesMixedCaseQuotedIdentifiers = databaseMetaData.storesMixedCaseQuotedIdentifiers();
        identifierQuoteString = databaseMetaData.getIdentifierQuoteString();
        sqlKeywords = databaseMetaData.getSQLKeywords();
        numericFunctions = databaseMetaData.getNumericFunctions();
        stringFunctions = databaseMetaData.getStringFunctions();
        systemFunctions = databaseMetaData.getSystemFunctions();
        timeDateFunctions = databaseMetaData.getTimeDateFunctions();
        searchStringEscape = databaseMetaData.getSearchStringEscape();
        extraNameCharacters = databaseMetaData.getExtraNameCharacters();
        supportsAlterTableWithAddColumn = databaseMetaData.supportsAlterTableWithAddColumn();
        supportsAlterTableWithDropColumn = databaseMetaData.supportsAlterTableWithDropColumn();
        supportsColumnAliasing = databaseMetaData.supportsColumnAliasing();
        nullPlusNonNullIsNull = databaseMetaData.nullPlusNonNullIsNull();
        supportsConvert = databaseMetaData.supportsConvert();
        supportsTableCorrelationNames = databaseMetaData.supportsTableCorrelationNames();
        supportsDifferentTableCorrelationNames = databaseMetaData.supportsDifferentTableCorrelationNames();
        supportsExpressionsInOrderBy = databaseMetaData.supportsExpressionsInOrderBy();
        supportsOrderByUnrelated = databaseMetaData.supportsOrderByUnrelated();
        supportsGroupBy = databaseMetaData.supportsGroupBy();
        supportsGroupByUnrelated = databaseMetaData.supportsGroupByUnrelated();
        supportsGroupByBeyondSelect = databaseMetaData.supportsGroupByBeyondSelect();
        supportsLikeEscapeClause = databaseMetaData.supportsLikeEscapeClause();
        supportsMultipleResultSets = databaseMetaData.supportsMultipleResultSets();
        supportsMultipleTransactions = databaseMetaData.supportsMultipleTransactions();
        supportsNonNullableColumns = databaseMetaData.supportsNonNullableColumns();
        supportsMinimumSQLGrammar = databaseMetaData.supportsMinimumSQLGrammar();
        supportsCoreSQLGrammar = databaseMetaData.supportsCoreSQLGrammar();
        supportsExtendedSQLGrammar = databaseMetaData.supportsExtendedSQLGrammar();
        supportsANSI92EntryLevelSQL = databaseMetaData.supportsANSI92EntryLevelSQL();
        supportsANSI92IntermediateSQL = databaseMetaData.supportsANSI92IntermediateSQL();
        supportsANSI92FullSQL = databaseMetaData.supportsANSI92FullSQL();
        supportsIntegrityEnhancementFacility = databaseMetaData.supportsIntegrityEnhancementFacility();
        supportsOuterJoins = databaseMetaData.supportsOuterJoins();
        supportsFullOuterJoins = databaseMetaData.supportsFullOuterJoins();
        supportsLimitedOuterJoins = databaseMetaData.supportsLimitedOuterJoins();
        schemaTerm = databaseMetaData.getSchemaTerm();
        procedureTerm = databaseMetaData.getProcedureTerm();
        catalogTerm = databaseMetaData.getCatalogTerm();
        isCatalogAtStart = databaseMetaData.isCatalogAtStart();
        catalogSeparator = databaseMetaData.getCatalogSeparator();
        supportsSchemasInDataManipulation = databaseMetaData.supportsSchemasInDataManipulation();
        supportsSchemasInProcedureCalls = databaseMetaData.supportsSchemasInProcedureCalls();
        supportsSchemasInTableDefinitions = databaseMetaData.supportsSchemasInTableDefinitions();
        supportsSchemasInIndexDefinitions = databaseMetaData.supportsSchemasInIndexDefinitions();
        supportsSchemasInPrivilegeDefinitions = databaseMetaData.supportsSchemasInPrivilegeDefinitions();
        supportsCatalogsInDataManipulation = databaseMetaData.supportsCatalogsInDataManipulation();
        supportsCatalogsInProcedureCalls = databaseMetaData.supportsCatalogsInProcedureCalls();
        supportsCatalogsInTableDefinitions = databaseMetaData.supportsCatalogsInTableDefinitions();
        supportsCatalogsInIndexDefinitions = databaseMetaData.supportsCatalogsInIndexDefinitions();
        supportsCatalogsInPrivilegeDefinitions = databaseMetaData.supportsCatalogsInPrivilegeDefinitions();
        supportsPositionedDelete = databaseMetaData.supportsPositionedDelete();
        supportsPositionedUpdate = databaseMetaData.supportsPositionedUpdate();
        supportsSelectForUpdate = databaseMetaData.supportsSelectForUpdate();
        supportsStoredProcedures = databaseMetaData.supportsStoredProcedures();
        supportsSubqueriesInComparisons = databaseMetaData.supportsSubqueriesInComparisons();
        supportsSubqueriesInExists = databaseMetaData.supportsSubqueriesInExists();
        supportsSubqueriesInIns = databaseMetaData.supportsSubqueriesInIns();
        supportsSubqueriesInQuantifieds = databaseMetaData.supportsSubqueriesInQuantifieds();
        supportsCorrelatedSubqueries = databaseMetaData.supportsCorrelatedSubqueries();
        supportsUnion = databaseMetaData.supportsUnion();
        supportsUnionAll = databaseMetaData.supportsUnionAll();
        supportsOpenCursorsAcrossCommit = databaseMetaData.supportsOpenCursorsAcrossCommit();
        supportsOpenCursorsAcrossRollback = databaseMetaData.supportsOpenCursorsAcrossRollback();
        supportsOpenStatementsAcrossCommit = databaseMetaData.supportsOpenStatementsAcrossCommit();
        supportsOpenStatementsAcrossRollback = databaseMetaData.supportsOpenStatementsAcrossRollback();
        maxBinaryLiteralLength = databaseMetaData.getMaxBinaryLiteralLength();
        maxCharLiteralLength = databaseMetaData.getMaxCharLiteralLength();
        maxColumnNameLength = databaseMetaData.getMaxColumnNameLength();
        maxColumnsInGroupBy = databaseMetaData.getMaxColumnsInGroupBy();
        maxColumnsInIndex = databaseMetaData.getMaxColumnsInIndex();
        maxColumnsInOrderBy = databaseMetaData.getMaxColumnsInOrderBy();
        maxColumnsInSelect = databaseMetaData.getMaxColumnsInSelect();
        maxColumnsInTable = databaseMetaData.getMaxColumnsInTable();
        maxConnections = databaseMetaData.getMaxConnections();
        maxCursorNameLength = databaseMetaData.getMaxCursorNameLength();
        maxIndexLength = databaseMetaData.getMaxIndexLength();
        maxSchemaNameLength = databaseMetaData.getMaxSchemaNameLength();
        maxProcedureNameLength = databaseMetaData.getMaxProcedureNameLength();
        maxCatalogNameLength = databaseMetaData.getMaxCatalogNameLength();
        maxRowSize = databaseMetaData.getMaxRowSize();
        doesMaxRowSizeIncludeBlobs = databaseMetaData.doesMaxRowSizeIncludeBlobs();
        maxStatementLength = databaseMetaData.getMaxStatementLength();
        maxStatements = databaseMetaData.getMaxStatements();
        maxTableNameLength = databaseMetaData.getMaxTableNameLength();
        maxTablesInSelect = databaseMetaData.getMaxTablesInSelect();
        maxUserNameLength = databaseMetaData.getMaxUserNameLength();
        defaultTransactionIsolation = databaseMetaData.getDefaultTransactionIsolation();
        supportsTransactions = databaseMetaData.supportsTransactions();
        supportsDataDefinitionAndDataManipulationTransactions = databaseMetaData.supportsDataDefinitionAndDataManipulationTransactions();
        supportsDataManipulationTransactionsOnly = databaseMetaData.supportsDataManipulationTransactionsOnly();
        dataDefinitionCausesTransactionCommit = databaseMetaData.dataDefinitionCausesTransactionCommit();
        dataDefinitionIgnoredInTransactions = databaseMetaData.dataDefinitionIgnoredInTransactions();
        supportsBatchUpdates = databaseMetaData.supportsBatchUpdates();
        supportsSavepoints = databaseMetaData.supportsSavepoints();
        supportsNamedParameters = databaseMetaData.supportsNamedParameters();
        supportsMultipleOpenResults = databaseMetaData.supportsMultipleOpenResults();
        supportsGetGeneratedKeys = databaseMetaData.supportsGetGeneratedKeys();
        resultSetHoldability = databaseMetaData.getResultSetHoldability();
        sqlStateType = databaseMetaData.getSQLStateType();
        locatorsUpdateCopy = databaseMetaData.locatorsUpdateCopy();
        supportsStatementPooling = databaseMetaData.supportsStatementPooling();
        supportsStoredFunctionsUsingCallSyntax = databaseMetaData.supportsStoredFunctionsUsingCallSyntax();
        autoCommitFailureClosesAllResultSets = databaseMetaData.autoCommitFailureClosesAllResultSets();
        rowIdLifetime = getRowIdLifetimeFromOriginMetaData(databaseMetaData);
        generatedKeyAlwaysReturned = isGeneratedKeyAlwaysReturned(databaseMetaData);
    }
    
    private RowIdLifetime getRowIdLifetimeFromOriginMetaData(final DatabaseMetaData databaseMetaData) throws SQLException {
        try {
            return databaseMetaData.getRowIdLifetime();
        } catch (final SQLFeatureNotSupportedException ignore) {
            return RowIdLifetime.ROWID_UNSUPPORTED;
        }
    }
    
    private boolean isGeneratedKeyAlwaysReturned(final DatabaseMetaData databaseMetaData) throws SQLException {
        try {
            return databaseMetaData.generatedKeyAlwaysReturned();
        } catch (final AbstractMethodError ignored) {
            return false;
        }
    }
}
