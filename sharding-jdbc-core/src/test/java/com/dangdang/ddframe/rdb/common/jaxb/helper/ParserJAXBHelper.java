package com.dangdang.ddframe.rdb.common.jaxb.helper;

import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Conditions;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.LimitValue;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Tables;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Assert;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Value;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ParserJAXBHelper {
    
    public static String[] getParameters(final Assert assertObj) {
        if (null == assertObj.getParameters()) {
            return new String[]{};
        }
        return assertObj.getParameters().split(",");
    }
    
    public static Set<DatabaseType> getDatabaseTypes(final Assert assertObj) {
        if (null == assertObj.getTypes()) {
            Set<DatabaseType> result = Sets.newHashSet(DatabaseType.values());
            result.remove(DatabaseType.H2);
            return result;
        }
        Set<DatabaseType> types = new HashSet<>();
        for (String each : assertObj.getTypes().split(",")) {
            types.add(DatabaseType.valueOf(each));
        }
        return types;
    }
    
    public static Tables getTables(final Assert assertObj) {
        Tables result = new Tables();
        if (null == assertObj.getTables()) {
            return result;
        }
        for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Table each : assertObj.getTables().getTables()) {
            Table table = new Table(each.getName(), Optional.fromNullable(each.getAlias()));
            result.add(table);
        }
        return result;
    }
    
    public static Conditions getConditions(final Assert assertObj) {
        Conditions result = new Conditions();
        if (null == assertObj.getConditions()) {
            return result;
        }
        for (com.dangdang.ddframe.rdb.sharding.parsing.parser.jaxb.Condition each : assertObj.getConditions().getConditions()) {
            List<SQLExpression> sqlExpressions = new LinkedList<>();
            for (Value value : each.getValues()) {
                if (null != value.getIndex()) {
                    sqlExpressions.add(new SQLPlaceholderExpression(value.getIndex()));
                } else {
                    Comparable<?> valueWithType = value.getValueWithType();
                    if (valueWithType instanceof Number) {
                        sqlExpressions.add(new SQLNumberExpression((Number) valueWithType));
                    } else {
                        sqlExpressions.add(new SQLTextExpression(valueWithType.toString()));
                    }
                }
            }
            Condition condition;
            switch (ShardingOperator.valueOf(each.getOperator().toUpperCase())) {
                case EQUAL:
                    condition = new Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0));
                    break;
                case BETWEEN:
                    condition = new Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0), sqlExpressions.get(1));
                    break;
                case IN:
                    condition = new Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            result.add(condition);
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
                        return new OrderItem(input.getIndex(), OrderType.valueOf(input.getOrderByType().toUpperCase()));
                    }
                    if (Strings.isNullOrEmpty(input.getOwner())) {
                        return new OrderItem(input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                    }
                    return new OrderItem(input.getOwner(), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                }
            });
            result.getOrderByItems().addAll(orderItems);
        }
        if (null != assertObj.getGroupByColumns()) {
            result.getGroupByItems().addAll(Lists.transform(assertObj.getGroupByColumns(), new Function<GroupByColumn, OrderItem>() {
                
                @Override
                public OrderItem apply(final GroupByColumn input) {
                    if (null == input.getOwner()) {
                        return new OrderItem(input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
                    }
                    return new OrderItem(input.getOwner(), input.getName(), OrderType.valueOf(input.getOrderByType().toUpperCase()), Optional.fromNullable(input.getAlias()));
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
        if (null != assertObj.getLimit()) {
            Limit limit = new Limit(true);
            if (null != assertObj.getLimit().getOffset() && null != assertObj.getLimit().getOffsetParameterIndex()) {
                limit.setRowCount(new LimitValue(assertObj.getLimit().getRowCount(), assertObj.getLimit().getRowCountParameterIndex()));
                limit.setOffset(new LimitValue(assertObj.getLimit().getOffset(), assertObj.getLimit().getOffsetParameterIndex()));
            } else {
                limit.setRowCount(new LimitValue(assertObj.getLimit().getRowCount(), assertObj.getLimit().getRowCountParameterIndex()));
            }
            result.setLimit(limit);
        }
        return result;
    }
}
