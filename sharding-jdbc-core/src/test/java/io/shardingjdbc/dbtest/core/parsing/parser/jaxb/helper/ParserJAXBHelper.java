package io.shardingjdbc.dbtest.core.parsing.parser.jaxb.helper;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.shardingjdbc.core.constant.AggregationType;
import io.shardingjdbc.core.constant.OrderType;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingjdbc.core.parsing.parser.context.table.Table;
import io.shardingjdbc.core.parsing.parser.context.table.Tables;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.dbtest.config.bean.parsecontext.GroupByColumn;
import io.shardingjdbc.dbtest.config.bean.parsecontext.OrderByColumn;
import io.shardingjdbc.dbtest.config.bean.parsecontext.ParseContexDefinition;

public class ParserJAXBHelper {
    
    public static String[] getParameters(final String parameters) {
        if (Strings.isNullOrEmpty(parameters)) {
            return new String[]{};
        }
        return parameters.split(",");
    }
    
    public static Tables getTables(final io.shardingjdbc.dbtest.config.bean.parsecontext.Tables tables) {
        Tables result = new Tables();
        if (null == tables) {
            return result;
        }
        for (io.shardingjdbc.dbtest.config.bean.parsecontext.Table each : tables.getTables()) {
            Table table = new Table(each.getName(), Optional.fromNullable(each.getAlias()));
            result.add(table);
        }
        return result;
    }
    
    public static SelectStatement getSelectStatement(final ParseContexDefinition assertObj) {
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
                    new Function<io.shardingjdbc.dbtest.config.bean.parsecontext.AggregationSelectItem, AggregationSelectItem>() {
                        
                        @Override
                        public AggregationSelectItem apply(final io.shardingjdbc.dbtest.config.bean.parsecontext.AggregationSelectItem input) {
                            AggregationSelectItem result = new AggregationSelectItem(
                                    AggregationType.valueOf(input.getAggregationType().toUpperCase()), input.getInnerExpression(), Optional.fromNullable(input.getAlias()));
                            for (io.shardingjdbc.dbtest.config.bean.parsecontext.AggregationSelectItem each : input.getDerivedColumns()) {
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
