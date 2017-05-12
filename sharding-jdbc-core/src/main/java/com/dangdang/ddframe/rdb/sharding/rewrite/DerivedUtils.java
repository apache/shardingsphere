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

package com.dangdang.ddframe.rdb.sharding.rewrite;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.google.common.base.Optional;

/**
 * 补列工具类.
 *
 * @author zhangliang
 */
public final class DerivedUtils {
    
    private static final String DERIVED_COUNT_ALIAS = "AVG_DERIVED_COUNT_%s";
    
    private static final String DERIVED_SUM_ALIAS = "AVG_DERIVED_SUM_%s";
    
    /**
     * 追加派生列.
     *
     * @param selectSQLContext 解析结果
     */
    public static void appendDerivedColumns(final SelectSQLContext selectSQLContext) {
        ItemsToken itemsToken = new ItemsToken(selectSQLContext.getSelectListLastPosition());
        appendAvgDerivedColumns(selectSQLContext, itemsToken);
        appendOrderByDerivedColumns(selectSQLContext, itemsToken);
        appendGroupByDerivedColumns(selectSQLContext, itemsToken);
        if (!itemsToken.getItems().isEmpty()) {
            selectSQLContext.getSqlTokens().add(itemsToken);
        }
    }
    
    private static void appendAvgDerivedColumns(final SelectSQLContext selectSQLContext, final ItemsToken itemsToken) {
        int derivedColumnOffset = 0;
        for (SelectItemContext each : selectSQLContext.getItemContexts()) {
            if (each instanceof AggregationSelectItemContext) {
                AggregationSelectItemContext aggregationSelectItemContext = (AggregationSelectItemContext) each;
                if (AggregationType.AVG.equals(aggregationSelectItemContext.getAggregationType())) {
                    AggregationSelectItemContext countSelectItemContext = new AggregationSelectItemContext(
                            aggregationSelectItemContext.getInnerExpression(), Optional.of(String.format(DERIVED_COUNT_ALIAS, derivedColumnOffset)), -1, AggregationType.COUNT);
                    AggregationSelectItemContext sumSelectItemContext = new AggregationSelectItemContext(
                            aggregationSelectItemContext.getInnerExpression(), Optional.of(String.format(DERIVED_SUM_ALIAS, derivedColumnOffset)), -1, AggregationType.SUM);
                    aggregationSelectItemContext.getDerivedAggregationSelectItemContexts().add(countSelectItemContext);
                    aggregationSelectItemContext.getDerivedAggregationSelectItemContexts().add(sumSelectItemContext);
                    // TODO 将AVG列替换成常数，避免数据库再计算无用的AVG函数
                    itemsToken.getItems().add(countSelectItemContext.getExpression() + " AS " + countSelectItemContext.getAlias().get() + " ");
                    itemsToken.getItems().add(sumSelectItemContext.getExpression() + " AS " + sumSelectItemContext.getAlias().get() + " ");
                    derivedColumnOffset++;
                }
            }
        }
    }
    
    private static void appendOrderByDerivedColumns(final SelectSQLContext selectSQLContext, final ItemsToken itemsToken) {
        for (OrderByContext each : selectSQLContext.getOrderByContexts()) {
            if (!each.getIndex().isPresent()) {
                boolean found = false;
                String orderByExpression = each.getOwner().isPresent() ? each.getOwner().get() + "." + each.getName().get() : each.getName().get();
                for (SelectItemContext context : selectSQLContext.getItemContexts()) {
                    if (context.getExpression().equalsIgnoreCase(orderByExpression) || orderByExpression.equalsIgnoreCase(context.getAlias().orNull())) {
                        found = true;
                        break;
                    }
                }
                // TODO 需重构,目前的做法是通过补列有别名则补列,如果不包含select item则生成别名,进而补列,这里逻辑不直观
                if (!found && each.getAlias().isPresent()) {
                    itemsToken.getItems().add(orderByExpression + " AS " + each.getAlias().get() + " ");
                }
            }
        }
    }
    
    private static void appendGroupByDerivedColumns(final SelectSQLContext selectSQLContext, final ItemsToken itemsToken) {
        for (GroupByContext each : selectSQLContext.getGroupByContexts()) {
            boolean found = false;
            String groupByExpression = each.getOwner().isPresent() ? each.getOwner().get() + "." + each.getName() : each.getName();
            for (SelectItemContext context : selectSQLContext.getItemContexts()) {
                if ((!context.getAlias().isPresent() && context.getExpression().equalsIgnoreCase(groupByExpression))
                        || (context.getAlias().isPresent() && context.getAlias().get().equalsIgnoreCase(groupByExpression))) {
                    found = true;
                    break;
                }
            }
            // TODO 需重构,目前的做法是通过补列有别名则补列,如果不包含select item则生成别名,进而补列,这里逻辑不直观
            if (!found && each.getAlias().isPresent()) {
                itemsToken.getItems().add(groupByExpression + " AS " + each.getAlias().get() + " ");
            }
        }
    }
}
