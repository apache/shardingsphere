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

package org.apache.shardingsphere.mask.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.AlterMaskRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.CountMaskRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.CreateMaskRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.DropMaskRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.MaskRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.MaskDistSQLStatementParser.ShowMaskRulesContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.mask.distsql.parser.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.parser.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.parser.statement.AlterMaskRuleStatement;
import org.apache.shardingsphere.mask.distsql.parser.statement.CountMaskRuleStatement;
import org.apache.shardingsphere.mask.distsql.parser.statement.CreateMaskRuleStatement;
import org.apache.shardingsphere.mask.distsql.parser.statement.DropMaskRuleStatement;
import org.apache.shardingsphere.mask.distsql.parser.statement.ShowMaskRulesStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for mask DistSQL.
 */
public final class MaskDistSQLStatementVisitor extends MaskDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCreateMaskRule(final CreateMaskRuleContext ctx) {
        return new CreateMaskRuleStatement(null != ctx.ifNotExists(), ctx.maskRuleDefinition().stream().map(each -> (MaskRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterMaskRule(final AlterMaskRuleContext ctx) {
        return new AlterMaskRuleStatement(ctx.maskRuleDefinition().stream().map(each -> (MaskRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropMaskRule(final DropMaskRuleContext ctx) {
        return new DropMaskRuleStatement(null != ctx.ifExists(), ctx.ruleName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowMaskRules(final ShowMaskRulesContext ctx) {
        return new ShowMaskRulesStatement(null == ctx.RULE() ? null : getIdentifierValue(ctx.ruleName()),
                null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitCountMaskRule(final CountMaskRuleContext ctx) {
        return new CountMaskRuleStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitMaskRuleDefinition(final MaskRuleDefinitionContext ctx) {
        return new MaskRuleSegment(getIdentifierValue(ctx.ruleName()), ctx.columnDefinition().stream().map(each -> (MaskColumnSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        return new MaskColumnSegment(getIdentifierValue(ctx.columnName()), (AlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), getProperties(ctx.propertiesDefinition()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
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
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
}
