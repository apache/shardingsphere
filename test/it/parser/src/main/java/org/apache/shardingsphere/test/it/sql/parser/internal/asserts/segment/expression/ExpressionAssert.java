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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExtractArgExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.IntervalDayToSecondExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.IntervalYearToMonthExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ValuesExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ColumnWithJoinOperatorSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnWithJoinOperatorAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.generic.DataTypeAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.InsertValuesClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.projection.ProjectionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.impl.SelectStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedBetweenExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedBinaryOperationExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedCaseWhenExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedCollateExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedExistsSubquery;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedExtractArgExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedInExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedIntervalDayToSecondExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedIntervalExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedIntervalYearToMonthExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedListExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedMatchExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedNotExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedTypeCastExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedValuesExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedVariableSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.complex.ExpectedCommonExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedLiteralExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedParameterMarkerExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedSubquery;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.function.ExpectedFunction;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *  Expression assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionAssert {
    
    /**
     * Assert parameter marker expression.
     *
     * @param assertContext assert context
     * @param actual actual parameter marker expression segment
     * @param expected expected parameter marker expression
     */
    public static void assertParameterMarkerExpression(final SQLCaseAssertContext assertContext,
                                                       final ParameterMarkerExpressionSegment actual, final ExpectedParameterMarkerExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual parameter marker expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual parameter marker expression should exist."));
            assertThat(assertContext.getText("Parameter marker index assertion error: "), actual.getParameterMarkerIndex(), is(expected.getParameterIndex()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert literal expression.
     *
     * @param assertContext assert context
     * @param actual actual literal expression segment
     * @param expected expected literal expression
     */
    public static void assertLiteralExpression(final SQLCaseAssertContext assertContext,
                                               final LiteralExpressionSegment actual, final ExpectedLiteralExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual literal expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual literal expression should exist."));
            assertThat(assertContext.getText("Literal assertion error: "), String.valueOf(actual.getLiterals()), is(expected.getValue()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert common expression.
     *
     * @param assertContext assert context
     * @param actual actual common expression segment
     * @param expected expected common expression
     */
    public static void assertCommonExpression(final SQLCaseAssertContext assertContext,
                                              final ComplexExpressionSegment actual, final ExpectedCommonExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual common expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual common expression should exist."));
            String expectedText = SQLCaseType.LITERAL == assertContext.getCaseType() && null != expected.getLiteralText() ? expected.getLiteralText() : expected.getText();
            assertThat(assertContext.getText("Common expression text assertion error: "), actual.getText(), is(expectedText));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert subquery expression.
     *
     * @param assertContext assert context
     * @param actual actual subquery expression segment
     * @param expected expected subquery expression
     */
    public static void assertSubqueryExpression(final SQLCaseAssertContext assertContext,
                                                final SubqueryExpressionSegment actual, final ExpectedSubquery expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual subquery expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual subquery expression should exist."));
            assertSubquery(assertContext, actual.getSubquery(), expected);
        }
    }
    
    /**
     * Assert subquery.
     *
     * @param assertContext assert context
     * @param actual actual subquery segment
     * @param expected expected subquery expression
     */
    public static void assertSubquery(final SQLCaseAssertContext assertContext,
                                      final SubquerySegment actual, final ExpectedSubquery expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual subquery should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual subquery should exist."));
            SelectStatementAssert.assertIs(assertContext, actual.getSelect(), expected.getSelectTestCases());
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert exists subquery expression.
     *
     * @param assertContext assert context
     * @param actual actual exists subquery expression
     * @param expected expected exists subquery expression
     */
    public static void assertExistsSubqueryExpression(final SQLCaseAssertContext assertContext,
                                                      final ExistsSubqueryExpression actual, final ExpectedExistsSubquery expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual exists subquery should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual exists subquery should exist."));
            assertSubquery(assertContext, actual.getSubquery(), expected.getSubquery());
            assertThat(assertContext.getText("Exists subquery expression not value assert error."),
                    actual.isNot(), is(expected.isNot()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert binary operation expression.
     * @param assertContext assert context
     * @param actual actual binary operation expression
     * @param expected expected binary operation expression
     */
    public static void assertBinaryOperationExpression(final SQLCaseAssertContext assertContext,
                                                       final BinaryOperationExpression actual, final ExpectedBinaryOperationExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual binary operation expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual binary operation expression should exist."));
            assertExpression(assertContext, actual.getLeft(), expected.getLeft());
            assertThat(assertContext.getText("Binary operation expression operator assert error."),
                    actual.getOperator(), is(expected.getOperator()));
            assertExpression(assertContext, actual.getRight(), expected.getRight());
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert in operation expression.
     * @param assertContext assert context
     * @param actual actual in operation expression
     * @param expected expected in operation expression
     */
    public static void assertInExpression(final SQLCaseAssertContext assertContext,
                                          final InExpression actual, final ExpectedInExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual in expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual in expression should exist."));
            assertExpression(assertContext, actual.getLeft(), expected.getLeft());
            assertThat(assertContext.getText("In expression not value assert error."),
                    actual.isNot(), is(expected.isNot()));
            assertExpression(assertContext, actual.getRight(), expected.getRight());
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert not operation expression.
     * @param assertContext assert context
     * @param actual actual not operation expression
     * @param expected expected not operation expression
     */
    public static void assertNotExpression(final SQLCaseAssertContext assertContext,
                                           final NotExpression actual, final ExpectedNotExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual not expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual not expression should exist."));
            assertExpression(assertContext, actual.getExpression(), expected.getExpr());
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert list operation expression.
     * @param assertContext assert context
     * @param actual actual list operation expression
     * @param expected expected list operation expression
     */
    public static void assertListExpression(final SQLCaseAssertContext assertContext,
                                            final ListExpression actual, final ExpectedListExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual list expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual list expression should exist."));
            assertThat(assertContext.getText("List expression item size assert error."),
                    actual.getItems().size(), is(expected.getItems().size()));
            Iterator<ExpressionSegment> actualItems = actual.getItems().iterator();
            Iterator<ExpectedExpression> expectedItems = expected.getItems().iterator();
            while (actualItems.hasNext()) {
                assertExpression(assertContext, actualItems.next(), expectedItems.next());
            }
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert between operation expression.
     * @param assertContext assert context
     * @param actual actual between operation expression
     * @param expected expected between operation expression
     */
    public static void assertBetweenExpression(final SQLCaseAssertContext assertContext,
                                               final BetweenExpression actual, final ExpectedBetweenExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual between expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual between expression should exist."));
            assertExpression(assertContext, actual.getLeft(), expected.getLeft());
            assertExpression(assertContext, actual.getBetweenExpr(), expected.getBetweenExpr());
            assertExpression(assertContext, actual.getAndExpr(), expected.getAndExpr());
            assertThat(assertContext.getText("Between expression not value assert error."),
                    actual.isNot(), is(expected.isNot()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert function.
     *
     * @param assertContext assert context
     * @param actual actual function segment
     * @param expected expected function segment
     */
    public static void assertFunction(final SQLCaseAssertContext assertContext, final FunctionSegment actual, final ExpectedFunction expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        assertThat(assertContext.getText("Function method name assertion error: "), actual.getFunctionName(), is(expected.getFunctionName()));
        String expectedText = SQLCaseType.LITERAL == assertContext.getCaseType() && null != expected.getLiteralText()
                ? expected.getLiteralText()
                : expected.getText();
        assertThat(assertContext.getText("Function text name assertion error: "), actual.getText(), is(expectedText));
        assertThat(assertContext.getText("Function parameter size assertion error: "), actual.getParameters().size(), is(expected.getParameters().size()));
        Iterator<ExpectedExpression> expectedIterator = expected.getParameters().iterator();
        Iterator<ExpressionSegment> actualIterator = actual.getParameters().iterator();
        while (expectedIterator.hasNext()) {
            ExpressionAssert.assertExpression(assertContext, actualIterator.next(), expectedIterator.next());
        }
        if (expected.getOwner() != null) {
            OwnerAssert.assertIs(assertContext, actual.getOwner(), expected.getOwner());
        }
    }
    
    /**
     * Assert collate.
     *
     * @param assertContext assert context
     * @param actual actual collate expression
     * @param expected expected collate expression
     */
    public static void assertCollateExpression(final SQLCaseAssertContext assertContext, final CollateExpression actual, final ExpectedCollateExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual collate expression should not exist."));
        } else {
            assertExpression(assertContext, actual.getCollateName(), expected.getCollateName());
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert case when expression.
     *
     * @param assertContext assert context
     * @param actual actual case when expression
     * @param expected expected case when expression
     */
    public static void assertCaseWhenExpression(final SQLCaseAssertContext assertContext, final CaseWhenExpression actual, final ExpectedCaseWhenExpression expected) {
        assertThat(assertContext.getText("When exprs size is not same!"), actual.getWhenExprs().size(), is(expected.getWhenExprs().size()));
        assertThat(assertContext.getText("Then exprs size is not same!"), actual.getThenExprs().size(), is(expected.getThenExprs().size()));
        Iterator<ExpectedExpression> whenExprsIterator = expected.getWhenExprs().iterator();
        for (ExpressionSegment each : actual.getWhenExprs()) {
            assertExpression(assertContext, each, whenExprsIterator.next());
        }
        Iterator<ExpectedExpression> thenExprsIterator = expected.getThenExprs().iterator();
        for (ExpressionSegment each : actual.getThenExprs()) {
            assertExpression(assertContext, each, thenExprsIterator.next());
        }
        assertExpression(assertContext, actual.getCaseExpr(), expected.getCaseExpr());
        assertExpression(assertContext, actual.getElseExpr(), expected.getElseExpr());
    }
    
    private static void assertTypeCastExpression(final SQLCaseAssertContext assertContext, final TypeCastExpression actual, final ExpectedTypeCastExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Type cast expression should not exist."));
            return;
        }
        assertNotNull(actual, assertContext.getText("Type cast expression is expected."));
        assertThat(assertContext.getText("Actual data type is different with expected in type case expression."), actual.getDataType(), is(expected.getDataType()));
        assertExpression(assertContext, actual.getExpression(), expected.getExpression());
    }
    
    private static void assertVariableSegment(final SQLCaseAssertContext assertContext, final VariableSegment actual, final ExpectedVariableSegment expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Variable segment should not exist."));
            return;
        }
        assertThat(assertContext.getText("Actual scope is different with expected scope."), actual.getScope().orElse(null), is(expected.getScope()));
        assertThat(assertContext.getText("Actual variable is different with expected variable."), actual.getVariable(), is(expected.getVariable()));
    }
    
    private static void assertValuesExpression(final SQLCaseAssertContext assertContext, final ValuesExpression actual, final ExpectedValuesExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Values segment should not exist."));
            return;
        }
        assertNotNull(actual, assertContext.getText("Values segment should exist."));
        if (null == expected.getInsertValuesClause()) {
            assertTrue(actual.getRowConstructorList().isEmpty(), "Values expression should not exist.");
        } else {
            assertFalse(actual.getRowConstructorList().isEmpty(), assertContext.getText("Values expression should exist."));
            InsertValuesClauseAssert.assertIs(assertContext, actual.getRowConstructorList(), expected.getInsertValuesClause());
        }
    }
    
    /**
     * Assert extract arg expression.
     *
     * @param assertContext assert context
     * @param actual actual extract arg expression
     * @param expected expected extract arg expression
     */
    private static void assertExtractArgExpression(final SQLCaseAssertContext assertContext, final ExtractArgExpression actual, final ExpectedExtractArgExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Extract arg expression should not exist."));
            return;
        }
        assertThat(assertContext.getText("Extract arg expression assertion error: "), actual.getText(), is(expected.getText()));
    }
    
    private static void assertMatchSegment(final SQLCaseAssertContext assertContext, final MatchAgainstExpression actual, final ExpectedMatchExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual match expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual match expression should exist"));
            assertExpression(assertContext, actual.getExpr(), expected.getExpr());
        }
    }
    
    /**
     * Assert expression by actual expression segment class type.
     *
     * @param assertContext assert context
     * @param actual actual interval expression
     * @param expected expected interval expression
     */
    private static void assertIntervalExpression(final SQLCaseAssertContext assertContext, final IntervalExpressionProjection actual, final ExpectedIntervalExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual interval expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual interval expression should exist"));
            assertExpression(assertContext, actual.getLeft(), expected.getLeft());
            assertExpression(assertContext, actual.getRight(), expected.getRight());
            assertExpression(assertContext, actual.getMinus(), expected.getOperator());
            if (null != actual.getDayToSecondExpression()) {
                assertIntervalDayToSecondExpression(assertContext, actual.getDayToSecondExpression(), expected.getDayToSecondExpression());
            } else {
                assertIntervalYearToMonthExpression(assertContext, actual.getYearToMonthExpression(), expected.getYearToMonthExpression());
            }
        }
    }
    
    /**
     * Assert expression by actual expression segment class type.
     *
     * @param assertContext assert context
     * @param actual actual interval day to second expression
     * @param expected expected interval day to second expression
     */
    private static void assertIntervalDayToSecondExpression(final SQLCaseAssertContext assertContext,
                                                            final IntervalDayToSecondExpression actual, final ExpectedIntervalDayToSecondExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual interval expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual interval expression should exist"));
            if (null != actual.getLeadingFieldPrecision()) {
                assertEquals(actual.getLeadingFieldPrecision(), expected.getLeadingFieldPrecision());
            } else {
                assertNull(expected.getLeadingFieldPrecision(), assertContext.getText("Actual leading field precision should not exist."));
            }
            if (null != actual.getFractionalSecondPrecision()) {
                assertEquals(actual.getFractionalSecondPrecision(), expected.getFractionalSecondPrecision());
            } else {
                assertNull(expected.getFractionalSecondPrecision(), assertContext.getText("Actual fractional second precision should not exist."));
            }
        }
    }
    
    /**
     * Assert expression by actual expression segment class type.
     *
     * @param assertContext assert context
     * @param actual actual interval year to month expression
     * @param expected expected interval year to month expression
     */
    private static void assertIntervalYearToMonthExpression(final SQLCaseAssertContext assertContext,
                                                            final IntervalYearToMonthExpression actual, final ExpectedIntervalYearToMonthExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual interval expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual interval expression should exist"));
            if (null != actual.getLeadingFieldPrecision()) {
                assertEquals(actual.getLeadingFieldPrecision(), expected.getLeadingFieldPrecision());
            } else {
                assertNull(expected.getLeadingFieldPrecision(), assertContext.getText("Actual leading field precision should not exist."));
            }
        }
    }
    
    /**
     * Assert expression by actual expression segment class type.
     *
     * @param assertContext assert context
     * @param actual actual expression segment
     * @param expected expected expression
     * @throws UnsupportedOperationException When expression segment class type is not supported.
     */
    public static void assertExpression(final SQLCaseAssertContext assertContext,
                                        final ExpressionSegment actual, final ExpectedExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual expression should not exist."));
            return;
        }
        assertNotNull(actual, assertContext.getText("Actual expression should exist."));
        if (actual instanceof BinaryOperationExpression) {
            assertBinaryOperationExpression(assertContext, (BinaryOperationExpression) actual, expected.getBinaryOperationExpression());
        } else if (actual instanceof SubqueryExpressionSegment) {
            assertSubqueryExpression(assertContext, (SubqueryExpressionSegment) actual, expected.getSubquery());
        } else if (actual instanceof ColumnSegment) {
            ColumnAssert.assertIs(assertContext, (ColumnSegment) actual, expected.getColumn());
        } else if (actual instanceof DataTypeSegment) {
            DataTypeAssert.assertIs(assertContext, (DataTypeSegment) actual, expected.getDataType());
        } else if (actual instanceof LiteralExpressionSegment) {
            assertLiteralExpression(assertContext, (LiteralExpressionSegment) actual, expected.getLiteralExpression());
        } else if (actual instanceof ParameterMarkerExpressionSegment) {
            assertParameterMarkerExpression(assertContext, (ParameterMarkerExpressionSegment) actual, expected.getParameterMarkerExpression());
        } else if (actual instanceof ExistsSubqueryExpression) {
            assertExistsSubqueryExpression(assertContext, (ExistsSubqueryExpression) actual, expected.getExistsSubquery());
        } else if (actual instanceof CommonExpressionSegment) {
            assertCommonExpression(assertContext, (ComplexExpressionSegment) actual, expected.getCommonExpression());
        } else if (actual instanceof InExpression) {
            assertInExpression(assertContext, (InExpression) actual, expected.getInExpression());
        } else if (actual instanceof NotExpression) {
            assertNotExpression(assertContext, (NotExpression) actual, expected.getNotExpression());
        } else if (actual instanceof ListExpression) {
            assertListExpression(assertContext, (ListExpression) actual, expected.getListExpression());
        } else if (actual instanceof BetweenExpression) {
            assertBetweenExpression(assertContext, (BetweenExpression) actual, expected.getBetweenExpression());
        } else if (actual instanceof ExpressionProjectionSegment) {
            ProjectionAssert.assertProjection(assertContext, (ExpressionProjectionSegment) actual, expected.getExpressionProjection());
        } else if (actual instanceof AggregationProjectionSegment) {
            ProjectionAssert.assertProjection(assertContext, (AggregationProjectionSegment) actual, expected.getAggregationProjection());
        } else if (actual instanceof FunctionSegment) {
            assertFunction(assertContext, (FunctionSegment) actual, expected.getFunction());
        } else if (actual instanceof CollateExpression) {
            assertCollateExpression(assertContext, (CollateExpression) actual, expected.getCollateExpression());
        } else if (actual instanceof CaseWhenExpression) {
            assertCaseWhenExpression(assertContext, (CaseWhenExpression) actual, expected.getCaseWhenExpression());
        } else if (actual instanceof TypeCastExpression) {
            assertTypeCastExpression(assertContext, (TypeCastExpression) actual, expected.getTypeCastExpression());
        } else if (actual instanceof VariableSegment) {
            assertVariableSegment(assertContext, (VariableSegment) actual, expected.getVariableSegment());
        } else if (actual instanceof ValuesExpression) {
            assertValuesExpression(assertContext, (ValuesExpression) actual, expected.getValuesExpression());
        } else if (actual instanceof ExtractArgExpression) {
            assertExtractArgExpression(assertContext, (ExtractArgExpression) actual, expected.getExtractArgExpression());
        } else if (actual instanceof MatchAgainstExpression) {
            assertMatchSegment(assertContext, (MatchAgainstExpression) actual, expected.getMatchExpression());
        } else if (actual instanceof ColumnWithJoinOperatorSegment) {
            ColumnWithJoinOperatorAssert.assertIs(assertContext, (ColumnWithJoinOperatorSegment) actual, expected.getColumnWithJoinOperatorSegment());
        } else if (actual instanceof IntervalExpressionProjection) {
            assertIntervalExpression(assertContext, (IntervalExpressionProjection) actual, expected.getIntervalExpression());
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported expression: %s", actual.getClass().getName()));
        }
    }
}
