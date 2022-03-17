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
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AddShardingHintDatabaseValueContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AddShardingHintTableValueContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterDefaultShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterShardingAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterShardingKeyGeneratorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.BindTableRulesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ClearShardingHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateDefaultShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateShardingAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateShardingKeyGeneratorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DataNodesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropDefaultShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingKeyGeneratorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.KeyGenerateDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.KeyGenerateStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.KeyGeneratorDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.SetShardingHintDatabaseValueContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingAlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingAutoTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingColumnDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingTableRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingDefaultShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingHintStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingKeyGeneratorsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableNodesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableRulesUsedAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableRulesUsedKeyGeneratorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowUnusedShardingAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowUnusedShardingKeyGeneratorsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment.EmptyTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingKeyGeneratorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAlgorithmStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintTableValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ClearShardingHintStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.SetShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ShowShardingHintStatusStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for sharding dist SQL.
 */
public final class ShardingDistSQLStatementVisitor extends ShardingDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitAlterShardingAlgorithm(final AlterShardingAlgorithmContext ctx) {
        return new AlterShardingAlgorithmStatement(ctx.shardingAlgorithmDefinition().stream().map(this::buildAlgorithmSegment).collect(Collectors.toCollection(LinkedList::new)));
    }
    
    @Override
    public ASTNode visitCreateShardingTableRule(final CreateShardingTableRuleContext ctx) {
        List<AbstractTableRuleSegment> tableRuleSegments = ctx.shardingTableRuleDefinition().stream().map(each -> (AbstractTableRuleSegment) visit(each))
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (tableRuleSegments.isEmpty()) {
            return new EmptyTableRuleSegment();
        }
        return new CreateShardingTableRuleStatement(tableRuleSegments);
    }
    
    @Override
    public ASTNode visitCreateShardingBindingTableRules(final CreateShardingBindingTableRulesContext ctx) {
        return new CreateShardingBindingTableRulesStatement(createBindingTableRuleSegment(ctx.bindTableRulesDefinition()));
    }
    
    private Collection<BindingTableRuleSegment> createBindingTableRuleSegment(final List<BindTableRulesDefinitionContext> contexts) {
        return contexts.stream().map(each -> Joiner.on(",").join(each.tableName().stream().map(t -> getIdentifierValue(t)).collect(Collectors.toList())))
                .map(BindingTableRuleSegment::new).collect(Collectors.toCollection(LinkedList::new));
    }
    
    @Override
    public ASTNode visitCreateShardingBroadcastTableRules(final CreateShardingBroadcastTableRulesContext ctx) {
        return new CreateShardingBroadcastTableRulesStatement(ctx.tableName().stream().map(each -> getIdentifierValue(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterShardingTableRule(final AlterShardingTableRuleContext ctx) {
        List<AbstractTableRuleSegment> tableRuleSegments = ctx.shardingTableRuleDefinition().stream().map(each -> (AbstractTableRuleSegment) visit(each))
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (tableRuleSegments.isEmpty()) {
            return new EmptyTableRuleSegment();
        }
        return new AlterShardingTableRuleStatement(tableRuleSegments);
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
        return new AlterShardingBroadcastTableRulesStatement(ctx.tableName().stream().map(each -> getIdentifierValue(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropShardingTableRule(final DropShardingTableRuleContext ctx) {
        return new DropShardingTableRuleStatement(null != ctx.existsClause(), ctx.tableName().stream().map(each -> (TableNameSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropShardingBindingTableRules(final DropShardingBindingTableRulesContext ctx) {
        Collection<BindingTableRuleSegment> tableNames = null == ctx.bindTableRulesDefinition() ? Collections.emptyList()
                : createBindingTableRuleSegment(ctx.bindTableRulesDefinition());
        return new DropShardingBindingTableRulesStatement(null != ctx.existsClause(), tableNames);
    }
    
    @Override
    public ASTNode visitCreateDefaultShardingStrategy(final CreateDefaultShardingStrategyContext ctx) {
        ShardingStrategyContext shardingStrategyContext = ctx.shardingStrategy();
        String shardingAlgorithmName = null;
        if (null != shardingStrategyContext.shardingAlgorithm().existingAlgorithm()) {
            shardingAlgorithmName = getIdentifierValue(shardingStrategyContext.shardingAlgorithm().existingAlgorithm().shardingAlgorithmName()).toLowerCase();
        }
        AlgorithmSegment algorithmSegment = null;
        if (null != shardingStrategyContext.shardingAlgorithm().autoCreativeAlgorithm()) {
            algorithmSegment = (AlgorithmSegment) visitAlgorithmDefinition(shardingStrategyContext.shardingAlgorithm().autoCreativeAlgorithm().algorithmDefinition());
        }
        String defaultType = new IdentifierValue(ctx.type.getText()).getValue();
        String strategyType = getIdentifierValue(shardingStrategyContext.strategyType());
        String shardingColumn = buildShardingColumn(ctx.shardingStrategy().shardingColumnDefinition());
        return new CreateDefaultShardingStrategyStatement(defaultType, strategyType, shardingColumn, shardingAlgorithmName, algorithmSegment);
    }
    
    @Override
    public ASTNode visitSetShardingHintDatabaseValue(final SetShardingHintDatabaseValueContext ctx) {
        return new SetShardingHintDatabaseValueStatement(getIdentifierValue(ctx.shardingValue()));
    }
    
    @Override
    public ASTNode visitAddShardingHintDatabaseValue(final AddShardingHintDatabaseValueContext ctx) {
        return new AddShardingHintDatabaseValueStatement(getIdentifierValue(ctx.tableName()), getIdentifierValue(ctx.shardingValue()));
    }
    
    @Override
    public ASTNode visitAddShardingHintTableValue(final AddShardingHintTableValueContext ctx) {
        return new AddShardingHintTableValueStatement(getIdentifierValue(ctx.tableName()), getIdentifierValue(ctx.shardingValue()));
    }
    
    @Override
    public ASTNode visitShowShardingHintStatus(final ShowShardingHintStatusContext ctx) {
        return new ShowShardingHintStatusStatement();
    }
    
    @Override
    public ASTNode visitAlterDefaultShardingStrategy(final AlterDefaultShardingStrategyContext ctx) {
        ShardingStrategyContext shardingStrategyContext = ctx.shardingStrategy();
        String shardingAlgorithmName = null;
        if (null != shardingStrategyContext.shardingAlgorithm().existingAlgorithm()) {
            shardingAlgorithmName = getIdentifierValue(shardingStrategyContext.shardingAlgorithm().existingAlgorithm().shardingAlgorithmName()).toLowerCase();
        }
        AlgorithmSegment algorithmSegment = null;
        if (null != shardingStrategyContext.shardingAlgorithm().autoCreativeAlgorithm()) {
            algorithmSegment = (AlgorithmSegment) visitAlgorithmDefinition(shardingStrategyContext.shardingAlgorithm().autoCreativeAlgorithm().algorithmDefinition());
        }
        String defaultType = new IdentifierValue(ctx.type.getText()).getValue();
        String strategyType = getIdentifierValue(shardingStrategyContext.strategyType());
        String shardingColumn = buildShardingColumn(ctx.shardingStrategy().shardingColumnDefinition());
        return new AlterDefaultShardingStrategyStatement(defaultType, strategyType, shardingColumn, shardingAlgorithmName, algorithmSegment);
    }
    
    @Override
    public ASTNode visitDropDefaultShardingStrategy(final DropDefaultShardingStrategyContext ctx) {
        return new DropDefaultShardingStrategyStatement(null != ctx.existsClause(), new IdentifierValue(ctx.type.getText()).getValue().toLowerCase());
    }
    
    @Override
    public ASTNode visitClearShardingHint(final ClearShardingHintContext ctx) {
        return new ClearShardingHintStatement();
    }
    
    @Override
    public ASTNode visitDropShardingBroadcastTableRules(final DropShardingBroadcastTableRulesContext ctx) {
        Collection<String> tableNames = null == ctx.tableName() ? Collections.emptyList()
                : ctx.tableName().stream().map(this::getIdentifierValue).collect(Collectors.toCollection(LinkedList::new));
        return new DropShardingBroadcastTableRulesStatement(null != ctx.existsClause(), tableNames);
    }
    
    @Override
    public ASTNode visitDropShardingAlgorithm(final DropShardingAlgorithmContext ctx) {
        DropShardingAlgorithmStatement result = new DropShardingAlgorithmStatement(ctx.algorithmName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
        result.setContainsExistClause(null != ctx.existsClause());
        return result;
    }
    
    @Override
    public ASTNode visitShowShardingTableRules(final ShowShardingTableRulesContext ctx) {
        return new ShowShardingTableRulesStatement(null == ctx.tableRule() ? null : getIdentifierValue(ctx.tableRule().tableName()),
                null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShowShardingAlgorithms(final ShowShardingAlgorithmsContext ctx) {
        return new ShowShardingAlgorithmsStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitShardingTableRuleDefinition(final ShardingTableRuleDefinitionContext ctx) {
        if (null != ctx.shardingTableRule()) {
            return visit(ctx.shardingTableRule());
        }
        if (null != ctx.shardingAutoTableRule()) {
            return visit(ctx.shardingAutoTableRule());
        }
        return null;
    }
    
    @Override
    public ASTNode visitShardingTableRule(final ShardingTableRuleContext ctx) {
        String tableName = getIdentifierValue(ctx.tableName());
        Collection<String> dataNodes = getDataNodes(ctx.dataNodes());
        KeyGenerateStrategySegment keyGenerateSegment = null != ctx.keyGenerateDeclaration() ? (KeyGenerateStrategySegment) visit(ctx.keyGenerateDeclaration()) : null;
        TableRuleSegment result = new TableRuleSegment(tableName, dataNodes, keyGenerateSegment);
        Optional.ofNullable(ctx.tableStrategy()).ifPresent(op -> result.setTableStrategySegment((ShardingStrategySegment) visit(ctx.tableStrategy().shardingStrategy())));
        Optional.ofNullable(ctx.databaseStrategy()).ifPresent(op -> result.setDatabaseStrategySegment((ShardingStrategySegment) visit(ctx.databaseStrategy().shardingStrategy())));
        return result;
    }
    
    @Override
    public ASTNode visitShowShardingTableNodes(final ShowShardingTableNodesContext ctx) {
        return new ShowShardingTableNodesStatement(null == ctx.tableName() ? null : getIdentifierValue(ctx.tableName()),
                null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShardingAutoTableRule(final ShardingAutoTableRuleContext ctx) {
        String tableName = getIdentifierValue(ctx.tableName());
        Collection<String> dataSources = getResources(ctx.resources());
        AutoTableRuleSegment result = new AutoTableRuleSegment(tableName, dataSources);
        Optional.ofNullable(ctx.keyGenerateDeclaration()).ifPresent(op -> result.setKeyGenerateStrategySegment((KeyGenerateStrategySegment) visit(ctx.keyGenerateDeclaration())));
        Optional.ofNullable(ctx.shardingColumnDefinition()).ifPresent(op -> result.setShardingColumn(buildShardingColumn(ctx.shardingColumnDefinition())));
        Optional.ofNullable(ctx.algorithmDefinition()).ifPresent(op -> result.setShardingAlgorithmSegment((AlgorithmSegment) visit(ctx.algorithmDefinition())));
        return result;
    }
    
    @Override
    public ASTNode visitKeyGenerateStrategy(final KeyGenerateStrategyContext ctx) {
        if (null == ctx) {
            return null;
        }
        return new KeyGenerateStrategySegment(getIdentifierValue(ctx.columnName()), getIdentifierValue(ctx.keyGenerator().shardingAlgorithmName()));
    }
    
    @Override
    public ASTNode visitKeyGenerateDefinition(final KeyGenerateDefinitionContext ctx) {
        if (null == ctx) {
            return null;
        }
        return new KeyGenerateStrategySegment(getIdentifierValue(ctx.columnName()), (AlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitShardingStrategy(final ShardingStrategyContext ctx) {
        if (null == ctx) {
            return null;
        }
        String shardingAlgorithmName = null;
        if (null != ctx.shardingAlgorithm().existingAlgorithm()) {
            shardingAlgorithmName = getIdentifierValue(ctx.shardingAlgorithm().existingAlgorithm().shardingAlgorithmName()).toLowerCase();
        }
        AlgorithmSegment algorithmSegment = null;
        if (null != ctx.shardingAlgorithm().autoCreativeAlgorithm()) {
            algorithmSegment = (AlgorithmSegment) visitAlgorithmDefinition(ctx.shardingAlgorithm().autoCreativeAlgorithm().algorithmDefinition());
        }
        return new ShardingStrategySegment(getIdentifierValue(ctx.strategyType()), buildShardingColumn(ctx.shardingColumnDefinition()), shardingAlgorithmName, algorithmSegment);
    }
    
    private Collection<String> getResources(final ResourcesContext ctx) {
        return ctx.resource().stream().map(each -> getIdentifierValue(each)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Collection<String> getDataNodes(final DataNodesContext ctx) {
        return ctx.dataNode().stream().map(each -> getIdentifierValueForDataNodes(each)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmName()), getAlgorithmProperties(ctx));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        if (null == context) {
            return null;
        }
        return new IdentifierValue(context.getText()).getValue();
    }
    
    private String getIdentifierValueForDataNodes(final ParseTree context) {
        if (null == context) {
            return null;
        }
        String value = new IdentifierValue(context.getText(), "[]'").getValue();
        if (value.startsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value.trim();
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
    public ASTNode visitCreateShardingAlgorithm(final CreateShardingAlgorithmContext ctx) {
        return new CreateShardingAlgorithmStatement(ctx.shardingAlgorithmDefinition().stream().map(this::buildAlgorithmSegment).collect(Collectors.toCollection(LinkedList::new)));
    }
    
    private ShardingAlgorithmSegment buildAlgorithmSegment(final ShardingAlgorithmDefinitionContext ctx) {
        return new ShardingAlgorithmSegment(getIdentifierValue(ctx.shardingAlgorithmName()), (AlgorithmSegment) visitAlgorithmDefinition(ctx.algorithmDefinition()));
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
    
    @Override
    public ASTNode visitCreateShardingKeyGenerator(final CreateShardingKeyGeneratorContext ctx) {
        return new CreateShardingKeyGeneratorStatement(ctx.keyGeneratorDefinition().stream().map(this::buildShardingKeyGeneratorSegment).collect(Collectors.toCollection(LinkedList::new)));
    }
    
    @Override
    public ASTNode visitAlterShardingKeyGenerator(final AlterShardingKeyGeneratorContext ctx) {
        return new AlterShardingKeyGeneratorStatement(ctx.keyGeneratorDefinition().stream().map(this::buildShardingKeyGeneratorSegment).collect(Collectors.toCollection(LinkedList::new)));
    }
    
    private ShardingKeyGeneratorSegment buildShardingKeyGeneratorSegment(final KeyGeneratorDefinitionContext ctx) {
        return new ShardingKeyGeneratorSegment(getIdentifierValue(ctx.keyGeneratorName()), (AlgorithmSegment) visitAlgorithmDefinition(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitShowShardingKeyGenerators(final ShowShardingKeyGeneratorsContext ctx) {
        return new ShowShardingKeyGeneratorsStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitDropShardingKeyGenerator(final DropShardingKeyGeneratorContext ctx) {
        DropShardingKeyGeneratorStatement result = new DropShardingKeyGeneratorStatement(ctx.keyGeneratorName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
        result.setContainsExistClause(null != ctx.existsClause());
        return result;
    }
    
    @Override
    public ASTNode visitShowShardingDefaultShardingStrategy(final ShowShardingDefaultShardingStrategyContext ctx) {
        return new ShowDefaultShardingStrategyStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitShowUnusedShardingAlgorithms(final ShowUnusedShardingAlgorithmsContext ctx) {
        return new ShowUnusedShardingAlgorithmsStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    private String buildShardingColumn(final ShardingColumnDefinitionContext ctx) {
        String result = Optional.ofNullable(ctx.shardingColumn()).map(op -> getIdentifierValue(op.columnName()))
                .orElseGet(() -> ctx.shardingColumns().columnName().stream().map(this::getIdentifierValue).collect(Collectors.joining(",")));
        return result.isEmpty() ? null : result;
    }
    
    @Override
    public ASTNode visitShowUnusedShardingKeyGenerators(final ShowUnusedShardingKeyGeneratorsContext ctx) {
        return new ShowUnusedShardingKeyGeneratorsStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitShowShardingTableRulesUsedAlgorithm(final ShowShardingTableRulesUsedAlgorithmContext ctx) {
        return new ShowShardingTableRulesUsedAlgorithmStatement(getIdentifierValue(ctx.algorithmName()), Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitShowShardingTableRulesUsedKeyGenerator(final ShowShardingTableRulesUsedKeyGeneratorContext ctx) {
        return new ShowShardingTableRulesUsedKeyGeneratorStatement(getIdentifierValue(ctx.keyGeneratorName()), null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
}
