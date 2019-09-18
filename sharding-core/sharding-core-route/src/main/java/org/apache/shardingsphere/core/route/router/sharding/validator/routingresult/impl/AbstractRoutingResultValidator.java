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
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.ddl.ShardingDropIndexOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.router.sharding.validator.routingresult.RoutingResultValidator;
import org.apache.shardingsphere.core.route.type.RoutingEngine;
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
@Getter
public abstract class AbstractRoutingResultValidator implements RoutingResultValidator {
    
    private final ShardingRule shardingRule;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingOptimizedStatement shardingStatement;
    
    private final ShardingConditions shardingConditions;
    
    private boolean checkDataSource;
    
    private boolean checkTable;
    
    public AbstractRoutingResultValidator(final ShardingRule shardingRule,
                                          final ShardingSphereMetaData metaData, final ShardingOptimizedStatement shardingStatement, final ShardingConditions shardingConditions) {
        this.shardingRule = shardingRule;
        this.metaData = metaData;
        this.shardingStatement = shardingStatement;
        this.shardingConditions = shardingConditions;
        checkDataSource = shardingRule.getRuleConfiguration().getMasterSlaveRuleConfigs().isEmpty();
        checkTable = shardingStatement.getSQLStatement() instanceof DMLStatement;
    }
    
    @Override
    public final void validate(final RoutingEngine routingEngine, final RoutingResult routingResult) {
        if (shardingStatement instanceof ShardingDropIndexOptimizedStatement) {
            return;
        }
        Multimap<RoutingUnit, TableUnit> absentRoutingUnitMap = getAbsentRoutingUnit(routingResult.getRoutingUnits());
        if (absentRoutingUnitMap.isEmpty()) {
            return;
        }
        throwException(shardingStatement, shardingConditions, absentRoutingUnitMap);
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
    
    protected abstract void throwException(ShardingOptimizedStatement shardingStatement, ShardingConditions shardingConditions, Multimap<RoutingUnit, TableUnit> absentRoutingUnitMap);
}
