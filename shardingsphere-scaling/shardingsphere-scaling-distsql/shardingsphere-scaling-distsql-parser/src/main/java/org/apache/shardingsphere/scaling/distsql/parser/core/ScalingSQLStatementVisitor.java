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
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.CheckScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.CheckoutScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.DropScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ResetScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingCheckAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingListContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.ShowScalingStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StartScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StopScalingContext;
import org.apache.shardingsphere.distsql.parser.autogen.ScalingStatementParser.StopScalingSourceWritingContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.scaling.distsql.statement.CheckScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.CheckoutScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.DropScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ResetScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingCheckAlgorithmsStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingListStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingStatusStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StartScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingSourceWritingStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
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
        return new ShowScalingStatusStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitStartScaling(final StartScalingContext ctx) {
        return new StartScalingStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitStopScaling(final StopScalingContext ctx) {
        return new StopScalingStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitDropScaling(final DropScalingContext ctx) {
        return new DropScalingStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitResetScaling(final ResetScalingContext ctx) {
        return new ResetScalingStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitCheckScaling(final CheckScalingContext ctx) {
        AlgorithmSegment typeStrategy = null;
        if (null != ctx.algorithmDefinition()) {
            typeStrategy = (AlgorithmSegment) visit(ctx.algorithmDefinition());
        }
        return new CheckScalingStatement(Long.parseLong(ctx.jobId().getText()), typeStrategy);
    }
    
    @Override
    public ASTNode visitShowScalingCheckAlgorithms(final ShowScalingCheckAlgorithmsContext ctx) {
        return new ShowScalingCheckAlgorithmsStatement();
    }
    
    @Override
    public ASTNode visitStopScalingSourceWriting(final StopScalingSourceWritingContext ctx) {
        return new StopScalingSourceWritingStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitCheckoutScaling(final CheckoutScalingContext ctx) {
        return new CheckoutScalingStatement(Long.parseLong(ctx.jobId().getText()));
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
}
