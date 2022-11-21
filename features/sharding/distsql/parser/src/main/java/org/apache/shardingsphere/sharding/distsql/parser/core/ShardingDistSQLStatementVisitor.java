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

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AddShardingHintDatabaseValueContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AddShardingHintTableValueContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterDefaultShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterShardingAuditorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterShardingTableReferenceRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AlterShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AuditDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AuditStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AuditorDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AuditorNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.AutoShardingColumnDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ClearShardingHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CountShardingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateBroadcastTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateDefaultShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateShardingAuditorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateShardingTableReferenceRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.CreateShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DataNodesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropBroadcastTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropDefaultShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingAuditorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingKeyGeneratorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingTableReferenceRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.DropShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.KeyGenerateDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.SetShardingHintDatabaseValueContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingAutoTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingColumnDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShardingTableRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingAuditorsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingDefaultShardingStrategyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingHintStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingKeyGeneratorsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableNodesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableReferenceRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableRulesUsedAlgorithmContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableRulesUsedAuditorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowShardingTableRulesUsedKeyGeneratorContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowUnusedShardingAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowUnusedShardingAuditorsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.ShowUnusedShardingKeyGeneratorsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.SingleAuditDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.StorageUnitsContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.TableReferenceRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingAuditorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CountShardingRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateBroadcastTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingAuditorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropBroadcastTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAuditorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAuditorsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableReferenceRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAlgorithmStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedAuditorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingAlgorithmsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingAuditorsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintTableValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ClearShardingHintStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.SetShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ShowShardingHintStatusStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
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
 * Sharding DistSQL statement visitor.
 */
