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

package org.apache.shardingsphere.single.route;

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.lifecycle.DecorateSQLRouter;
import org.apache.shardingsphere.infra.route.lifecycle.EntranceSQLRouter;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.route.engine.SingleRouteEngine;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Single SQL router.
 */
@HighFrequencyInvocation
public final class SingleSQLRouter implements EntranceSQLRouter<SingleRule>, DecorateSQLRouter<SingleRule> {
    
    @Override
    public RouteContext createRouteContext(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database,
                                           final SingleRule rule, final Collection<String> tableNames, final ConfigurationProperties props) {
        if (1 == database.getResourceMetaData().getStorageUnits().size()) {
            return createSingleDataSourceRouteContext(rule, database, queryContext);
        }
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        RouteContext routeContext = new RouteContext();
        Collection<QualifiedTable> singleTables = getSingleTables(database, rule, sqlStatementContext);
        if (singleTables.isEmpty()) {
            return routeContext;
        }
        return new SingleRouteEngine(singleTables, sqlStatementContext.getSqlStatement(), queryContext.getHintValueContext()).route(routeContext, rule);
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database,
                                     final SingleRule rule, final Collection<String> tableNames, final ConfigurationProperties props) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        Collection<QualifiedTable> singleTables = getSingleTables(database, rule, sqlStatementContext);
        if (singleTables.isEmpty()) {
            return;
        }
        new SingleRouteEngine(singleTables, sqlStatementContext.getSqlStatement(), queryContext.getHintValueContext()).route(routeContext, rule);
    }
    
    private RouteContext createSingleDataSourceRouteContext(final SingleRule rule, final ShardingSphereDatabase database, final QueryContext queryContext) {
        String logicDataSource = rule.getDataSourceNames().iterator().next();
        String actualDataSource = database.getResourceMetaData().getStorageUnits().keySet().iterator().next();
        RouteContext result = new RouteContext();
        Collection<String> tableNames = queryContext.getSqlStatementContext().getTablesContext().getTableNames();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(logicDataSource, actualDataSource), createTableMappers(tableNames)));
        return result;
    }
    
    private Collection<RouteMapper> createTableMappers(final Collection<String> tableNames) {
        Collection<RouteMapper> result = new LinkedList<>();
        for (String each : tableNames) {
            result.add(new RouteMapper(each, each));
        }
        return result;
    }
    
    private Collection<QualifiedTable> getSingleTables(final ShardingSphereDatabase database, final SingleRule rule, final SQLStatementContext sqlStatementContext) {
        Collection<QualifiedTable> qualifiedTables = rule.getQualifiedTables(sqlStatementContext, database);
        Collection<String> distributedTableNames = getDistributedTableNames(database);
        Collection<QualifiedTable> result = new LinkedList<>();
        for (QualifiedTable each : qualifiedTables) {
            if (!distributedTableNames.contains(each.getTableName())) {
                result.add(each);
            }
        }
        return sqlStatementContext.getSqlStatement() instanceof CreateTableStatement ? result : rule.getSingleTables(result);
    }
    
    private Collection<String> getDistributedTableNames(final ShardingSphereDatabase database) {
        Collection<String> result = new CaseInsensitiveSet<>();
        for (TableMapperRuleAttribute each : database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class)) {
            result.addAll(each.getDistributedTableNames());
        }
        return result;
    }
    
    @Override
    public Type getType() {
        return Type.DATA_NODE;
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
    
    @Override
    public Class<SingleRule> getTypeClass() {
        return SingleRule.class;
    }
}
