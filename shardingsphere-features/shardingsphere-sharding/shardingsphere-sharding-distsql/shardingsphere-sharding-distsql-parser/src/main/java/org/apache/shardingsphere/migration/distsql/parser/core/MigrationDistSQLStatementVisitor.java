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

package org.apache.shardingsphere.migration.distsql.parser.core;

import com.google.common.base.Splitter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.AddMigrationSourceResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ApplyScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.BatchSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.CheckScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.CleanScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.CompletionDetectorContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.CreateShardingScalingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.DataConsistencyCheckerContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.DisableShardingScalingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.DropMigrationSourceResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.EnableShardingScalingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.InputDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.MigrateTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.OutputDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PasswordContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.RateLimiterContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ResetScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ResourceDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.RestoreScalingSourceWritingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ScalingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShardingSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowScalingCheckAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowScalingListContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowScalingStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowShardingScalingRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StartScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StopScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StopScalingSourceWritingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StreamChannelContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.WorkerThreadContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.migration.distsql.statement.AddMigrationSourceResourceStatement;
import org.apache.shardingsphere.migration.distsql.statement.ApplyMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.CheckMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.CleanMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.CreateShardingScalingRuleStatement;
import org.apache.shardingsphere.migration.distsql.statement.DisableShardingScalingRuleStatement;
import org.apache.shardingsphere.migration.distsql.statement.DropMigrationSourceResourceStatement;
import org.apache.shardingsphere.migration.distsql.statement.DropShardingScalingRuleStatement;
import org.apache.shardingsphere.migration.distsql.statement.EnableShardingScalingRuleStatement;
import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.migration.distsql.statement.ResetMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.RestoreMigrationSourceWritingStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationCheckAlgorithmsStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationListStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationStatusStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowShardingScalingRulesStatement;
import org.apache.shardingsphere.migration.distsql.statement.StartMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.StopMigrationSourceWritingStatement;
import org.apache.shardingsphere.migration.distsql.statement.StopMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.segment.InputOrOutputSegment;
import org.apache.shardingsphere.migration.distsql.statement.segment.ShardingScalingRuleConfigurationSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for migration dist SQL.
 */
