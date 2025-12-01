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

package org.apache.shardingsphere.distsql.parser.core.kernel;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CheckPrivilegesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ConvertYamlConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DisableComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.EnableComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ExportDatabaseConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ExportMetaDataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ExportStorageNodesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.FromSegmentContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.IgnoreBroadcastTablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.IgnoreSingleAndBroadcastTablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.IgnoreSingleTablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ImportDatabaseConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ImportMetaDataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.InstanceIdContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.LabelComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.LockClusterContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RefreshDatabaseMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RefreshTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RegisterStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.SetDistVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowComputeNodeInfoContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowComputeNodeModeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowComputeNodesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowDistVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowDistVariablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowKeyGenerateAlgorithmPluginsContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowLoadBalanceAlgorithmPluginsContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowLogicalTablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowPluginImplementationsContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowRulesUsedStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowStorageUnitsContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.StorageUnitDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.StorageUnitsDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.UnlabelComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.UnlockClusterContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.UnregisterStorageUnitContext;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.convert.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.export.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.export.ExportMetaDataStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.export.ExportStorageNodesStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodeInfoStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowComputeNodesStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariableStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariablesStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowTableMetaDataStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.AlterComputeNodeStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.LabelComputeNodeStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.LockClusterStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.RefreshDatabaseMetaDataStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetComputeNodeStateStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlabelComputeNodeStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlockClusterStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.AlterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowLogicalTablesStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.util.IdentifierValueUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for kernel DistSQL.
 */
