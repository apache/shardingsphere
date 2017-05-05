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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Select SQL上下文.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class SelectSQLContext extends AbstractSQLContext {
    
    private boolean distinct;
    
    private boolean containStar;
    
    private int selectListLastPosition;
    
    private final List<SelectItemContext> itemContexts = new LinkedList<>();
    
    private final List<GroupByContext>  groupByContexts = new LinkedList<>();
    
    private final List<OrderByContext>  orderByContexts = new LinkedList<>();
    
    private LimitContext limitContext;
    
    public SelectSQLContext() {
        super(SQLType.SELECT);
    }
    
    @Override
    public List<AggregationSelectItemContext> getAggregationSelectItemContexts() {
        List<AggregationSelectItemContext> result = new LinkedList<>();
        for (SelectItemContext each : itemContexts) {
            if (each instanceof AggregationSelectItemContext) {
                AggregationSelectItemContext aggregationSelectItemContext = (AggregationSelectItemContext) each;
                result.add(aggregationSelectItemContext);
                for (AggregationSelectItemContext derivedEach: aggregationSelectItemContext.getDerivedAggregationSelectItemContexts()) {
                    result.add(derivedEach);
                }
            }
        }
        return result;
    }
    
    @Override
    public LimitContext getLimitContext() {
        return limitContext;
    }
}
