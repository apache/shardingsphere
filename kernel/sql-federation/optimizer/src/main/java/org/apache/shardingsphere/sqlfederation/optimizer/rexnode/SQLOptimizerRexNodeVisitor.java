/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sqlfederation.optimizer.rexnode;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexUnknownAs;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.DateString;
import org.apache.calcite.util.Sarg;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeBaseVisitor;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.ArgListContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.ArgRangeContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.ArgRangeListContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.CastContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.ConstantContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.ExpressionContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.InputContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.InputRefContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.OpContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.ParamWithTypeContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.ParameterContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.SearchArgsContext;
import org.apache.shardingsphere.rexnode.autogen.SQLOptimizerRexNodeParser.TypeContext;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.exception.OptimizationSQLRexNodeException;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL optimizer rex node visitor.
 */
@AllArgsConstructor
public final class SQLOptimizerRexNodeVisitor extends SQLOptimizerRexNodeBaseVisitor<RexNode> {
    
    private RexBuilder rexBuilder;
    
    private JavaTypeFactory typeFactory;
    
    private Map<String, Object> parameters;
    
    private Map<Integer, Integer> columnMap;
    
    @Override
    public RexNode visitExpression(final ExpressionContext ctx) {
        SqlOperator operator = getOp(ctx.op());
        RexNode firstNode = visitParameter(ctx.parameter(0));
        RexNode secondNode = visitParameter(ctx.parameter(1));
        return rexBuilder.makeCall(operator, firstNode, secondNode);
    }
    
    private SqlOperator getOp(final OpContext ctx) {
        if (null != ctx.LIKE()) {
            return SqlStdOperatorTable.LIKE;
        }
        if (null != ctx.SEARCH()) {
            return SqlStdOperatorTable.SEARCH;
        }
        if (null != ctx.AND()) {
            return SqlStdOperatorTable.AND;
        }
        if (null != ctx.OR()) {
            return SqlStdOperatorTable.OR;
        }
        if (null != ctx.NOT()) {
            return SqlStdOperatorTable.NOT;
        }
        if (null != ctx.EQ_()) {
            return SqlStdOperatorTable.EQUALS;
        }
        if (null != ctx.LT_()) {
            return SqlStdOperatorTable.LESS_THAN;
        }
        if (null != ctx.LTE_()) {
            return SqlStdOperatorTable.LESS_THAN_OR_EQUAL;
        }
        if (null != ctx.GT_()) {
            return SqlStdOperatorTable.GREATER_THAN;
        }
        if (null != ctx.GTE_()) {
            return SqlStdOperatorTable.GREATER_THAN_OR_EQUAL;
        }
        if (null != ctx.NEQ_()) {
            return SqlStdOperatorTable.NOT_EQUALS;
        }
        throw new OptimizationSQLRexNodeException(ctx.getText());
    }
    
    @Override
    public RexNode visitParameter(final ParameterContext ctx) {
        if (null != ctx.expression()) {
            return visitExpression(ctx.expression());
        }
        if (null != ctx.input()) {
            return visitInput(ctx.input());
        }
        throw new OptimizationSQLRexNodeException(ctx.getText());
    }
    
    @Override
    public RexNode visitInput(final InputContext ctx) {
        if (null != ctx.inputRef()) {
            return visitInputRef(ctx.inputRef());
        }
        if (null != ctx.searchArgs()) {
            return visitSearchArgs(ctx.searchArgs());
        }
        if (null != ctx.constant()) {
            return visitConstant(ctx.constant());
        }
        if (null != ctx.cast()) {
            return visitCast(ctx.cast());
        }
        if (null != ctx.paramWithType()) {
            return visitParamWithType(ctx.paramWithType());
        }
        throw new OptimizationSQLRexNodeException(ctx.getText());
    }
    
    @Override
    public RexNode visitInputRef(final InputRefContext ctx) {
        Integer index = Integer.valueOf(ctx.INTEGER_().getText());
        String sign = ctx.getParent().getStop().getText();
        if (null != columnMap.get(index)) {
            Class<?> dataType = getClass(columnMap.get(index));
            return rexBuilder.makeInputRef(typeFactory.createJavaType(dataType), index);
        }
        if (ctx.getParent() instanceof CastContext) {
            return makeCastInputRef(sign, index);
        }
        return rexBuilder.makeInputRef(typeFactory.createJavaType(Integer.class), index);
    }
    
