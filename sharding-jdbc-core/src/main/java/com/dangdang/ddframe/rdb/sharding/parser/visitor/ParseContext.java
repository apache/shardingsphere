/**
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

package com.dangdang.ddframe.rdb.sharding.parser.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parser.result.SQLParsedResult;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.GroupByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn.OrderByType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.Column;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * 解析过程的上下文对象.
 * 
 * @author zhangliang
 */
@Getter
public final class ParseContext {
    
    private static final String SHARDING_GEN_ALIAS = "sharding_gen_%s";
    
    private final SQLParsedResult parsedResult = new SQLParsedResult();
    
    @Setter
    private Collection<String> shardingColumns;
    
    @Setter
    private boolean hasOrCondition;
    
    private final ConditionContext currentConditionContext = new ConditionContext();
    
    private Table currentTable;
    
    private int selectItemsCount;
    
    private final Collection<String> selectItems = new HashSet<>();
    
    private boolean hasAllColumn;
    
    /**
     * 设置当前正在访问的表.
     * 
     * @param currentTableName 表名称
     * @param currentAlias 表别名
     */
    public void setCurrentTable(final String currentTableName, final Optional<String> currentAlias) {
        Table table = new Table(SQLUtil.getExactlyValue(currentTableName), currentAlias.isPresent() ? Optional.of(SQLUtil.getExactlyValue(currentAlias.get())) : currentAlias);
        parsedResult.getRouteContext().getTables().add(table);
        currentTable = table;
    }
    
    /**
     * 将表对象加入解析上下文.
     * 
     * @param x 表名表达式, 来源于FROM, INSERT ,UPDATE, DELETE等语句
     */
    public Table addTable(final SQLExprTableSource x) {
        Table result = new Table(SQLUtil.getExactlyValue(x.getExpr().toString()), SQLUtil.getExactlyValue(x.getAlias()));
        parsedResult.getRouteContext().getTables().add(result);
        return result;
    }
    
    /**
     * 向解析上下文中添加条件对象.
     * 
     * @param expr SQL表达式
     * @param operator 操作符
     * @param valueExprList 值对象表达式集合
     * @param databaseType 数据库类型
     * @param parameters 通过占位符传进来的参数
     */
    public void addCondition(final SQLExpr expr, final BinaryOperator operator, final List<SQLExpr> valueExprList, final DatabaseType databaseType, final List<Object> parameters) {
        Optional<Column> column = getColumn(expr);
        if (!column.isPresent()) {
            return;
        }
        List<Comparable<?>> values = new ArrayList<>(valueExprList.size());
        for (SQLExpr each : valueExprList) {
            Comparable<?> evalValue = evalExpression(databaseType, each, parameters);
            if (null != evalValue) {
                values.add(evalValue);
            }
        }
        if (values.isEmpty()) {
            return;
        }
        addCondition(column.get(), operator, values);
    }
    
    /**
     * 将条件对象加入解析上下文.
     * 
     * @param columnName 列名称
     * @param tableName 表名称
     * @param operator 操作符
     * @param valueExpr 值对象表达式
     * @param databaseType 数据库类型
     * @param parameters 通过占位符传进来的参数
     */
    public void addCondition(final String columnName, final String tableName, final BinaryOperator operator, final SQLExpr valueExpr, final DatabaseType databaseType, final List<Object> parameters) {
        Comparable<?> value = evalExpression(databaseType, valueExpr, parameters);
        if (null != value) {
            addCondition(createColumn(columnName, tableName), operator, Collections.<Comparable<?>>singletonList(value));
        }
    }
    
    private void addCondition(final Column column, final BinaryOperator operator, final List<Comparable<?>> values) {
        if (!shardingColumns.contains(column.getColumnName())) {
            return;
        }
        Optional<Condition> optionalCondition = currentConditionContext.find(column.getTableName(), column.getColumnName(), operator);
        Condition condition;
        // TODO 待讨论
        if (optionalCondition.isPresent()) {
            condition = optionalCondition.get();
        } else {
            condition = new Condition(column, operator);
            currentConditionContext.add(condition);
        }
        condition.getValues().addAll(values);
    }
    
    private Comparable<?> evalExpression(final DatabaseType databaseType, final SQLObject sqlObject, final List<Object> parameters) {
        if (sqlObject instanceof SQLMethodInvokeExpr) {
            // TODO 解析函数中的sharingValue不支持
            return null;
        }
        Object result = SQLEvalVisitorUtils.eval(databaseType.name().toLowerCase(), sqlObject, parameters, false);
        if (null == result) {
            return null;
        }
        if (result instanceof Comparable<?>) {
            return (Comparable<?>) result;
        }
        // TODO 对于NULL目前解析为空字符串,此处待考虑解决方法
        return "";
    }
    
    private Optional<Column> getColumn(final SQLExpr expr) {
        if (expr instanceof SQLPropertyExpr) {
            return Optional.fromNullable(getColumnWithQualifiedName((SQLPropertyExpr) expr));
        }
        if (expr instanceof SQLIdentifierExpr) {
            return Optional.fromNullable(getColumnWithoutAlias((SQLIdentifierExpr) expr));
        }
        return Optional.absent();
    }
    
    private Column getColumnWithQualifiedName(final SQLPropertyExpr expr) {
        Optional<Table> table = findTable(((SQLIdentifierExpr) expr.getOwner()).getName());
        return expr.getOwner() instanceof SQLIdentifierExpr && table.isPresent() ? createColumn(expr.getName(), table.get().getName()) : null;
    }
    
