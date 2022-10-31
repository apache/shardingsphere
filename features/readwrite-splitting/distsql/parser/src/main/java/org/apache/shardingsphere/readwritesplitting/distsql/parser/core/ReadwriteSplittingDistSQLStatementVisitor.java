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

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlterReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ClearReadwriteSplittingHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.CountReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.CreateReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.DisableReadDataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.DropReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.EnableReadDataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ReadwriteSplittingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.SetReadwriteSplittingHintSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ShowReadwriteSplittingHintStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ShowReadwriteSplittingReadResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ShowReadwriteSplittingRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.StaticReadwriteSplittingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CountReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ClearReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.SetReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ShowReadwriteSplittingHintStatusStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.SetReadwriteSplittingStatusStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
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
        return new DropReadwriteSplittingRuleStatement(ctx.ifExists() != null, ctx.ruleName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitEnableReadDataSource(final EnableReadDataSourceContext ctx) {
        DatabaseSegment schemaSegment = Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null;
        return new SetReadwriteSplittingStatusStatement(ctx.ENABLE().getText().toUpperCase(), getIdentifierValue(ctx.resourceName()), schemaSegment);
    }
    
    @Override
    public ASTNode visitDisableReadDataSource(final DisableReadDataSourceContext ctx) {
        DatabaseSegment schemaSegment = Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null;
        return new SetReadwriteSplittingStatusStatement(ctx.DISABLE().getText().toUpperCase(), getIdentifierValue(ctx.resourceName()), schemaSegment);
    }
    
    @Override
    public ASTNode visitShowReadwriteSplittingRules(final ShowReadwriteSplittingRulesContext ctx) {
        return new ShowReadwriteSplittingRulesStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitReadwriteSplittingRuleDefinition(final ReadwriteSplittingRuleDefinitionContext ctx) {
        Properties props = new Properties();
        String algorithmName = null;
        if (null != ctx.algorithmDefinition()) {
            algorithmName = getIdentifierValue(ctx.algorithmDefinition().algorithmName());
            props = getAlgorithmProperties(ctx.algorithmDefinition());
        }
        if (null == ctx.staticReadwriteSplittingRuleDefinition()) {
            return new ReadwriteSplittingRuleSegment(getIdentifierValue(ctx.ruleName()), getIdentifierValue(ctx.dynamicReadwriteSplittingRuleDefinition().resourceName()),
                    getIdentifierValue(ctx.dynamicReadwriteSplittingRuleDefinition().writeDataSourceQueryEnabled()), algorithmName, props);
        }
        StaticReadwriteSplittingRuleDefinitionContext staticRuleDefinitionCtx = ctx.staticReadwriteSplittingRuleDefinition();
        return new ReadwriteSplittingRuleSegment(getIdentifierValue(ctx.ruleName()),
                getIdentifierValue(staticRuleDefinitionCtx.writeResourceName()),
                staticRuleDefinitionCtx.readResourceNames().resourceName().stream().map(this::getIdentifierValue).collect(Collectors.toList()),
                algorithmName, props);
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmName()), getAlgorithmProperties(ctx));
    }
    
    @Override
    public ASTNode visitShowReadwriteSplittingReadResources(final ShowReadwriteSplittingReadResourcesContext ctx) {
        return new ShowReadwriteSplittingReadResourcesStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitSetReadwriteSplittingHintSource(final SetReadwriteSplittingHintSourceContext ctx) {
        return new SetReadwriteSplittingHintStatement(getIdentifierValue(ctx.sourceValue()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
    
    @Override
    public ASTNode visitShowReadwriteSplittingHintStatus(final ShowReadwriteSplittingHintStatusContext ctx) {
        return new ShowReadwriteSplittingHintStatusStatement();
    }
    
    @Override
    public ASTNode visitClearReadwriteSplittingHint(final ClearReadwriteSplittingHintContext ctx) {
        return new ClearReadwriteSplittingHintStatement();
    }
    
    @Override
    public ASTNode visitCountReadwriteSplittingRule(final CountReadwriteSplittingRuleContext ctx) {
        return new CountReadwriteSplittingRuleStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    private Properties getAlgorithmProperties(final AlgorithmDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx.algorithmProperties()) {
            return result;
        }
        for (AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
}
