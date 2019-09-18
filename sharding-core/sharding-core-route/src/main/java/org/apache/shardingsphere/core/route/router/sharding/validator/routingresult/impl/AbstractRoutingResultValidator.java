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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.ddl.ShardingDropIndexOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.router.sharding.validator.routingresult.RoutingResultValidator;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Abstract routing result validator.
 *
 * @author sunbufu
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractRoutingResultValidator implements RoutingResultValidator {
    
    @Getter
    private final ShardingRule shardingRule;
    
    private final ShardingSphereMetaData metaData;
    
    @Override
    public final void validate(final ShardingOptimizedStatement shardingStatement, final ShardingConditions shardingConditions, final RoutingResult routingResult) {
        if (shardingStatement instanceof ShardingDropIndexOptimizedStatement) {
            return;
        }
        Multimap<RoutingUnit, TableUnit> absentRoutingUnitMap = getAbsentRoutingUnit(shardingStatement, routingResult.getRoutingUnits());
        if (absentRoutingUnitMap.isEmpty()) {
            return;
        }
        throwException(shardingStatement, shardingConditions, absentRoutingUnitMap);
    }
    
    private Multimap<RoutingUnit, TableUnit> getAbsentRoutingUnit(final ShardingOptimizedStatement shardingStatement, final Collection<RoutingUnit> routingUnits) {
        Multimap<RoutingUnit, TableUnit> result = HashMultimap.create();
        for (RoutingUnit each : routingUnits) {
            result.putAll(each, getAbsentTableUnit(shardingStatement, each));
        }
        return result;
    }
    
    private Collection<TableUnit> getAbsentTableUnit(final ShardingOptimizedStatement shardingStatement, final RoutingUnit routingUnit) {
        Collection<TableUnit> result = new LinkedList<>();
        for (TableUnit each : routingUnit.getTableUnits()) {
            if (containsInMetaData(shardingStatement, routingUnit.getDataSourceName(), each.getActualTableName()) || containsInShardingRule(routingUnit.getDataSourceName(), each)) {
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
    
    private boolean containsInMetaData(final ShardingOptimizedStatement shardingStatement, final String dataSourceName, final String actualTableName) {
        if (shardingRule.getRuleConfiguration().getMasterSlaveRuleConfigs().isEmpty()
                && (null == metaData.getDataSources() || null == metaData.getDataSources().getDataSourceMetaData(dataSourceName))) {
            return false;
        }
        if (shardingStatement.getSQLStatement() instanceof DMLStatement && (null == metaData.getTables() || !metaData.getTables().containsTable(actualTableName))) {
            return false;
        }
        return true;
    }
    
    protected abstract void throwException(ShardingOptimizedStatement shardingStatement, ShardingConditions shardingConditions, Multimap<RoutingUnit, TableUnit> absentRoutingUnitMap);
}
