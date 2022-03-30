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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngineFactory;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngineFactory;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidatorFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sharding SQL router.
 */
public final class ShardingSQLRouter implements SQLRouter<ShardingRule> {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShardingRule rule, final ConfigurationProperties props) {
        SQLStatement sqlStatement = logicSQL.getSqlStatementContext().getSqlStatement();
        ShardingConditions shardingConditions = createShardingConditions(logicSQL, metaData, rule);
        Optional<ShardingStatementValidator> validator = ShardingStatementValidatorFactory.newInstance(sqlStatement, shardingConditions);
        validator.ifPresent(v -> v.preValidate(rule, logicSQL.getSqlStatementContext(), logicSQL.getParameters(), metaData.getDefaultSchema()));
        if (sqlStatement instanceof DMLStatement && shardingConditions.isNeedMerge()) {
            shardingConditions.merge();
        }
        RouteContext result = ShardingRouteEngineFactory.newInstance(rule, metaData, logicSQL.getSqlStatementContext(), shardingConditions, props).route(rule);
        validator.ifPresent(v -> v.postValidate(rule, logicSQL.getSqlStatementContext(), logicSQL.getParameters(), metaData.getDefaultSchema(), props, result));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private ShardingConditions createShardingConditions(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShardingRule rule) {
        List<ShardingCondition> shardingConditions;
        if (logicSQL.getSqlStatementContext().getSqlStatement() instanceof DMLStatement) {
            ShardingConditionEngine shardingConditionEngine = ShardingConditionEngineFactory.createShardingConditionEngine(logicSQL, metaData, rule);
            shardingConditions = shardingConditionEngine.createShardingConditions(logicSQL.getSqlStatementContext(), logicSQL.getParameters());
        } else {
            shardingConditions = Collections.emptyList();
        }
        return new ShardingConditions(shardingConditions, logicSQL.getSqlStatementContext(), rule);
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final LogicSQL logicSQL, final ShardingSphereMetaData metaData, 
                                     final ShardingRule rule, final ConfigurationProperties props) {
        // TODO
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
