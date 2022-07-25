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
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AddResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlgorithmPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterSQLParserRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterTrafficRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.AlterTransactionRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ApplyDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CacheOptionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ClearHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ConvertYamlConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CountDatabaseRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CountSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CreateDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.CreateTrafficRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DisableInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DiscardDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DropDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DropResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.DropTrafficRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.EnableInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ExportDatabaseConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.FromSegmentContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ImportDatabaseConfigurationContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.InstanceIdContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.LabelDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.LabelInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.LoadBalancerDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PasswordContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PrepareDistSQLContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ProviderDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.RefreshTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.SetVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowAllVariablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowAuthorityRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowInstanceModeContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowRulesUsedResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowSQLParserRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowSQLTranslatorRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowSingleTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowSingleTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowTrafficRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowTransactionRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowUnusedResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.ShowVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.SqlParserRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.TrafficRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.TransactionRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.KernelDistSQLStatementParser.UnlabelInstanceContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.TransactionProviderSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.hint.ClearHintStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAllVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceModeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ApplyDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.CreateTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DiscardDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DropTrafficRuleStatement;
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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for kernel dist SQL.
 */
public final class KernelDistSQLStatementVisitor extends KernelDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitAddResource(final AddResourceContext ctx) {
        return new AddResourceStatement(ctx.dataSource().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterResource(final AlterResourceContext ctx) {
        return new AlterResourceStatement(ctx.dataSource().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowTableMetadata(final ShowTableMetadataContext ctx) {
        Collection<String> tableNames = ctx.tableName().stream().map(this::getIdentifierValue).collect(Collectors.toSet());
        return new ShowTableMetadataStatement(tableNames, null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitDataSource(final DataSourceContext ctx) {
        String password = null == ctx.password() ? "" : getPassword(ctx.password());
        Properties props = getProperties(ctx.propertiesDefinition());
        DataSourceSegment result = null;
        if (null != ctx.urlSource()) {
            result = new URLBasedDataSourceSegment(getIdentifierValue(ctx.dataSourceName()), getIdentifierValue(ctx.urlSource().url()), ctx.user().getText(), password, props);
        }
        if (null != ctx.simpleSource()) {
            result = new HostnameAndPortBasedDataSourceSegment(getIdentifierValue(ctx.dataSourceName()), ctx.simpleSource().hostname().getText(), ctx.simpleSource().port().getText(),
                    ctx.simpleSource().dbName().getText(), ctx.user().getText(), password, props);
        }
        return result;
    }
    
    private String getPassword(final List<PasswordContext> passwordContexts) {
        return passwordContexts.stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.joining(""));
    }
    
    @Override
    public ASTNode visitShowInstance(final ShowInstanceContext ctx) {
        return new ShowInstanceStatement();
    }
    
    @Override
    public ASTNode visitShowInstanceMode(final ShowInstanceModeContext ctx) {
        return new ShowInstanceModeStatement();
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
        return new CreateDefaultSingleTableRuleStatement(getIdentifierValue(ctx.dataSourceName()));
    }
    
    @Override
    public ASTNode visitAlterDefaultSingleTableRule(final AlterDefaultSingleTableRuleContext ctx) {
        return new AlterDefaultSingleTableRuleStatement(getIdentifierValue(ctx.dataSourceName()));
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
                ctx.IDENTIFIER().stream().map(ParseTree::getText).map(each -> new IdentifierValue(each).getValue()).collect(Collectors.toList()), ignoreSingleTables);
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
        return new ShowSingleTableStatement(null == ctx.table() ? null : getIdentifierValue(ctx.table().tableName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowVariable(final ShowVariableContext ctx) {
        return new ShowVariableStatement(getIdentifierValue(ctx.variableName()).toUpperCase());
    }
    
    @Override
    public ASTNode visitShowAllVariables(final ShowAllVariablesContext ctx) {
        return new ShowAllVariableStatement();
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
    public ASTNode visitShowAuthorityRule(final ShowAuthorityRuleContext ctx) {
        return new ShowAuthorityRuleStatement();
    }
    
    @Override
    public ASTNode visitShowTransactionRule(final ShowTransactionRuleContext ctx) {
        return new ShowTransactionRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterTransactionRule(final AlterTransactionRuleContext ctx) {
        return visit(ctx.transactionRuleDefinition());
    }
    
    @Override
    public ASTNode visitDropTrafficRule(final DropTrafficRuleContext ctx) {
        Collection<String> ruleNames = null == ctx.ruleName() ? null : ctx.ruleName().stream().map(this::getIdentifierValue).collect(Collectors.toSet());
        return new DropTrafficRuleStatement(null != ctx.ifExists(), ruleNames);
    }
    
    @Override
    public ASTNode visitTransactionRuleDefinition(final TransactionRuleDefinitionContext ctx) {
        String defaultType = getIdentifierValue(ctx.defaultType());
        if (null != ctx.providerDefinition()) {
            TransactionProviderSegment provider = (TransactionProviderSegment) visit(ctx.providerDefinition());
            return new AlterTransactionRuleStatement(defaultType, provider);
        }
        return new AlterTransactionRuleStatement(defaultType, new TransactionProviderSegment(null, null));
    }
    
    @Override
    public ASTNode visitProviderDefinition(final ProviderDefinitionContext ctx) {
        return new TransactionProviderSegment(getIdentifierValue(ctx.providerName()), getProperties(ctx.propertiesDefinition()));
    }
    
    @Override
    public ASTNode visitShowSQLParserRule(final ShowSQLParserRuleContext ctx) {
        return new ShowSQLParserRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterSQLParserRule(final AlterSQLParserRuleContext ctx) {
        return super.visit(ctx.sqlParserRuleDefinition());
    }
    
    @Override
    public ASTNode visitSqlParserRuleDefinition(final SqlParserRuleDefinitionContext ctx) {
        Boolean sqlCommentParseEnable = null == ctx.sqlCommentParseEnable() ? null : Boolean.parseBoolean(getIdentifierValue(ctx.sqlCommentParseEnable()));
        CacheOptionSegment parseTreeCache = null == ctx.parseTreeCache() ? null : visitCacheOption(ctx.parseTreeCache().cacheOption());
        CacheOptionSegment sqlStatementCache = null == ctx.sqlStatementCache() ? null : visitCacheOption(ctx.sqlStatementCache().cacheOption());
        return new AlterSQLParserRuleStatement(sqlCommentParseEnable, parseTreeCache, sqlStatementCache);
    }
    
    @Override
    public CacheOptionSegment visitCacheOption(final CacheOptionContext ctx) {
        return new CacheOptionSegment(
                null == ctx.initialCapacity() ? null : Integer.parseInt(getIdentifierValue(ctx.initialCapacity())),
                null == ctx.maximumSize() ? null : Long.parseLong(getIdentifierValue(ctx.maximumSize())));
    }
    
    @Override
    public ASTNode visitCreateTrafficRule(final CreateTrafficRuleContext ctx) {
        return new CreateTrafficRuleStatement(ctx.trafficRuleDefinition().stream().map(each -> (TrafficRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterTrafficRule(final AlterTrafficRuleContext ctx) {
        return new AlterTrafficRuleStatement(ctx.trafficRuleDefinition().stream().map(each -> (TrafficRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowTrafficRules(final ShowTrafficRulesContext ctx) {
        return new ShowTrafficRulesStatement(null == ctx.ruleName() ? null : getIdentifierValue(ctx.ruleName()));
    }
    
    @Override
    public ASTNode visitTrafficRuleDefinition(final TrafficRuleDefinitionContext ctx) {
        AlgorithmSegment loadBalancerSegment = null;
        if (null != ctx.loadBalancerDefinition()) {
            loadBalancerSegment = (AlgorithmSegment) visit(ctx.loadBalancerDefinition().algorithmDefinition());
        }
        return new TrafficRuleSegment(getIdentifierValue(ctx.ruleName()), buildLabels(ctx.labelDefinition()),
                (AlgorithmSegment) visit(ctx.trafficAlgorithmDefinition().algorithmDefinition()), loadBalancerSegment);
    }
    
    @Override
    public ASTNode visitLoadBalancerDefinition(final LoadBalancerDefinitionContext ctx) {
        if (null == ctx) {
            return null;
        }
        return visit(ctx.algorithmDefinition());
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.typeName()), buildProperties(ctx.algorithmProperties()));
    }
    
    private Properties buildProperties(final AlgorithmPropertiesContext algorithmProps) {
        Properties result = new Properties();
        if (null == algorithmProps) {
            return result;
        }
        for (AlgorithmPropertyContext each : algorithmProps.algorithmProperty()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
    
    private Collection<String> buildLabels(final LabelDefinitionContext labelDefinition) {
        if (null == labelDefinition) {
            return Collections.emptyList();
        }
        return labelDefinition.label().stream().map(this::getIdentifierValue).collect(Collectors.toList());
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
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
}
