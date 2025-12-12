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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.AlterEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.CountEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.CreateEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.DropEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.EncryptColumnDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.EncryptRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.ShowEncryptAlgorithmPluginsContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.ShowEncryptRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptDistSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.CountRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnItemSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.util.IdentifierValueUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for encrypt DistSQL.
 */
public final class EncryptDistSQLStatementVisitor extends EncryptDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCreateEncryptRule(final CreateEncryptRuleContext ctx) {
        return new CreateEncryptRuleStatement(null != ctx.ifNotExists(), ctx.encryptRuleDefinition().stream().map(each -> (EncryptRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterEncryptRule(final AlterEncryptRuleContext ctx) {
        return new AlterEncryptRuleStatement(ctx.encryptRuleDefinition().stream().map(each -> (EncryptRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropEncryptRule(final DropEncryptRuleContext ctx) {
        return new DropEncryptRuleStatement(null != ctx.ifExists(), ctx.tableName().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowEncryptRules(final ShowEncryptRulesContext ctx) {
        return new ShowEncryptRulesStatement(null == ctx.tableRule()
                ? null
                : IdentifierValueUtils.getValue(ctx.tableRule().tableName()),
                null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())));
    }
    
    @Override
    public ASTNode visitEncryptRuleDefinition(final EncryptRuleDefinitionContext ctx) {
        return new EncryptRuleSegment(IdentifierValueUtils.getValue(ctx.tableName()),
                ctx.encryptTableRuleDefinition().encryptColumnDefinition().stream().map(each -> (EncryptColumnSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitEncryptColumnDefinition(final EncryptColumnDefinitionContext ctx) {
        EncryptColumnItemSegment cipher = new EncryptColumnItemSegment(
                IdentifierValueUtils.getValue(ctx.cipherColumnDefinition().cipherColumnName()), (AlgorithmSegment) visit(ctx.encryptAlgorithm().algorithmDefinition()));
        EncryptColumnItemSegment assistedQuery = null == ctx.assistedQueryColumnDefinition()
                ? null
                : new EncryptColumnItemSegment(IdentifierValueUtils.getValue(ctx.assistedQueryColumnDefinition().assistedQueryColumnName()), getAssistedEncryptor(ctx));
        EncryptColumnItemSegment likeQuery = null == ctx.likeQueryColumnDefinition()
                ? null
                : new EncryptColumnItemSegment(IdentifierValueUtils.getValue(ctx.likeQueryColumnDefinition().likeQueryColumnName()), getLikeEncryptor(ctx));
        return new EncryptColumnSegment(IdentifierValueUtils.getValue(ctx.columnDefinition().columnName()), cipher, assistedQuery, likeQuery);
    }
    
    private AlgorithmSegment getAssistedEncryptor(final EncryptColumnDefinitionContext ctx) {
        return null == ctx.assistedQueryAlgorithm() ? null : (AlgorithmSegment) visit(ctx.assistedQueryAlgorithm().algorithmDefinition());
    }
    
    private AlgorithmSegment getLikeEncryptor(final EncryptColumnDefinitionContext ctx) {
        return null == ctx.likeQueryAlgorithm() ? null : (AlgorithmSegment) visit(ctx.likeQueryAlgorithm().algorithmDefinition());
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(IdentifierValueUtils.getValue(ctx.algorithmTypeName()), getProperties(ctx.propertiesDefinition()));
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
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitCountEncryptRule(final CountEncryptRuleContext ctx) {
        return new CountRuleStatement(null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())), "ENCRYPT");
    }
    
    @Override
    public ASTNode visitShowEncryptAlgorithmPlugins(final ShowEncryptAlgorithmPluginsContext ctx) {
        return new ShowPluginsStatement("ENCRYPT_ALGORITHM");
    }
}
