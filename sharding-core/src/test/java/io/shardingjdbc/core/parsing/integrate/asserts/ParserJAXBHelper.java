/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.integrate.asserts;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.shardingjdbc.core.constant.AggregationType;
import io.shardingjdbc.core.constant.OrderDirection;
import io.shardingjdbc.core.parsing.integrate.jaxb.groupby.GroupByColumnAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.item.AggregationSelectItemAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.orderby.OrderByColumnAssert;
import io.shardingjdbc.core.parsing.integrate.jaxb.root.ParserAssert;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;

import java.util.List;

public final class ParserJAXBHelper {
    
    /**
     * Get select statement.
     * 
     * @param assertObj assert object
     * @return select statement
     */
    public static SelectStatement getSelectStatement(final ParserAssert assertObj) {
        final SelectStatement result = new SelectStatement();
        if (null != assertObj.getOrderByColumns()) {
            List<OrderItem> orderItems = Lists.transform(assertObj.getOrderByColumns(), new Function<OrderByColumnAssert, OrderItem>() {
                
                @Override
                public OrderItem apply(final OrderByColumnAssert input) {
                    if (Strings.isNullOrEmpty(input.getName())) {
                        // TODO nullOrderType should config in xml
                        return new OrderItem(input.getIndex(), OrderDirection.valueOf(input.getOrderDirection().toUpperCase()), OrderDirection.ASC);
                    }
                    if (Strings.isNullOrEmpty(input.getOwner())) {
                        // TODO nullOrderType should config in xml
                        return new OrderItem(input.getName(), OrderDirection.valueOf(input.getOrderDirection().toUpperCase()), OrderDirection.ASC, Optional.fromNullable(input.getAlias()));
                    }
                    // TODO nullOrderType should config in xml
                    return new OrderItem(
                            input.getOwner(), input.getName(), OrderDirection.valueOf(input.getOrderDirection().toUpperCase()), OrderDirection.ASC, Optional.fromNullable(input.getAlias()));
                }
            });
            result.getOrderByItems().addAll(orderItems);
        }
        if (null != assertObj.getGroupByColumns()) {
            result.getGroupByItems().addAll(Lists.transform(assertObj.getGroupByColumns(), new Function<GroupByColumnAssert, OrderItem>() {
                
                @Override
                public OrderItem apply(final GroupByColumnAssert input) {
                    if (null == input.getOwner()) {
                        // TODO nullOrderType should config in xml
                        return new OrderItem(input.getName(), OrderDirection.valueOf(input.getOrderDirection().toUpperCase()), OrderDirection.ASC, Optional.fromNullable(input.getAlias()));
                    }
                    // TODO nullOrderType should config in xml
                    return new OrderItem(
                            input.getOwner(), input.getName(), OrderDirection.valueOf(input.getOrderDirection().toUpperCase()), OrderDirection.ASC, Optional.fromNullable(input.getAlias()));
                }
            }));
        }
        if (null != assertObj.getAggregationSelectItems()) {
            List<AggregationSelectItem> selectItems = Lists.transform(assertObj.getAggregationSelectItems(),
                    new Function<AggregationSelectItemAssert, AggregationSelectItem>() {
                        
                        @Override
                        public AggregationSelectItem apply(final AggregationSelectItemAssert input) {
                            AggregationSelectItem result = new AggregationSelectItem(
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()), input.getInnerExpression(), Optional.fromNullable(input.getAlias()));
                            for (AggregationSelectItemAssert each : input.getDerivedColumns()) {
                                result.getDerivedAggregationSelectItems().add(new AggregationSelectItem(
                                        AggregationType.valueOf(each.getAggregationType().toUpperCase()), each.getInnerExpression(), Optional.fromNullable(each.getAlias())));
                            }
                            return result;
                        }
                    });
            result.getItems().addAll(selectItems);
        }
        return result;
    }
}
