/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.type.complex;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.shardingsphere.core.routing.type.RoutingEngine;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.RoutingTable;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.routing.type.TableUnits;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * cartesian routing engine.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class CartesianRoutingEngine implements RoutingEngine {
    
    private final Collection<RoutingResult> routingResults;
    
    @Override
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();
        for (Entry<String, Set<String>> entry : getDataSourceLogicTablesMap().entrySet()) {
            List<Set<String>> actualTableGroups = getActualTableGroups(entry.getKey(), entry.getValue());
            List<Set<RoutingTable>> routingTableGroups = toRoutingTableGroups(entry.getKey(), actualTableGroups);
            result.getTableUnits().getTableUnits().addAll(getTableUnits(entry.getKey(), Sets.cartesianProduct(routingTableGroups)).getTableUnits());
        }
        return result;
    }
    
    private Map<String, Set<String>> getDataSourceLogicTablesMap() {
        Collection<String> intersectionDataSources = getIntersectionDataSources();
        Map<String, Set<String>> result = new HashMap<>(routingResults.size());
        for (RoutingResult each : routingResults) {
            for (Entry<String, Set<String>> entry : each.getTableUnits().getDataSourceLogicTablesMap(intersectionDataSources).entrySet()) {
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
        for (RoutingResult each : routingResults) {
            if (result.isEmpty()) {
                result.addAll(each.getTableUnits().getDataSourceNames());
            }
            result.retainAll(each.getTableUnits().getDataSourceNames());
        }
        return result;
    }
    
    private List<Set<String>> getActualTableGroups(final String dataSource, final Set<String> logicTables) {
        List<Set<String>> result = new ArrayList<>(logicTables.size());
        for (RoutingResult each : routingResults) {
            result.addAll(each.getTableUnits().getActualTableNameGroups(dataSource, logicTables));
        }
        return result;
    }
    
    private List<Set<RoutingTable>> toRoutingTableGroups(final String dataSource, final List<Set<String>> actualTableGroups) {
        List<Set<RoutingTable>> result = new ArrayList<>(actualTableGroups.size());
        for (Set<String> each : actualTableGroups) {
            result.add(new HashSet<>(Lists.transform(new ArrayList<>(each), new Function<String, RoutingTable>() {
    
                @Override
                public RoutingTable apply(final String input) {
                    return findRoutingTable(dataSource, input);
                }
            })));
        }
        return result;
    }
    
    private RoutingTable findRoutingTable(final String dataSource, final String actualTable) {
        for (RoutingResult each : routingResults) {
            Optional<RoutingTable> result = each.getTableUnits().findRoutingTable(dataSource, actualTable);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new IllegalStateException(String.format("Cannot found routing table factor, data source: %s, actual table: %s", dataSource, actualTable));
    }
    
    private TableUnits getTableUnits(final String dataSource, final Set<List<RoutingTable>> cartesianRoutingTableGroups) {
        TableUnits result = new TableUnits();
        for (List<RoutingTable> each : cartesianRoutingTableGroups) {
            TableUnit tableUnit = new TableUnit(dataSource);
            tableUnit.getRoutingTables().addAll(each);
            result.getTableUnits().add(tableUnit);
        }
        return result;
    }
}
