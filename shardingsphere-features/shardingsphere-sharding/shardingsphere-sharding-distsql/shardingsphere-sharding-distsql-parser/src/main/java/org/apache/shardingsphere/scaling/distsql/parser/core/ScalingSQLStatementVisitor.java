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

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ApplyScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.BatchSizeContext;
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
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.RestoreScalingSourceWritingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ScalingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShardingSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingCheckAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingListContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowShardingScalingRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StartScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StopScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StopScalingSourceWritingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StreamChannelContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.WorkerThreadContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.scaling.distsql.statement.ApplyScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.CheckScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DisableShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DropScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DropShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.EnableShardingScalingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ResetScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.RestoreScalingSourceWritingStatement;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
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
        return new ShowScalingStatusStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStartScaling(final StartScalingContext ctx) {
        return new StartScalingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStopScaling(final StopScalingContext ctx) {
        return new StopScalingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitDropScaling(final DropScalingContext ctx) {
        return new DropScalingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitResetScaling(final ResetScalingContext ctx) {
        return new ResetScalingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitCheckScaling(final CheckScalingContext ctx) {
        AlgorithmSegment typeStrategy = null;
        if (null != ctx.algorithmDefinition()) {
            typeStrategy = (AlgorithmSegment) visit(ctx.algorithmDefinition());
        }
        return new CheckScalingStatement(getIdentifierValue(ctx.jobId()), typeStrategy);
    }
    
    @Override
    public ASTNode visitShowScalingCheckAlgorithms(final ShowScalingCheckAlgorithmsContext ctx) {
        return new ShowScalingCheckAlgorithmsStatement();
    }
    
    @Override
    public ASTNode visitStopScalingSourceWriting(final StopScalingSourceWritingContext ctx) {
        return new StopScalingSourceWritingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitRestoreScalingSourceWriting(final RestoreScalingSourceWritingContext ctx) {
        return new RestoreScalingSourceWritingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitApplyScaling(final ApplyScalingContext ctx) {
        return new ApplyScalingStatement(getIdentifierValue(ctx.jobId()));
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
        Integer workerThread = getWorkerThread(ctx.workerThread());
        Integer batchSize = getBatchSize(ctx.batchSize());
        Integer shardingSize = getShardingSize(ctx.shardingSize());
        AlgorithmSegment rateLimiter = null;
        if (null != ctx.rateLimiter()) {
            rateLimiter = (AlgorithmSegment) visit(ctx.rateLimiter());
        }
        return new InputOrOutputSegment(workerThread, batchSize, shardingSize, rateLimiter);
    }
    
    @Override
    public ASTNode visitOutputDefinition(final OutputDefinitionContext ctx) {
        Integer workerThread = getWorkerThread(ctx.workerThread());
        Integer batchSize = getBatchSize(ctx.batchSize());
        AlgorithmSegment rateLimiter = null;
        if (null != ctx.rateLimiter()) {
            rateLimiter = (AlgorithmSegment) visit(ctx.rateLimiter());
        }
        return new InputOrOutputSegment(workerThread, batchSize, rateLimiter);
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
    public ASTNode visitDropShardingScalingRule(final DropShardingScalingRuleContext ctx) {
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
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmName()), getAlgorithmProperties(ctx));
    }
    
    private Properties getAlgorithmProperties(final AlgorithmDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx.algorithmProperties()) {
            return result;
        }
        for (ScalingStatementParser.AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
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
}
