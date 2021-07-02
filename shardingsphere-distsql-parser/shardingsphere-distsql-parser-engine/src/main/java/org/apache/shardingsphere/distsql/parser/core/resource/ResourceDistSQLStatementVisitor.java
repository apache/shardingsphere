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
import org.apache.shardingsphere.distsql.parser.autogen.ResourceStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.ResourceStatementParser.AddResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ResourceStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ResourceStatementParser.DropResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.ResourceStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.ResourceStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ResourceStatementParser.ConnectionPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.ResourceStatementParser.ConnectionPropertyContext;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for resource dist SQL.
 */
public final class ResourceDistSQLStatementVisitor extends ResourceStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitAddResource(final AddResourceContext ctx) {
        return new AddResourceStatement(ctx.dataSource().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDataSource(final DataSourceContext ctx) {
        return new DataSourceSegment(
                ctx.dataSourceName().getText(), ctx.hostName().getText(), ctx.port().getText(), ctx.dbName().getText(), ctx.user().getText(), null == ctx.password() ? "" : ctx.password().getText(),
                null == ctx.connectionProperties() ? new Properties() : getConnectionProperties(ctx.connectionProperties()));
    }
    
    private Properties getConnectionProperties(final ConnectionPropertiesContext ctx) {
        Properties result = new Properties();
        for (ConnectionPropertyContext each : ctx.connectionProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
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
}