public final class KernelDistSQLStatementVisitor extends KernelDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitRegisterStorageUnit(final RegisterStorageUnitContext ctx) {
        return new RegisterStorageUnitStatement(null != ctx.ifNotExists(), getStorageUnits(ctx.storageUnitsDefinition()), getExpectedPrivileges(ctx.checkPrivileges()));
    }
    
    @Override
    public ASTNode visitAlterStorageUnit(final AlterStorageUnitContext ctx) {
        return new AlterStorageUnitStatement(getStorageUnits(ctx.storageUnitsDefinition()), getExpectedPrivileges(ctx.checkPrivileges()));
    }
    
    private Collection<DataSourceSegment> getStorageUnits(final StorageUnitsDefinitionContext ctx) {
        return ctx.storageUnitDefinition().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList());
    }
    
    private Collection<String> getExpectedPrivileges(final CheckPrivilegesContext ctx) {
        return null == ctx ? Collections.emptySet() : ctx.privilegeType().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toSet());
    }
    
    @Override
    public ASTNode visitShowTableMetadata(final ShowTableMetadataContext ctx) {
        Collection<String> tableNames = ctx.tableName().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toSet());
        return new ShowTableMetaDataStatement(tableNames, null == ctx.databaseName()
                ? null
                : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())));
    }
    
    @Override
    public ASTNode visitStorageUnitDefinition(final StorageUnitDefinitionContext ctx) {
        String user = IdentifierValueUtils.getValue(ctx.user());
        String password = null == ctx.password() ? "" : new StringLiteralValue(StringUtils.replaceStandardEscapes(ctx.password().getText())).getValue();
        Properties props = getProperties(ctx.propertiesDefinition());
        return null == ctx.urlSource()
                ? new HostnameAndPortBasedDataSourceSegment(IdentifierValueUtils.getValue(ctx.storageUnitName()),
                        IdentifierValueUtils.getValue(ctx.simpleSource().hostname()), ctx.simpleSource().port().getText(), IdentifierValueUtils.getValue(ctx.simpleSource().dbName()), user, password,
                        props)
                : new URLBasedDataSourceSegment(IdentifierValueUtils.getValue(ctx.storageUnitName()), IdentifierValueUtils.getValue(ctx.urlSource().url()), user, password, props);
    }
    
    @Override
    public ASTNode visitShowComputeNodes(final ShowComputeNodesContext ctx) {
        return new ShowComputeNodesStatement();
    }
    
    @Override
    public ASTNode visitShowComputeNodeInfo(final ShowComputeNodeInfoContext ctx) {
        return new ShowComputeNodeInfoStatement();
    }
    
    @Override
    public ASTNode visitShowComputeNodeMode(final ShowComputeNodeModeContext ctx) {
        return new ShowComputeNodeModeStatement();
    }
    
    @Override
    public ASTNode visitEnableComputeNode(final EnableComputeNodeContext ctx) {
        return buildSetComputeNodeStateStatement(ctx.ENABLE().getText().toUpperCase(), ctx.instanceId());
    }
    
    @Override
    public ASTNode visitDisableComputeNode(final DisableComputeNodeContext ctx) {
        return buildSetComputeNodeStateStatement(ctx.DISABLE().getText().toUpperCase(), ctx.instanceId());
    }
    
    @Override
    public ASTNode visitLabelComputeNode(final LabelComputeNodeContext ctx) {
        Collection<String> labels = ctx.label().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toList());
        return new LabelComputeNodeStatement(null != ctx.RELABEL(), IdentifierValueUtils.getValue(ctx.instanceId()), labels);
    }
    
    @Override
    public ASTNode visitUnlabelComputeNode(final UnlabelComputeNodeContext ctx) {
        Collection<String> labels = ctx.label().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toList());
        return new UnlabelComputeNodeStatement(IdentifierValueUtils.getValue(ctx.instanceId()), labels);
    }
    
    private SetComputeNodeStateStatement buildSetComputeNodeStateStatement(final String status, final InstanceIdContext instanceIdContext) {
        return new SetComputeNodeStateStatement(status, IdentifierValueUtils.getValue(instanceIdContext));
    }
    
    @Override
    public ASTNode visitAlterComputeNode(final AlterComputeNodeContext ctx) {
        return new AlterComputeNodeStatement(IdentifierValueUtils.getValue(ctx.instanceId()), IdentifierValueUtils.getValue(ctx.variableName()), IdentifierValueUtils.getValue(ctx.variableValues()));
    }
    
    private Properties getProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx || null == ctx.properties()) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(QuoteCharacter.unwrapAndTrimText(each.key.getText()), QuoteCharacter.unwrapAndTrimText(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUnregisterStorageUnit(final UnregisterStorageUnitContext ctx) {
        boolean ignoreSingleTables = ctx.ignoreTables() instanceof IgnoreSingleAndBroadcastTablesContext || ctx.ignoreTables() instanceof IgnoreSingleTablesContext;
        boolean ignoreBroadcastTables = ctx.ignoreTables() instanceof IgnoreSingleAndBroadcastTablesContext || ctx.ignoreTables() instanceof IgnoreBroadcastTablesContext;
        return new UnregisterStorageUnitStatement(null != ctx.ifExists(),
                ctx.storageUnitName().stream().map(ParseTree::getText).map(each -> new IdentifierValue(each).getValue()).collect(Collectors.toList()),
                ignoreSingleTables, ignoreBroadcastTables);
    }
    
    @Override
    public ASTNode visitShowStorageUnits(final ShowStorageUnitsContext ctx) {
        FromDatabaseSegment fromDatabase = null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName()));
        String likePattern = null == ctx.showLike() ? null : IdentifierValueUtils.getValue(ctx.showLike().likePattern());
        return new ShowStorageUnitsStatement(fromDatabase, likePattern);
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitSetDistVariable(final SetDistVariableContext ctx) {
        return new SetDistVariableStatement(IdentifierValueUtils.getValue(ctx.variableName()), IdentifierValueUtils.getValue(ctx.variableValue()));
    }
    
    @Override
    public ASTNode visitShowLogicalTables(final ShowLogicalTablesContext ctx) {
        FromDatabaseSegment fromDatabase = null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName()));
        return new ShowLogicalTablesStatement(null != ctx.FULL(), fromDatabase, null == ctx.showLike() ? null : IdentifierValueUtils.getValue(ctx.showLike().likePattern()));
    }
    
    @Override
    public ASTNode visitShowDistVariable(final ShowDistVariableContext ctx) {
        return new ShowDistVariableStatement(Objects.requireNonNull(IdentifierValueUtils.getValue(ctx.variableName())).toUpperCase());
    }
    
    @Override
    public ASTNode visitShowDistVariables(final ShowDistVariablesContext ctx) {
        return new ShowDistVariablesStatement(null != ctx.TEMP(), null == ctx.showLike() ? null : IdentifierValueUtils.getValue(ctx.showLike().likePattern()));
    }
    
    @Override
    public ASTNode visitRefreshDatabaseMetadata(final RefreshDatabaseMetadataContext ctx) {
        return new RefreshDatabaseMetaDataStatement(null == ctx.databaseName() ? null : IdentifierValueUtils.getValue(ctx.databaseName()), null != ctx.FORCE());
    }
    
    @Override
    public ASTNode visitRefreshTableMetadata(final RefreshTableMetadataContext ctx) {
        if (null == ctx.refreshScope()) {
            return new RefreshTableMetaDataStatement();
        }
        String storageUnitName = null;
        String schemaName = null;
        String tableName = IdentifierValueUtils.getValue(ctx.refreshScope().tableName());
        if (null != ctx.refreshScope().fromSegment()) {
            FromSegmentContext fromSegment = ctx.refreshScope().fromSegment();
            storageUnitName = IdentifierValueUtils.getValue(fromSegment.storageUnitName());
            schemaName = null == fromSegment.schemaName() ? null : IdentifierValueUtils.getValue(fromSegment.schemaName());
        }
        return new RefreshTableMetaDataStatement(tableName, storageUnitName, schemaName);
    }
    
    @Override
    public ASTNode visitExportDatabaseConfiguration(final ExportDatabaseConfigurationContext ctx) {
        return new ExportDatabaseConfigurationStatement(IdentifierValueUtils.getValue(ctx.filePath()),
                null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())));
    }
    
    @Override
    public ASTNode visitExportMetaData(final ExportMetaDataContext ctx) {
        return new ExportMetaDataStatement(null == ctx.filePath() ? null : IdentifierValueUtils.getValue(ctx.filePath()));
    }
    
    @Override
    public ASTNode visitExportStorageNodes(final ExportStorageNodesContext ctx) {
        return new ExportStorageNodesStatement(
                null == ctx.databaseName() ? null : IdentifierValueUtils.getValue(ctx.databaseName()), null == ctx.filePath() ? null : IdentifierValueUtils.getValue(ctx.filePath()));
    }
    
    @Override
    public ASTNode visitConvertYamlConfiguration(final ConvertYamlConfigurationContext ctx) {
        return new ConvertYamlConfigurationStatement(IdentifierValueUtils.getValue(ctx.filePath()));
    }
    
    @Override
    public ASTNode visitShowRulesUsedStorageUnit(final ShowRulesUsedStorageUnitContext ctx) {
        return new ShowRulesUsedStorageUnitStatement(IdentifierValueUtils.getValue(ctx.storageUnitName()),
                null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())));
    }
    
    @Override
    public ASTNode visitImportDatabaseConfiguration(final ImportDatabaseConfigurationContext ctx) {
        return new ImportDatabaseConfigurationStatement(IdentifierValueUtils.getValue(ctx.filePath()));
    }
    
    @Override
    public ASTNode visitImportMetaData(final ImportMetaDataContext ctx) {
        return new ImportMetaDataStatement(null == ctx.metaDataValue() ? null : QuoteCharacter.unwrapText(ctx.metaDataValue().getText()), IdentifierValueUtils.getValue(ctx.filePath()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(IdentifierValueUtils.getValue(ctx.algorithmTypeName()), buildProperties(ctx.propertiesDefinition()));
    }
    
    private Properties buildProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(QuoteCharacter.unwrapAndTrimText(each.key.getText()), QuoteCharacter.unwrapAndTrimText(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitLockCluster(final LockClusterContext ctx) {
        return new LockClusterStatement((AlgorithmSegment) visitAlgorithmDefinition(ctx.lockStrategy().algorithmDefinition()), Long.parseLong(IdentifierValueUtils.getValue(ctx.INT_())));
    }
    
    @Override
    public ASTNode visitUnlockCluster(final UnlockClusterContext ctx) {
        return new UnlockClusterStatement(Long.parseLong(IdentifierValueUtils.getValue(ctx.INT_())));
    }
    
    @Override
    public ASTNode visitShowPluginImplementations(final ShowPluginImplementationsContext ctx) {
        return new ShowPluginsStatement("COMMON", IdentifierValueUtils.getValue(ctx.pluginClass()));
    }
    
    @Override
    public ASTNode visitShowKeyGenerateAlgorithmPlugins(final ShowKeyGenerateAlgorithmPluginsContext ctx) {
        return new ShowPluginsStatement("KEY_GENERATE_ALGORITHM");
    }
    
    @Override
    public ASTNode visitShowLoadBalanceAlgorithmPlugins(final ShowLoadBalanceAlgorithmPluginsContext ctx) {
        return new ShowPluginsStatement("LOAD_BALANCE_ALGORITHM");
    }
}
