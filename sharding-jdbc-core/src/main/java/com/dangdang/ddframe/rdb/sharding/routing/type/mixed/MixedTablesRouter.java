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

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.routing.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleTableRouter;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * 混合多库表路由类.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class MixedTablesRouter {
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final Collection<String> logicTables;
    
    private final SQLStatement sqlStatement;
    
    /**
     * 路由.
     * 
     * @return 路由结果
     */
    // TODO 支持多bindingTable rule
    public RoutingResult route() {
        Collection<SingleRoutingResult> result = new ArrayList<>(logicTables.size());
        Collection<String> bindingTableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String each : logicTables) {
            Optional<TableRule> tableRule = shardingRule.tryFindTableRule(each);
            if (tableRule.isPresent()) {
                if (!bindingTableNames.contains(each)) {
                    result.add(new SingleTableRouter(shardingRule, parameters, tableRule.get().getLogicTable(), sqlStatement).route());
                }
                Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(each);
                if (bindingTableRule.isPresent()) {
                    bindingTableNames.addAll(Lists.transform(bindingTableRule.get().getTableRules(), new Function<TableRule, String>() {
                        
                        @Override
                        public String apply(final TableRule input) {
                            return input.getLogicTable();
                        }
                    }));
                }
            }
        }
        
        
        
//        Collection<String> bindingTables = shardingRule.filterAllBindingTables(logicTables);
//        Collection<String> remainingTables = new ArrayList<>(logicTables);
//        Collection<SingleRoutingResult> result = new ArrayList<>(logicTables.size());
//        if (bindingTables.size() > 1) {
//            result.add(new SingleTableRouter(shardingRule, parameters, bindingTables, sqlStatement).route());
//            remainingTables.removeAll(bindingTables);
//        }
//        for (String each : remainingTables) {
//            SingleRoutingResult routingResult = new SingleTableRouter(shardingRule, parameters, each, sqlStatement).route();
//            if (null != routingResult) {
//                result.add(routingResult);
//            }
//        }
        log.trace("mixed tables sharding result: {}", result);
        if (result.isEmpty()) {
            throw new ShardingJdbcException("Cannot find table rule and default data source with logic tables: '%s'", logicTables);
        }
        if (1 == result.size()) {
            return result.iterator().next();
        }
        return new CartesianTablesRouter(result).route();
    }
}
