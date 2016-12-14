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

package com.dangdang.ddframe.rdb.sharding.parser.visitor;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;
import com.alibaba.druid.util.JdbcConstants;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
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
import com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.mysql.MySQLEvalVisitor;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 解析过程的上下文对象.
 * 
 * @author zhangliang
 */
@Getter
public final class ParseContext {
    
    private static final String AUTO_GEN_TOKE_KEY_TEMPLATE = "sharding_auto_gen_%d";
    
    private static final String SHARDING_GEN_ALIAS = "sharding_gen_%s";
    
    private final String autoGenTokenKey;
    
    private final SQLParsedResult parsedResult = new SQLParsedResult();
    
    private final int parseContextIndex;
    
    @Setter
    private ShardingRule shardingRule;
    
    @Setter
    private boolean hasOrCondition;
    
    private final ConditionContext currentConditionContext = new ConditionContext();
    
    private Table currentTable;
    
    private int selectItemsCount;
    
    private final Collection<String> selectItems = new HashSet<>();
    
    private boolean hasAllColumn;
    
    @Setter
    private ParseContext parentParseContext;
    
    private List<ParseContext> subParseContext = new LinkedList<>();
    
    private int itemIndex;
    
    private final Multimap<String, String> tableShardingColumnsMap = Multimaps.newSetMultimap(new TreeMap<String, Collection<String>>(String.CASE_INSENSITIVE_ORDER), new Supplier<Set<String>>() {
        @Override
        public Set<String> get() {
            return new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        }
    });
    
    public ParseContext(final int parseContextIndex) {
        this.parseContextIndex = parseContextIndex;
        autoGenTokenKey = String.format(AUTO_GEN_TOKE_KEY_TEMPLATE, parseContextIndex);
    }
    
    /**
     * 增加查询投射项数量.
     */
    public void increaseItemIndex() {
        itemIndex++;
    }
    
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
        if (notShardingColumns(column.get())) {
            return;
        }
        List<ValuePair> values = new ArrayList<>(valueExprList.size());
        for (SQLExpr each : valueExprList) {
            ValuePair evalValue = evalExpression(databaseType, each, parameters);
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
        Column column = createColumn(columnName, tableName);
        if (notShardingColumns(column)) {
            return; 
        }
        ValuePair value = evalExpression(databaseType, valueExpr, parameters);
        if (null != value) {
            addCondition(column, operator, Collections.singletonList(value));
        }
    }
    
    private void addCondition(final Column column, final BinaryOperator operator, final List<ValuePair> valuePairs) {
        Optional<Condition> optionalCondition = currentConditionContext.find(column.getTableName(), column.getColumnName(), operator);
        Condition condition;
        // TODO 待讨论
        if (optionalCondition.isPresent()) {
            condition = optionalCondition.get();
        } else {
            condition = new Condition(column, operator);
            currentConditionContext.add(condition);
        }
        for (ValuePair each : valuePairs) {
            condition.getValues().add(each.value);
            if (each.paramIndex > -1) {
                condition.getValueIndices().add(each.paramIndex);
            }
        }
    }
    
    private boolean notShardingColumns(final Column column) {
        if (!tableShardingColumnsMap.containsKey(column.getTableName())) {
            tableShardingColumnsMap.putAll(column.getTableName(), shardingRule.getAllShardingColumns(column.getTableName()));
        }
        return !tableShardingColumnsMap.containsEntry(column.getTableName(), column.getColumnName());
    }
    
