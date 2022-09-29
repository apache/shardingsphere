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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable;

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
import org.apache.calcite.util.Sarg;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.exception.OptimizationSQLRexNodeException;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeBaseVisitor;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.ArgListContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.ArgRangeContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.ArgRangeListContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.CastContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.ConstantContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.ExpressionContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.InputContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.InputRefContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.OpContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.ParamWithTypeContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.ParameterContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.SearchArgsContext;
import org.apache.shardingsphere.sqlfederation.optimizer.parser.rexnode.ParseRexNodeParser.TypeContext;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
public final class ParseRexNodeVisitorImpl extends ParseRexNodeBaseVisitor<RexNode> {
    
    private RexBuilder rexBuilder;
    
    private JavaTypeFactory typeFactory;
    
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
        RelDataType nonNullableInt = typeFactory.createSqlType(SqlTypeName.INTEGER);
        return rexBuilder.makeInputRef(nonNullableInt, index);
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
            RelDataType varchar = typeFactory.createSqlType(SqlTypeName.VARCHAR);
            return rexBuilder.makeLiteral(ctx.STRING_().getText(), varchar, false);
        }
        throw new OptimizationSQLRexNodeException(ctx.getText());
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
            if ((null != each.NEGETIVE_INFINITY_()) && (null != each.INTEGER_(0))) {
                String upper = each.INTEGER_(0).getText();
                upperValue = BigDecimal.valueOf(Long.parseLong(upper));
            }
            if ((null != each.POSITIVE_INFINITY_()) && (null != each.INTEGER_(0))) {
                String lower = each.INTEGER_(0).getText();
                lowerValue = BigDecimal.valueOf(Long.parseLong(lower));
            }
            if ((null == each.NEGETIVE_INFINITY_()) && (null == each.POSITIVE_INFINITY_())) {
                String lower = each.INTEGER_(0).getText();
                String upper = each.INTEGER_(1).getText();
                lowerValue = BigDecimal.valueOf(Long.parseLong(lower));
                upperValue = BigDecimal.valueOf(Long.parseLong(upper));
            }
            if (null != each.LP_()) {
                Range<BigDecimal> range = Range.range(lowerValue, BoundType.OPEN, upperValue, BoundType.OPEN);
                rangeList.add(range);
            } else {
                Range<BigDecimal> range = Range.range(lowerValue, BoundType.CLOSED, upperValue, BoundType.CLOSED);
                rangeList.add(range);
            }
        }
        return Sarg.of(RexUnknownAs.UNKNOWN, ImmutableRangeSet.copyOf(rangeList));
    }
    
    private RelDataType getType(final TypeContext ctx) {
        return null == ctx.INTEGER() ? typeFactory.createSqlType(SqlTypeName.VARCHAR) : typeFactory.createSqlType(SqlTypeName.INTEGER);
    }
}
