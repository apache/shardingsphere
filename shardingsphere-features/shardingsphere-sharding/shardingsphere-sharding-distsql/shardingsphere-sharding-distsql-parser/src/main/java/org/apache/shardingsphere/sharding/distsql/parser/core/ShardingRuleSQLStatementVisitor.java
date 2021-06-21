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

package org.apache.shardingsphere.sharding.distsql.parser.core;

import com.google.common.base.Joiner;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.AlterShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.AlterShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.AlterShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.BindTableRulesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.CreateShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.CreateShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.CreateShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.DropShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.DropShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.DropShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.ShardingTableRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.ShowShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.ShowShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.ShowShardingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for sharding rule.
 */
public final class ShardingRuleSQLStatementVisitor extends ShardingRuleStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateShardingTableRule(final CreateShardingTableRuleContext ctx) {
        return new CreateShardingTableRuleStatement(ctx.shardingTableRuleDefinition().stream().map(each -> (TableRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitCreateShardingBindingTableRules(final CreateShardingBindingTableRulesContext ctx) {
        Collection<BindingTableRuleSegment> rules = new LinkedList<>();
        for (BindTableRulesDefinitionContext each : ctx.bindTableRulesDefinition()) {
            rules.add(new BindingTableRuleSegment(Joiner.on(",").join(each.tableName().stream().map(t -> new IdentifierValue(t.getText()).getValue()).collect(Collectors.toList()))));
        }
        return new CreateShardingBindingTableRulesStatement(rules);
    }
    
    @Override
    public ASTNode visitCreateShardingBroadcastTableRules(final CreateShardingBroadcastTableRulesContext ctx) {
        return new CreateShardingBroadcastTableRulesStatement(ctx.tableName().stream().map(ParseTree::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterShardingTableRule(final AlterShardingTableRuleContext ctx) {
        return new AlterShardingTableRuleStatement(ctx.shardingTableRuleDefinition().stream().map(each -> (TableRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowShardingBroadcastTableRules(final ShowShardingBroadcastTableRulesContext ctx) {
        return new ShowShardingBroadcastTableRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitAlterShardingBindingTableRules(final AlterShardingBindingTableRulesContext ctx) {
        Collection<BindingTableRuleSegment> rules = new LinkedList<>();
        for (BindTableRulesDefinitionContext each : ctx.bindTableRulesDefinition()) {
            rules.add(new BindingTableRuleSegment(Joiner.on(",").join(each.tableName().stream().map(t -> new IdentifierValue(t.getText()).getValue()).collect(Collectors.toList()))));
        }
        return new AlterShardingBindingTableRulesStatement(rules);
    }
    
    @Override
    public ASTNode visitAlterShardingBroadcastTableRules(final AlterShardingBroadcastTableRulesContext ctx) {
        return new AlterShardingBroadcastTableRulesStatement(ctx.tableName().stream().map(ParseTree::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropShardingTableRule(final DropShardingTableRuleContext ctx) {
        return new DropShardingTableRuleStatement(ctx.tableName().stream().map(each -> (TableNameSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropShardingBindingTableRules(final DropShardingBindingTableRulesContext ctx) {
        return new DropShardingBindingTableRulesStatement();
    }
    
    @Override
    public ASTNode visitDropShardingBroadcastTableRules(final DropShardingBroadcastTableRulesContext ctx) {
        return new DropShardingBroadcastTableRulesStatement();
    }
    
    @Override
    public ASTNode visitShowShardingTableRules(final ShowShardingTableRulesContext ctx) {
        return new ShowShardingTableRulesStatement(null == ctx.tableRule() ? null : ctx.tableRule().tableName().getText(), null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShardingTableRuleDefinition(final ShardingTableRuleDefinitionContext ctx) {
        Collection<String> dataSources = new LinkedList<>();
        if (null != ctx.resources()) {
            for (TerminalNode each : ctx.resources().IDENTIFIER()) {
                dataSources.add(new IdentifierValue(each.getText()).getValue());
            }
        }
        String tableStrategyColumn = null;
        AlgorithmSegment tableStrategy = null;
        if (null != ctx.algorithmDefinition()) {
            tableStrategyColumn = ctx.shardingColumn().columnName().getText();
            tableStrategy = (AlgorithmSegment) visit(ctx.algorithmDefinition());
        }
        String keyGenerateStrategyColumn = null;
        AlgorithmSegment keyGenerateStrategy = null;
        if (null != ctx.keyGenerateStrategy()) {
            keyGenerateStrategyColumn = ctx.keyGenerateStrategy().columnName().getText();
            keyGenerateStrategy = (AlgorithmSegment) visit(ctx.keyGenerateStrategy().algorithmDefinition());
        }
        return new TableRuleSegment(ctx.tableName().getText(), dataSources, tableStrategyColumn, tableStrategy, keyGenerateStrategyColumn, keyGenerateStrategy);
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
    public ASTNode visitShowShardingBindingTableRules(final ShowShardingBindingTableRulesContext ctx) {
        return new ShowShardingBindingTableRulesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
}