    private ValuePair evalExpression(final DatabaseType databaseType, final SQLObject sqlObject, final List<Object> parameters) {
        if (sqlObject instanceof SQLMethodInvokeExpr) {
            // TODO 解析函数中的sharingValue不支持
            return null;
        }
        SQLEvalVisitor visitor;
        switch (databaseType.name().toLowerCase()) {
            case JdbcConstants.MYSQL:
            case JdbcConstants.H2: 
                visitor = new MySQLEvalVisitor();
                break;
            default: 
                visitor = SQLEvalVisitorUtils.createEvalVisitor(databaseType.name());    
        }
        visitor.setParameters(parameters);
        sqlObject.accept(visitor);
        
        Object value = SQLEvalVisitorUtils.getValue(sqlObject);
        if (null == value) {
            // TODO 对于NULL目前解析为空字符串,此处待考虑解决方法
            return null;
        }
        
        Comparable<?> finalValue;
        if (value instanceof Comparable<?>) {
            finalValue = (Comparable<?>) value;
        } else {
            finalValue = "";
        }
        Integer index = (Integer) sqlObject.getAttribute(MySQLEvalVisitor.EVAL_VAR_INDEX);
        if (null == index) {
            index = -1;
        }
        return new ValuePair(finalValue, index);
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
        Optional<Table> table = findTable(((SQLIdentifierExpr) expr.getOwner()).getSimpleName());
        return expr.getOwner() instanceof SQLIdentifierExpr && table.isPresent() ? createColumn(expr.getSimpleName(), table.get().getName()) : null;
    }
    
    private Column getColumnWithoutAlias(final SQLIdentifierExpr expr) {
        return null != currentTable ? createColumn(expr.getSimpleName(), currentTable.getName()) : null;
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
    
    private String generateDerivedColumnAlias() {
        return String.format(SHARDING_GEN_ALIAS, ++selectItemsCount);
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
     * @param owner 列拥有者
     * @param name 列名称
     * @param orderByType 排序类型
     */
    public void addOrderByColumn(final Optional<String> owner, final String name, final OrderByType orderByType) {
        String rawName = SQLUtil.getExactlyValue(name);
        parsedResult.getMergeContext().getOrderByColumns().add(new OrderByColumn(owner, rawName, getAlias(rawName), orderByType));
    }
    
    private Optional<String> getAlias(final String name) {
        if (containsSelectItem(name)) {
            return Optional.absent();
        }
        return Optional.of(generateDerivedColumnAlias());
    }
    
    private boolean containsSelectItem(final String selectItem) {
        return hasAllColumn || selectItems.contains(selectItem);
    }
    
    /**
     * 将分组列加入解析上下文.
     * 
     * @param owner 列拥有者
     * @param name 列名称
     * @param orderByType 排序类型
     */
    public void addGroupByColumns(final Optional<String> owner, final String name, final OrderByType orderByType) {
        String rawName = SQLUtil.getExactlyValue(name);
        parsedResult.getMergeContext().getGroupByColumns().add(new GroupByColumn(owner, rawName, getAlias(rawName), orderByType));
    }
    
    
    /**
     * 将当前解析的条件对象归并入解析结果.
     */
    public void mergeCurrentConditionContext() {
        if (!parsedResult.getRouteContext().getTables().isEmpty()) {
            if (parsedResult.getConditionContexts().isEmpty()) {
                parsedResult.getConditionContexts().add(currentConditionContext);
            }
            return;
        }
        Optional<SQLParsedResult> target = findValidParseResult();
        if (!target.isPresent()) {
            if (parsedResult.getConditionContexts().isEmpty()) {
                parsedResult.getConditionContexts().add(currentConditionContext);
            }
            return;
        }
        parsedResult.getRouteContext().getTables().addAll(target.get().getRouteContext().getTables());
        parsedResult.getConditionContexts().addAll(target.get().getConditionContexts());
    }
    
    private Optional<SQLParsedResult> findValidParseResult() {
        for (ParseContext each : subParseContext) {
            each.mergeCurrentConditionContext();
            if (each.getParsedResult().getRouteContext().getTables().isEmpty()) {
                continue;
            }
            return Optional.of(each.getParsedResult()); 
        }
        return Optional.absent();
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
    
    @RequiredArgsConstructor
    private static class ValuePair {
        
        private final Comparable<?> value;
        
        private final Integer paramIndex;
    }
}
