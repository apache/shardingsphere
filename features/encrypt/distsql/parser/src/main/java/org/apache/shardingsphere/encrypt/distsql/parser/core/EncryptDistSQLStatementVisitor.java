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

package org.apache.shardingsphere.encrypt.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.AlterEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.CountEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.CreateEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.DropEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.EncryptColumnDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.EncryptRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.ShowEncryptRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CountEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for encrypt dist SQL.
 */
public final class EncryptDistSQLStatementVisitor extends EncryptDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateEncryptRule(final CreateEncryptRuleContext ctx) {
        return new CreateEncryptRuleStatement(ctx.encryptRuleDefinition().stream().map(each -> (EncryptRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterEncryptRule(final AlterEncryptRuleContext ctx) {
        return new AlterEncryptRuleStatement(ctx.encryptRuleDefinition().stream().map(each -> (EncryptRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropEncryptRule(final DropEncryptRuleContext ctx) {
        return new DropEncryptRuleStatement(null != ctx.ifExists(), ctx.tableName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowEncryptRules(final ShowEncryptRulesContext ctx) {
        return new ShowEncryptRulesStatement(null == ctx.tableRule()
                ? null
                : getIdentifierValue(ctx.tableRule().tableName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitEncryptRuleDefinition(final EncryptRuleDefinitionContext ctx) {
        return new EncryptRuleSegment(getIdentifierValue(ctx.tableName()),
                ctx.encryptColumnDefinition().stream().map(each -> (EncryptColumnSegment) visit(each)).collect(Collectors.toList()),
                null == ctx.queryWithCipherColumn() ? null : Boolean.parseBoolean(getIdentifierValue(ctx.queryWithCipherColumn())));
    }
    
    @Override
    public ASTNode visitEncryptColumnDefinition(final EncryptColumnDefinitionContext ctx) {
        List<AlgorithmSegment> algorithmSegments = new ArrayList<>();
        for (AlgorithmDefinitionContext each : ctx.algorithmDefinition()) {
            algorithmSegments.add((AlgorithmSegment) visit(each));
        }
        return new EncryptColumnSegment(getIdentifierValue(ctx.columnDefinition().columnName()),
                getIdentifierValue(ctx.cipherColumnDefinition().cipherColumnName()),
                null == ctx.plainColumnDefinition() ? null : getIdentifierValue(ctx.plainColumnDefinition().plainColumnName()),
                null == ctx.assistedQueryColumnDefinition() ? null : getIdentifierValue(ctx.assistedQueryColumnDefinition().assistedQueryColumnName()),
                null == ctx.fuzzyQueryColumnDefinition() ? null : getIdentifierValue(ctx.fuzzyQueryColumnDefinition().fuzzyQueryColumnName()),
                getIdentifierValue(ctx.columnDefinition().dataType()),
                getIdentifierValue(ctx.cipherColumnDefinition().dataType()),
                null == ctx.plainColumnDefinition() ? null : getIdentifierValue(ctx.plainColumnDefinition().dataType()),
                null == ctx.assistedQueryColumnDefinition() ? null : getIdentifierValue(ctx.assistedQueryColumnDefinition().dataType()),
                null == ctx.fuzzyQueryColumnDefinition() ? null : getIdentifierValue(ctx.fuzzyQueryColumnDefinition().dataType()),
                algorithmSegments.get(0),
                null == ctx.assistedQueryColumnDefinition() || 1 == algorithmSegments.size() ? null : algorithmSegments.get(1),
                null == ctx.fuzzyQueryColumnDefinition() ? null
                        : Optional.ofNullable(algorithmSegments).filter(algorithm -> algorithm.size() > 2).map(algorithm -> algorithm.get(2)).orElse(null));
        
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmName()), getAlgorithmProperties(ctx));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
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
    
    @Override
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitCountEncryptRule(final CountEncryptRuleContext ctx) {
        return new CountEncryptRuleStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
}
