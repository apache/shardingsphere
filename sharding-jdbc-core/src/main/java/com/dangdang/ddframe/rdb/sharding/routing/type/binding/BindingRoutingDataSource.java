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
import com.dangdang.ddframe.rdb.sharding.routing.type.TableUnit;
import com.dangdang.ddframe.rdb.sharding.routing.type.single.SingleRoutingDataSource;
import lombok.Getter;
import lombok.ToString;

/**
 * Binding表路由数据源.
 * 
 * @author zhangliang
 */
@Getter
@ToString(callSuper = true)
public final class BindingRoutingDataSource extends SingleRoutingDataSource {
    
    BindingRoutingDataSource(final SingleRoutingDataSource routingDataSource) {
        super(routingDataSource.getDataSource());
        getTableUnits().addAll(routingDataSource.getTableUnits());
    }
    
    void bind(final BindingTableRule bindingTableRule, final String bindingLogicTable) {
        for (TableUnit each : getTableUnits()) {
            each.getBindingTableUnits().add(new TableUnit(bindingLogicTable, bindingTableRule.getBindingActualTable(getDataSource(), bindingLogicTable, each.getActualTable())));
        }
    }
}
