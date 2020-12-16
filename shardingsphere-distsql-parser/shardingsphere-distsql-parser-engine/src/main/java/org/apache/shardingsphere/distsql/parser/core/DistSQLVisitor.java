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
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropShardingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShardingAlgorithmPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShardingAlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShardingTableRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowDataSourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DataSourceConnectionSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.show.impl.ShowDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.show.impl.ShowRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.props.PropertiesValue;

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
        for (ShardingTableRuleDefinitionContext each : ctx.shardingTableRuleDefinition()) {
            tables.add((TableRuleSegment) visit(each));
        }
        return new CreateShardingRuleStatement(tables);
    }
    
    @Override
    public ASTNode visitShardingTableRuleDefinition(final ShardingTableRuleDefinitionContext ctx) {
        TableRuleSegment result = new TableRuleSegment();
        result.setLogicTable(ctx.tableName().getText());
        result.setShardingColumn(ctx.columName().getText());
        result.setAlgorithmType(ctx.shardingAlgorithmDefinition().shardingAlgorithmType().getText());
        // TODO Future feature.
        result.setDataSources(new LinkedList<>());
        PropertiesValue propertiesValue = (PropertiesValue) visit(ctx.shardingAlgorithmDefinition().shardingAlgorithmProperties());
        result.setAlgorithmProps(propertiesValue.getValue());
        return result;
    }
    
    @Override
    public ASTNode visitShardingAlgorithmProperties(final ShardingAlgorithmPropertiesContext ctx) {
        PropertiesValue result = new PropertiesValue();
        for (ShardingAlgorithmPropertyContext each : ctx.shardingAlgorithmProperty()) {
            result.getValue().setProperty(each.shardingAlgorithmPropertyKey().getText(), each.shardingAlgorithmPropertyValue().getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropShardingRule(final DropShardingRuleContext ctx) {
        DropShardingRuleStatement result = new DropShardingRuleStatement();
        for (TableNameContext each : ctx.tableName()) {
            result.getTableNames().add((TableNameSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
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
