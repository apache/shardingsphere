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

package org.apache.shardingsphere.sharding.route.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.DefaultRouteStageContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngineFactory;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidatorFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding route decorator.
 */
public final class ShardingRouteDecorator implements RouteDecorator<ShardingRule> {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RouteContext decorate(final RouteContext routeContext, final ShardingSphereMetaData metaData, final ShardingRule shardingRule, final ConfigurationProperties props) {
        SQLStatementContext<?> sqlStatementContext = routeContext.getSqlStatementContext();
        List<Object> parameters = routeContext.getParameters();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        Optional<ShardingStatementValidator> shardingStatementValidator = ShardingStatementValidatorFactory.newInstance(sqlStatement);
        shardingStatementValidator.ifPresent(validator -> validator.preValidate(shardingRule, routeContext, metaData));
        ShardingConditions shardingConditions = getShardingConditions(parameters, sqlStatementContext, metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData(), shardingRule);
        boolean needMergeShardingValues = isNeedMergeShardingValues(sqlStatementContext, shardingRule);
        if (sqlStatement instanceof DMLStatement && needMergeShardingValues) {
            mergeShardingConditions(shardingConditions);
        }
        ShardingRouteEngine shardingRouteEngine = ShardingRouteEngineFactory.newInstance(shardingRule, metaData, sqlStatementContext, shardingConditions, props);
        RouteResult routeResult = shardingRouteEngine.route(shardingRule);
        shardingStatementValidator.ifPresent(validator -> validator.postValidate(sqlStatement, routeResult));
        return new RouteContext(routeContext, routeResult, new DefaultRouteStageContext(), getTypeClass());
    }

    private ShardingConditions getShardingConditions(final List<Object> parameters, 
                                                     final SQLStatementContext<?> sqlStatementContext, final SchemaMetaData schemaMetaData, final ShardingRule shardingRule) {
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            if (sqlStatementContext instanceof InsertStatementContext) {
                return new ShardingConditions(new InsertClauseShardingConditionEngine(shardingRule, schemaMetaData).createShardingConditions((InsertStatementContext) sqlStatementContext, parameters));
            }
            return new ShardingConditions(new WhereClauseShardingConditionEngine(shardingRule, schemaMetaData).createShardingConditions(sqlStatementContext, parameters));
        }
        return new ShardingConditions(Collections.emptyList());
    }
    
    private boolean isNeedMergeShardingValues(final SQLStatementContext<?> sqlStatementContext, final ShardingRule shardingRule) {
        boolean selectContainsSubquery = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsSubquery();
        boolean insertSelectContainsSubquery = sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()
                && ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext().isContainsSubquery();
        return (selectContainsSubquery || insertSelectContainsSubquery) && !shardingRule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames()).isEmpty();
    }
    
    private void mergeShardingConditions(final ShardingConditions shardingConditions) {
        if (shardingConditions.getConditions().size() > 1) {
            ShardingCondition shardingCondition = shardingConditions.getConditions().remove(shardingConditions.getConditions().size() - 1);
            shardingConditions.getConditions().clear();
            shardingConditions.getConditions().add(shardingCondition);
        }
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}
