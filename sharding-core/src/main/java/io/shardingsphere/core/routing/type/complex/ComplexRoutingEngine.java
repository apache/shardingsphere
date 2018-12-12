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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.type.AbstractRoutingEngine;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.standard.StandardRoutingEngine;
import io.shardingsphere.core.rule.BindingTableRule;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

/**
 * Complex routing engine.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ComplexRoutingEngine extends AbstractRoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final Collection<String> logicTables;
    
    private final ShardingConditions shardingConditions;
    
    private final Optional<SQLStatement> sqlStatement;
    
    @Override
    public RoutingResult route() {
        Collection<RoutingResult> result = new ArrayList<>(logicTables.size());
        Collection<String> bindingTableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Set<String>> shardingSelectedDatasource = new HashMap<>();
        for (String each : logicTables) {
            Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByLogicTable(each);
            if (tableRule.isPresent()) {
                if (!bindingTableNames.contains(each)) {
                    RoutingResult routingResult = new StandardRoutingEngine(shardingRule, tableRule.get().getLogicTable(), shardingConditions, sqlStatement).route();
                    result.add(routingResult);
                    if(checkSharding(sqlStatement) && !shardingConditions.getShardingConditions().isEmpty()) {
                        fillTableDatasourceMapping(shardingSelectedDatasource, routingResult.getTableUnits().getTableUnits());
                    }
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
        if (result.isEmpty()) {
            throw new ShardingException("Cannot find table rule and default data source with logic tables: '%s'", logicTables);
        }
        if (1 == result.size()) {
            return result.iterator().next();
        }
        if(checkSharding(sqlStatement) && !shardingSelectedDatasource.isEmpty()) {
            checkTableDatasourceMapping(shardingSelectedDatasource);
        }
        return new CartesianRoutingEngine(result).route();
    }
}
