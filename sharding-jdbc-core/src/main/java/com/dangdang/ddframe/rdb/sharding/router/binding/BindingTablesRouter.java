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

package com.dangdang.ddframe.rdb.sharding.router.binding;

import java.util.Collection;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.contstant.SQLType;
import com.dangdang.ddframe.rdb.sharding.router.single.SingleTableRouter;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

/**
 * Binding库表路由类.
 * 
 * @author zhangliang
 */
@Slf4j
public class BindingTablesRouter {
    
    private final ShardingRule shardingRule;
    
    private final Collection<String> logicTables;
    
    private final ConditionContext conditionContext;
    
    private final BindingTableRule bindingTableRule;
    
    private final SQLType sqlStatementType;
    
    public BindingTablesRouter(final ShardingRule shardingRule, final Collection<String> logicTables, final ConditionContext conditionContext, final SQLType sqlStatementType) {
        this.shardingRule = shardingRule;
        this.logicTables = logicTables;
        this.conditionContext = conditionContext;
        this.sqlStatementType = sqlStatementType;
        Optional<BindingTableRule> optionalBindingTableRule = shardingRule.findBindingTableRule(logicTables.iterator().next());
        Preconditions.checkState(optionalBindingTableRule.isPresent());
        bindingTableRule = optionalBindingTableRule.get();
    }
    
    /**
     * 路由.
     * 
     * @return 路由结果
     */
    public BindingRoutingResult route() {
        BindingRoutingResult result = null;
        for (final String each : logicTables) {
            if (null == result) {
                result = new BindingRoutingResult(new SingleTableRouter(shardingRule, each, conditionContext, sqlStatementType).route());
            } else {
                result.bind(bindingTableRule, each);
            }
        }
        log.trace("binding table sharding result: {}", result);
        return result;
    }
}
