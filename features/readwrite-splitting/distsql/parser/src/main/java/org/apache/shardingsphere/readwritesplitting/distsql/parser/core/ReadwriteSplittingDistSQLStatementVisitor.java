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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlterReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.AlterReadwriteSplittingStorageUnitStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.CountReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.CreateReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.DropReadwriteSplittingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ReadwriteSplittingRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ShowReadwriteSplittingRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ReadwriteSplittingDistSQLStatementParser.ShowStatusFromReadwriteSplittingRulesContext;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.CountRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingStorageUnitStatusStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.util.IdentifierValueUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for readwrite-splitting DistSQL.
 */
public final class ReadwriteSplittingDistSQLStatementVisitor extends ReadwriteSplittingDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCreateReadwriteSplittingRule(final CreateReadwriteSplittingRuleContext ctx) {
        return new CreateReadwriteSplittingRuleStatement(null != ctx.ifNotExists(),
                ctx.readwriteSplittingRuleDefinition().stream().map(each -> (ReadwriteSplittingRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterReadwriteSplittingRule(final AlterReadwriteSplittingRuleContext ctx) {
        return new AlterReadwriteSplittingRuleStatement(ctx.readwriteSplittingRuleDefinition().stream().map(each -> (ReadwriteSplittingRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropReadwriteSplittingRule(final DropReadwriteSplittingRuleContext ctx) {
        return new DropReadwriteSplittingRuleStatement(null != ctx.ifExists(), ctx.ruleName().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterReadwriteSplittingStorageUnitStatus(final AlterReadwriteSplittingStorageUnitStatusContext ctx) {
        FromDatabaseSegment fromDatabase = null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName()));
        return new AlterReadwriteSplittingStorageUnitStatusStatement(fromDatabase, IdentifierValueUtils.getValue(ctx.ruleName()), IdentifierValueUtils.getValue(ctx.storageUnitName()),
                null == ctx.DISABLE());
    }
    
    @Override
    public ASTNode visitShowReadwriteSplittingRules(final ShowReadwriteSplittingRulesContext ctx) {
        return new ShowReadwriteSplittingRulesStatement(null == ctx.ruleName() ? null : IdentifierValueUtils.getValue(ctx.ruleName()),
                null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())));
    }
    
    @Override
    public ASTNode visitReadwriteSplittingRuleDefinition(final ReadwriteSplittingRuleDefinitionContext ctx) {
        return new ReadwriteSplittingRuleSegment(IdentifierValueUtils.getValue(ctx.ruleName()), IdentifierValueUtils.getValue(ctx.dataSourceDefinition().writeStorageUnit().writeStorageUnitName()),
                ctx.dataSourceDefinition().readStorageUnits().readStorageUnitsNames().storageUnitName().stream().map(IdentifierValueUtils::getValue).collect(Collectors.toList()),
                null == ctx.transactionalReadQueryStrategy() ? null : IdentifierValueUtils.getValue(ctx.transactionalReadQueryStrategy().transactionalReadQueryStrategyName()),
                null == ctx.algorithmDefinition() ? null : (AlgorithmSegment) visitAlgorithmDefinition(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(IdentifierValueUtils.getValue(ctx.algorithmTypeName()), getProperties(ctx.propertiesDefinition()));
    }
    
    @Override
    public ASTNode visitShowStatusFromReadwriteSplittingRules(final ShowStatusFromReadwriteSplittingRulesContext ctx) {
        FromDatabaseSegment fromDatabase = null == ctx.databaseName() ? null : new FromDatabaseSegment(ctx.FROM().get(1).getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName()));
        String ruleName = IdentifierValueUtils.getValue(ctx.ruleName());
        return new ShowStatusFromReadwriteSplittingRulesStatement(fromDatabase, ruleName);
    }
    
    @Override
    public ASTNode visitCountReadwriteSplittingRule(final CountReadwriteSplittingRuleContext ctx) {
        return new CountRuleStatement(null == ctx.databaseName()
                ? null
                : new FromDatabaseSegment(ctx.FROM().getSymbol().getStartIndex(), (DatabaseSegment) visit(ctx.databaseName())), "READWRITE_SPLITTING");
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
}
