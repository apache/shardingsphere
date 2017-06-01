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

package com.dangdang.ddframe.rdb.sharding.routing.type.simple;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.routing.type.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.TableUnit;
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
 * 简单路由结果.
 * 
 * @author gaohongtao
 */
@ToString
public class SimpleRoutingResult implements RoutingResult {
    
    @Getter
    private final List<SimpleRoutingDataSource> routingDataSources = new ArrayList<>();
    
    void put(final String dataSourceName, final TableUnit routingTableFactor) {
        for (SimpleRoutingDataSource each : routingDataSources) {
            if (each.getDataSource().equalsIgnoreCase(dataSourceName)) {
                each.getTableUnits().add(routingTableFactor);
                return;
            }
        }
        routingDataSources.add(new SimpleRoutingDataSource(dataSourceName, routingTableFactor));
    }
    
    /**
     * 根据数据源名称获取数据源和逻辑表名称集合的映射关系.
     * 
     * @param dataSources 待获取的数据源名称集合
     * @return 数据源和逻辑表名称集合的映射关系
     */
    public Map<String, Set<String>> getDataSourceLogicTablesMap(final Collection<String> dataSources) {
        Map<String, Set<String>> result = new HashMap<>();
        for (SimpleRoutingDataSource each : routingDataSources) {
            if (!dataSources.contains(each.getDataSource())) {
                continue;
            }
            Set<String> logicTableNames = each.getLogicTableNames();
            if (logicTableNames.isEmpty()) {
                continue;
            }
            if (result.containsKey(each.getDataSource())) {
                result.get(each.getDataSource()).addAll(logicTableNames);
            } else {
                result.put(each.getDataSource(), logicTableNames);
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
        return Lists.transform(routingDataSources, new Function<SimpleRoutingDataSource, String>() {
            
            @Override
            public String apply(final SimpleRoutingDataSource input) {
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
        Optional<SimpleRoutingDataSource> routingDataSource = findRoutingDataSource(dataSource);
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
    public Optional<TableUnit> findRoutingTableFactor(final String dataSource, final String actualTable) {
        Optional<SimpleRoutingDataSource> routingDataSource = findRoutingDataSource(dataSource);
        if (!routingDataSource.isPresent()) {
            return Optional.absent();
        }
        return routingDataSource.get().findTableUnits(actualTable);
    }
    
    private Optional<SimpleRoutingDataSource> findRoutingDataSource(final String dataSource) {
        for (SimpleRoutingDataSource each : routingDataSources) {
            if (each.getDataSource().equals(dataSource)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    @Override
    public boolean isSingleRouting() {
        return 1 == routingDataSources.size() && 1 == routingDataSources.get(0).getTableUnits().size();
    }
    
    void bind(final BindingTableRule bindingTableRule, final String bindingLogicTable) {
        for (SimpleRoutingDataSource each : getRoutingDataSources()) {
            each.bind(bindingTableRule, bindingLogicTable);
        }
    }
}
