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

package org.apache.shardingsphere.readwritesplitting.distsql.parser.core;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.AlterReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.CreateReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.DropReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.DynamicReadwriteSplittingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.FunctionDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.ReadwriteSplittingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.ShowReadwriteSplittingRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingRuleStatementParser.StaticReadwriteSplittingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for readwrite-splitting rule.
 */
public final class ReadwriteSplittingRuleSQLStatementVisitor extends ReadwriteSplittingRuleStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateReadwriteSplittingRule(final CreateReadwriteSplittingRuleContext ctx) {
        return new CreateReadwriteSplittingRuleStatement(ctx.readwriteSplittingRuleDefinition().stream().map(each -> (ReadwriteSplittingRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitReadwriteSplittingRuleDefinition(final ReadwriteSplittingRuleDefinitionContext ctx) {
        ReadwriteSplittingRuleSegment result = (ReadwriteSplittingRuleSegment) (null != ctx.dynamicReadwriteSplittingRuleDefinition()
                ? visit(ctx.dynamicReadwriteSplittingRuleDefinition()) : visit(ctx.staticReadwriteSplittingRuleDefinition()));
        Properties props = new Properties();
        if (null != ctx.functionDefinition().algorithmProperties()) {
            ctx.functionDefinition().algorithmProperties().algorithmProperty()
                    .forEach(each -> props.setProperty(each.key.getText(), each.value.getText()));
        }
        result.setName(ctx.ruleName().getText());
        result.setLoadBalancer(ctx.functionDefinition().functionName().getText());
        result.setProps(props);
        return result;
    }
    
    @Override
    public ASTNode visitStaticReadwriteSplittingRuleDefinition(final StaticReadwriteSplittingRuleDefinitionContext ctx) {
        ReadwriteSplittingRuleSegment result = new ReadwriteSplittingRuleSegment();
        result.setWriteDataSource(ctx.writeResourceName().getText());
        result.setReadDataSources(ctx.resourceName().stream().map(RuleContext::getText).collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public ASTNode visitDynamicReadwriteSplittingRuleDefinition(final DynamicReadwriteSplittingRuleDefinitionContext ctx) {
        ReadwriteSplittingRuleSegment result = new ReadwriteSplittingRuleSegment();
        result.setAutoAwareResource(ctx.IDENTIFIER().getText());
        return result;
    }
    
    @Override
    public ASTNode visitAlterReadwriteSplittingRule(final AlterReadwriteSplittingRuleContext ctx) {
        return new AlterReadwriteSplittingRuleStatement(ctx.readwriteSplittingRuleDefinition().stream().map(each -> (ReadwriteSplittingRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropReadwriteSplittingRule(final DropReadwriteSplittingRuleContext ctx) {
        return new DropReadwriteSplittingRuleStatement(ctx.IDENTIFIER().stream().map(TerminalNode::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowReadwriteSplittingRules(final ShowReadwriteSplittingRulesContext ctx) {
        return new ShowReadwriteSplittingRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitFunctionDefinition(final FunctionDefinitionContext ctx) {
        FunctionSegment result = new FunctionSegment();
        result.setAlgorithmName(ctx.functionName().getText());
        Properties algorithmProps = new Properties();
        if (null != ctx.algorithmProperties()) {
            for (AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
                algorithmProps.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
            }
        }
        result.setAlgorithmProps(algorithmProps);
        return result;
    }
}
