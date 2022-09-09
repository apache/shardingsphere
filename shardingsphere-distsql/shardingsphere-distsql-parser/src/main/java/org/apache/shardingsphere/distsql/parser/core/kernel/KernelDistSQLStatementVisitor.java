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
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AddResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterMigrationProcessConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ApplyDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.BatchSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ClearHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ConvertYamlConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CountDatabaseRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CountSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CreateDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CreateMigrationProcessConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DisableInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DiscardDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DropDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DropResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.EnableInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ExportDatabaseConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.FromSegmentContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ImportDatabaseConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.InstanceIdContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.InventoryIncrementalProcessConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.LabelInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PasswordContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PrepareDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RateLimiterContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ReadDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RefreshTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ResourceDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.SetVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShardingSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowAllVariablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowInstanceInfoContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowInstanceListContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowMigrationProcessConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowModeInfoContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowRulesUsedResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowSQLTranslatorRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowSingleTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowSingleTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowUnusedResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.StreamChannelContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.UnlabelInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.WorkerThreadContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.WriteDefinitionContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.InventoryIncrementalProcessConfigurationSegment;
import org.apache.shardingsphere.distsql.parser.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.hint.ClearHintStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAllVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceListStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowMigrationProcessConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowModeInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterInventoryIncrementalProcessConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ApplyDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.CreateInventoryIncrementalProcessConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DiscardDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DropPipelineProcessConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.PrepareDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountDatabaseRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountSingleTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowUnusedResourcesStatement;
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
    public ASTNode visitAddResource(final AddResourceContext ctx) {
        return new AddResourceStatement(ctx.resourceDefinition().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterResource(final AlterResourceContext ctx) {
        return new AlterResourceStatement(ctx.resourceDefinition().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowTableMetadata(final ShowTableMetadataContext ctx) {
        Collection<String> tableNames = ctx.tableName().stream().map(this::getIdentifierValue).collect(Collectors.toSet());
        return new ShowTableMetadataStatement(tableNames, null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitResourceDefinition(final ResourceDefinitionContext ctx) {
        String user = getIdentifierValue(ctx.user());
        String password = null == ctx.password() ? "" : getPassword(ctx.password());
        Properties props = getProperties(ctx.propertiesDefinition());
        DataSourceSegment result = null;
        if (null != ctx.urlSource()) {
            result = new URLBasedDataSourceSegment(getIdentifierValue(ctx.resourceName()), getIdentifierValue(ctx.urlSource().url()), user, password, props);
        }
        if (null != ctx.simpleSource()) {
            result = new HostnameAndPortBasedDataSourceSegment(getIdentifierValue(ctx.resourceName()), getIdentifierValue(ctx.simpleSource().hostname()), ctx.simpleSource().port().getText(),
                    getIdentifierValue(ctx.simpleSource().dbName()), user, password, props);
        }
        return result;
    }
    
    private String getPassword(final PasswordContext ctx) {
        return getIdentifierValue(ctx);
    }
    
    @Override
    public ASTNode visitShowInstanceList(final ShowInstanceListContext ctx) {
        return new ShowInstanceListStatement();
    }
    
    @Override
    public ASTNode visitShowInstanceInfo(final ShowInstanceInfoContext ctx) {
        return new ShowInstanceInfoStatement();
    }
    
    @Override
    public ASTNode visitShowModeInfo(final ShowModeInfoContext ctx) {
        return new ShowModeInfoStatement();
    }
    
    @Override
    public ASTNode visitEnableInstance(final EnableInstanceContext ctx) {
        return buildSetInstanceStatusStatement(ctx.ENABLE().getText().toUpperCase(), ctx.instanceId());
    }
    
    @Override
    public ASTNode visitDisableInstance(final DisableInstanceContext ctx) {
        return buildSetInstanceStatusStatement(ctx.DISABLE().getText().toUpperCase(), ctx.instanceId());
    }
    
    @Override
    public ASTNode visitLabelInstance(final LabelInstanceContext ctx) {
        Collection<String> labels = ctx.label().stream().map(this::getIdentifierValue).collect(Collectors.toList());
        return new LabelInstanceStatement(ctx.RELABEL() != null, getIdentifierValue(ctx.instanceId()), labels);
    }
    
    @Override
    public ASTNode visitUnlabelInstance(final UnlabelInstanceContext ctx) {
        Collection<String> labels = ctx.label().stream().map(this::getIdentifierValue).collect(Collectors.toList());
        return new UnlabelInstanceStatement(getIdentifierValue(ctx.instanceId()), labels);
    }
    
    private SetInstanceStatusStatement buildSetInstanceStatusStatement(final String status, final InstanceIdContext instanceIdContext) {
        return new SetInstanceStatusStatement(status, getIdentifierValue(instanceIdContext));
    }
    
    @Override
    public ASTNode visitCountSingleTableRule(final CountSingleTableRuleContext ctx) {
        return new CountSingleTableRuleStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitAlterInstance(final AlterInstanceContext ctx) {
        return new AlterInstanceStatement(getIdentifierValue(ctx.instanceId()), getIdentifierValue(ctx.variableName()), getIdentifierValue(ctx.variableValues()));
    }
    
    @Override
    public ASTNode visitCountDatabaseRules(final CountDatabaseRulesContext ctx) {
        return new CountDatabaseRulesStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitCreateDefaultSingleTableRule(final CreateDefaultSingleTableRuleContext ctx) {
        return new CreateDefaultSingleTableRuleStatement(getIdentifierValue(ctx.resourceName()));
    }
    
    @Override
    public ASTNode visitAlterDefaultSingleTableRule(final AlterDefaultSingleTableRuleContext ctx) {
        return new AlterDefaultSingleTableRuleStatement(getIdentifierValue(ctx.resourceName()));
    }
    
    @Override
    public ASTNode visitDropDefaultSingleTableRule(final DropDefaultSingleTableRuleContext ctx) {
        return new DropDefaultSingleTableRuleStatement(null != ctx.ifExists());
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
    public ASTNode visitDropResource(final DropResourceContext ctx) {
        boolean ignoreSingleTables = null != ctx.ignoreSingleTables();
        return new DropResourceStatement(ctx.ifExists() != null,
                ctx.resourceName().stream().map(ParseTree::getText).map(each -> new IdentifierValue(each).getValue()).collect(Collectors.toList()), ignoreSingleTables);
    }
    
    @Override
    public ASTNode visitShowResources(final ShowResourcesContext ctx) {
        return new ShowResourcesStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowUnusedResources(final ShowUnusedResourcesContext ctx) {
        return new ShowUnusedResourcesStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        return new SetVariableStatement(getIdentifierValue(ctx.variableName()), getIdentifierValue(ctx.variableValue()));
    }
    
    @Override
    public ASTNode visitShowSingleTableRules(final ShowSingleTableRulesContext ctx) {
        return new ShowSingleTableRulesStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowSingleTable(final ShowSingleTableContext ctx) {
        return new ShowSingleTableStatement(null == ctx.TABLE() ? null : getIdentifierValue(ctx.tableName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowVariable(final ShowVariableContext ctx) {
        return new ShowVariableStatement(getIdentifierValue(ctx.variableName()).toUpperCase());
    }
    
    @Override
    public ASTNode visitShowAllVariables(final ShowAllVariablesContext ctx) {
        return new ShowAllVariablesStatement();
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
        String resourceName = null;
        String schemaName = null;
        String tableName = getIdentifierValue(ctx.refreshScope().tableName());
        if (null != ctx.refreshScope().fromSegment()) {
            FromSegmentContext fromSegment = ctx.refreshScope().fromSegment();
            resourceName = getIdentifierValue(fromSegment.resourceName());
            schemaName = null == fromSegment.schemaName() ? null : getIdentifierValue(fromSegment.schemaName());
        }
        return new RefreshTableMetadataStatement(tableName, resourceName, schemaName);
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
    public ASTNode visitShowRulesUsedResource(final ShowRulesUsedResourceContext ctx) {
        return new ShowRulesUsedResourceStatement(getIdentifierValue(ctx.resourceName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
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
    public ASTNode visitShowSQLTranslatorRule(final ShowSQLTranslatorRuleContext ctx) {
        return new ShowSQLTranslatorRuleStatement();
    }
    
    @Override
    public ASTNode visitShowMigrationProcessConfiguration(final ShowMigrationProcessConfigurationContext ctx) {
        return new ShowMigrationProcessConfigurationStatement();
    }
    
    @Override
    public ASTNode visitCreateMigrationProcessConfiguration(final CreateMigrationProcessConfigurationContext ctx) {
        InventoryIncrementalProcessConfigurationSegment segment = null == ctx.inventoryIncrementalProcessConfiguration() ? null
                : (InventoryIncrementalProcessConfigurationSegment) visit(ctx.inventoryIncrementalProcessConfiguration());
        return new CreateInventoryIncrementalProcessConfigurationStatement("MIGRATION", segment);
    }
    
    @Override
    public ASTNode visitAlterMigrationProcessConfiguration(final AlterMigrationProcessConfigurationContext ctx) {
        InventoryIncrementalProcessConfigurationSegment segment = null == ctx.inventoryIncrementalProcessConfiguration() ? null
                : (InventoryIncrementalProcessConfigurationSegment) visit(ctx.inventoryIncrementalProcessConfiguration());
        return new AlterInventoryIncrementalProcessConfigurationStatement("MIGRATION", segment);
    }
    
    @Override
    public ASTNode visitDropMigrationProcessConfiguration(final KernelDistSQLStatementParser.DropMigrationProcessConfigurationContext ctx) {
        return new DropPipelineProcessConfigurationStatement("MIGRATION", getIdentifierValue(ctx.confPath()));
    }
    
    @Override
    public ASTNode visitInventoryIncrementalProcessConfiguration(final InventoryIncrementalProcessConfigurationContext ctx) {
        InventoryIncrementalProcessConfigurationSegment result = new InventoryIncrementalProcessConfigurationSegment();
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
