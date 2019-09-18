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

package org.apache.shardingsphere.core.route.router.sharding;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ShardingStrategyConfiguration;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.ddl.ShardingDropIndexOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingConditionOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.route.type.RoutingEngine;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.route.type.standard.StandardRoutingEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Routing result validator.
 *
 * @author sunbufu
 */
public final class RoutingResultValidator implements RoutingResultChecker {
    
    private final ShardingRule shardingRule;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingOptimizedStatement shardingStatement;
    
    private final ShardingConditions shardingConditions;
    
    private boolean checkDataSource;
    
    private boolean checkTable;
    
    public RoutingResultValidator(final ShardingRule shardingRule,
                                  final ShardingSphereMetaData metaData, final ShardingOptimizedStatement shardingStatement, final ShardingConditions shardingConditions) {
        this.shardingRule = shardingRule;
        this.metaData = metaData;
        this.shardingStatement = shardingStatement;
        this.shardingConditions = shardingConditions;
        if (shardingRule.getRuleConfiguration().getMasterSlaveRuleConfigs().isEmpty()) {
            checkDataSource = true;
        }
        if (shardingStatement.getSQLStatement() instanceof DMLStatement) {
            checkTable = true;
        }
    }
    
    @Override
    public void check(final RoutingEngine routingEngine, final RoutingResult routingResult) {
        if (shardingStatement instanceof ShardingDropIndexOptimizedStatement) {
            return;
        }
        Multimap<RoutingUnit, TableUnit> absentRoutingUnitMap = getAbsentRoutingUnit(routingResult.getRoutingUnits());
        if (absentRoutingUnitMap.isEmpty()) {
            return;
        }
        if (routingEngine instanceof StandardRoutingEngine) {
            throwAbsentStandardRoutingResultException(shardingStatement, absentRoutingUnitMap);
        } else {
            throwOthersRoutingResultException(absentRoutingUnitMap);
        }
    }
    
    private Multimap<RoutingUnit, TableUnit> getAbsentRoutingUnit(final Collection<RoutingUnit> routingUnits) {
        Multimap<RoutingUnit, TableUnit> result = HashMultimap.create();
        for (RoutingUnit each : routingUnits) {
            String dataSourceName = each.getDataSourceName();
            result.putAll(each, getAbsentTableUnit(each, dataSourceName));
        }
        return result;
    }
    
    private Collection<TableUnit> getAbsentTableUnit(final RoutingUnit routingUnit, final String dataSourceName) {
        Collection<TableUnit> result = new LinkedList<>();
        for (TableUnit each : routingUnit.getTableUnits()) {
            if (containsInMetaData(dataSourceName, each.getActualTableName()) || containsInShardingRule(dataSourceName, each)) {
                continue;
            }
            result.add(each);
        }
        return result;
    }
    
    private boolean containsInShardingRule(final String dataSourceName, final TableUnit tableUnit) {
        Optional<TableRule> tableRule = shardingRule.findTableRule(tableUnit.getLogicTableName());
        return tableRule.isPresent() && tableRule.get().getActualTableNames(dataSourceName).contains(tableUnit.getActualTableName());
    }
    
    private boolean containsInMetaData(final String dataSourceName, final String actualTableName) {
        if (checkDataSource && (null == metaData.getDataSources() || null == metaData.getDataSources().getDataSourceMetaData(dataSourceName))) {
            return false;
        }
        if (checkTable && (null == metaData.getTables() || !metaData.getTables().containsTable(actualTableName))) {
            return false;
        }
        return true;
    }
    
    private void throwOthersRoutingResultException(final Multimap<RoutingUnit, TableUnit> absentRoutingUnitMap) {
        RoutingUnit routingUnit = absentRoutingUnitMap.keySet().iterator().next();
        Collection<String> absentDataNodes = Lists.newArrayListWithExpectedSize(absentRoutingUnitMap.get(routingUnit).size());
        for (TableUnit each : absentRoutingUnitMap.get(routingUnit)) {
            absentDataNodes.add(routingUnit.getDataSourceName() + "." + each.getActualTableName());
        }
        if (!absentDataNodes.isEmpty()) {
            throwExceptionForAbsentDataNode(absentDataNodes, "");
        }
    }
    
    private void throwAbsentStandardRoutingResultException(final ShardingOptimizedStatement shardingStatement, final Multimap<RoutingUnit, TableUnit> absentRoutingUnitMap) {
        RoutingUnit routingUnit = absentRoutingUnitMap.keySet().iterator().next();
        Collection<String> absentDataNodes = Lists.newArrayListWithExpectedSize(absentRoutingUnitMap.get(routingUnit).size());
        ShardingStrategyConfiguration databaseStrategy = null;
        ShardingStrategyConfiguration tableStrategy = null;
        if (null != shardingRule.getRuleConfiguration().getDefaultDatabaseShardingStrategyConfig()) {
            databaseStrategy = shardingRule.getRuleConfiguration().getDefaultDatabaseShardingStrategyConfig();
        }
        if (null != shardingRule.getRuleConfiguration().getDefaultTableShardingStrategyConfig()) {
            tableStrategy = shardingRule.getRuleConfiguration().getDefaultTableShardingStrategyConfig();
        }
        for (TableUnit each : absentRoutingUnitMap.get(routingUnit)) {
            absentDataNodes.add(routingUnit.getDataSourceName() + "." + each.getActualTableName());
            Optional<TableRuleConfiguration> tableRuleConfiguration = getTableRuleConfiguration(shardingRule.getRuleConfiguration().getTableRuleConfigs(), each.getLogicTableName());
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