    private RexNode makeCastInputRef(final String sign, final Integer index) {
        if (sign.contains("VARCHAR")) {
            return rexBuilder.makeInputRef(typeFactory.createJavaType(String.class), index);
        } else if ("INTEGER".equals(sign)) {
            return rexBuilder.makeInputRef(typeFactory.createJavaType(Integer.class), index);
        } else if ("BIGINT".equals(sign)) {
            return rexBuilder.makeInputRef(typeFactory.createJavaType(Long.class), index);
        } else if ("DATE".equals(sign)) {
            return rexBuilder.makeInputRef(typeFactory.createJavaType(Date.class), index);
        }
        throw new OptimizationSQLRexNodeException(sign);
    }
    
    /**
     * Switch sql type to java type, reference to java.sql.Types.
     *
     * @param dataType sql type
     * @return java type
     */
    private Class<?> getClass(final int dataType) {
        switch (dataType) {
            case Types.BIGINT:
                return Long.class;
            case Types.CHAR:
                return String.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.FLOAT:
                return Float.class;
            case Types.DOUBLE:
                return Double.class;
            case Types.VARCHAR:
                return String.class;
            case Types.DATE:
                return Date.class;
            default:
                return String.class;
        }
    }
    
    @Override
    public RexNode visitSearchArgs(final SearchArgsContext ctx) {
        Sarg<BigDecimal> sarg;
        if (null != ctx.argList()) {
            sarg = getArgList(ctx.argList());
        } else if (null != ctx.argRange()) {
            sarg = getArgRange(ctx.argRange());
        } else if (null != ctx.argRangeList()) {
            sarg = getArgRangeList(ctx.argRangeList());
        } else {
            throw new OptimizationSQLRexNodeException(ctx.getText());
        }
        RelDataType sargType = typeFactory.createSqlType(SqlTypeName.DECIMAL);
        return rexBuilder.makeSearchArgumentLiteral(sarg, sargType);
    }
    
    @Override
    public RexNode visitConstant(final ConstantContext ctx) {
        if (null != ctx.INTEGER_()) {
            Integer number = Integer.valueOf(ctx.INTEGER_().getText());
            RelDataType nonNullableInt = typeFactory.createSqlType(SqlTypeName.INTEGER);
            return rexBuilder.makeLiteral(number, nonNullableInt, false);
        }
        if (null != ctx.STRING_()) {
            String literalValue = ctx.STRING_().getText().replace("\"", "").replace("'", "");
            return rexBuilder.makeLiteral(literalValue, typeFactory.createSqlType(SqlTypeName.VARCHAR), false);
        }
        if (null != ctx.DATE_()) {
            String data = ctx.DATE_().getText();
            DateString value = new DateString(data);
            return rexBuilder.makeLiteral(value, typeFactory.createSqlType(SqlTypeName.DATE), false);
        }
        if (null != ctx.PLACEHOLDER_()) {
            return makeLiteral(ctx.PLACEHOLDER_().getText());
        }
        if (null != ctx.string_zh().STRING_()) {
            String literalValue = ctx.string_zh().STRING_().getText().replace("\"", "").replace("'", "");
            return rexBuilder.makeLiteral(literalValue, typeFactory.createSqlType(SqlTypeName.VARCHAR), false);
        }
        throw new OptimizationSQLRexNodeException(ctx.getText());
    }
    
    private RexNode makeLiteral(final String text) {
        Class<?> parameterType = parameters.get(text).getClass();
        Object parameter = parameters.get(text);
        if (parameterType.equals(Integer.class)) {
            return rexBuilder.makeLiteral(parameter, typeFactory.createSqlType(SqlTypeName.INTEGER), false);
        } else if (parameterType.equals(Long.class)) {
            return rexBuilder.makeLiteral(parameter, typeFactory.createSqlType(SqlTypeName.BIGINT), false);
        } else if (parameterType.equals(String.class)) {
            return rexBuilder.makeLiteral(parameter, typeFactory.createSqlType(SqlTypeName.VARCHAR), false);
        } else if (parameterType.equals(Date.class)) {
            Date data = (Date) parameter;
            DateString value = new DateString(data.toString());
            return rexBuilder.makeLiteral(value, typeFactory.createSqlType(SqlTypeName.DATE), true);
        } else {
            return rexBuilder.makeLiteral(parameter.toString(), typeFactory.createSqlType(SqlTypeName.VARCHAR), false);
        }
    }
    
