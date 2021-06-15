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

import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.AlterEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.CreateEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.DropEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.EncryptRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.ShowEncryptRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.EncryptRuleStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.segment.EncryptRuleSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for encrypt rule.
 */
public final class EncryptRuleSQLStatementVisitor extends EncryptRuleStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
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
        return new DropEncryptRuleStatement(ctx.tableName().stream().map(TableNameContext::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowEncryptRules(final ShowEncryptRulesContext ctx) {
        return new ShowEncryptRulesStatement(null == ctx.tableRule() ? null : ctx.tableRule().tableName().getText(),
                null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitEncryptRuleDefinition(final EncryptRuleDefinitionContext ctx) {
        return new EncryptRuleSegment(ctx.tableName().getText(), ctx.columnDefinition().stream().map(each -> (EncryptColumnSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        EncryptColumnSegment result = new EncryptColumnSegment();
        result.setName(ctx.columnName().getText());
        result.setCipherColumn(ctx.cipherColumnName().getText());
        if (null != ctx.plainColumnName()) {
            result.setPlainColumn(ctx.plainColumnName().getText());
        }
        result.setEncryptor((AlgorithmSegment) visit(ctx.algorithmDefinition()));
        return result;
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
        for (AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
}
