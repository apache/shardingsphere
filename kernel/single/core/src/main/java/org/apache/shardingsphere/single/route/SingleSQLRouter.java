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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.validator.ShardingSphereMetaDataValidateUtils;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.route.engine.SingleRouteEngineFactory;
import org.apache.shardingsphere.single.route.validator.SingleMetaDataValidatorFactory;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Single SQL router.
 */
public final class SingleSQLRouter implements SQLRouter<SingleRule> {
    
    @Override
    public RouteContext createRouteContext(final QueryContext queryContext, final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final SingleRule rule,
                                           final ConfigurationProperties props, final ConnectionContext connectionContext) {
        if (1 == database.getResourceMetaData().getDataSources().size()) {
            return createSingleDataSourceRouteContext(rule, database);
        }
        RouteContext result = new RouteContext();
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SingleMetaDataValidatorFactory.newInstance(sqlStatementContext.getSqlStatement()).ifPresent(optional -> optional.validate(rule, sqlStatementContext, database));
        Collection<QualifiedTable> singleTables = getSingleTables(database, rule, result, sqlStatementContext);
        validateSingleTableMetaData(database, sqlStatementContext, singleTables);
        SingleRouteEngineFactory.newInstance(singleTables, sqlStatementContext.getSqlStatement()).ifPresent(optional -> optional.route(result, rule));
        return result;
    }
    
    private void validateSingleTableMetaData(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext, final Collection<QualifiedTable> singleTables) {
        // TODO move single table metadata validate logic to infra validator
        if (!singleTables.isEmpty() && sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            ShardingSphereMetaDataValidateUtils.validateTableExist(sqlStatementContext, database);
        }
    }
    
    private Collection<QualifiedTable> getSingleTables(final ShardingSphereDatabase database, final SingleRule rule, final RouteContext result, final SQLStatementContext sqlStatementContext) {
        Collection<QualifiedTable> qualifiedTables = rule.getQualifiedTables(sqlStatementContext, database);
        return result.getRouteUnits().isEmpty() && sqlStatementContext.getSqlStatement() instanceof CreateTableStatement ? qualifiedTables : rule.getSingleTables(qualifiedTables);
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database,
                                     final SingleRule rule, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        Collection<QualifiedTable> singleTables = getSingleTables(database, rule, routeContext, sqlStatementContext);
        validateSingleTableMetaData(database, sqlStatementContext, singleTables);
        SingleRouteEngineFactory.newInstance(singleTables, sqlStatementContext.getSqlStatement()).ifPresent(optional -> optional.route(routeContext, rule));
    }
    
    private RouteContext createSingleDataSourceRouteContext(final SingleRule rule, final ShardingSphereDatabase database) {
        String logicDataSource = rule.getDataSourceNames().iterator().next();
        String actualDataSource = database.getResourceMetaData().getDataSources().keySet().iterator().next();
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(logicDataSource, actualDataSource), Collections.emptyList()));
        return result;
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
