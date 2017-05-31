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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select;

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupBy;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderBy;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.AbstractSQLStatement;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Select SQL语句对象.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class SelectStatement extends AbstractSQLStatement {
    
    private boolean distinct;
    
    private boolean containStar;
    
    private int selectListLastPosition;
    
    private final List<SelectItem> items = new LinkedList<>();
    
    private final List<GroupBy> groupByList = new LinkedList<>();
    
    private final List<OrderBy> orderByList = new LinkedList<>();
    
    private Limit limit;
    
    public SelectStatement() {
        super(SQLType.SELECT);
    }
    
    @Override
    public List<AggregationSelectItem> getAggregationSelectItems() {
        List<AggregationSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof AggregationSelectItem) {
                AggregationSelectItem aggregationSelectItem = (AggregationSelectItem) each;
                result.add(aggregationSelectItem);
                for (AggregationSelectItem derivedEach: aggregationSelectItem.getDerivedAggregationSelectItems()) {
                    result.add(derivedEach);
                }
            }
        }
        return result;
    }
    
    public Limit getLimit() {
        return limit;
    }
}
