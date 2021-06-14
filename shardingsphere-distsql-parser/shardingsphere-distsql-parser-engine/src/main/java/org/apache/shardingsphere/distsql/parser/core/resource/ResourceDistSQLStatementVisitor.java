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

package org.apache.shardingsphere.distsql.parser.core.resource;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AddResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlgorithmPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlterDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlterEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CheckScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DatabaseDiscoveryRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropEncryptRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.EncryptRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.FunctionDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ResetScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowDatabaseDiscoveryRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowEncryptRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowScalingJobListContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowScalingJobStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.StartScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.StopScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptColumnSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.CheckScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.DropScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ResetScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ShowScalingJobListStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ShowScalingJobStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.StartScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.StopScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowEncryptRulesStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for resource dist SQL.
 */
public final class ResourceDistSQLStatementVisitor extends DistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitAddResource(final AddResourceContext ctx) {
        return new AddResourceStatement(ctx.dataSource().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDataSource(final DataSourceContext ctx) {
        DataSourceSegment result = new DataSourceSegment();
        result.setName(ctx.dataSourceName().getText());
        result.setHostName(ctx.hostName().getText());
        result.setPort(ctx.port().getText());
        result.setDb(ctx.dbName().getText());
        result.setUser(ctx.user().getText());
        result.setPassword(null == ctx.password() ? "" : ctx.password().getText());
        return result;
    }
    
    @Override
    public ASTNode visitDropResource(final DropResourceContext ctx) {
        return new DropResourceStatement(ctx.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList()));
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
    public ASTNode visitCreateDatabaseDiscoveryRule(final CreateDatabaseDiscoveryRuleContext ctx) {
        return new CreateDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRuleDefinition().stream().map(each -> (DatabaseDiscoveryRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDatabaseDiscoveryRuleDefinition(final DatabaseDiscoveryRuleDefinitionContext ctx) {
        DatabaseDiscoveryRuleSegment result = new DatabaseDiscoveryRuleSegment();
        result.setName(ctx.ruleName().getText());
        result.setDataSources(ctx.resources().IDENTIFIER().stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.toList()));
        result.setDiscoveryTypeName(ctx.functionDefinition().functionName().getText());
        result.setProps(buildAlgorithmProperties(ctx.functionDefinition().algorithmProperties()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterDatabaseDiscoveryRule(final AlterDatabaseDiscoveryRuleContext ctx) {
        return new AlterDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRuleDefinition().stream().map(each -> (DatabaseDiscoveryRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropDatabaseDiscoveryRule(final DropDatabaseDiscoveryRuleContext ctx) {
        return new DropDatabaseDiscoveryRuleStatement(ctx.IDENTIFIER().stream().map(TerminalNode::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitCreateEncryptRule(final CreateEncryptRuleContext ctx) {
        return new CreateEncryptRuleStatement(ctx.encryptRuleDefinition().stream().map(each -> (EncryptRuleSegment) visit(each)).collect(Collectors.toList()));
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
        if (Objects.nonNull(ctx.plainColumnName())) {
            result.setPlainColumn(ctx.plainColumnName().getText());
        }
        result.setEncryptor((FunctionSegment) visit(ctx.functionDefinition()));
        return result;
    }
    
    @Override
    public ASTNode visitAlterEncryptRule(final AlterEncryptRuleContext ctx) {
        return new AlterEncryptRuleStatement(ctx.encryptRuleDefinition().stream().map(each -> (EncryptRuleSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDropEncryptRule(final DropEncryptRuleContext ctx) {
        return new DropEncryptRuleStatement(ctx.IDENTIFIER().stream().map(TerminalNode::getText).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
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
    public ASTNode visitShowScalingJobList(final ShowScalingJobListContext ctx) {
        return new ShowScalingJobListStatement();
    }
    
    @Override
    public ASTNode visitShowScalingJobStatus(final ShowScalingJobStatusContext ctx) {
        return new ShowScalingJobStatusStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitStartScalingJob(final StartScalingJobContext ctx) {
        return new StartScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitStopScalingJob(final StopScalingJobContext ctx) {
        return new StopScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitDropScalingJob(final DropScalingJobContext ctx) {
        return new DropScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitShowDatabaseDiscoveryRules(final ShowDatabaseDiscoveryRulesContext ctx) {
        return new ShowDatabaseDiscoveryRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitShowEncryptRules(final ShowEncryptRulesContext ctx) {
        return new ShowEncryptRulesStatement(Objects.nonNull(ctx.tableRule()) ? ctx.tableRule().tableName().getText() : null,
                Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }
    
    @Override
    public ASTNode visitResetScalingJob(final ResetScalingJobContext ctx) {
        return new ResetScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitCheckScalingJob(final CheckScalingJobContext ctx) {
        return new CheckScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    private Properties buildAlgorithmProperties(final AlgorithmPropertiesContext ctx) {
        Properties result = new Properties();
        for (AlgorithmPropertyContext each : ctx.algorithmProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
        return result;
    }
}
