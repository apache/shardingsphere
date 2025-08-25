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

package org.apache.shardingsphere.data.pipeline.cdc.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingListStatement;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingRuleStatement;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingStatusStatement;
import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.updatable.DropStreamingStatement;
import org.apache.shardingsphere.data.pipeline.distsql.statement.updatable.AlterTransmissionRuleStatement;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.AlterStreamingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.BatchSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.DropStreamingContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.RateLimiterContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.ReadDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.ShardingSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.ShowStreamingListContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.ShowStreamingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.ShowStreamingStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.StreamChannelContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.TransmissionRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.WorkerThreadContext;
import org.apache.shardingsphere.distsql.parser.autogen.CDCDistSQLStatementParser.WriteDefinitionContext;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.distsql.segment.TransmissionRuleSegment;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Properties;

/**
 * SQL statement visitor for CDC DistSQL.
 */
public final class CDCDistSQLStatementVisitor extends CDCDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitShowStreamingList(final ShowStreamingListContext ctx) {
        return new ShowStreamingListStatement();
    }
    
    @Override
    public ASTNode visitShowStreamingStatus(final ShowStreamingStatusContext ctx) {
        return new ShowStreamingStatusStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitDropStreaming(final DropStreamingContext ctx) {
        return new DropStreamingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    private String getIdentifierValue(final ParseTree ctx) {
        return null == ctx ? null : new IdentifierValue(ctx.getText()).getValue();
    }
    
    @Override
    public ASTNode visitShowStreamingRule(final ShowStreamingRuleContext ctx) {
        return new ShowStreamingRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterStreamingRule(final AlterStreamingRuleContext ctx) {
        return new AlterTransmissionRuleStatement("STREAMING", (TransmissionRuleSegment) visit(ctx.transmissionRule()));
    }
    
    @Override
    public ASTNode visitTransmissionRule(final TransmissionRuleContext ctx) {
        TransmissionRuleSegment result = new TransmissionRuleSegment();
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
    
    private Integer getWorkerThread(final WorkerThreadContext ctx) {
        return null == ctx ? null : Integer.parseInt(ctx.intValue().getText());
    }
    
    private Integer getBatchSize(final BatchSizeContext ctx) {
        return null == ctx ? null : Integer.parseInt(ctx.intValue().getText());
    }
    
    private Integer getShardingSize(final ShardingSizeContext ctx) {
        return null == ctx ? null : Integer.parseInt(ctx.intValue().getText());
    }
    
    private AlgorithmSegment getAlgorithmSegment(final RateLimiterContext ctx) {
        return null == ctx ? null : (AlgorithmSegment) visit(ctx);
    }
    
    @Override
    public ASTNode visitWriteDefinition(final WriteDefinitionContext ctx) {
        return new ReadOrWriteSegment(getWorkerThread(ctx.workerThread()), getBatchSize(ctx.batchSize()), getAlgorithmSegment(ctx.rateLimiter()));
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
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), buildProperties(ctx.propertiesDefinition()));
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
}
