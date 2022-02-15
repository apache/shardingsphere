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

package org.apache.shardingsphere.scaling.distsql.parser.core;

import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ApplyScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.CheckScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.CompletionDetectorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.CreateShardingScalingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.DataConsistencyCheckerContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.DisableShardingScalingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.DropScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.DropShardingScalingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.EnableShardingScalingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.InputDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.OutputDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.RateLimiterContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ResetScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ScalingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingCheckAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingListContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowShardingScalingRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StartScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StopScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StopScalingSourceWritingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StreamChannelContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.scaling.distsql.statement.ApplyScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.CheckScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DisableShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DropScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DropShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.EnableShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ResetScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingCheckAlgorithmsStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingListStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingStatusStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowShardingScalingRulesStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StartScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingSourceWritingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.segment.InputOrOutputSegment;
import org.apache.shardingsphere.scaling.distsql.statement.segment.ShardingScalingRuleConfigurationSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Properties;

/**
 * SQL statement visitor for scaling.
 */
public final class ScalingSQLStatementVisitor extends ScalingStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitShowScalingList(final ShowScalingListContext ctx) {
        return new ShowScalingListStatement();
    }
    
    @Override
    public ASTNode visitShowScalingStatus(final ShowScalingStatusContext ctx) {
        return new ShowScalingStatusStatement(ctx.jobId().getText());
    }
    
    @Override
    public ASTNode visitStartScaling(final StartScalingContext ctx) {
        return new StartScalingStatement(ctx.jobId().getText());
    }
    
    @Override
    public ASTNode visitStopScaling(final StopScalingContext ctx) {
        return new StopScalingStatement(ctx.jobId().getText());
    }
    
    @Override
    public ASTNode visitDropScaling(final DropScalingContext ctx) {
        return new DropScalingStatement(ctx.jobId().getText());
    }
    
    @Override
    public ASTNode visitResetScaling(final ResetScalingContext ctx) {
        return new ResetScalingStatement(ctx.jobId().getText());
    }
    
    @Override
    public ASTNode visitCheckScaling(final CheckScalingContext ctx) {
        AlgorithmSegment typeStrategy = null;
        if (null != ctx.algorithmDefinition()) {
            typeStrategy = (AlgorithmSegment) visit(ctx.algorithmDefinition());
        }
        return new CheckScalingStatement(ctx.jobId().getText(), typeStrategy);
    }
    
    @Override
    public ASTNode visitShowScalingCheckAlgorithms(final ShowScalingCheckAlgorithmsContext ctx) {
        return new ShowScalingCheckAlgorithmsStatement();
    }
    
    @Override
    public ASTNode visitStopScalingSourceWriting(final StopScalingSourceWritingContext ctx) {
        return new StopScalingSourceWritingStatement(ctx.jobId().getText());
    }
    
    @Override
    public ASTNode visitApplyScaling(final ApplyScalingContext ctx) {
        return new ApplyScalingStatement(ctx.jobId().getText());
    }
    
    @Override
    public ASTNode visitCreateShardingScalingRule(final CreateShardingScalingRuleContext ctx) {
        CreateShardingScalingRuleStatement result = new CreateShardingScalingRuleStatement(new IdentifierValue(ctx.scalingName().getText()).getValue());
        if (null != ctx.scalingRuleDefinition()) {
            result.setConfigurationSegment((ShardingScalingRuleConfigurationSegment) visit(ctx.scalingRuleDefinition()));
        }
        return result;
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
            result.setDataConsistencyChecker((AlgorithmSegment) visit(ctx.dataConsistencyChecker()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInputDefinition(final InputDefinitionContext ctx) {
        int workerThread = Integer.parseInt(ctx.workerThread().intValue().getText());
        int batchSize = Integer.parseInt(ctx.batchSize().intValue().getText());
        AlgorithmSegment rateLimiter = (AlgorithmSegment) visit(ctx.rateLimiter());
        return new InputOrOutputSegment(workerThread, batchSize, rateLimiter);
    }
    
    @Override
    public ASTNode visitOutputDefinition(final OutputDefinitionContext ctx) {
        int workerThread = Integer.parseInt(ctx.workerThread().intValue().getText());
        int batchSize = Integer.parseInt(ctx.batchSize().intValue().getText());
        AlgorithmSegment rateLimiter = (AlgorithmSegment) visit(ctx.rateLimiter());
        return new InputOrOutputSegment(workerThread, batchSize, rateLimiter);
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
    public ASTNode visitDropShardingScalingRule(final DropShardingScalingRuleContext ctx) {
        return new DropShardingScalingRuleStatement(new IdentifierValue(ctx.scalingName().getText()).getValue());
    }
    
    @Override
    public ASTNode visitEnableShardingScalingRule(final EnableShardingScalingRuleContext ctx) {
        return new EnableShardingScalingRuleStatement(new IdentifierValue(ctx.scalingName().getText()).getValue());
    }
    
    @Override
    public ASTNode visitDisableShardingScalingRule(final DisableShardingScalingRuleContext ctx) {
        return new DisableShardingScalingRuleStatement(new IdentifierValue(ctx.scalingName().getText()).getValue());
    }
    
    @Override
    public ASTNode visitShowShardingScalingRules(final ShowShardingScalingRulesContext ctx) {
        return new ShowShardingScalingRulesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(ctx.algorithmName().getText(), getAlgorithmProperties(ctx));
    }
    
    private Properties getAlgorithmProperties(final AlgorithmDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx.algorithmProperties()) {
            return result;
        }
        for (ScalingStatementParser.AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
}
