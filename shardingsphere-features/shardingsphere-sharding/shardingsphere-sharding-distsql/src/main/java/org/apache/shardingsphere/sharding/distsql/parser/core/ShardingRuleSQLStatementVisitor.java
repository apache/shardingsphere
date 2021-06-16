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
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.FunctionDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.ShardingTableRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.ShowShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.ShowShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.ShowShardingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingRuleStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ShardingBindingTableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingTableRulesStatement;
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
        Collection<ShardingBindingTableRuleSegment> rules = new LinkedList<>();
        for (BindTableRulesDefinitionContext each : ctx.bindTableRulesDefinition()) {
            ShardingBindingTableRuleSegment segment = new ShardingBindingTableRuleSegment();
            segment.setTables(Joiner.on(",").join(each.tableName().stream().map(t -> new IdentifierValue(t.getText()).getValue()).collect(Collectors.toList())));
            rules.add(segment);
        }
        return new CreateShardingBindingTableRulesStatement(rules);
    }
    
    @Override
    public ASTNode visitCreateShardingBroadcastTableRules(final CreateShardingBroadcastTableRulesContext ctx) {
        return new CreateShardingBroadcastTableRulesStatement(ctx.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList()));
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
        Collection<ShardingBindingTableRuleSegment> rules = new LinkedList<>();
        for (BindTableRulesDefinitionContext each : ctx.bindTableRulesDefinition()) {
            ShardingBindingTableRuleSegment segment = new ShardingBindingTableRuleSegment();
            segment.setTables(Joiner.on(",").join(each.tableName().stream().map(t -> new IdentifierValue(t.getText()).getValue()).collect(Collectors.toList())));
            rules.add(segment);
        }
        return new AlterShardingBindingTableRulesStatement(rules);
    }
    
    @Override
    public ASTNode visitAlterShardingBroadcastTableRules(final AlterShardingBroadcastTableRulesContext ctx) {
        return new AlterShardingBroadcastTableRulesStatement(ctx.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList()));
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
    public ASTNode visitShardingTableRuleDefinition(final ShardingTableRuleDefinitionContext ctx) {
        TableRuleSegment result = new TableRuleSegment();
        result.setLogicTable(ctx.tableName().getText());
        Collection<String> dataSources = new LinkedList<>();
        if (null != ctx.resources()) {
            for (TerminalNode each : ctx.resources().IDENTIFIER()) {
                dataSources.add(new IdentifierValue(each.getText()).getValue());
            }
        }
        result.setDataSources(dataSources);
        if (null != ctx.functionDefinition()) {
            result.setTableStrategy((FunctionSegment) visit(ctx.functionDefinition()));
            result.setTableStrategyColumn(ctx.shardingColumn().columnName().getText());
        }
        if (null != ctx.keyGenerateStrategy()) {
            result.setKeyGenerateStrategy((FunctionSegment) visit(ctx.keyGenerateStrategy().functionDefinition()));
            result.setKeyGenerateStrategyColumn(ctx.keyGenerateStrategy().columnName().getText());
        }
        return result;
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
    
    @Override
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitShowShardingBindingTableRules(final ShowShardingBindingTableRulesContext ctx) {
        return new ShowShardingBindingTableRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitShowShardingTableRules(final ShowShardingTableRulesContext ctx) {
        return new ShowShardingTableRulesStatement(Objects.nonNull(ctx.tableRule()) ? ctx.tableRule().tableName().getText() : null,
                Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
}
