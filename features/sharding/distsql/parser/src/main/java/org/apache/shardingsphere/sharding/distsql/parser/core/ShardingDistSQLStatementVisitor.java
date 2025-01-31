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
import org.apache.shardingsphere.distsql.parser.autogen.ShardingDistSQLStatementParser.*;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.distsql.statement.rql.rule.database.CountRuleStatement;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.statement.*;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sharding DistSQL statement visitor.
 */
public final class ShardingDistSQLStatementVisitor extends ShardingDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCreateShardingTableRule(final CreateShardingTableRuleContext ctx) {
        Collection<AbstractTableRuleSegment> tableRuleSegments = ctx.shardingTableRuleDefinition().stream()
                .map(each -> (AbstractTableRuleSegment) visit(each)).filter(Objects::nonNull).collect(Collectors.toList());
        return new CreateShardingTableRuleStatement(null != ctx.ifNotExists(), tableRuleSegments);
    }
    
    @Override
    public ASTNode visitCreateShardingTableReferenceRule(final CreateShardingTableReferenceRuleContext ctx) {
        return new CreateShardingTableReferenceRuleStatement(null != ctx.ifNotExists(), getTableReferenceRuleSegments(ctx.tableReferenceRuleDefinition()));
    }
    
    private Collection<TableReferenceRuleSegment> getTableReferenceRuleSegments(final List<TableReferenceRuleDefinitionContext> ctx) {
        Collection<TableReferenceRuleSegment> result = new LinkedList<>();
        for (TableReferenceRuleDefinitionContext each : ctx) {
            String ruleName = getIdentifierValue(each.ruleName());
            String reference = each.tableName().stream().map(this::getIdentifierValue).collect(Collectors.joining(","));
            result.add(new TableReferenceRuleSegment(ruleName, reference));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterShardingTableRule(final AlterShardingTableRuleContext ctx) {
        List<AbstractTableRuleSegment> tableRuleSegments = ctx.shardingTableRuleDefinition().stream()
                .map(each -> (AbstractTableRuleSegment) visit(each)).filter(Objects::nonNull).collect(Collectors.toList());
        return new AlterShardingTableRuleStatement(tableRuleSegments);
    }
    
    @Override
    public ASTNode visitAlterShardingTableReferenceRule(final AlterShardingTableReferenceRuleContext ctx) {
        return new AlterShardingTableReferenceRuleStatement(getTableReferenceRuleSegments(ctx.tableReferenceRuleDefinition()));
    }
    
    @Override
    public ASTNode visitDropShardingTableRule(final DropShardingTableRuleContext ctx) {
        return new DropShardingTableRuleStatement(null != ctx.ifExists(), ctx.tableName().stream().map(each -> (TableNameSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropShardingTableReferenceRule(final DropShardingTableReferenceRuleContext ctx) {
        return new DropShardingTableReferenceRuleStatement(null != ctx.ifExists(), ctx.ruleName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitCreateDefaultShardingStrategy(final CreateDefaultShardingStrategyContext ctx) {
        ShardingStrategyContext shardingStrategyContext = ctx.shardingStrategy();
        String defaultType = new IdentifierValue(ctx.type.getText()).getValue();
        String strategyType = getIdentifierValue(shardingStrategyContext.strategyType());
        if ("none".equalsIgnoreCase(strategyType)) {
            return new CreateDefaultShardingStrategyStatement(null != ctx.ifNotExists(), defaultType, "none", null, null);
        }
        AlgorithmSegment algorithmSegment = null == shardingStrategyContext.shardingAlgorithm().algorithmDefinition()
                ? null
                : (AlgorithmSegment) visitAlgorithmDefinition(shardingStrategyContext.shardingAlgorithm().algorithmDefinition());
        String shardingColumn = null == ctx.shardingStrategy().shardingColumnDefinition() ? null : buildShardingColumn(ctx.shardingStrategy().shardingColumnDefinition());
        return new CreateDefaultShardingStrategyStatement(null != ctx.ifNotExists(), defaultType, strategyType, shardingColumn, algorithmSegment);
    }
    
    @Override
    public ASTNode visitAlterDefaultShardingStrategy(final AlterDefaultShardingStrategyContext ctx) {
        String defaultType = new IdentifierValue(ctx.type.getText()).getValue();
        String strategyType = getIdentifierValue(ctx.shardingStrategy().strategyType());
        if ("none".equalsIgnoreCase(strategyType)) {
            return new AlterDefaultShardingStrategyStatement(defaultType, "none", null, null);
        }
        AlgorithmSegment algorithmSegment = null == ctx.shardingStrategy().shardingAlgorithm().algorithmDefinition()
                ? null
                : (AlgorithmSegment) visitAlgorithmDefinition(ctx.shardingStrategy().shardingAlgorithm().algorithmDefinition());
        String shardingColumn = null == ctx.shardingStrategy().shardingColumnDefinition() ? null : buildShardingColumn(ctx.shardingStrategy().shardingColumnDefinition());
        return new AlterDefaultShardingStrategyStatement(defaultType, strategyType, shardingColumn, algorithmSegment);
    }
    
    @Override
    public ASTNode visitDropDefaultShardingStrategy(final DropDefaultShardingStrategyContext ctx) {
        return new DropDefaultShardingStrategyStatement(null != ctx.ifExists(), new IdentifierValue(ctx.type.getText()).getValue().toLowerCase());
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
        return new ShowShardingAlgorithmsStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
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
        AuditStrategySegment auditStrategySegment = null == ctx.auditDefinition() ? null : (AuditStrategySegment) visitAuditDefinition(getIdentifierValue(ctx.tableName()), ctx.auditDefinition());
        TableRuleSegment result = new TableRuleSegment(getIdentifierValue(ctx.tableName()), getDataNodes(ctx.dataNodes()), keyGenerateSegment, auditStrategySegment);
        Optional.ofNullable(ctx.tableStrategy()).ifPresent(optional -> result.setTableStrategySegment((ShardingStrategySegment) visit(ctx.tableStrategy().shardingStrategy())));
        Optional.ofNullable(ctx.databaseStrategy()).ifPresent(optional -> result.setDatabaseStrategySegment((ShardingStrategySegment) visit(ctx.databaseStrategy().shardingStrategy())));
        return result;
    }
    
    private ASTNode visitAuditDefinition(final String tableName, final AuditDefinitionContext ctx) {
        if (null == ctx) {
            return null;
        }
        Collection<ShardingAuditorSegment> shardingAuditorSegments = new LinkedList<>();
        int index = 0;
        for (SingleAuditDefinitionContext each : ctx.multiAuditDefinition().singleAuditDefinition()) {
            String algorithmTypeName = getIdentifierValue(each.algorithmDefinition().algorithmTypeName());
            String auditorName = String.format("%s_%s_%s", tableName, algorithmTypeName, index++).toLowerCase();
            shardingAuditorSegments.add(new ShardingAuditorSegment(auditorName, (AlgorithmSegment) visit(each.algorithmDefinition())));
        }
        return new AuditStrategySegment(shardingAuditorSegments, Boolean.parseBoolean(getIdentifierValue(ctx.auditAllowHintDisable())));
    }
    
    @Override
    public ASTNode visitShowShardingTableNodes(final ShowShardingTableNodesContext ctx) {
        return new ShowShardingTableNodesStatement(null == ctx.tableName() ? null : getIdentifierValue(ctx.tableName()),
                null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShardingAutoTableRule(final ShardingAutoTableRuleContext ctx) {
        AutoTableRuleSegment result = new AutoTableRuleSegment(getIdentifierValue(ctx.tableName()), getDataNodes(ctx.dataNodes()));
        Optional.ofNullable(ctx.keyGenerateDefinition()).ifPresent(optional -> result.setKeyGenerateStrategySegment((KeyGenerateStrategySegment) visit(ctx.keyGenerateDefinition())));
        Optional.ofNullable(ctx.auditDefinition()).ifPresent(optional -> result.setAuditStrategySegment((AuditStrategySegment) visitAuditDefinition(getIdentifierValue(ctx.tableName()),
                ctx.auditDefinition())));
        Optional.ofNullable(ctx.autoShardingColumnDefinition()).ifPresent(optional -> result.setShardingColumn(buildShardingColumn(ctx.autoShardingColumnDefinition())));
        Optional.ofNullable(ctx.algorithmDefinition()).ifPresent(optional -> result.setShardingAlgorithmSegment((AlgorithmSegment) visit(ctx.algorithmDefinition())));
        return result;
    }
    
    @Override
    public ASTNode visitKeyGenerateDefinition(final KeyGenerateDefinitionContext ctx) {
        return null == ctx ? null : new KeyGenerateStrategySegment(getIdentifierValue(ctx.columnName()), (AlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitShardingStrategy(final ShardingStrategyContext ctx) {
        String strategyType = getIdentifierValue(ctx.strategyType());
        if ("none".equalsIgnoreCase(strategyType)) {
            return new ShardingStrategySegment(strategyType, null, null);
        }
        AlgorithmSegment algorithmSegment = null == ctx.shardingAlgorithm().algorithmDefinition() ? null : (AlgorithmSegment) visitAlgorithmDefinition(ctx.shardingAlgorithm().algorithmDefinition());
        String shardingColumn = null == ctx.shardingColumnDefinition() ? null : buildShardingColumn(ctx.shardingColumnDefinition());
        return new ShardingStrategySegment(strategyType, shardingColumn, algorithmSegment);
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
            result.setProperty(QuoteCharacter.unwrapAndTrimText(each.key.getText()), QuoteCharacter.unwrapAndTrimText(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitShowShardingTableReferenceRules(final ShowShardingTableReferenceRulesContext ctx) {
        return new ShowShardingTableReferenceRulesStatement(null == ctx.ruleName() ? null : getIdentifierValue(ctx.ruleName()),
                null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitShowShardingKeyGenerators(final ShowShardingKeyGeneratorsContext ctx) {
        return new ShowShardingKeyGeneratorsStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitDropShardingKeyGenerator(final DropShardingKeyGeneratorContext ctx) {
        return new DropShardingKeyGeneratorStatement(null != ctx.ifExists(), ctx.keyGeneratorName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropShardingAuditor(final DropShardingAuditorContext ctx) {
        return new DropShardingAuditorStatement(null != ctx.ifExists(), ctx.auditorName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowShardingAuditors(final ShowShardingAuditorsContext ctx) {
        return new ShowShardingAuditorsStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowDefaultShardingStrategy(final ShowDefaultShardingStrategyContext ctx) {
        return new ShowDefaultShardingStrategyStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowUnusedShardingAlgorithms(final ShowUnusedShardingAlgorithmsContext ctx) {
        return new ShowUnusedShardingAlgorithmsStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
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
        return new ShowUnusedShardingKeyGeneratorsStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowUnusedShardingAuditors(final ShowUnusedShardingAuditorsContext ctx) {
        return new ShowUnusedShardingAuditorsStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowShardingTableRulesUsedAlgorithm(final ShowShardingTableRulesUsedAlgorithmContext ctx) {
        return new ShowShardingTableRulesUsedAlgorithmStatement(
                getIdentifierValue(ctx.shardingAlgorithmName()), null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
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
        return new CountRuleStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()), "SHARDING");
    }
    
    @Override
    public ASTNode visitShowShardingAlgorithmPlugins(final ShowShardingAlgorithmPluginsContext ctx) {
        return new ShowPluginsStatement("SHARDING_ALGORITHM");
    }
}