public final class MigrationDistSQLStatementVisitor extends MigrationDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitMigrateTable(final MigrateTableContext ctx) {
        List<String> source = Splitter.on('.').splitToList(getIdentifierValue(ctx.sourceTableName()));
        List<String> target = Splitter.on('.').splitToList(getIdentifierValue(ctx.targetTableName()));
        return new MigrateTableStatement(source.size() > 1 ? source.get(0) : null, source.get(source.size() - 1), target.size() > 1 ? target.get(0) : null, target.get(target.size() - 1));
    }
    
    @Override
    public ASTNode visitShowScalingList(final ShowScalingListContext ctx) {
        return new ShowMigrationListStatement();
    }
    
    @Override
    public ASTNode visitShowScalingStatus(final ShowScalingStatusContext ctx) {
        return new ShowMigrationStatusStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStartScaling(final StartScalingContext ctx) {
        return new StartMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStopScaling(final StopScalingContext ctx) {
        return new StopMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitCleanScaling(final CleanScalingContext ctx) {
        return new CleanMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitResetScaling(final ResetScalingContext ctx) {
        return new ResetMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitCheckScaling(final CheckScalingContext ctx) {
        return new CheckMigrationStatement(getIdentifierValue(ctx.jobId()), null == ctx.algorithmDefinition() ? null : (AlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitShowScalingCheckAlgorithms(final ShowScalingCheckAlgorithmsContext ctx) {
        return new ShowMigrationCheckAlgorithmsStatement();
    }
    
    @Override
    public ASTNode visitStopScalingSourceWriting(final StopScalingSourceWritingContext ctx) {
        return new StopMigrationSourceWritingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitRestoreScalingSourceWriting(final RestoreScalingSourceWritingContext ctx) {
        return new RestoreMigrationSourceWritingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitApplyScaling(final ApplyScalingContext ctx) {
        return new ApplyMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitCreateShardingScalingRule(final CreateShardingScalingRuleContext ctx) {
        ShardingScalingRuleConfigurationSegment scalingRuleConfigSegment = null == ctx.scalingRuleDefinition() ? null : (ShardingScalingRuleConfigurationSegment) visit(ctx.scalingRuleDefinition());
        return new CreateShardingScalingRuleStatement(getIdentifierValue(ctx.scalingName()), scalingRuleConfigSegment);
    }
    
    @Override
    public ASTNode visitScalingRuleDefinition(final ScalingRuleDefinitionContext ctx) {
        ShardingScalingRuleConfigurationSegment result = new ShardingScalingRuleConfigurationSegment();
        if (null != ctx.inputDefinition()) {
            result.setInputSegment((InputOrOutputSegment) visit(ctx.inputDefinition()));
        }
        if (null != ctx.outputDefinition()) {
            result.setOutputSegment((InputOrOutputSegment) visit(ctx.outputDefinition()));
        }
        if (null != ctx.streamChannel()) {
            result.setStreamChannel((AlgorithmSegment) visit(ctx.streamChannel()));
        }
        if (null != ctx.completionDetector()) {
            result.setCompletionDetector((AlgorithmSegment) visit(ctx.completionDetector()));
        }
        if (null != ctx.dataConsistencyChecker()) {
            result.setDataConsistencyCalculator((AlgorithmSegment) visit(ctx.dataConsistencyChecker()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInputDefinition(final InputDefinitionContext ctx) {
        return new InputOrOutputSegment(getWorkerThread(ctx.workerThread()), getBatchSize(ctx.batchSize()), getShardingSize(ctx.shardingSize()), getAlgorithmSegment(ctx.rateLimiter()));
    }
    
    @Override
    public ASTNode visitOutputDefinition(final OutputDefinitionContext ctx) {
        return new InputOrOutputSegment(getWorkerThread(ctx.workerThread()), getBatchSize(ctx.batchSize()), getAlgorithmSegment(ctx.rateLimiter()));
    }
    
    private AlgorithmSegment getAlgorithmSegment(final RateLimiterContext ctx) {
        if (null == ctx) {
            return null;
        }
        return (AlgorithmSegment) visit(ctx);
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
    
    @Override
    public ASTNode visitCompletionDetector(final CompletionDetectorContext ctx) {
        return visit(ctx.algorithmDefinition());
    }
    
    @Override
    public ASTNode visitDataConsistencyChecker(final DataConsistencyCheckerContext ctx) {
        return visit(ctx.algorithmDefinition());
    }
    
    @Override
    public ASTNode visitDropShardingScalingRule(final MigrationDistSQLStatementParser.DropShardingScalingRuleContext ctx) {
        return new DropShardingScalingRuleStatement(null != ctx.ifExists(), getIdentifierValue(ctx.scalingName()));
    }
    
    @Override
    public ASTNode visitEnableShardingScalingRule(final EnableShardingScalingRuleContext ctx) {
        return new EnableShardingScalingRuleStatement(getIdentifierValue(ctx.scalingName()));
    }
    
    @Override
    public ASTNode visitDisableShardingScalingRule(final DisableShardingScalingRuleContext ctx) {
        return new DisableShardingScalingRuleStatement(getIdentifierValue(ctx.scalingName()));
    }
    
    @Override
    public ASTNode visitShowShardingScalingRules(final ShowShardingScalingRulesContext ctx) {
        return new ShowShardingScalingRulesStatement(null == ctx.schemaName() ? null : (DatabaseSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), getAlgorithmProperties(ctx));
    }
    
    private Properties getAlgorithmProperties(final AlgorithmDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx.algorithmProperties()) {
            return result;
        }
        for (MigrationDistSQLStatementParser.AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        if (null == context) {
            return null;
        }
        return new IdentifierValue(context.getText()).getValue();
    }
    
    @Override
    public ASTNode visitResourceDefinition(final MigrationDistSQLStatementParser.ResourceDefinitionContext ctx) {
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
    public ASTNode visitAddMigrationSourceResource(final AddMigrationSourceResourceContext ctx) {
        Collection<DataSourceSegment> dataSources = new ArrayList<>();
        for (ResourceDefinitionContext each : ctx.resourceDefinition()) {
            dataSources.add((DataSourceSegment) visit(each));
        }
        return new AddMigrationSourceResourceStatement(dataSources);
    }
    
    @Override
    public ASTNode visitDropMigrationSourceResource(final DropMigrationSourceResourceContext ctx) {
        return new DropMigrationSourceResourceStatement(ctx.resourceName().stream().map(ParseTree::getText).map(each -> new IdentifierValue(each).getValue()).collect(Collectors.toList()));
    }
}
