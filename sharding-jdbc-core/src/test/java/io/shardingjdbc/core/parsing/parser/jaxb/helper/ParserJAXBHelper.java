package io.shardingjdbc.core.parsing.parser.jaxb.helper;

import io.shardingjdbc.core.constant.AggregationType;
import io.shardingjdbc.core.constant.OrderType;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingjdbc.core.parsing.parser.jaxb.Assert;
import io.shardingjdbc.core.parsing.parser.jaxb.GroupByColumn;
import io.shardingjdbc.core.parsing.parser.jaxb.OrderByColumn;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
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
    
    public static io.shardingjdbc.core.parsing.parser.context.table.Tables getTables(final io.shardingjdbc.core.parsing.parser.jaxb.Tables tables) {
        io.shardingjdbc.core.parsing.parser.context.table.Tables result = new io.shardingjdbc.core.parsing.parser.context.table.Tables();
        if (null == tables) {
            return result;
        }
        for (io.shardingjdbc.core.parsing.parser.jaxb.Table each : tables.getTables()) {
            io.shardingjdbc.core.parsing.parser.context.table.Table table = new io.shardingjdbc.core.parsing.parser.context.table.Table(each.getName(), Optional.fromNullable(each.getAlias()));
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
                    new Function<io.shardingjdbc.core.parsing.parser.jaxb.AggregationSelectItem, AggregationSelectItem>() {
                        
                        @Override
                        public AggregationSelectItem apply(final io.shardingjdbc.core.parsing.parser.jaxb.AggregationSelectItem input) {
                            AggregationSelectItem result = new AggregationSelectItem(
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()), input.getInnerExpression(), Optional.fromNullable(input.getAlias()));
                            for (io.shardingjdbc.core.parsing.parser.jaxb.AggregationSelectItem each : input.getDerivedColumns()) {
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
