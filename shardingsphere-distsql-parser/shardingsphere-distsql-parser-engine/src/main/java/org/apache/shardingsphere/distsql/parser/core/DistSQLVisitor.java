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

package org.apache.shardingsphere.distsql.parser.core;

import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateDataSourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateShardingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DataSourceDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowDataSourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.StrategyPropContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.StrategyPropsContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.TableRuleContext;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DataSourceConnectionSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.show.impl.ShowDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.show.impl.ShowRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Dist SQL visitor.
 */
public final class DistSQLVisitor extends DistSQLStatementBaseVisitor<ASTNode> {
    
    @Override
    public ASTNode visitCreateDataSources(final CreateDataSourcesContext ctx) {
        Collection<DataSourceConnectionSegment> connectionInfos = new LinkedList<>();
        for (DataSourceContext each : ctx.dataSource()) {
            connectionInfos.add((DataSourceConnectionSegment) visit(each));
        }
        return new CreateDataSourcesStatement(connectionInfos);
    }
    
    @Override
    public ASTNode visitDataSource(final DataSourceContext ctx) {
        DataSourceConnectionSegment result = (DataSourceConnectionSegment) visit(ctx.dataSourceDefinition());
        result.setName(ctx.dataSourceName().getText());
        return result;
    }
    
    @Override
    public ASTNode visitDataSourceDefinition(final DataSourceDefinitionContext ctx) {
        DataSourceConnectionSegment result = new DataSourceConnectionSegment();
        result.setHostName(ctx.hostName().getText());
        result.setPort(ctx.port().getText());
        result.setDb(ctx.dbName().getText());
        result.setUser(null == ctx.user() ? "" : ctx.user().getText());
        result.setPassword(null == ctx.password() ? "" : ctx.password().getText());
        return result;
    }
    
    @Override
    public ASTNode visitCreateShardingRule(final CreateShardingRuleContext ctx) {
        Collection<TableRuleSegment> tables = new LinkedList<>();
        for (TableRuleContext each : ctx.tableRule()) {
            tables.add((TableRuleSegment) visit(each));
        }
        return new CreateShardingRuleStatement(tables);
    }
    
    @Override
    public ASTNode visitTableRule(final TableRuleContext ctx) {
        TableRuleSegment result = new TableRuleSegment();
        result.setLogicTable(ctx.tableName().getText());
        result.setAlgorithmType(ctx.tableRuleDefinition().strategyType().getText());
        result.setShardingColumn(ctx.tableRuleDefinition().strategyDefinition().columName().getText());
        // TODO Future feature.
        result.setDataSources(new LinkedList<>());
        CollectionValue<String> props = (CollectionValue) visit(ctx.tableRuleDefinition().strategyDefinition().strategyProps());
        result.setProperties(props.getValue());
        return result;
    }
    
    @Override
    public ASTNode visitStrategyProps(final StrategyPropsContext ctx) {
        CollectionValue<String> result = new CollectionValue<>();
        for (StrategyPropContext each : ctx.strategyProp()) {
            result.getValue().add(each.getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowDataSources(final ShowDataSourcesContext ctx) {
        return new ShowDataSourcesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShowRule(final ShowRuleContext ctx) {
        return new ShowRuleStatement(ctx.ruleType().getText(), null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
}
