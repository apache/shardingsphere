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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExtractArgExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.KeyValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ValuesExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.interval.IntervalDayToSecondExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.interval.IntervalUnitExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.interval.IntervalYearToMonthExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.join.OuterJoinExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.json.JsonNullClauseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.multiset.MultisetExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlQueryAndExistsFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.OuterJoinExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.generic.DataTypeAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.InsertValuesClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.projection.ProjectionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.SelectStatementAssert;
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
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedIntervalExpressionProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedIntervalUnitExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedIntervalYearToMonthExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedKeyValueSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedListExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedMatchExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedMultisetExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedNotExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedQuantifySubqueryExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedRowExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedTypeCastExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedUnaryOperationExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedValuesExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedVariableSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.complex.ExpectedCommonExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedLiteralExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedParameterMarkerExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedSubquery;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.function.ExpectedFunction;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.json.ExpectedJsonNullClauseSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.xmlquery.ExpectedXmlQueryAndExistsFunctionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Expression assert.
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
     *
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
     *
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
     *
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
     *
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
     *
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
            assertExpression(assertContext, actualIterator.next(), expectedIterator.next());
        }
        if (null != expected.getOwner()) {
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
    
    private static void assertIntervalExpression(final SQLCaseAssertContext assertContext, final IntervalExpression actual, final ExpectedIntervalExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual interval expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual interval expression should exist"));
            assertExpression(assertContext, actual.getValue(), expected.getValue());
            assertThat(assertContext.getText("Actual interval unit is different with expected interval unit."), actual.getIntervalUnit(), is(expected.getIntervalUnit()));
        }
    }
    
    /**
     * Assert expression by actual expression segment class type.
     *
     * @param assertContext assert context
     * @param actual actual interval expression
     * @param expected expected interval expression
     */
    private static void assertIntervalExpression(final SQLCaseAssertContext assertContext, final IntervalExpressionProjection actual, final ExpectedIntervalExpressionProjection expected) {
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
    
    private static void assertIntervalUnitExpression(final SQLCaseAssertContext assertContext, final IntervalUnitExpression actual, final ExpectedIntervalUnitExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual interval unit expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual interval unit expression should exist"));
            assertThat(assertContext.getText("Actual interval unit is different with expected interval unit."), actual.getIntervalUnit(), is(expected.getIntervalUnit()));
        }
    }
    
    private static void assertQuantifySubqueryExpression(final SQLCaseAssertContext assertContext, final QuantifySubqueryExpression actual, final ExpectedQuantifySubqueryExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual quantify subquery expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual quantify subquery expression should exist."));
            assertThat(assertContext.getText("Quantify operator assertion error: "), actual.getQuantifyOperator(), is(expected.getOperator()));
            assertSubquery(assertContext, actual.getSubquery(), expected.getSubquery());
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
                assertThat(actual.getLeadingFieldPrecision(), is(expected.getLeadingFieldPrecision()));
            } else {
                assertNull(expected.getLeadingFieldPrecision(), assertContext.getText("Actual leading field precision should not exist."));
            }
            if (null != actual.getFractionalSecondPrecision()) {
                assertThat(actual.getFractionalSecondPrecision(), is(expected.getFractionalSecondPrecision()));
            } else {
                assertThat(expected.getFractionalSecondPrecision(), is(assertContext.getText("Actual fractional second precision should not exist.")));
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
                assertThat(actual.getLeadingFieldPrecision(), is(expected.getLeadingFieldPrecision()));
            } else {
                assertNull(expected.getLeadingFieldPrecision(), assertContext.getText("Actual leading field precision should not exist."));
            }
        }
    }
    
    private static void assertMultisetExpression(final SQLCaseAssertContext assertContext, final MultisetExpression actual, final ExpectedMultisetExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Multiset expression should not exist."));
            return;
        }
        assertNotNull(actual, assertContext.getText("Multiset expression should exist."));
        assertExpression(assertContext, actual.getLeft(), expected.getLeft());
        assertExpression(assertContext, actual.getRight(), expected.getRight());
        assertThat(assertContext.getText("Multiset operator assertion error: "), actual.getOperator(), is(expected.getOperator()));
        assertThat(assertContext.getText("Multiset keyword assertion error: "), actual.getKeyWord(), is(expected.getKeyWord()));
    }
    
    /**
     * Assert row expression.
     *
     * @param assertContext assert context
     * @param actual actual row expression
     * @param expected expected row expression
     */
    private static void assertRowExpression(final SQLCaseAssertContext assertContext, final RowExpression actual, final ExpectedRowExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Row expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual list expression should not exist."));
            assertThat(assertContext.getText("Row expression item size assert error."),
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
     * Assert unary operation expression.
     *
     * @param assertContext assert context
     * @param actual actual unary operation expression
     * @param expected expected unary operation expression
     */
    private static void assertUnaryOperationExpression(final SQLCaseAssertContext assertContext, final UnaryOperationExpression actual, final ExpectedUnaryOperationExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual unary operation expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual unary operation expression should exist."));
            assertExpression(assertContext, actual.getExpression(), expected.getExpr());
            assertThat(assertContext.getText("Unary operation expression operator assert error."),
                    actual.getOperator(), is(expected.getOperator()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert xml query and exists function segment.
     *
     * @param assertContext assert context
     * @param actual actual xml query and exists function segment
     * @param expected expected xml query and exists function segment
     */
    private static void assertXmlQueryAndExistsFunctionSegment(final SQLCaseAssertContext assertContext, final XmlQueryAndExistsFunctionSegment actual,
                                                               final ExpectedXmlQueryAndExistsFunctionSegment expected) {
        assertThat(assertContext.getText("function name assertion error"), actual.getFunctionName(), is(expected.getFunctionName()));
        assertThat(assertContext.getText("xquery string assertion error"), actual.getXQueryString(), is(expected.getXQueryString()));
        assertThat(assertContext.getText("parameter size assertion error: "), actual.getParameters().size(), is(expected.getParameters().size()));
        Iterator<ExpectedExpression> expectedIterator = expected.getParameters().iterator();
        Iterator<ExpressionSegment> actualIterator = actual.getParameters().iterator();
        while (expectedIterator.hasNext()) {
            assertExpression(assertContext, actualIterator.next(), expectedIterator.next());
        }
    }
    
    /**
     * Assert key value segment.
     *
     * @param assertContext assert context
     * @param actual actual key value segment
     * @param expected expected key value segment
     */
    private static void assertKeyValueSegment(final SQLCaseAssertContext assertContext, final KeyValueSegment actual, final ExpectedKeyValueSegment expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual key value should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual key value should exist."));
            assertExpression(assertContext, actual.getKey(), expected.getKey());
            assertExpression(assertContext, actual.getValue(), expected.getValue());
        }
    }
    
    /**
     * Assert json null clause segment.
     *
     * @param assertContext assert context
     * @param actual actual json null clause segment
     * @param expected expected json null clause segment
     */
    private static void assertJsonNullClauseSegment(final SQLCaseAssertContext assertContext, final JsonNullClauseSegment actual, final ExpectedJsonNullClauseSegment expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual json null clause should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual json null clause should exist."));
            assertThat(assertContext.getText("Json null clause assertion error."), actual.getText(), is(expected.getText()));
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
        } else if (actual instanceof OuterJoinExpression) {
            OuterJoinExpressionAssert.assertIs(assertContext, (OuterJoinExpression) actual, expected.getOuterJoinExpression());
        } else if (actual instanceof IntervalExpressionProjection) {
            assertIntervalExpression(assertContext, (IntervalExpressionProjection) actual, expected.getIntervalExpressionProjection());
        } else if (actual instanceof MultisetExpression) {
            assertMultisetExpression(assertContext, (MultisetExpression) actual, expected.getMultisetExpression());
        } else if (actual instanceof RowExpression) {
            assertRowExpression(assertContext, (RowExpression) actual, expected.getRowExpression());
        } else if (actual instanceof UnaryOperationExpression) {
            assertUnaryOperationExpression(assertContext, (UnaryOperationExpression) actual, expected.getUnaryOperationExpression());
        } else if (actual instanceof XmlQueryAndExistsFunctionSegment) {
            assertXmlQueryAndExistsFunctionSegment(assertContext, (XmlQueryAndExistsFunctionSegment) actual, expected.getExpectedXmlQueryAndExistsFunctionSegment());
        } else if (actual instanceof KeyValueSegment) {
            assertKeyValueSegment(assertContext, (KeyValueSegment) actual, expected.getKeyValueSegment());
        } else if (actual instanceof JsonNullClauseSegment) {
            assertJsonNullClauseSegment(assertContext, (JsonNullClauseSegment) actual, expected.getJsonNullClauseSegment());
        } else if (actual instanceof IntervalExpression) {
            assertIntervalExpression(assertContext, (IntervalExpression) actual, expected.getIntervalExpression());
        } else if (actual instanceof IntervalUnitExpression) {
            assertIntervalUnitExpression(assertContext, (IntervalUnitExpression) actual, expected.getIntervalUnitExpression());
        } else if (actual instanceof QuantifySubqueryExpression) {
            assertQuantifySubqueryExpression(assertContext, (QuantifySubqueryExpression) actual, expected.getQuantifySubqueryExpression());
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported expression: %s", actual.getClass().getName()));
        }
    }
}