public final class ShardingDistSQLStatementVisitor extends ShardingDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateShardingTableRule(final CreateShardingTableRuleContext ctx) {
        Collection<AbstractTableRuleSegment> tableRuleSegments = ctx.shardingTableRuleDefinition().stream()
                .map(each -> (AbstractTableRuleSegment) visit(each)).filter(Objects::nonNull).collect(Collectors.toList());
        return new CreateShardingTableRuleStatement(tableRuleSegments);
    }
    
    @Override
    public ASTNode visitCreateShardingTableReferenceRule(final CreateShardingTableReferenceRuleContext ctx) {
        return new CreateShardingTableReferenceRuleStatement(createTableReferenceRuleSegment(ctx.tableReferenceRuleDefinition()));
    }
    
    private Collection<TableReferenceRuleSegment> createTableReferenceRuleSegment(final List<TableReferenceRuleDefinitionContext> contexts) {
        return contexts.stream().map(each -> each.tableName().stream()
                .map(this::getIdentifierValue).collect(Collectors.joining(","))).map(TableReferenceRuleSegment::new).collect(Collectors.toList());
    }
    
    @Override
    public ASTNode visitCreateBroadcastTableRule(final CreateBroadcastTableRuleContext ctx) {
        return new CreateBroadcastTableRuleStatement(ctx.tableName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterShardingTableRule(final AlterShardingTableRuleContext ctx) {
        List<AbstractTableRuleSegment> tableRuleSegments = ctx.shardingTableRuleDefinition().stream()
                .map(each -> (AbstractTableRuleSegment) visit(each)).filter(Objects::nonNull).collect(Collectors.toList());
        return new AlterShardingTableRuleStatement(tableRuleSegments);
    }
    
    @Override
    public ASTNode visitShowBroadcastTableRules(final ShowBroadcastTableRulesContext ctx) {
        return new ShowBroadcastTableRulesStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitAlterShardingTableReferenceRule(final AlterShardingTableReferenceRuleContext ctx) {
        Collection<TableReferenceRuleSegment> rules = new LinkedList<>();
        for (TableReferenceRuleDefinitionContext each : ctx.tableReferenceRuleDefinition()) {
            rules.add(new TableReferenceRuleSegment(each.tableName().stream().map(tableName -> new IdentifierValue(tableName.getText()).getValue()).collect(Collectors.joining(","))));
        }
        return new AlterShardingTableReferenceRuleStatement(rules);
    }
    
    @Override
    public ASTNode visitDropShardingTableRule(final DropShardingTableRuleContext ctx) {
        return new DropShardingTableRuleStatement(null != ctx.ifExists(),
                ctx.tableName().stream().map(each -> (TableNameSegment) visit(each)).collect(Collectors.toList()), null != ctx.withUnusedAlgorithmsClause());
    }
    
    @Override
    public ASTNode visitDropShardingTableReferenceRule(final DropShardingTableReferenceRuleContext ctx) {
        Collection<TableReferenceRuleSegment> tableNames = null == ctx.tableReferenceRuleDefinition() ? Collections.emptyList() : createTableReferenceRuleSegment(ctx.tableReferenceRuleDefinition());
        return new DropShardingTableReferenceRuleStatement(null != ctx.ifExists(), tableNames);
    }
    
    @Override
    public ASTNode visitCreateDefaultShardingStrategy(final CreateDefaultShardingStrategyContext ctx) {
        ShardingStrategyContext shardingStrategyContext = ctx.shardingStrategy();
        AlgorithmSegment algorithmSegment = null;
        if (null != shardingStrategyContext.shardingAlgorithm().algorithmDefinition()) {
            algorithmSegment = (AlgorithmSegment) visitAlgorithmDefinition(shardingStrategyContext.shardingAlgorithm().algorithmDefinition());
        }
        String defaultType = new IdentifierValue(ctx.type.getText()).getValue();
        String strategyType = getIdentifierValue(shardingStrategyContext.strategyType());
        String shardingColumn = buildShardingColumn(ctx.shardingStrategy().shardingColumnDefinition());
        return new CreateDefaultShardingStrategyStatement(defaultType, strategyType, shardingColumn, algorithmSegment);
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
        String defaultType = new IdentifierValue(ctx.type.getText()).getValue();
        String strategyType = getIdentifierValue(ctx.shardingStrategy().strategyType());
        String shardingColumn = buildShardingColumn(ctx.shardingStrategy().shardingColumnDefinition());
        AlgorithmSegment algorithmSegment = null == ctx.shardingStrategy().shardingAlgorithm().algorithmDefinition()
                ? null
                : (AlgorithmSegment) visitAlgorithmDefinition(ctx.shardingStrategy().shardingAlgorithm().algorithmDefinition());
        return new AlterDefaultShardingStrategyStatement(defaultType, strategyType, shardingColumn, algorithmSegment);
    }
    
    @Override
    public ASTNode visitDropDefaultShardingStrategy(final DropDefaultShardingStrategyContext ctx) {
        return new DropDefaultShardingStrategyStatement(null != ctx.ifExists(), new IdentifierValue(ctx.type.getText()).getValue().toLowerCase());
    }
    
    @Override
    public ASTNode visitClearShardingHint(final ClearShardingHintContext ctx) {
        return new ClearShardingHintStatement();
    }
    
    @Override
    public ASTNode visitDropBroadcastTableRule(final DropBroadcastTableRuleContext ctx) {
        Collection<String> tableNames = ctx.tableName().stream().map(this::getIdentifierValue).collect(Collectors.toList());
        return new DropBroadcastTableRuleStatement(null != ctx.ifExists(), tableNames);
    }
    
    @Override
    public ASTNode visitDropShardingAlgorithm(final DropShardingAlgorithmContext ctx) {
        return new DropShardingAlgorithmStatement(null != ctx.ifExists(), ctx.shardingAlgorithmName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowShardingTableRules(final ShowShardingTableRulesContext ctx) {
        return new ShowShardingTableRulesStatement(null == ctx.tableRule() ? null : getIdentifierValue(ctx.tableRule().tableName()),
                null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowShardingAlgorithms(final ShowShardingAlgorithmsContext ctx) {
        return new ShowShardingAlgorithmsStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
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
        KeyGenerateStrategySegment keyGenerateSegment = null == ctx.keyGenerateDefinition() ? null : (KeyGenerateStrategySegment) visit(ctx.keyGenerateDefinition());
        AuditStrategySegment auditStrategySegment = null == ctx.auditDeclaration() ? null : (AuditStrategySegment) visit(ctx.auditDeclaration());
        TableRuleSegment result = new TableRuleSegment(getIdentifierValue(ctx.tableName()), getDataNodes(ctx.dataNodes()), keyGenerateSegment, auditStrategySegment);
        Optional.ofNullable(ctx.tableStrategy()).ifPresent(optional -> result.setTableStrategySegment((ShardingStrategySegment) visit(ctx.tableStrategy().shardingStrategy())));
        Optional.ofNullable(ctx.databaseStrategy()).ifPresent(optional -> result.setDatabaseStrategySegment((ShardingStrategySegment) visit(ctx.databaseStrategy().shardingStrategy())));
        return result;
    }
    
    @Override
    public ASTNode visitShowShardingTableNodes(final ShowShardingTableNodesContext ctx) {
        return new ShowShardingTableNodesStatement(null == ctx.tableName() ? null : getIdentifierValue(ctx.tableName()),
                null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShardingAutoTableRule(final ShardingAutoTableRuleContext ctx) {
        AutoTableRuleSegment result = new AutoTableRuleSegment(getIdentifierValue(ctx.tableName()), getResources(ctx.storageUnits()));
        Optional.ofNullable(ctx.keyGenerateDefinition()).ifPresent(optional -> result.setKeyGenerateStrategySegment((KeyGenerateStrategySegment) visit(ctx.keyGenerateDefinition())));
        Optional.ofNullable(ctx.auditDeclaration()).ifPresent(optional -> result.setAuditStrategySegment((AuditStrategySegment) visit(ctx.auditDeclaration())));
        Optional.ofNullable(ctx.autoShardingColumnDefinition()).ifPresent(optional -> result.setShardingColumn(buildShardingColumn(ctx.autoShardingColumnDefinition())));
        Optional.ofNullable(ctx.algorithmDefinition()).ifPresent(optional -> result.setShardingAlgorithmSegment((AlgorithmSegment) visit(ctx.algorithmDefinition())));
        return result;
    }
    
    @Override
    public ASTNode visitKeyGenerateDefinition(final KeyGenerateDefinitionContext ctx) {
        return null == ctx ? null : new KeyGenerateStrategySegment(getIdentifierValue(ctx.columnName()), (AlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitAuditStrategy(final AuditStrategyContext ctx) {
        if (null == ctx) {
            return null;
        }
        Collection<String> auditorNames = new LinkedList<>();
        for (AuditorNameContext each : ctx.auditorNames().auditorName()) {
            auditorNames.add(getIdentifierValue(each));
        }
        return new AuditStrategySegment(auditorNames, Boolean.parseBoolean(getIdentifierValue(ctx.auditAllowHintDisable())));
    }
    
    @Override
    public ASTNode visitAuditDefinition(final AuditDefinitionContext ctx) {
        if (null == ctx) {
            return null;
        }
        Collection<String> auditorNames = new LinkedList<>();
        Collection<ShardingAuditorSegment> shardingAuditorSegments = new LinkedList<>();
        for (SingleAuditDefinitionContext each : ctx.multiAuditDefinition().singleAuditDefinition()) {
            String auditorName = getIdentifierValue(each.auditorName());
            auditorNames.add(auditorName);
            shardingAuditorSegments.add(new ShardingAuditorSegment(auditorName, (AlgorithmSegment) visit(each.algorithmDefinition())));
        }
        return new AuditStrategySegment(auditorNames, shardingAuditorSegments, Boolean.parseBoolean(getIdentifierValue(ctx.auditAllowHintDisable())));
    }
    
    @Override
    public ASTNode visitShardingStrategy(final ShardingStrategyContext ctx) {
        if (null == ctx) {
            return null;
        }
        AlgorithmSegment algorithmSegment = null;
        if (null != ctx.shardingAlgorithm().algorithmDefinition()) {
            algorithmSegment = (AlgorithmSegment) visitAlgorithmDefinition(ctx.shardingAlgorithm().algorithmDefinition());
        }
        return new ShardingStrategySegment(getIdentifierValue(ctx.strategyType()), buildShardingColumn(ctx.shardingColumnDefinition()), algorithmSegment);
    }
    
    private Collection<String> getResources(final StorageUnitsContext ctx) {
        return ctx.storageUnit().stream().map(this::getIdentifierValue).collect(Collectors.toList());
    }
    
    private Collection<String> getDataNodes(final DataNodesContext ctx) {
        return ctx.dataNode().stream().map(this::getIdentifierValueForDataNodes).collect(Collectors.toList());
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), getProperties(ctx.propertiesDefinition()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
    
    private String getIdentifierValueForDataNodes(final ParseTree context) {
        if (null == context) {
            return null;
        }
        String value = new IdentifierValue(context.getText(), "[]'").getValue();
        return value.startsWith("'") ? value.substring(1, value.length() - 1) : value.trim();
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
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitShowShardingTableReferenceRules(final ShowShardingTableReferenceRulesContext ctx) {
        return new ShowShardingTableReferenceRulesStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitShowShardingKeyGenerators(final ShowShardingKeyGeneratorsContext ctx) {
        return new ShowShardingKeyGeneratorsStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitDropShardingKeyGenerator(final DropShardingKeyGeneratorContext ctx) {
        return new DropShardingKeyGeneratorStatement(null != ctx.ifExists(), ctx.keyGeneratorName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitCreateShardingAuditor(final CreateShardingAuditorContext ctx) {
        return new CreateShardingAuditorStatement(ctx.auditorDefinition().stream().map(this::buildShardingAuditorSegment).collect(Collectors.toCollection(LinkedList::new)));
    }
    
    @Override
    public ASTNode visitAlterShardingAuditor(final AlterShardingAuditorContext ctx) {
        return new AlterShardingAuditorStatement(ctx.auditorDefinition().stream().map(this::buildShardingAuditorSegment).collect(Collectors.toList()));
    }
    
    private ShardingAuditorSegment buildShardingAuditorSegment(final AuditorDefinitionContext ctx) {
        return new ShardingAuditorSegment(getIdentifierValue(ctx.auditorName()), (AlgorithmSegment) visitAlgorithmDefinition(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitDropShardingAuditor(final DropShardingAuditorContext ctx) {
        return new DropShardingAuditorStatement(null != ctx.ifExists(), ctx.auditorName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowShardingAuditors(final ShowShardingAuditorsContext ctx) {
        return new ShowShardingAuditorsStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitShowShardingDefaultShardingStrategy(final ShowShardingDefaultShardingStrategyContext ctx) {
        return new ShowDefaultShardingStrategyStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitShowUnusedShardingAlgorithms(final ShowUnusedShardingAlgorithmsContext ctx) {
        return new ShowUnusedShardingAlgorithmsStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    private String buildShardingColumn(final AutoShardingColumnDefinitionContext ctx) {
        return null == ctx.shardingColumn() ? null : getIdentifierValue(ctx.shardingColumn().columnName());
    }
    
    private String buildShardingColumn(final ShardingColumnDefinitionContext ctx) {
        String result = Optional.ofNullable(ctx.shardingColumn()).map(optional -> getIdentifierValue(optional.columnName()))
                .orElseGet(() -> ctx.shardingColumns().columnName().stream().map(this::getIdentifierValue).collect(Collectors.joining(",")));
        return result.isEmpty() ? null : result;
    }
    
    @Override
    public ASTNode visitShowUnusedShardingKeyGenerators(final ShowUnusedShardingKeyGeneratorsContext ctx) {
        return new ShowUnusedShardingKeyGeneratorsStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitShowUnusedShardingAuditors(final ShowUnusedShardingAuditorsContext ctx) {
        return new ShowUnusedShardingAuditorsStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitShowShardingTableRulesUsedAlgorithm(final ShowShardingTableRulesUsedAlgorithmContext ctx) {
        return new ShowShardingTableRulesUsedAlgorithmStatement(
                getIdentifierValue(ctx.shardingAlgorithmName()), Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
    
    @Override
    public ASTNode visitShowShardingTableRulesUsedKeyGenerator(final ShowShardingTableRulesUsedKeyGeneratorContext ctx) {
        return new ShowShardingTableRulesUsedKeyGeneratorStatement(getIdentifierValue(ctx.keyGeneratorName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowShardingTableRulesUsedAuditor(final ShowShardingTableRulesUsedAuditorContext ctx) {
        return new ShowShardingTableRulesUsedAuditorStatement(getIdentifierValue(ctx.auditorName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitCountShardingRule(final CountShardingRuleContext ctx) {
        return new CountShardingRuleStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
    }
}
