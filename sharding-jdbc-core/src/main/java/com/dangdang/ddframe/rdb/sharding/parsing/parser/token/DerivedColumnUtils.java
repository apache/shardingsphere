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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.token;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectItemContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 补列工具类.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DerivedColumnUtils {
    
    private static final String DERIVED_COUNT_ALIAS = "AVG_DERIVED_COUNT_%s";
    
    private static final String DERIVED_SUM_ALIAS = "AVG_DERIVED_SUM_%s";
    
    private static final String ORDER_BY_DERIVED_ALIAS = "ORDER_BY_DERIVED_%s";
    
    private static final String GROUP_BY_DERIVED_ALIAS = "GROUP_BY_DERIVED_%s";
    
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
            if (!(each instanceof AggregationSelectItemContext) || AggregationType.AVG != ((AggregationSelectItemContext) each).getAggregationType()) {
                continue;
            }
            AggregationSelectItemContext avgContext = (AggregationSelectItemContext) each;
            String countAlias = String.format(DERIVED_COUNT_ALIAS, derivedColumnOffset);
            AggregationSelectItemContext countContext = new AggregationSelectItemContext(avgContext.getInnerExpression(), Optional.of(countAlias), -1, AggregationType.COUNT);
            String sumAlias = String.format(DERIVED_SUM_ALIAS, derivedColumnOffset);
            AggregationSelectItemContext sumContext = new AggregationSelectItemContext(avgContext.getInnerExpression(), Optional.of(sumAlias), -1, AggregationType.SUM);
            avgContext.getDerivedAggregationSelectItemContexts().add(countContext);
            avgContext.getDerivedAggregationSelectItemContexts().add(sumContext);
            // TODO 将AVG列替换成常数，避免数据库再计算无用的AVG函数
            itemsToken.getItems().add(countContext.getExpression() + " AS " + countAlias + " ");
            itemsToken.getItems().add(sumContext.getExpression() + " AS " + sumAlias + " ");
            derivedColumnOffset++;
        }
    }
    
    private static void appendOrderByDerivedColumns(final SelectSQLContext selectSQLContext, final ItemsToken itemsToken) {
        int derivedColumnOffset = 0;
        for (OrderByContext each : selectSQLContext.getOrderByContexts()) {
            if (!each.getIndex().isPresent() && !each.getAlias().isPresent() && !selectSQLContext.isContainStar()) {
                String orderByExpression = each.getOwner().isPresent() ? each.getOwner().get() + "." + each.getName().get() : each.getName().get();
                String alias = String.format(ORDER_BY_DERIVED_ALIAS, derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(orderByExpression + " AS " + alias + " ");
            }
        }
    }
    
    private static void appendGroupByDerivedColumns(final SelectSQLContext selectSQLContext, final ItemsToken itemsToken) {
        int derivedColumnOffset = 0;
        for (GroupByContext each : selectSQLContext.getGroupByContexts()) {
            if (!each.getAlias().isPresent() && !selectSQLContext.isContainStar()) {
                String groupByExpression = each.getOwner().isPresent() ? each.getOwner().get() + "." + each.getName() : each.getName();
                String alias = String.format(GROUP_BY_DERIVED_ALIAS, derivedColumnOffset++);
                each.setAlias(Optional.of(alias));
                itemsToken.getItems().add(groupByExpression + " AS " + alias + " ");
            }
        }
    }
}
