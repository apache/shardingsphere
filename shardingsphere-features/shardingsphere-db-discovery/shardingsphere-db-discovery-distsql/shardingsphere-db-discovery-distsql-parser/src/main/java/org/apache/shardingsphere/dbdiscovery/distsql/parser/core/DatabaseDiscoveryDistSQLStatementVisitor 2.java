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

import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.AlgorithmPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.AlterDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.CreateDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DatabaseDiscoveryRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DropDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.RuleNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.ShowDatabaseDiscoveryRulesContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for database discovery dist SQL.
 */
public final class DatabaseDiscoveryDistSQLStatementVisitor extends DatabaseDiscoveryDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateDatabaseDiscoveryRule(final CreateDatabaseDiscoveryRuleContext ctx) {
        return new CreateDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRuleDefinition().stream().map(each -> (DatabaseDiscoveryRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterDatabaseDiscoveryRule(final AlterDatabaseDiscoveryRuleContext ctx) {
        return new AlterDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRuleDefinition().stream().map(each -> (DatabaseDiscoveryRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropDatabaseDiscoveryRule(final DropDatabaseDiscoveryRuleContext ctx) {
        return new DropDatabaseDiscoveryRuleStatement(ctx.ruleName().stream().map(RuleNameContext::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowDatabaseDiscoveryRules(final ShowDatabaseDiscoveryRulesContext ctx) {
        return new ShowDatabaseDiscoveryRulesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitDatabaseDiscoveryRuleDefinition(final DatabaseDiscoveryRuleDefinitionContext ctx) {
        Collection<String> dataSources = ctx.resources().resourceName().stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.toList());
        return new DatabaseDiscoveryRuleSegment(ctx.ruleName().getText(), 
                dataSources, ctx.algorithmDefinition().algorithmName().getText(), getAlgorithmProperties(ctx.algorithmDefinition().algorithmProperties()));
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(ctx.algorithmName().getText(), null == ctx.algorithmProperties() ? new Properties() : getAlgorithmProperties(ctx.algorithmProperties()));
    }
    
    private Properties getAlgorithmProperties(final AlgorithmPropertiesContext ctx) {
        Properties result = new Properties();
        for (AlgorithmPropertyContext each : ctx.algorithmProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
        return result;
    }
}
