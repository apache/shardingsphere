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

package org.apache.shardingsphere.dbdiscovery.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.AbstractDatabaseDiscoverySegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CountDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryHeartbeatStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryHeartbeatsStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryTypesStatement;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.AlterDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.CreateDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DropDatabaseDiscoveryHeartbeatContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DropDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DropDatabaseDiscoveryTypeContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.ShowDatabaseDiscoveryHeartbeatsContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.ShowDatabaseDiscoveryRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.ShowDatabaseDiscoveryTypesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.StorageUnitsContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for database discovery DistSQL.
 */
public final class DatabaseDiscoveryDistSQLStatementVisitor extends DatabaseDiscoveryDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateDatabaseDiscoveryRule(final CreateDatabaseDiscoveryRuleContext ctx) {
        return new CreateDatabaseDiscoveryRuleStatement(null != ctx.ifNotExists(),
                ctx.databaseDiscoveryRule().stream().map(each -> (AbstractDatabaseDiscoverySegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterDatabaseDiscoveryRule(final AlterDatabaseDiscoveryRuleContext ctx) {
        return new AlterDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRule().stream().map(each -> (AbstractDatabaseDiscoverySegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDatabaseDiscoveryRule(final DatabaseDiscoveryRuleContext ctx) {
        return new DatabaseDiscoveryDefinitionSegment(getIdentifierValue(ctx.ruleName()), buildResources(ctx.storageUnits()), (AlgorithmSegment) visit(ctx.algorithmDefinition()),
                getProperties(ctx.discoveryHeartbeat().propertiesDefinition()));
    }
    
    private List<String> buildResources(final StorageUnitsContext ctx) {
        return ctx.storageUnitName().stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.toList());
    }
    
    @Override
    public ASTNode visitDropDatabaseDiscoveryRule(final DropDatabaseDiscoveryRuleContext ctx) {
        return new DropDatabaseDiscoveryRuleStatement(null != ctx.ifExists(), ctx.ruleName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowDatabaseDiscoveryRules(final ShowDatabaseDiscoveryRulesContext ctx) {
        return new ShowDatabaseDiscoveryRulesStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), getProperties(ctx.propertiesDefinition()));
    }
    
    @Override
    public ASTNode visitShowDatabaseDiscoveryTypes(final ShowDatabaseDiscoveryTypesContext ctx) {
        return new ShowDatabaseDiscoveryTypesStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    @Override
    public ASTNode visitShowDatabaseDiscoveryHeartbeats(final ShowDatabaseDiscoveryHeartbeatsContext ctx) {
        return new ShowDatabaseDiscoveryHeartbeatsStatement(null == ctx.databaseName() ? null : (DatabaseSegment) visit(ctx.databaseName()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitDropDatabaseDiscoveryType(final DropDatabaseDiscoveryTypeContext ctx) {
        return new DropDatabaseDiscoveryTypeStatement(null != ctx.ifExists(), ctx.discoveryTypeName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropDatabaseDiscoveryHeartbeat(final DropDatabaseDiscoveryHeartbeatContext ctx) {
        return new DropDatabaseDiscoveryHeartbeatStatement(null != ctx.ifExists(), ctx.discoveryHeartbeatName().stream().map(this::getIdentifierValue).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitCountDatabaseDiscoveryRule(final DatabaseDiscoveryDistSQLStatementParser.CountDatabaseDiscoveryRuleContext ctx) {
        return new CountDatabaseDiscoveryRuleStatement(Objects.nonNull(ctx.databaseName()) ? (DatabaseSegment) visit(ctx.databaseName()) : null);
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
}
