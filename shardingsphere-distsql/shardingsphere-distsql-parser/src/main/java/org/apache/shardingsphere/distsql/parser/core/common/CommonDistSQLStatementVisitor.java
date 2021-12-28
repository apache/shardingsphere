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

package org.apache.shardingsphere.distsql.parser.core.common;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.AddResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.AlterDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.AlterResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ClearHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.CreateDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.DisableInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.DropDefaultSingleTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.DropResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.EnableInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.InstanceDefinationContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.InstanceIdContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.PasswordContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.PoolPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.PoolPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.RefreshTableMetadataContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.SetVariableContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowAllVariablesContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowAuthorityRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowInstanceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowSQLParserRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowSingleTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowSingleTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowTransactionRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowVariableContext;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.hint.ClearHintStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAllVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AlterDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowTransactionRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for common dist SQL.
 */
public final class CommonDistSQLStatementVisitor extends CommonDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitAddResource(final AddResourceContext ctx) {
        return new AddResourceStatement(ctx.dataSource().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterResource(final AlterResourceContext ctx) {
        return new AlterResourceStatement(ctx.dataSource().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDataSource(final DataSourceContext ctx) {
        String url = null;
        String hostName = null;
        String port = null;
        String dbName = null;
        if (null != ctx.urlSource()) {
            url = new IdentifierValue(ctx.urlSource().url().getText()).getValue();
        }
        if (null != ctx.simpleSource()) {
            hostName = ctx.simpleSource().hostName().getText();
            port = ctx.simpleSource().port().getText();
            dbName = ctx.simpleSource().dbName().getText();
        }
        return new DataSourceSegment(getIdentifierValue(ctx.dataSourceName()), url, hostName, port, dbName,
                ctx.user().getText(), null == ctx.password() ? "" : getPassword(ctx.password()),
                null == ctx.poolProperties() ? new Properties() : getPoolProperties(ctx.poolProperties()));
    }
    
    private String getPassword(final List<PasswordContext> passwordContexts) {
        return passwordContexts.stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.joining(""));
    }
    
    @Override
    public ASTNode visitShowInstance(final ShowInstanceContext ctx) {
        return new ShowInstanceStatement();
    }
    
    @Override
    public ASTNode visitEnableInstance(final EnableInstanceContext ctx) {
        return buildSetInstanceStatusStatement(ctx.ENABLE().getText().toUpperCase(), ctx.instanceDefination(), ctx.instanceId());
    }
    
    @Override
    public ASTNode visitDisableInstance(final DisableInstanceContext ctx) {
        return buildSetInstanceStatusStatement(ctx.DISABLE().getText().toUpperCase(), ctx.instanceDefination(), ctx.instanceId());
    }
    
    private SetInstanceStatusStatement buildSetInstanceStatusStatement(final String status, final InstanceDefinationContext instanceDefinationContext, final InstanceIdContext instanceIdContext) {
        String ip;
        String port;
        if (null != instanceDefinationContext) {
            ip = getIdentifierValue(instanceDefinationContext.ip());
            port = getIdentifierValue(instanceDefinationContext.port());
        } else {
            ip = getIdentifierValue(instanceIdContext.ip());
            port = getIdentifierValue(instanceIdContext.port());
        }
        return new SetInstanceStatusStatement(status, ip, port);
    }
    
    @Override
    public ASTNode visitCreateDefaultSingleTableRule(final CreateDefaultSingleTableRuleContext ctx) {
        return new CreateDefaultSingleTableRuleStatement(getIdentifierValue(ctx.dataSourceName()));
    }
    
    @Override
    public ASTNode visitAlterDefaultSingleTableRule(final AlterDefaultSingleTableRuleContext ctx) {
        return new AlterDefaultSingleTableRuleStatement(getIdentifierValue(ctx.dataSourceName()));
    }
    
    @Override
    public ASTNode visitDropDefaultSingleTableRule(final DropDefaultSingleTableRuleContext ctx) {
        return new DropDefaultSingleTableRuleStatement();
    }
    
    private Properties getPoolProperties(final PoolPropertiesContext ctx) {
        Properties result = new Properties();
        for (PoolPropertyContext each : ctx.poolProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropResource(final DropResourceContext ctx) {
        boolean ignoreSingleTables = null != ctx.ignoreSingleTables();
        return new DropResourceStatement(ctx.IDENTIFIER().stream().map(ParseTree::getText).map(each -> new IdentifierValue(each).getValue()).collect(Collectors.toList()), ignoreSingleTables);
    }
    
    @Override
    public ASTNode visitShowResources(final ShowResourcesContext ctx) {
        return new ShowResourcesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        return new SetVariableStatement(getIdentifierValue(ctx.variableName()), getIdentifierValue(ctx.variableValue()));
    }
    
    @Override
    public ASTNode visitShowSingleTableRules(final ShowSingleTableRulesContext ctx) {
        return new ShowSingleTableRulesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShowSingleTable(final ShowSingleTableContext ctx) {
        return new ShowSingleTableStatement(null == ctx.table() ? null : getIdentifierValue(ctx.table().tableName()),
                null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShowSQLParserRule(final ShowSQLParserRuleContext ctx) {
        return new ShowSQLParserRuleStatement();
    }
    
    @Override
    public ASTNode visitShowAuthorityRule(final ShowAuthorityRuleContext ctx) {
        return new ShowAuthorityRuleStatement();
    }
    
    @Override
    public ASTNode visitShowVariable(final ShowVariableContext ctx) {
        return new ShowVariableStatement(getIdentifierValue(ctx.variableName()).toUpperCase());
    }
    
    @Override
    public ASTNode visitShowAllVariables(final ShowAllVariablesContext ctx) {
        return new ShowAllVariablesStatement();
    }
    
    private String getIdentifierValue(final ParseTree context) {
        if (null == context) {
            return null;
        }
        return new IdentifierValue(context.getText()).getValue();
    }
    
    @Override
    public ASTNode visitClearHint(final ClearHintContext ctx) {
        return new ClearHintStatement();
    }
    
    @Override
    public ASTNode visitRefreshTableMetadata(final RefreshTableMetadataContext ctx) {
        RefreshTableMetadataStatement result = new RefreshTableMetadataStatement();
        result.setTableName(null == ctx.refreshScope() ? Optional.empty() : Optional.ofNullable(getIdentifierValue(ctx.refreshScope().tableName())));
        result.setResourceName(null == ctx.refreshScope() ? Optional.empty() : Optional.ofNullable(getIdentifierValue(ctx.refreshScope().resourceName())));
        return result;
    }
    
    @Override
    public ASTNode visitShowTransactionRule(final ShowTransactionRuleContext ctx) {
        return new ShowTransactionRuleStatement(null);
    }
}
