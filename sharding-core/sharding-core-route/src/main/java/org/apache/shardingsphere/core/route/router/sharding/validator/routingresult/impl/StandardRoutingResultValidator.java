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

package org.apache.shardingsphere.core.route.router.sharding.validator.routingresult.impl;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingConditionOptimizedStatement;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Standard routing result validator.
 *
 * @author sunbufu
 * @author zhangliang
 */
public final class StandardRoutingResultValidator extends AbstractRoutingResultValidator {
    
    public StandardRoutingResultValidator(final ShardingRule shardingRule, final ShardingSphereMetaData metaData) {
        super(shardingRule, metaData);
    }
    
    @Override
    protected void throwException(final ShardingOptimizedStatement shardingStatement, final ShardingConditions shardingConditions, final Multimap<RoutingUnit, TableUnit> unconfiguredRoutingUnits) {
        RoutingUnit routingUnit = unconfiguredRoutingUnits.keySet().iterator().next();
        Collection<String> absentDataNodes = Lists.newArrayListWithExpectedSize(unconfiguredRoutingUnits.get(routingUnit).size());
        ShardingStrategyConfiguration databaseStrategy = null;
        ShardingStrategyConfiguration tableStrategy = null;
        if (null != getShardingRule().getRuleConfiguration().getDefaultDatabaseShardingStrategyConfig()) {
            databaseStrategy = getShardingRule().getRuleConfiguration().getDefaultDatabaseShardingStrategyConfig();
        }
        if (null != getShardingRule().getRuleConfiguration().getDefaultTableShardingStrategyConfig()) {
            tableStrategy = getShardingRule().getRuleConfiguration().getDefaultTableShardingStrategyConfig();
        }
        for (TableUnit each : unconfiguredRoutingUnits.get(routingUnit)) {
            absentDataNodes.add(routingUnit.getDataSourceName() + "." + each.getActualTableName());
            Optional<TableRuleConfiguration> tableRuleConfiguration = getTableRuleConfiguration(getShardingRule().getRuleConfiguration().getTableRuleConfigs(), each.getLogicTableName());
            if (tableRuleConfiguration.isPresent()) {
                databaseStrategy = tableRuleConfiguration.get().getDatabaseShardingStrategyConfig();
                tableStrategy = tableRuleConfiguration.get().getTableShardingStrategyConfig();
            }
        }
        if (!absentDataNodes.isEmpty()) {
            StringBuilder detail = new StringBuilder();
            if (null != databaseStrategy) {
                detail.append("DatabaseStrategy=[").append(databaseStrategy).append("], ");
            }
            detail.append("TableStrategy=[").append(tableStrategy).append("], ");
            if (shardingStatement instanceof ShardingConditionOptimizedStatement) {
                detail.append("with ").append(getAllRouteValues(shardingConditions.getConditions())).append(", ");
            }
            throwExceptionForAbsentDataNode(absentDataNodes, detail);
        }
    }
    
    private void throwExceptionForAbsentDataNode(final Collection<String> absentDataNodes, final CharSequence detail) {
        String msg = "We get some absent DataNodes=" + absentDataNodes + " in routing result, " + (Strings.isNullOrEmpty(detail.toString()) ? "" : detail)
            + "please check the configuration of rule and data node.";
        throw new ShardingException(msg.replace("%", "%%"));
    }
    
    private Optional<TableRuleConfiguration> getTableRuleConfiguration(final Collection<TableRuleConfiguration> tableRuleConfigurations, final String tableName) {
        for (TableRuleConfiguration each : tableRuleConfigurations) {
            if (tableName.equals(each.getLogicTable())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private Collection<RouteValue> getAllRouteValues(final Collection<ShardingCondition> shardingConditions) {
        Collection<RouteValue> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions) {
            result.addAll(each.getRouteValues());
        }
        return result;
    }
}
