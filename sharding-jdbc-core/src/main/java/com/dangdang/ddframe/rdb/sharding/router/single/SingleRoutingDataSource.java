/**
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

package com.dangdang.ddframe.rdb.sharding.router.single;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.router.SQLExecutionUnit;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.ToString;

/**
 * 单表路由数据源.
 * 
 * @author zhangliang
 */
@Getter
@ToString
public class SingleRoutingDataSource {
    
    private final String dataSource;
    
    private final List<SingleRoutingTableFactor> routingTableFactors = new ArrayList<>();
    
    public SingleRoutingDataSource(final String dataSource) {
        this.dataSource = dataSource;
    }
    
    SingleRoutingDataSource(final String dataSource, final SingleRoutingTableFactor routingTableFactor) {
        this(dataSource);
        routingTableFactors.add(routingTableFactor);
    }
    
    Collection<SQLExecutionUnit> getSQLExecutionUnits(final SQLBuilder sqlBuilder) {
        Collection<SQLExecutionUnit> result = new ArrayList<>();
        for (SingleRoutingTableFactor each : routingTableFactors) {
            each.buildSQL(sqlBuilder);
            result.add(new SQLExecutionUnit(dataSource, sqlBuilder.toSQL()));
        }
        return result;
    }
    
    Set<String> getLogicTables() {
        Set<String> result = new HashSet<>(routingTableFactors.size());
        result.addAll(Lists.transform(routingTableFactors, new Function<SingleRoutingTableFactor, String>() {
            
            @Override
            public String apply(final SingleRoutingTableFactor input) {
                return input.getLogicTable();
            }
        }));
        return result;
    }
    
    List<Set<String>> getActualTableGroups(final Set<String> logicTables) {
        List<Set<String>> result = new ArrayList<>();
        for (String logicTable : logicTables) {
            Set<String> actualTables = getActualTables(logicTable);
            if (!actualTables.isEmpty()) {
                result.add(actualTables);
            }
        }
        return result;
    }
    
    private Set<String> getActualTables(final String logicTable) {
        Set<String> result = new HashSet<>();
        for (SingleRoutingTableFactor each : routingTableFactors) {
            if (each.getLogicTable().equals(logicTable)) {
                result.add(each.getActualTable());
            }
        }
        return result;
    }
    
    Optional<SingleRoutingTableFactor> findRoutingTableFactor(final String actualTable) {
        for (SingleRoutingTableFactor each : routingTableFactors) {
            if (each.getActualTable().equals(actualTable)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
