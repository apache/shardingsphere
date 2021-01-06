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

import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AddResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlgorithmPropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlterReplicaQueryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlterReplicaQueryRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateReplicaQueryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateShardingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DataSourceDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropReplicaQueryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropShardingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ReplicaQueryRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShardingTableRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ReplicaQueryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AlterReplicaQueryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReplicaQueryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropReplicaQueryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.props.PropertiesValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Dist SQL visitor.
 */
public final class DistSQLVisitor extends DistSQLStatementBaseVisitor<ASTNode> {
    
    @Override
    public ASTNode visitAddResource(final AddResourceContext ctx) {
        Collection<DataSourceSegment> connectionInfos = new LinkedList<>();
        for (DataSourceContext each : ctx.dataSource()) {
            connectionInfos.add((DataSourceSegment) visit(each));
        }
        return new AddResourceStatement(connectionInfos);
    }
    
    @Override
    public ASTNode visitDataSource(final DataSourceContext ctx) {
        DataSourceSegment result = (DataSourceSegment) visit(ctx.dataSourceDefinition());
        result.setName(ctx.dataSourceName().getText());
        return result;
    }
    
    @Override
    public ASTNode visitDataSourceDefinition(final DataSourceDefinitionContext ctx) {
        DataSourceSegment result = new DataSourceSegment();
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
    public ASTNode visitCreateReplicaQueryRule(final CreateReplicaQueryRuleContext ctx) {
        Collection<ReplicaQueryRuleSegment> replicaQueryRules = new LinkedList<>();
        for (ReplicaQueryRuleDefinitionContext each : ctx.replicaQueryRuleDefinition()) {
            replicaQueryRules.add((ReplicaQueryRuleSegment) visit(each));
        }
        return new CreateReplicaQueryRuleStatement(replicaQueryRules);
    }

    @Override
    public ASTNode visitReplicaQueryRuleDefinition(final ReplicaQueryRuleDefinitionContext ctx) {
        ReplicaQueryRuleSegment result = new ReplicaQueryRuleSegment();
        Collection<String> replicaDatasources = new LinkedList<>();
        for (SchemaNameContext each : ctx.schemaNames().schemaName()) {
            replicaDatasources.add(each.getText());
        }
        Properties props = new Properties();
        for (AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
            props.setProperty(each.key.getText(), each.value.getText());
        }
        result.setName(ctx.ruleName.getText());
        result.setPrimaryDatasource(ctx.primary.getText());
        result.setReplicaDatasources(replicaDatasources);
        result.setLoadBalancer(ctx.loadBalancer.getText());
        result.setProps(props);
        return result;
    }

    @Override
    public ASTNode visitAlterReplicaQueryRule(final AlterReplicaQueryRuleContext ctx) {
        Collection<ReplicaQueryRuleSegment> replicaQueryRules = new LinkedList<>();
        for (AlterReplicaQueryRuleDefinitionContext each : ctx.alterReplicaQueryRuleDefinition()) {
            replicaQueryRules.add((ReplicaQueryRuleSegment) visit(each));
        }
        return new AlterReplicaQueryRuleStatement(replicaQueryRules);
    }

    @Override
    public ASTNode visitDropReplicaQueryRule(final DropReplicaQueryRuleContext ctx) {
        DropReplicaQueryRuleStatement result = new DropReplicaQueryRuleStatement();
        for (TerminalNode each : ctx.IDENTIFIER()) {
            result.getRuleNames().add(each.getText());
        }
        return result;
    }

    @Override
    public ASTNode visitAlterReplicaQueryRuleDefinition(final AlterReplicaQueryRuleDefinitionContext ctx) {
        ReplicaQueryRuleSegment result = new ReplicaQueryRuleSegment();
        Collection<String> replicaDatasources = new LinkedList<>();
        for (SchemaNameContext each : ctx.schemaNames().schemaName()) {
            replicaDatasources.add(each.getText());
        }
        result.setName(ctx.ruleName.getText());
        result.setPrimaryDatasource(ctx.primary.getText());
        result.setReplicaDatasources(replicaDatasources);
        if (null != ctx.loadBalancer) {
            Properties props = new Properties();
            for (AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
                props.setProperty(each.key.getText(), each.value.getText());
            }
            result.setLoadBalancer(ctx.loadBalancer.getText());
            result.setProps(props);
        }
        return result;
    }

    @Override
    public ASTNode visitShardingTableRuleDefinition(final ShardingTableRuleDefinitionContext ctx) {
        TableRuleSegment result = new TableRuleSegment();
        result.setLogicTable(ctx.tableName().getText());
        result.setShardingColumn(ctx.columnName().getText());
        result.setAlgorithmType(ctx.shardingAlgorithmType.getText());
        // TODO Future feature.
        result.setDataSources(new LinkedList<>());
        PropertiesValue propertiesValue = (PropertiesValue) visit(ctx.algorithmProperties());
        result.setAlgorithmProps(propertiesValue.getValue());
        return result;
    }
    
    @Override
    public ASTNode visitAlgorithmProperties(final AlgorithmPropertiesContext ctx) {
        PropertiesValue result = new PropertiesValue();
        for (AlgorithmPropertyContext each : ctx.algorithmProperty()) {
            result.getValue().setProperty(each.key.getText(), each.value.getText());
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
    public ASTNode visitShowResources(final ShowResourcesContext ctx) {
        return new ShowResourcesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
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
