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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.AlterDefaultShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.AlterShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.CountShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.CreateDefaultShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.CreateShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.DropDefaultShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.DropShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.DropShadowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShadowRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShowDefaultShadowAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShowShadowAlgorithmPluginsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShowShadowAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShowShadowRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShadowDistSQLStatementParser.ShowShadowTableRulesContext;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.CountRuleStatement;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.AlterDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.statement.DropDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.statement.ShowDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowTableRulesStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for shadow DistSQL.
 */
public final class ShadowDistSQLStatementVisitor extends ShadowDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCreateShadowRule(final CreateShadowRuleContext ctx) {
        List<ShadowRuleSegment> shadowRuleSegments = ctx.shadowRuleDefinition().stream().map(this::visit).map(ShadowRuleSegment.class::cast).collect(Collectors.toList());
        return new CreateShadowRuleStatement(null != ctx.ifNotExists(), autoCreateAlgorithmName(shadowRuleSegments));
    }
    
    @Override
    public ASTNode visitCreateDefaultShadowAlgorithm(final CreateDefaultShadowAlgorithmContext ctx) {
        return new CreateDefaultShadowAlgorithmStatement(null != ctx.ifNotExists(), (ShadowAlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitAlterDefaultShadowAlgorithm(final AlterDefaultShadowAlgorithmContext ctx) {
        return new AlterDefaultShadowAlgorithmStatement((ShadowAlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitShadowRuleDefinition(final ShadowRuleDefinitionContext ctx) {
        Map<String, Collection<ShadowAlgorithmSegment>> shadowAlgorithms = ctx.shadowTableRule().stream()
                .collect(Collectors.toMap(each -> getIdentifierValue(each.tableName()), each -> visitShadowAlgorithms(each.algorithmDefinition())));
        return new ShadowRuleSegment(getIdentifierValue(ctx.ruleName()), getIdentifierValue(ctx.source()), getIdentifierValue(ctx.shadow()), shadowAlgorithms);
    }
    
    private Collection<ShadowAlgorithmSegment> visitShadowAlgorithms(final List<AlgorithmDefinitionContext> contexts) {
        return contexts.stream().map(this::visit).map(ShadowAlgorithmSegment.class::cast).collect(Collectors.toList());
    }
    
    @Override
    public ASTNode visitShowShadowAlgorithms(final ShowShadowAlgorithmsContext ctx) {
        return new ShowShadowAlgorithmsStatement(null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())));
    }
    
    @Override
    public ASTNode visitShowDefaultShadowAlgorithm(final ShowDefaultShadowAlgorithmContext ctx) {
        return new ShowDefaultShadowAlgorithmStatement(
                null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        AlgorithmSegment segment = new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), getProperties(ctx.propertiesDefinition()));
        return new ShadowAlgorithmSegment(null, segment);
    }
    
    private Properties getProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx || null == ctx.properties()) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(QuoteCharacter.unwrapAndTrimText(each.key.getText()), QuoteCharacter.unwrapAndTrimText(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterShadowRule(final AlterShadowRuleContext ctx) {
        List<ShadowRuleSegment> shadowRuleSegments = ctx.shadowRuleDefinition().stream().map(this::visit).map(ShadowRuleSegment.class::cast).collect(Collectors.toList());
        return new AlterShadowRuleStatement(autoCreateAlgorithmName(shadowRuleSegments));
    }
    
    @Override
    public ASTNode visitDropShadowRule(final DropShadowRuleContext ctx) {
        return new DropShadowRuleStatement(null != ctx.ifExists(), ctx.ruleName().stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropShadowAlgorithm(final DropShadowAlgorithmContext ctx) {
        return new DropShadowAlgorithmStatement(null != ctx.ifExists(), null == ctx.algorithmName()
                ? Collections.emptyList()
                : ctx.algorithmName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropDefaultShadowAlgorithm(final DropDefaultShadowAlgorithmContext ctx) {
        return new DropDefaultShadowAlgorithmStatement(null != ctx.ifExists());
    }
    
    @Override
    public ASTNode visitShowShadowRules(final ShowShadowRulesContext ctx) {
        String ruleName = null == ctx.shadowRule() ? null : getIdentifierValue(ctx.shadowRule().ruleName());
        FromDatabaseSegment fromDatabase = null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName()));
        return new ShowShadowRulesStatement(ruleName, null == ctx.databaseName() ? null : fromDatabase);
    }
    
    @Override
    public ASTNode visitShowShadowTableRules(final ShowShadowTableRulesContext ctx) {
        FromDatabaseSegment fromDatabase = null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName()));
        String tableName = null == ctx.tableRule() ? null : getIdentifierValue(ctx.tableRule().tableName());
        return new ShowShadowTableRulesStatement(fromDatabase, tableName);
    }
    
    private String getIdentifierValue(final ParserRuleContext ctx) {
        return null == ctx || ctx.isEmpty() ? null : new IdentifierValue(ctx.getText()).getValue();
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitCountShadowRule(final CountShadowRuleContext ctx) {
        return new CountRuleStatement(null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())), "SHADOW");
    }
    
    private Collection<ShadowRuleSegment> autoCreateAlgorithmName(final Collection<ShadowRuleSegment> shadowRuleSegments) {
        Collection<ShadowRuleSegment> result = new LinkedList<>();
        shadowRuleSegments.forEach(each -> buildShadowRuleSegment(result, each));
        return result;
    }
    
    private void buildShadowRuleSegment(final Collection<ShadowRuleSegment> collection, final ShadowRuleSegment shadowRuleSegment) {
        Map<String, Collection<ShadowAlgorithmSegment>> shadowTableRules = new LinkedHashMap<>(shadowRuleSegment.getShadowTableRules().size(), 1F);
        for (Entry<String, Collection<ShadowAlgorithmSegment>> entry : shadowRuleSegment.getShadowTableRules().entrySet()) {
            int index = 0;
            Collection<ShadowAlgorithmSegment> shadowAlgorithmSegments = new LinkedList<>();
            for (ShadowAlgorithmSegment each : entry.getValue()) {
                shadowAlgorithmSegments.add(buildShadowAlgorithmSegment(shadowRuleSegment.getRuleName(), entry.getKey(), index++, each));
            }
            shadowTableRules.put(entry.getKey(), shadowAlgorithmSegments);
        }
        collection.add(new ShadowRuleSegment(shadowRuleSegment.getRuleName(), shadowRuleSegment.getSource(), shadowRuleSegment.getShadow(), shadowTableRules));
    }
    
    private ShadowAlgorithmSegment buildShadowAlgorithmSegment(final String ruleName, final String tableName, final int index, final ShadowAlgorithmSegment shadowAlgorithmSegment) {
        String algorithmName = buildAlgorithmName(ruleName, tableName, shadowAlgorithmSegment.getAlgorithmSegment().getName(), index);
        AlgorithmSegment algorithmSegment = new AlgorithmSegment(shadowAlgorithmSegment.getAlgorithmSegment().getName(), shadowAlgorithmSegment.getAlgorithmSegment().getProps());
        return new ShadowAlgorithmSegment(algorithmName, algorithmSegment);
    }
    
    private String buildAlgorithmName(final String ruleName, final String tableName, final String algorithmType, final int index) {
        return String.format("%s_%s_%s_%d", ruleName, tableName, algorithmType, index).toLowerCase();
    }
    
    @Override
    public ASTNode visitShowShadowAlgorithmPlugins(final ShowShadowAlgorithmPluginsContext ctx) {
        return new ShowPluginsStatement("SHADOW_ALGORITHM");
    }
}
