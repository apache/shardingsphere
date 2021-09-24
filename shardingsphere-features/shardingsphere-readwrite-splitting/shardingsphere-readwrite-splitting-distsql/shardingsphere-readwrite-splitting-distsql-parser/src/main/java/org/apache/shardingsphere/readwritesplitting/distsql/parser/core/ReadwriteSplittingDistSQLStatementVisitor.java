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
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlterReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ClearReadwriteSplittingHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.CreateReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.DisableReadDataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.DropReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.EnableReadDataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ReadwriteSplittingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.RuleNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.SetReadwriteSplittingHintSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ShowReadwriteSplittingHintStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ShowReadwriteSplittingRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.StaticReadwriteSplittingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.status.SetStatusStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ClearReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.SetReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ShowReadwriteSplittingHintStatusStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for readwrite-splitting dist SQL.
 */
public final class ReadwriteSplittingDistSQLStatementVisitor extends ReadwriteSplittingDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateReadwriteSplittingRule(final CreateReadwriteSplittingRuleContext ctx) {
        return new CreateReadwriteSplittingRuleStatement(ctx.readwriteSplittingRuleDefinition().stream().map(each -> (ReadwriteSplittingRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterReadwriteSplittingRule(final AlterReadwriteSplittingRuleContext ctx) {
        return new AlterReadwriteSplittingRuleStatement(ctx.readwriteSplittingRuleDefinition().stream().map(each -> (ReadwriteSplittingRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropReadwriteSplittingRule(final DropReadwriteSplittingRuleContext ctx) {
        return new DropReadwriteSplittingRuleStatement(ctx.ruleName().stream().map(RuleNameContext::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitEnableReadDataSource(final EnableReadDataSourceContext ctx) {
        SchemaSegment schemaSegment = Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null;
        return new SetStatusStatement(ctx.READWRITE_SPLITTING().getText(), ctx.ENABLE().getText(), new IdentifierValue(ctx.resourceName().getText()).getValue(), schemaSegment);
    }
    
    @Override
    public ASTNode visitDisableReadDataSource(final DisableReadDataSourceContext ctx) {
        SchemaSegment schemaSegment = Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null;
        return new SetStatusStatement(ctx.READWRITE_SPLITTING().getText(), ctx.DISABLE().getText(), new IdentifierValue(ctx.resourceName().getText()).getValue(), schemaSegment);
    }
    
    @Override
    public ASTNode visitShowReadwriteSplittingRules(final ShowReadwriteSplittingRulesContext ctx) {
        return new ShowReadwriteSplittingRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitReadwriteSplittingRuleDefinition(final ReadwriteSplittingRuleDefinitionContext ctx) {
        Properties props = new Properties();
        if (null != ctx.algorithmDefinition().algorithmProperties()) {
            ctx.algorithmDefinition().algorithmProperties().algorithmProperty().forEach(each -> props.setProperty(each.key.getText(), each.value.getText()));
        }
        if (null == ctx.staticReadwriteSplittingRuleDefinition()) {
            return new ReadwriteSplittingRuleSegment(
                    ctx.ruleName().getText(), ctx.dynamicReadwriteSplittingRuleDefinition().resourceName().getText(), ctx.algorithmDefinition().algorithmName().getText(), props);
        }
        StaticReadwriteSplittingRuleDefinitionContext staticRuleDefinitionCtx = ctx.staticReadwriteSplittingRuleDefinition();
        return new ReadwriteSplittingRuleSegment(ctx.ruleName().getText(), 
                staticRuleDefinitionCtx.writeResourceName().getText(), staticRuleDefinitionCtx.readResourceNames().resourceName().stream().map(RuleContext::getText).collect(Collectors.toList()),
                ctx.algorithmDefinition().algorithmName().getText(), props);
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(ctx.algorithmName().getText(), getAlgorithmProperties(ctx));
    }
    
    @Override
    public ASTNode visitSetReadwriteSplittingHintSource(final SetReadwriteSplittingHintSourceContext ctx) {
        return new SetReadwriteSplittingHintStatement(ctx.sourceValue().getText());
    }
    
    @Override
    public ASTNode visitShowReadwriteSplittingHintStatus(final ShowReadwriteSplittingHintStatusContext ctx) {
        return new ShowReadwriteSplittingHintStatusStatement();
    }
    
    @Override
    public ASTNode visitClearReadwriteSplittingHint(final ClearReadwriteSplittingHintContext ctx) {
        return new ClearReadwriteSplittingHintStatement();
    }
    
    private Properties getAlgorithmProperties(final AlgorithmDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx.algorithmProperties()) {
            return result;
        }
        for (AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
        return result;
    }
}
