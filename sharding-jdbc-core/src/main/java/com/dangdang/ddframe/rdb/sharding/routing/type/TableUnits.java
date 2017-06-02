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

package com.dangdang.ddframe.rdb.sharding.routing.type;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 路由表单元集合.
 * 
 * @author zhangliang
 */
@Getter
@ToString
public final class TableUnits {
    
    private final List<TableUnit> tableUnits = new LinkedList<>();
    
    /**
     * 获取全部数据源名称.
     *
     * @return 数据源名称集合
     */
    public Collection<String> getDataSourceNames() {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (TableUnit each : tableUnits) {
            result.add(each.getDataSourceName());
        }
        return result;
    }
    
    /**
     * 根据数据源和真实表名称查找路由表单元.
     *
     * @param dataSourceName 数据源名称
     * @param actualTableName 真实表名称
     * @return 查找结果
     */
    public Optional<TableUnit> findTableUnit(final String dataSourceName, final String actualTableName) {
        for (TableUnit each : tableUnits) {
            if (each.getDataSourceName().equalsIgnoreCase(dataSourceName) && each.getActualTableName().equalsIgnoreCase(actualTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * 根据数据源和逻辑表名称获取真实表集合组.
     * <p>
     * 每一组的真实表集合都属于同一逻辑表.
     * </p>
     *
     * @param dataSourceName 数据源名称
     * @param logicTableNames 逻辑表名称集合
     * @return 真实表集合组
     */
    public List<Set<String>> getActualTableNameGroups(final String dataSourceName, final Set<String> logicTableNames) {
        List<Set<String>> result = new ArrayList<>();
        for (String logicTableName : logicTableNames) {
            Set<String> actualTableNames = getActualTableNames(dataSourceName, logicTableName);
            if (!actualTableNames.isEmpty()) {
                result.add(actualTableNames);
            }
        }
        return result;
    }
    
    private Set<String> getActualTableNames(final String dataSourceName, final String logicTableName) {
        Set<String> result = new HashSet<>();
        for (TableUnit each : tableUnits) {
            if (each.getDataSourceName().equalsIgnoreCase(dataSourceName) && each.getLogicTableName().equalsIgnoreCase(logicTableName)) {
                result.add(each.getActualTableName());
            }
        }
        return result;
    }
    
    /**
     * 根据数据源名称获取数据源和逻辑表名称集合的映射关系.
     *
     * @param dataSourceNames 待获取的数据源名称集合
     * @return 数据源和逻辑表名称集合的映射关系
     */
    public Map<String, Set<String>> getDataSourceLogicTablesMap(final Collection<String> dataSourceNames) {
        Map<String, Set<String>> result = new HashMap<>();
        for (String each : dataSourceNames) {
            Set<String> logicTableNames = getLogicTableNames(each);
            if (!logicTableNames.isEmpty()) {
                result.put(each, logicTableNames);
            }
        }
        return result;
    }
    
    private Set<String> getLogicTableNames(final String dataSourceName) {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (TableUnit each : tableUnits) {
            if (each.getDataSourceName().equalsIgnoreCase(dataSourceName)) {
                result.addAll(Lists.transform(tableUnits, new Function<TableUnit, String>() {
                    
                    @Override
                    public String apply(final TableUnit input) {
                        return input.getLogicTableName();
                    }
                }));
            }
        }
        return result;
    }
}