    private Column getColumnWithoutAlias(final SQLIdentifierExpr expr) {
        return null != currentTable ? createColumn(expr.getName(), currentTable.getName()) : null;
    }
    
    private Column createColumn(final String columnName, final String tableName) {
        return new Column(SQLUtil.getExactlyValue(columnName), SQLUtil.getExactlyValue(tableName));
    }
    
    private Optional<Table> findTable(final String tableNameOrAlias) {
        Optional<Table> tableFromName = findTableFromName(tableNameOrAlias);
        return tableFromName.isPresent() ? tableFromName : findTableFromAlias(tableNameOrAlias);
    }
    
    /**
     * 判断SQL表达式是否为二元操作且带有别名.
     * 
     * @param x 待判断的SQL表达式
     * @param tableOrAliasName 表名称或别名
     * @return 是否为二元操作且带有别名
     */
    public boolean isBinaryOperateWithAlias(final SQLPropertyExpr x, final String tableOrAliasName) {
        return x.getParent() instanceof SQLBinaryOpExpr && findTableFromAlias(SQLUtil.getExactlyValue(tableOrAliasName)).isPresent();
    }
    
    private Optional<Table> findTableFromName(final String name) {
        for (Table each : parsedResult.getRouteContext().getTables()) {
            if (each.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(name))) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private Optional<Table> findTableFromAlias(final String alias) {
        for (Table each : parsedResult.getRouteContext().getTables()) {
            if (each.getAlias().isPresent() && each.getAlias().get().equalsIgnoreCase(SQLUtil.getExactlyValue(alias))) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * 将求平均值函数的补列加入解析上下文.
     * 
     * @param avgColumn 求平均值的列
     */
    public void addDerivedColumnsForAvgColumn(final AggregationColumn avgColumn) {
        addDerivedColumnForAvgColumn(avgColumn, getDerivedCountColumn(avgColumn));
        addDerivedColumnForAvgColumn(avgColumn, getDerivedSumColumn(avgColumn));
    }
    
    private void addDerivedColumnForAvgColumn(final AggregationColumn avgColumn, final AggregationColumn derivedColumn) {
        avgColumn.getDerivedColumns().add(derivedColumn);
        parsedResult.getMergeContext().getAggregationColumns().add(derivedColumn);
    }
    
    private AggregationColumn getDerivedCountColumn(final AggregationColumn avgColumn) {
        String expression = avgColumn.getExpression().replaceFirst(AggregationType.AVG.toString(), AggregationType.COUNT.toString());
        return new AggregationColumn(expression, AggregationType.COUNT, Optional.of(generateDerivedColumnAlias()), avgColumn.getOption());
    }
    
    private AggregationColumn getDerivedSumColumn(final AggregationColumn avgColumn) {
        String expression = avgColumn.getExpression().replaceFirst(AggregationType.AVG.toString(), AggregationType.SUM.toString());
        if (avgColumn.getOption().isPresent()) {
            expression = expression.replaceFirst(avgColumn.getOption().get() + " ", "");
        }
        return new AggregationColumn(expression, AggregationType.SUM, Optional.of(generateDerivedColumnAlias()), Optional.<String>absent());
    }
    
    /**
     * 将排序列加入解析上下文.
     * 
     * @param index 列顺序索引
     * @param orderByType 排序类型
     */
    public void addOrderByColumn(final int index, final OrderByType orderByType) {
        parsedResult.getMergeContext().getOrderByColumns().add(new OrderByColumn(index, orderByType));
    }
    
    /**
     * 将排序列加入解析上下文.
     * 
     * @param name 列名称
     * @param orderByType 排序类型
     */
    public void addOrderByColumn(final String name, final OrderByType orderByType) {
        String rawName = SQLUtil.getExactlyValue(name);
        String alias = null;
        if (!containsSelectItem(rawName)) {
            alias = generateDerivedColumnAlias();
        }
        parsedResult.getMergeContext().getOrderByColumns().add(new OrderByColumn(rawName, alias, orderByType));
    }
    
    private boolean containsSelectItem(final String selectItem) {
        return hasAllColumn || selectItems.contains(selectItem);
    }
    
    /**
     * 将分组列加入解析上下文.
     * 
     * @param name 列名称
     * @param alias 列别名
     * @param orderByType 排序类型
     */
    public void addGroupByColumns(final String name, final String alias, final OrderByType orderByType) {
        parsedResult.getMergeContext().getGroupByColumns().add(new GroupByColumn(SQLUtil.getExactlyValue(name), alias, orderByType));
    }
    
    /**
     * 生成补列别名.
     * 
     * @return 补列的别名
     */
    public String generateDerivedColumnAlias() {
        return String.format(SHARDING_GEN_ALIAS, ++selectItemsCount);
    }
    
    /**
     * 将当前解析的条件对象归并入解析结果.
     */
    public void mergeCurrentConditionContext() {
        parsedResult.getConditionContexts().add(currentConditionContext);
    }
    
    /**
     * 注册SELECT语句中声明的列名称或别名.
     *
     * @param selectItem SELECT语句中声明的列名称或别名
     */
    public void registerSelectItem(final String selectItem) {
        String rawItemExpr = SQLUtil.getExactlyValue(selectItem);
        if ("*".equals(rawItemExpr)) {
            hasAllColumn = true;
            return;
        }
        selectItems.add(rawItemExpr);
    }
    
}
