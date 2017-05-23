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

package com.dangdang.ddframe.rdb.sharding.routing.type.mixed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingTableFactor;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 笛卡尔积的库表路由.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
final class CartesianTablesRouter {
    
    private final Collection<SingleRoutingResult> routingResults;
    
    CartesianResult route() {
        CartesianResult result = new CartesianResult();
        for (Entry<String, Set<String>> entry : getDataSourceLogicTablesMap().entrySet()) {
            List<Set<String>> actualTableGroups = getActualTableGroups(entry.getKey(), entry.getValue());
            List<Set<SingleRoutingTableFactor>> routingTableFactorGroups = toRoutingTableFactorGroups(entry.getKey(), actualTableGroups);
            result.merge(entry.getKey(), getCartesianTableReferences(Sets.cartesianProduct(routingTableFactorGroups)));
        }
        log.trace("cartesian tables sharding result: {}", result);
        return result;
    }
    
    private Map<String, Set<String>> getDataSourceLogicTablesMap() {
        Collection<String> intersectionDataSources = getIntersectionDataSources();
        Map<String, Set<String>> result = new HashMap<>(routingResults.size());
        for (SingleRoutingResult each : routingResults) {
            for (Entry<String, Set<String>> entry : each.getDataSourceLogicTablesMap(intersectionDataSources).entrySet()) {
                if (result.containsKey(entry.getKey())) {
                    result.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }
    
    private Collection<String> getIntersectionDataSources() {
        Collection<String> result = new HashSet<>();
        for (SingleRoutingResult each : routingResults) {
            if (result.isEmpty()) {
                result.addAll(each.getDataSources());
            }
            result.retainAll(each.getDataSources());
        }
        return result;
    }
    
    private List<Set<String>> getActualTableGroups(final String dataSource, final Set<String> logicTables) {
        List<Set<String>> result = new ArrayList<>(logicTables.size());
        for (SingleRoutingResult each : routingResults) {
            result.addAll(each.getActualTableGroups(dataSource, logicTables));
        }
        return result;
    }
    
    private List<Set<SingleRoutingTableFactor>> toRoutingTableFactorGroups(final String dataSource, final List<Set<String>> actualTableGroups) {
        List<Set<SingleRoutingTableFactor>> result = new ArrayList<>(actualTableGroups.size());
        for (Set<String> each : actualTableGroups) {
            result.add(new HashSet<>(Lists.transform(new ArrayList<>(each), new Function<String, SingleRoutingTableFactor>() {
    
                @Override
                public SingleRoutingTableFactor apply(final String input) {
                    return findRoutingTableFactor(dataSource, input);
                }
            })));
        }
        return result;
    }
    
    private SingleRoutingTableFactor findRoutingTableFactor(final String dataSource, final String actualTable) {
        for (SingleRoutingResult each : routingResults) {
            Optional<SingleRoutingTableFactor> result = each.findRoutingTableFactor(dataSource, actualTable);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new IllegalStateException(String.format("Cannot found routing table factor, data source: %s, actual table: %s", dataSource, actualTable));
    }
    
    private List<CartesianTableReference> getCartesianTableReferences(final Set<List<SingleRoutingTableFactor>> cartesianRoutingTableFactorGroups) {
        List<CartesianTableReference> result = new ArrayList<>(cartesianRoutingTableFactorGroups.size());
        for (List<SingleRoutingTableFactor> each : cartesianRoutingTableFactorGroups) {
            result.add(new CartesianTableReference(each));
        }
        return result;
    }
}
