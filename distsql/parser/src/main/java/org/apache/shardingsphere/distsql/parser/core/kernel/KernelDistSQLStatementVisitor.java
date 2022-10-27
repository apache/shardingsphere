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
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterMigrationRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ApplyDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.BatchSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ClearHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ConvertYamlConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CountSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DisableComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DiscardDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.EnableComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ExportDatabaseConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.FromSegmentContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ImportDatabaseConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.InstanceIdContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.InventoryIncrementalRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.LabelComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PasswordContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PrepareDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RateLimiterContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ReadDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RefreshTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RegisterStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.SetDefaultSingleTableStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.SetDistVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShardingSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowComputeNodeInfoContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowComputeNodeModeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowComputeNodesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowDefaultSingleTableStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowDistVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowDistVariablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowMigrationRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowRulesUsedStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowSingleTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowStorageUnitsContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.StorageUnitDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.StreamChannelContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.UnlabelComputeNodeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.UnregisterStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.WorkerThreadContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.WriteDefinitionContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.InventoryIncrementalRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.hint.ClearHintStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowMigrationRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterComputeNodeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterInventoryIncrementalRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ApplyDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DiscardDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LabelComputeNodeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.PrepareDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlabelComputeNodeStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.SetDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountSingleTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowStorageUnitsStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for kernel dist SQL.
 */
