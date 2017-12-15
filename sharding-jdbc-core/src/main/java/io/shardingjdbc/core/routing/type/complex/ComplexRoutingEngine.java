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

package io.shardingjdbc.core.routing.type.complex;

import io.shardingjdbc.core.rule.BindingTableRule;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.simple.SimpleRoutingEngine;
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
 * Complex routing engine.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class ComplexRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final Collection<String> logicTables;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public RoutingResult route() {
        Collection<RoutingResult> result = new ArrayList<>(logicTables.size());
        Collection<String> bindingTableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String each : logicTables) {
            Optional<TableRule> tableRule = shardingRule.tryFindTableRule(each);
            if (tableRule.isPresent()) {
                if (!bindingTableNames.contains(each)) {
                    result.add(new SimpleRoutingEngine(shardingRule, parameters, tableRule.get().getLogicTable(), sqlStatement).route());
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
        log.trace("mixed tables sharding result: {}", result);
        if (result.isEmpty()) {
            throw new ShardingJdbcException("Cannot find table rule and default data source with logic tables: '%s'", logicTables);
        }
        if (1 == result.size()) {
            return result.iterator().next();
        }
        return new CartesianRoutingEngine(result).route();
    }
}
