/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.routing.type.single;

import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.routing.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 单表路由结果.
 * 
 * @author gaohongtao
 */
@ToString
public class SingleRoutingResult implements RoutingResult {
    
    @Getter
    private final List<SingleRoutingDataSource> routingDataSources = new ArrayList<>();
    
    void put(final String dataSourceName, final SingleRoutingTableFactor routingTableFactor) {
        for (SingleRoutingDataSource each : routingDataSources) {
            if (each.getDataSource().equals(dataSourceName)) {
                each.getRoutingTableFactors().add(routingTableFactor);
                return;
            }
        }
        routingDataSources.add(new SingleRoutingDataSource(dataSourceName, routingTableFactor));
    }
    
    /**
     * 根据数据源名称获取数据源和逻辑表名称集合的映射关系.
     * 
     * @param dataSources 待获取的数据源名称集合
     * @return 数据源和逻辑表名称集合的映射关系
     */
    public Map<String, Set<String>> getDataSourceLogicTablesMap(final Collection<String> dataSources) {
        Map<String, Set<String>> result = new HashMap<>();
        for (SingleRoutingDataSource each : routingDataSources) {
            if (!dataSources.contains(each.getDataSource())) {
                continue;
            }
            Set<String> logicTables = each.getLogicTables();
            if (logicTables.isEmpty()) {
                continue;
            }
            if (result.containsKey(each.getDataSource())) {
                result.get(each.getDataSource()).addAll(logicTables);
            } else {
                result.put(each.getDataSource(), logicTables);
            }
        }
        return result;
    }
    
    /**
     * 获取全部数据源名称.
     * 
     * @return 数据源名称集合
     */
    public Collection<String> getDataSources() {
        return Lists.transform(routingDataSources, new Function<SingleRoutingDataSource, String>() {
            
            @Override
            public String apply(final SingleRoutingDataSource input) {
                return input.getDataSource();
            }
        });
    }
    
    /**
     * 根据数据源和逻辑表名称获取真实表集合组.
     * <p>
     * 每一组的真实表集合都属于同一逻辑表.
     * </p>
     * 
     * @param dataSource 数据源名称
     * @param logicTables 逻辑表名称集合
     * @return 真实表集合组
     */
    public List<Set<String>> getActualTableGroups(final String dataSource, final Set<String> logicTables) {
        Optional<SingleRoutingDataSource> routingDataSource = findRoutingDataSource(dataSource);
        if (!routingDataSource.isPresent()) {
            return Collections.emptyList();
        }
        return routingDataSource.get().getActualTableGroups(logicTables);
    }
    
    /**
     * 根据数据源和真实表名称查找路由表单元.
     * 
     * @param dataSource 数据源名称
     * @param actualTable 真实表名称
     * @return 查找结果
     */
    public Optional<SingleRoutingTableFactor> findRoutingTableFactor(final String dataSource, final String actualTable) {
        Optional<SingleRoutingDataSource> routingDataSource = findRoutingDataSource(dataSource);
        if (!routingDataSource.isPresent()) {
            return Optional.absent();
        }
        return routingDataSource.get().findRoutingTableFactor(actualTable);
    }
    
    private Optional<SingleRoutingDataSource> findRoutingDataSource(final String dataSource) {
        for (SingleRoutingDataSource each : routingDataSources) {
            if (each.getDataSource().equals(dataSource)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    @Override
    public Collection<SQLExecutionUnit> getSQLExecutionUnits(final SQLBuilder sqlBuilder) {
        Collection<SQLExecutionUnit> result = new ArrayList<>();
        for (SingleRoutingDataSource each : routingDataSources) {
            result.addAll(each.getSQLExecutionUnits(sqlBuilder));
        }
        return result;
    }
}
