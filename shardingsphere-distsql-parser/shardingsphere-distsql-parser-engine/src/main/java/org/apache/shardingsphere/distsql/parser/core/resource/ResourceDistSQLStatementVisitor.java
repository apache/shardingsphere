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
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AddResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CheckScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ResetScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowScalingJobListContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowScalingJobStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.StartScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.StopScalingJobContext;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.CheckScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.DropScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ResetScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ShowScalingJobListStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ShowScalingJobStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.StartScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.StopScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

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
    public ASTNode visitResetScalingJob(final ResetScalingJobContext ctx) {
        return new ResetScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitCheckScalingJob(final CheckScalingJobContext ctx) {
        return new CheckScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
}
