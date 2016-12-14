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

package com.alibaba.druid.wall;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.visitor.SQLEvalVisitor;
import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils;
import com.alibaba.druid.sql.visitor.functions.Nil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

import static com.alibaba.druid.sql.visitor.SQLEvalVisitor.EVAL_VALUE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WallVisitorUtils {
    
    public static Object getValue(final SQLExpr sqlExpr) {
        if (null != sqlExpr && sqlExpr.getAttributes().containsKey(EVAL_VALUE)) {
            return sqlExpr.getAttribute(EVAL_VALUE);
        }
        if (sqlExpr instanceof SQLBinaryOpExpr) {
            return getBinaryOpValue((SQLBinaryOpExpr) sqlExpr);
        }
        if (sqlExpr instanceof SQLBooleanExpr) {
            return ((SQLBooleanExpr) sqlExpr).isValue();
        }
        if (sqlExpr instanceof SQLNumericLiteralExpr) {
            return ((SQLNumericLiteralExpr) sqlExpr).getNumber();
        }
        if (sqlExpr instanceof SQLCharExpr) {
            return ((SQLCharExpr) sqlExpr).getText();
        }
        if (sqlExpr instanceof SQLNCharExpr) {
            return ((SQLNCharExpr) sqlExpr).getText();
        }
        if (sqlExpr instanceof SQLNotExpr) {
            Object result = getValue(((SQLNotExpr) sqlExpr).getExpr());
            if (result instanceof Boolean) {
                return !(Boolean) result;
            }
        }
        if (sqlExpr instanceof SQLQueryExpr) {
            SQLSelect select = ((SQLQueryExpr) sqlExpr).getSubQuery();
            if (SQLEvalVisitorUtils.isSimpleCountTableSource(select)) {
                return 1;
            }
            if (SQLEvalVisitorUtils.isSimpleCaseTableSource(select)) {
                return getValue(((SQLSelectQueryBlock) select.getQuery()).getSelectList().get(0).getExpr());
            }
        }
        if (sqlExpr instanceof SQLMethodInvokeExpr || sqlExpr instanceof SQLBetweenExpr || sqlExpr instanceof SQLInListExpr || sqlExpr instanceof SQLUnaryExpr || sqlExpr instanceof SQLCaseExpr) {
            return eval(sqlExpr, Collections.emptyList());
        }
        return null;
    }
    
    private static Object getBinaryOpValue(final SQLBinaryOpExpr sqlBinaryOpExpr) {
        if (SQLBinaryOperator.BooleanOr == sqlBinaryOpExpr.getOperator()) {
            return getBooleanOrValue(sqlBinaryOpExpr);
        }
        if (SQLBinaryOperator.BooleanAnd == sqlBinaryOpExpr.getOperator()) {
            return getBooleanAndValue(sqlBinaryOpExpr);
        }
        if (sqlBinaryOpExpr.getLeft() instanceof SQLName) {
            if (sqlBinaryOpExpr.getRight() instanceof SQLName) {
                if (sqlBinaryOpExpr.getLeft().toString().equalsIgnoreCase(sqlBinaryOpExpr.getRight().toString())) {
                    switch (sqlBinaryOpExpr.getOperator()) {
                        case Equality:
                        case Like:
                            return Boolean.TRUE;
                        case NotEqual:
                        case GreaterThan:
                        case GreaterThanOrEqual:
                        case LessThan:
                        case LessThanOrEqual:
                        case LessThanOrGreater:
                        case NotLike:
                            return Boolean.FALSE;
                        default:
                            break;
                    }
                }
            } else {
                switch (sqlBinaryOpExpr.getOperator()) {
                    case Equality:
                    case NotEqual:
                    case GreaterThan:
                    case GreaterThanOrEqual:
                    case LessThan:
                    case LessThanOrEqual:
                    case LessThanOrGreater:
                        return null;
                    default:
                        break;
                }
            }
        }
        if (sqlBinaryOpExpr.getLeft() instanceof SQLValuableExpr && sqlBinaryOpExpr.getRight() instanceof SQLValuableExpr) {
            Object evalValue = getValuableEqualValue(sqlBinaryOpExpr);
            if (null != evalValue) {
                return evalValue;
            }
        }
        return eval(sqlBinaryOpExpr, Collections.emptyList());
    }
    
    private static Object getBooleanOrValue(final SQLBinaryOpExpr sqlBinaryOpExpr) {
        List<SQLExpr> groupList = SQLUtils.split(sqlBinaryOpExpr);
        boolean allFalse = true;
        for (int i = groupList.size() - 1; i >= 0; --i) {
            Boolean booleanVal = SQLEvalVisitorUtils.castToBoolean(getValue(groupList.get(i)));
            if (Boolean.TRUE == booleanVal) {
                return true;
            }
            if (Boolean.FALSE != booleanVal) {
                allFalse = false;
            }
        }
        return allFalse ? false : null;
    }
    
    private static Object getBooleanAndValue(final SQLBinaryOpExpr sqlBinaryOpExpr) {
        List<SQLExpr> groupList = SQLUtils.split(sqlBinaryOpExpr);
        Boolean result = Boolean.TRUE;
        for (int i = groupList.size() - 1; i >= 0; --i) {
            Boolean booleanVal = SQLEvalVisitorUtils.castToBoolean(getValue(groupList.get(i)));
            if (null == booleanVal && Boolean.FALSE != result) {
                result = null;
            } else {
                result = booleanVal;
            }
        }
        return result;
    }
    
    private static Object getValuableEqualValue(final SQLBinaryOpExpr sqlBinaryOpExpr) {
        Object leftValue = ((SQLValuableExpr) sqlBinaryOpExpr.getLeft()).getValue();
        Object rightValue = ((SQLValuableExpr) sqlBinaryOpExpr.getRight()).getValue();
        Boolean result = null;
        if (SQLBinaryOperator.Equality == sqlBinaryOpExpr.getOperator()) {
            result = SQLEvalVisitorUtils.eq(leftValue, rightValue);
            sqlBinaryOpExpr.putAttribute(EVAL_VALUE, result);
        } else if (SQLBinaryOperator.NotEqual == sqlBinaryOpExpr.getOperator()) {
            result = !SQLEvalVisitorUtils.eq(leftValue, rightValue);
            sqlBinaryOpExpr.putAttribute(EVAL_VALUE, result);
        }
        return result;
    }
    
    private static Object eval(final SQLObject sqlObject, final List<Object> parameters) {
        SQLEvalVisitor visitor = SQLEvalVisitorUtils.createEvalVisitor(null);
        visitor.setParameters(parameters);
        visitor.registerFunction("rand", Nil.instance);
        visitor.registerFunction("sin", Nil.instance);
        visitor.registerFunction("cos", Nil.instance);
        visitor.registerFunction("asin", Nil.instance);
        visitor.registerFunction("acos", Nil.instance);
        sqlObject.accept(visitor);
        return sqlObject instanceof SQLNumericLiteralExpr ? ((SQLNumericLiteralExpr) sqlObject).getNumber() : sqlObject.getAttribute(EVAL_VALUE);
    }
}