    @Override
    public RexNode visitCast(final CastContext ctx) {
        RexNode inputRef = visitInputRef(ctx.inputRef());
        RelDataType type = getType(ctx.type());
        return rexBuilder.makeCast(type, inputRef);
    }
    
    @Override
    public RexNode visitParamWithType(final ParamWithTypeContext ctx) {
        RelDataType type = getType(ctx.type());
        return null == ctx.INTEGER_() ? rexBuilder.makeLiteral(ctx.STRING_().getText(), type) : rexBuilder.makeLiteral(Integer.valueOf(ctx.INTEGER_().getText()), type);
    }
    
    private Sarg<BigDecimal> getArgRange(final ArgRangeContext ctx) {
        BigDecimal lowerValue = BigDecimal.valueOf(Long.parseLong(ctx.INTEGER_(0).getText()));
        BigDecimal upperValue = BigDecimal.valueOf(Long.parseLong(ctx.INTEGER_(1).getText()));
        Range.range(lowerValue, BoundType.OPEN, upperValue, BoundType.OPEN);
        return null == ctx.LP_()
                ? Sarg.of(RexUnknownAs.UNKNOWN, ImmutableRangeSet.of(Range.range(lowerValue, BoundType.CLOSED, upperValue, BoundType.CLOSED)))
                : Sarg.of(RexUnknownAs.UNKNOWN, ImmutableRangeSet.of(Range.range(lowerValue, BoundType.OPEN, upperValue, BoundType.OPEN)));
    }
    
    private Sarg<BigDecimal> getArgList(final ArgListContext ctx) {
        RangeSet<BigDecimal> rangeSet = TreeRangeSet.create();
        for (TerminalNode each : ctx.INTEGER_()) {
            BigDecimal value = BigDecimal.valueOf(Long.parseLong(each.getText()));
            rangeSet.add(Range.singleton(value));
        }
        return Sarg.of(RexUnknownAs.UNKNOWN, rangeSet);
    }
    
    private Sarg<BigDecimal> getArgRangeList(final ArgRangeListContext ctx) {
        List<Range<BigDecimal>> rangeList = new LinkedList<>();
        for (ArgRangeContext each : ctx.argRange()) {
            BigDecimal lowerValue = BigDecimal.valueOf(Long.MIN_VALUE);
            BigDecimal upperValue = BigDecimal.valueOf(Long.MAX_VALUE);
            if (null != each.NEGETIVE_INFINITY_() && null != each.INTEGER_(0)) {
                String upper = each.INTEGER_(0).getText();
                upperValue = BigDecimal.valueOf(Long.parseLong(upper));
            }
            if (null != each.POSITIVE_INFINITY_() && null != each.INTEGER_(0)) {
                String lower = each.INTEGER_(0).getText();
                lowerValue = BigDecimal.valueOf(Long.parseLong(lower));
            }
            if (null == each.NEGETIVE_INFINITY_() && null == each.POSITIVE_INFINITY_()) {
                String lower = each.INTEGER_(0).getText();
                String upper = each.INTEGER_(1).getText();
                lowerValue = BigDecimal.valueOf(Long.parseLong(lower));
                upperValue = BigDecimal.valueOf(Long.parseLong(upper));
            }
            if (null == each.LP_()) {
                Range<BigDecimal> range = Range.range(lowerValue, BoundType.CLOSED, upperValue, BoundType.CLOSED);
                rangeList.add(range);
            } else {
                Range<BigDecimal> range = Range.range(lowerValue, BoundType.OPEN, upperValue, BoundType.OPEN);
                rangeList.add(range);
            }
        }
        return Sarg.of(RexUnknownAs.UNKNOWN, ImmutableRangeSet.copyOf(rangeList));
    }
    
    private RelDataType getType(final TypeContext ctx) {
        if (null != ctx.INTEGER()) {
            return typeFactory.createSqlType(SqlTypeName.INTEGER);
        }
        if (null != ctx.DATE()) {
            return typeFactory.createSqlType(SqlTypeName.DATE);
        }
        if (null != ctx.BIGINT()) {
            return typeFactory.createSqlType(SqlTypeName.BIGINT);
        }
        if (null != ctx.VARCHAR()) {
            return typeFactory.createSqlType(SqlTypeName.VARCHAR);
        }
        return typeFactory.createSqlType(SqlTypeName.VARCHAR);
    }
}
