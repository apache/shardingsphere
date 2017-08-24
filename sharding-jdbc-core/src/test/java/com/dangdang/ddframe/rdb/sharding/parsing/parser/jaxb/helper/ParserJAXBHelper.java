package com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.helper;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

public class ParserJAXBHelper {
    
    public static String[] getParameters(final String parameters) {
        if (Strings.isNullOrEmpty(parameters)) {
            return new String[]{};
        }
        return parameters.split(",");
    }
    
    public static Tables getTables(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Tables tables) {
        Tables result = new Tables();
        if (null == tables) {
            return result;
        }
        for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Table each : tables.getTables()) {
            Table table = new Table(each.getName(), Optional.fromNullable(each.getAlias()));
            result.add(table);
        }
        return result;
    }
    
    public static SelectStatement getSelectStatement(final Assert assertObj) {
        final SelectStatement result = new SelectStatement();
        if (null != assertObj.getOrderByColumns()) {
            List<OrderItem> orderItems = Lists.transform(assertObj.getOrderByColumns(), new Function<OrderByColumn, OrderItem>() {
                
                @Override
                public OrderItem apply(final OrderByColumn input) {
                    if (Strings.isNullOrEmpty(input.getName())) {
                        // TODO nullOrderType should config in xml
                        return new OrderItem(input.getIndex(), OrderType.valueOf(input.getOrderByType().toUpperCase()), OrderType.ASC);
                    }
                    if (Strings.isNullOrEmpty(input.getOwner())) {
                        // TODO nullOrderType should config in xml
                        return new OrderItem(input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), OrderType.ASC, Optional.fromNullable(input.getAlias()));
                    }
                    // TODO nullOrderType should config in xml
                    return new OrderItem(input.getOwner(), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), OrderType.ASC, Optional.fromNullable(input.getAlias()));
                }
            });
            result.getOrderByItems().addAll(orderItems);
        }
        if (null != assertObj.getGroupByColumns()) {
            result.getGroupByItems().addAll(Lists.transform(assertObj.getGroupByColumns(), new Function<GroupByColumn, OrderItem>() {
                
                @Override
                public OrderItem apply(final GroupByColumn input) {
                    if (null == input.getOwner()) {
                        // TODO nullOrderType should config in xml
                        return new OrderItem(input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), OrderType.ASC, Optional.fromNullable(input.getAlias()));
                    }
                    // TODO nullOrderType should config in xml
                    return new OrderItem(input.getOwner(), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), OrderType.ASC, Optional.fromNullable(input.getAlias()));
                }
            }));
        }
        if (null != assertObj.getAggregationSelectItems()) {
            List<AggregationSelectItem> selectItems = Lists.transform(assertObj.getAggregationSelectItems(),
                    new Function<com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem, AggregationSelectItem>() {
                        
                        @Override
                        public AggregationSelectItem apply(final com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem input) {
                            AggregationSelectItem result = new AggregationSelectItem(
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()), input.getInnerExpression(), Optional.fromNullable(input.getAlias()));
                            for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.AggregationSelectItem each : input.getDerivedColumns()) {
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
