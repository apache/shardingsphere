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

package org.apache.shardingsphere.shadow.distsql.parser.core;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.AlgorithmPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.AlterShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.AlterShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.CountShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.CreateDefaultShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.CreateShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.CreateShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.DropDefaultShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.DropShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.DropShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShadowAlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShadowRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShadowTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShowShadowAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShowShadowRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShowShadowTableRulesContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CountShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowTableRulesStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for shadow dist SQL.
 */
public final class ShadowDistSQLStatementVisitor extends ShadowDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateShadowRule(final CreateShadowRuleContext ctx) {
        List<ShadowRuleSegment> shadowRuleSegments = ctx.shadowRuleDefinition().stream().map(this::visit).map(each -> (ShadowRuleSegment) each).collect(Collectors.toList());
        return new CreateShadowRuleStatement(shadowRuleSegments);
    }
    
    @Override
    public ASTNode visitCreateDefaultShadowAlgorithm(final CreateDefaultShadowAlgorithmContext ctx) {
        return new CreateDefaultShadowAlgorithmStatement(getIdentifierValue(ctx.algorithmName()));
    }
    
    @Override
    public ASTNode visitShadowRuleDefinition(final ShadowRuleDefinitionContext ctx) {
        Map<String, Collection<ShadowAlgorithmSegment>> shadowAlgorithms = ctx.shadowTableRule().stream()
                .collect(Collectors.toMap(each -> getIdentifierValue(each.tableName()), each -> visitShadowAlgorithms(each.shadowAlgorithmDefinition())));
        return new ShadowRuleSegment(getIdentifierValue(ctx.ruleName()), getIdentifierValue(ctx.source()), getIdentifierValue(ctx.shadow()), shadowAlgorithms);
    }
    
    @Override
    public ASTNode visitShowShadowAlgorithms(final ShowShadowAlgorithmsContext ctx) {
        return new ShowShadowAlgorithmsStatement(null != ctx.databaseName() ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitShadowAlgorithmDefinition(final ShadowAlgorithmDefinitionContext ctx) {
        AlgorithmSegment segment = new AlgorithmSegment(getIdentifierValue(ctx.shadowAlgorithmType()), getAlgorithmProperties(ctx.algorithmProperties()));
        String algorithmName = null != ctx.algorithmName() ? getIdentifierValue(ctx.algorithmName())
                : createAlgorithmName(getIdentifierValue(((ShadowRuleDefinitionContext) ctx.getParent().getParent()).ruleName()),
                        getIdentifierValue(((ShadowTableRuleContext) ctx.getParent()).tableName()), segment);
        return new ShadowAlgorithmSegment(algorithmName, segment);
    }
    
    private Properties getAlgorithmProperties(final AlgorithmPropertiesContext ctx) {
        Properties result = new Properties();
        ctx.algorithmProperty().forEach(each -> result.put(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText())));
        return result;
    }
    
    @Override
    public ASTNode visitAlterShadowRule(final AlterShadowRuleContext ctx) {
        List<ShadowRuleSegment> shadowRuleSegments = ctx.shadowRuleDefinition().stream().map(this::visit).map(each -> (ShadowRuleSegment) each).collect(Collectors.toList());
        return new AlterShadowRuleStatement(shadowRuleSegments);
    }
    
    @Override
    public ASTNode visitCreateShadowAlgorithm(final CreateShadowAlgorithmContext ctx) {
        return new CreateShadowAlgorithmStatement(visitShadowAlgorithms(ctx.shadowAlgorithmDefinition()));
    }
    
    @Override
    public ASTNode visitDropShadowRule(final DropShadowRuleContext ctx) {
        return new DropShadowRuleStatement(null != ctx.ifExists(), ctx.ruleName().stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterShadowAlgorithm(final AlterShadowAlgorithmContext ctx) {
        return new AlterShadowAlgorithmStatement(visitShadowAlgorithms(ctx.shadowAlgorithmDefinition()));
    }
    
    @Override
    public ASTNode visitDropShadowAlgorithm(final DropShadowAlgorithmContext ctx) {
        return new DropShadowAlgorithmStatement(null != ctx.ifExists(), null == ctx.algorithmName() ? Collections.emptyList()
                : ctx.algorithmName().stream().map(this::getIdentifierValue).collect(Collectors.toSet()));
    }
    
    @Override
    public ASTNode visitDropDefaultShadowAlgorithm(final DropDefaultShadowAlgorithmContext ctx) {
        return new DropDefaultShadowAlgorithmStatement(null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitShowShadowRules(final ShowShadowRulesContext ctx) {
        String ruleName = null == ctx.shadowRule() ? null : getIdentifierValue(ctx.shadowRule().ruleName());
        DatabaseSegment databaseSegment = null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName());
        return new ShowShadowRulesStatement(ruleName, null == ctx.databaseName() ? null : databaseSegment);
    }
    
    @Override
    public ASTNode visitShowShadowTableRules(final ShowShadowTableRulesContext ctx) {
        return new ShowShadowTableRulesStatement(null != ctx.databaseName() ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    private String getIdentifierValue(final ParserRuleContext ctx) {
        if (null == ctx || ctx.isEmpty()) {
            return null;
        }
        return new IdentifierValue(ctx.getText()).getValue();
    }
    
    private Collection<ShadowAlgorithmSegment> visitShadowAlgorithms(final List<ShadowAlgorithmDefinitionContext> ctxs) {
        return ctxs.stream().map(this::visit).map(each -> (ShadowAlgorithmSegment) each).collect(Collectors.toList());
    }
    
    private String createAlgorithmName(final String ruleName, final String tableName, final AlgorithmSegment algorithmSegment) {
        return String.format("%s_%s_%s", ruleName, tableName, algorithmSegment.getName()).toLowerCase();
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitCountShadowRule(final CountShadowRuleContext ctx) {
        return new CountShadowRuleStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
}