public final class KernelDistSQLStatementVisitor extends KernelDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitRegisterStorageUnit(final RegisterStorageUnitContext ctx) {
        return new RegisterStorageUnitStatement(ctx.storageUnitDefinition().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterStorageUnit(final AlterStorageUnitContext ctx) {
        return new AlterStorageUnitStatement(ctx.storageUnitDefinition().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowTableMetadata(final ShowTableMetadataContext ctx) {
        Collection<String> tableNames = ctx.tableName().stream().map(this::getIdentifierValue).collect(Collectors.toSet());
        return new ShowTableMetadataStatement(tableNames, null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitStorageUnitDefinition(final StorageUnitDefinitionContext ctx) {
        String user = getIdentifierValue(ctx.user());
        String password = null == ctx.password() ? "" : getPassword(ctx.password());
        Properties props = getProperties(ctx.propertiesDefinition());
        DataSourceSegment result = null;
        if (null != ctx.urlSource()) {
            result = new URLBasedDataSourceSegment(getIdentifierValue(ctx.storageUnitName()), getIdentifierValue(ctx.urlSource().url()), user, password, props);
        }
        if (null != ctx.simpleSource()) {
            result = new HostnameAndPortBasedDataSourceSegment(getIdentifierValue(ctx.storageUnitName()), getIdentifierValue(ctx.simpleSource().hostname()), ctx.simpleSource().port().getText(),
                    getIdentifierValue(ctx.simpleSource().dbName()), user, password, props);
        }
        return result;
    }
    
    private String getPassword(final PasswordContext ctx) {
        return getIdentifierValue(ctx);
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
        return buildSetInstanceStatusStatement(ctx.ENABLE().getText().toUpperCase(), ctx.instanceId());
    }
    
    @Override
    public ASTNode visitDisableComputeNode(final DisableComputeNodeContext ctx) {
        return buildSetInstanceStatusStatement(ctx.DISABLE().getText().toUpperCase(), ctx.instanceId());
    }
    
    @Override
    public ASTNode visitLabelComputeNode(final LabelComputeNodeContext ctx) {
        Collection<String> labels = ctx.label().stream().map(this::getIdentifierValue).collect(Collectors.toList());
        return new LabelComputeNodeStatement(ctx.RELABEL() != null, getIdentifierValue(ctx.instanceId()), labels);
    }
    
    @Override
    public ASTNode visitUnlabelComputeNode(final UnlabelComputeNodeContext ctx) {
        Collection<String> labels = ctx.label().stream().map(this::getIdentifierValue).collect(Collectors.toList());
        return new UnlabelComputeNodeStatement(getIdentifierValue(ctx.instanceId()), labels);
    }
    
    private SetInstanceStatusStatement buildSetInstanceStatusStatement(final String status, final InstanceIdContext instanceIdContext) {
        return new SetInstanceStatusStatement(status, getIdentifierValue(instanceIdContext));
    }
    
    @Override
    public ASTNode visitCountSingleTableRule(final CountSingleTableRuleContext ctx) {
        return new CountSingleTableRuleStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitAlterComputeNode(final AlterComputeNodeContext ctx) {
        return new AlterComputeNodeStatement(getIdentifierValue(ctx.instanceId()), getIdentifierValue(ctx.variableName()), getIdentifierValue(ctx.variableValues()));
    }
    
    @Override
    public ASTNode visitSetDefaultSingleTableStorageUnit(final SetDefaultSingleTableStorageUnitContext ctx) {
        return new SetDefaultSingleTableStorageUnitStatement(null == ctx.storageUnitName() ? null : getIdentifierValue(ctx.storageUnitName()));
    }
    
    private Properties getProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx || null == ctx.properties()) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUnregisterStorageUnit(final UnregisterStorageUnitContext ctx) {
        boolean ignoreSingleTables = null != ctx.ignoreSingleTables();
        return new UnregisterStorageUnitStatement(ctx.ifExists() != null,
                ctx.storageUnitName().stream().map(ParseTree::getText).map(each -> new IdentifierValue(each).getValue()).collect(Collectors.toList()), ignoreSingleTables);
    }
    
    @Override
    public ASTNode visitShowStorageUnits(final ShowStorageUnitsContext ctx) {
        DatabaseSegment database = null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName());
        Integer usageCount = null == ctx.usageCount() ? null : Integer.parseInt(ctx.usageCount().getText());
        return new ShowStorageUnitsStatement(database, usageCount);
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitSetDistVariable(final SetDistVariableContext ctx) {
        return new SetDistVariableStatement(getIdentifierValue(ctx.variableName()), getIdentifierValue(ctx.variableValue()));
    }
    
    @Override
    public ASTNode visitShowDefaultSingleTableStorageUnit(final ShowDefaultSingleTableStorageUnitContext ctx) {
        return new ShowDefaultSingleTableStorageUnitStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowSingleTable(final ShowSingleTableContext ctx) {
        return new ShowSingleTableStatement(null == ctx.TABLE() ? null : getIdentifierValue(ctx.tableName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowDistVariable(final ShowDistVariableContext ctx) {
        return new ShowDistVariableStatement(getIdentifierValue(ctx.variableName()).toUpperCase());
    }
    
    @Override
    public ASTNode visitShowDistVariables(final ShowDistVariablesContext ctx) {
        return new ShowDistVariablesStatement();
    }
    
    @Override
    public ASTNode visitClearHint(final ClearHintContext ctx) {
        return new ClearHintStatement();
    }
    
    @Override
    public ASTNode visitRefreshTableMetadata(final RefreshTableMetadataContext ctx) {
        if (null == ctx.refreshScope()) {
            return new RefreshTableMetadataStatement();
        }
        String storageUnitName = null;
        String schemaName = null;
        String tableName = getIdentifierValue(ctx.refreshScope().tableName());
        if (null != ctx.refreshScope().fromSegment()) {
            FromSegmentContext fromSegment = ctx.refreshScope().fromSegment();
            storageUnitName = getIdentifierValue(fromSegment.storageUnitName());
            schemaName = null == fromSegment.schemaName() ? null : getIdentifierValue(fromSegment.schemaName());
        }
        return new RefreshTableMetadataStatement(tableName, storageUnitName, schemaName);
    }
    
    @Override
    public ASTNode visitExportDatabaseConfiguration(final ExportDatabaseConfigurationContext ctx) {
        return new ExportDatabaseConfigurationStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()), getIdentifierValue(ctx.filePath()));
    }
    
    @Override
    public ASTNode visitConvertYamlConfiguration(final ConvertYamlConfigurationContext ctx) {
        return new ConvertYamlConfigurationStatement(getIdentifierValue(ctx.filePath()));
    }
    
    @Override
    public ASTNode visitShowRulesUsedStorageUnit(final ShowRulesUsedStorageUnitContext ctx) {
        return new ShowRulesUsedStorageUnitStatement(getIdentifierValue(ctx.storageUnitName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitPrepareDistSQL(final PrepareDistSQLContext ctx) {
        return new PrepareDistSQLStatement();
    }
    
    @Override
    public ASTNode visitApplyDistSQL(final ApplyDistSQLContext ctx) {
        return new ApplyDistSQLStatement();
    }
    
    @Override
    public ASTNode visitDiscardDistSQL(final DiscardDistSQLContext ctx) {
        return new DiscardDistSQLStatement();
    }
    
    @Override
    public ASTNode visitImportDatabaseConfiguration(final ImportDatabaseConfigurationContext ctx) {
        return new ImportDatabaseConfigurationStatement(getIdentifierValue(ctx.filePath()));
    }
    
    @Override
    public ASTNode visitShowMigrationRule(final ShowMigrationRuleContext ctx) {
        return new ShowMigrationRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterMigrationRule(final AlterMigrationRuleContext ctx) {
        InventoryIncrementalRuleSegment segment = null == ctx.inventoryIncrementalRule() ? null
                : (InventoryIncrementalRuleSegment) visit(ctx.inventoryIncrementalRule());
        return new AlterInventoryIncrementalRuleStatement("MIGRATION", segment);
    }
    
    @Override
    public ASTNode visitInventoryIncrementalRule(final InventoryIncrementalRuleContext ctx) {
        InventoryIncrementalRuleSegment result = new InventoryIncrementalRuleSegment();
        if (null != ctx.readDefinition()) {
            result.setReadSegment((ReadOrWriteSegment) visit(ctx.readDefinition()));
        }
        if (null != ctx.writeDefinition()) {
            result.setWriteSegment((ReadOrWriteSegment) visit(ctx.writeDefinition()));
        }
        if (null != ctx.streamChannel()) {
            result.setStreamChannel((AlgorithmSegment) visit(ctx.streamChannel()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitReadDefinition(final ReadDefinitionContext ctx) {
        return new ReadOrWriteSegment(getWorkerThread(ctx.workerThread()), getBatchSize(ctx.batchSize()), getShardingSize(ctx.shardingSize()), getAlgorithmSegment(ctx.rateLimiter()));
    }
    
    @Override
    public ASTNode visitWriteDefinition(final WriteDefinitionContext ctx) {
        return new ReadOrWriteSegment(getWorkerThread(ctx.workerThread()), getBatchSize(ctx.batchSize()), getAlgorithmSegment(ctx.rateLimiter()));
    }
    
    private AlgorithmSegment getAlgorithmSegment(final RateLimiterContext ctx) {
        if (null == ctx) {
            return null;
        }
        return (AlgorithmSegment) visit(ctx);
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), buildProperties(ctx.propertiesDefinition()));
    }
    
    private Properties buildProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
    
    private Integer getWorkerThread(final WorkerThreadContext ctx) {
        if (null == ctx) {
            return null;
        }
        return Integer.parseInt(ctx.intValue().getText());
    }
    
    private Integer getBatchSize(final BatchSizeContext ctx) {
        if (null == ctx) {
            return null;
        }
        return Integer.parseInt(ctx.intValue().getText());
    }
    
    private Integer getShardingSize(final ShardingSizeContext ctx) {
        if (null == ctx) {
            return null;
        }
        return Integer.parseInt(ctx.intValue().getText());
    }
    
    @Override
    public ASTNode visitRateLimiter(final RateLimiterContext ctx) {
        return visit(ctx.algorithmDefinition());
    }
    
    @Override
    public ASTNode visitStreamChannel(final StreamChannelContext ctx) {
        return visit(ctx.algorithmDefinition());
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
}
