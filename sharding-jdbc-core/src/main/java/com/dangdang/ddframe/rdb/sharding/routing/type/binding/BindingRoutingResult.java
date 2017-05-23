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

package com.dangdang.ddframe.rdb.sharding.routing.type.binding;

import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingDataSource;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingResult;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import lombok.ToString;

/**
 * Binding表路由结果.
 * 
 * @author zhangliang
 */
@ToString(callSuper = true)
final class BindingRoutingResult extends SingleRoutingResult {
    
    BindingRoutingResult(final SingleRoutingResult singleRoutingResult) {
        getRoutingDataSources().addAll(Lists.transform(singleRoutingResult.getRoutingDataSources(), new Function<SingleRoutingDataSource, BindingRoutingDataSource>() {
            
            @Override
            public BindingRoutingDataSource apply(final SingleRoutingDataSource input) {
                return new BindingRoutingDataSource(input);
            }
        }));
    }
    
    void bind(final BindingTableRule bindingTableRule, final String bindingLogicTable) {
        for (SingleRoutingDataSource each : getRoutingDataSources()) {
            ((BindingRoutingDataSource) each).bind(bindingTableRule, bindingLogicTable);
        }
    }
}
