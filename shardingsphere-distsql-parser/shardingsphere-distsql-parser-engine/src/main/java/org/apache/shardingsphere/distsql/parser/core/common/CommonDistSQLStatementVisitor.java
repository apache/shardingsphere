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
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ClearHintContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.DropResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.PoolPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.PoolPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.CommonDistSQLStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.hint.ClearHintStatement;
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
 * SQL statement visitor for common dist SQL.
 */
public final class CommonDistSQLStatementVisitor extends CommonDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitAddResource(final AddResourceContext ctx) {
        return new AddResourceStatement(ctx.dataSource().stream().map(each -> (DataSourceSegment) visit(each)).collect(Collectors.toList()));
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
        return new DataSourceSegment(ctx.dataSourceName().getText(), url, hostName, port, dbName,
                ctx.user().getText(), null == ctx.password() ? "" : ctx.password().getText(),
                null == ctx.poolProperties() ? new Properties() : getPoolProperties(ctx.poolProperties()));
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
    public ASTNode visitClearHint(final ClearHintContext ctx) {
        return new ClearHintStatement();
    }
}
