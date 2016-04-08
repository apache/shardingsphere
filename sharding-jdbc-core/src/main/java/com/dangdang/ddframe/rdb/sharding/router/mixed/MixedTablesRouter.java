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

package com.dangdang.ddframe.rdb.sharding.router.mixed;

import java.util.ArrayList;
import java.util.Collection;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
import com.dangdang.ddframe.rdb.sharding.router.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.router.binding.BindingTablesRouter;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleRoutingResult;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleTableRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 混合多库表路由类.
 * 
 * @author gaohongtao, zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public class MixedTablesRouter {
    
    private final ShardingRule shardingRule;
    
    private final Collection<String> logicTables;
    
    private final ConditionContext conditionContext;
    
    private final SQLStatementType sqlStatementType;
    
    /**
     * 路由.
     * 
     * @return 路由结果
     */
    // TODO 支持多bindingTable rule
    public RoutingResult route() {
        Collection<String> bindingTables = shardingRule.filterAllBindingTables(logicTables);
        Collection<String> remainingTables = new ArrayList<>(logicTables);
        Collection<SingleRoutingResult> result = new ArrayList<>(logicTables.size());
        if (1 < bindingTables.size()) {
            result.add(new BindingTablesRouter(shardingRule, bindingTables, conditionContext, sqlStatementType).route());
            remainingTables.removeAll(bindingTables);
        }
        for (String each : remainingTables) {
            SingleRoutingResult routingResult = new SingleTableRouter(shardingRule, each, conditionContext, sqlStatementType).route();
            if (null != routingResult) {
                result.add(routingResult);
            }
        }
        log.trace("mixed tables sharding result: {}", result);
        if (result.isEmpty()) {
            return null;
        }
        if (1 == result.size()) {
            return result.iterator().next();
        }
        return new CartesianTablesRouter(result).route();
    }
}
