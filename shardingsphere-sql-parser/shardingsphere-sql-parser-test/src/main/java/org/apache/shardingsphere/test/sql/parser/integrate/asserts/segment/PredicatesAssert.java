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

package org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.predicate.ExpectedAndPredicate;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.predicate.ExpectedOperator;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.predicate.ExpectedPredicate;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.predicate.value.ExpectedPredicateBetweenRightValue;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.predicate.value.ExpectedPredicateCompareRightValue;
import org.apache.shardingsphere.test.sql.parser.integrate.jaxb.cases.domain.segment.impl.predicate.value.ExpectedPredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Predicates assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PredicatesAssert {
    
    /**
     * Assert actual Predicate segments is correct with expected Predicate.
     *
     * @param assertContext assert context
     * @param actual actual Predicates
     * @param expected expected Predicate
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Collection<AndPredicate> actual, final List<ExpectedAndPredicate> expected) {
        assertThat(assertContext.getText("And predicate size assertion error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (AndPredicate each: actual) {
            Collection<PredicateSegment> actualPredicates = each.getPredicates();
            List<ExpectedPredicate> expectedPredicates = expected.get(count).getPredicates();
            assertThat(assertContext.getText("Predicates size assertion error: "), actualPredicates.size(), is(expectedPredicates.size()));
            assertPredicates(assertContext, actualPredicates, expectedPredicates);
            count++;
        }
    }
    
    private static void assertPredicates(final SQLCaseAssertContext assertContext, final Collection<PredicateSegment> actual, final List<ExpectedPredicate> expected) {
        int count = 0;
        for (PredicateSegment each: actual) {
            ExpectedPredicate expectedPredicate = expected.get(count);
            // TODO assert other type of left value
            ColumnAssert.assertIs(assertContext, each.getColumn(), expectedPredicate.getColumnLeftValue());
            if (each.getRightValue() instanceof ColumnSegment) {
                ColumnAssert.assertIs(assertContext, (ColumnSegment) each.getRightValue(), expectedPredicate.getColumnRightValue());
            } else if (each.getRightValue() instanceof PredicateCompareRightValue) {
                assertOperator(assertContext, each, expectedPredicate.getOperator());
                assertCompareRightValue(assertContext, (PredicateCompareRightValue) each.getRightValue(), expectedPredicate.getCompareRightValue());
            } else if (each.getRightValue() instanceof PredicateInRightValue) {
                assertOperator(assertContext, each, expectedPredicate.getOperator());
                assertInRightValue(assertContext, (PredicateInRightValue) each.getRightValue(), expectedPredicate.getInRightValue());
            } else if (each.getRightValue() instanceof PredicateBetweenRightValue) {
                assertOperator(assertContext, each, expectedPredicate.getOperator());
                assertBetweenRightValue(assertContext, (PredicateBetweenRightValue) each.getRightValue(), expectedPredicate.getBetweenRightValue());
            }
            // TODO add other right value assertion
            SQLSegmentAssert.assertIs(assertContext, each, expectedPredicate);
            count++;
        }
    }
    
    private static void assertOperator(final SQLCaseAssertContext assertContext, final PredicateSegment actual, final ExpectedOperator expected) {
        if (actual.getRightValue() instanceof PredicateCompareRightValue) {
            assertNotNull(assertContext.getText("Operator assertion error: "), expected);
            assertThat(assertContext.getText("Operator assertion error: "), ((PredicateCompareRightValue) actual.getRightValue()).getOperator(), is(expected.getType()));
        }
    }
    
    private static void assertCompareRightValue(final SQLCaseAssertContext assertContext, final PredicateCompareRightValue actual, final ExpectedPredicateCompareRightValue expected) {
        if (actual.getExpression() instanceof ParameterMarkerExpressionSegment) {
            ExpressionAssert.assertParameterMarkerExpression(assertContext, (ParameterMarkerExpressionSegment) actual.getExpression(), expected.getParameterMarkerExpression());
        } else if (actual.getExpression() instanceof LiteralExpressionSegment) {
            ExpressionAssert.assertLiteralExpression(assertContext, (LiteralExpressionSegment) actual.getExpression(), expected.getLiteralExpression());
        } else if (actual.getExpression() instanceof CommonExpressionSegment) {
            ExpressionAssert.assertCommonExpression(assertContext, (ComplexExpressionSegment) actual.getExpression(), expected.getCommonExpression());
        } else if (actual.getExpression() instanceof SubqueryExpressionSegment) {
            ExpressionAssert.assertSubqueryExpression(assertContext, (SubqueryExpressionSegment) actual.getExpression(), expected.getSubquery());
        }
    }
    
    private static void assertInRightValue(final SQLCaseAssertContext assertContext, final PredicateInRightValue actual, final ExpectedPredicateInRightValue expected) {
        assertNotNull(assertContext.getText("Expected predicate in right value can not be null"), expected);
        assertParameterMarkerExpressionSegment(assertContext, actual, expected);
        assertLiteralExpressionSegment(assertContext, actual, expected);
        assertCommonExpressionSegment(assertContext, actual, expected);
        assertSubqueryExpressionSegment(assertContext, actual, expected);
    }
    
    private static void assertParameterMarkerExpressionSegment(final SQLCaseAssertContext assertContext, final PredicateInRightValue actual, final ExpectedPredicateInRightValue expected) {
        int count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                ExpressionAssert.assertParameterMarkerExpression(assertContext, (ParameterMarkerExpressionSegment) each, expected.getParameterMarkerExpressions().get(count));
                count++;
            }
        }
    }
    
    private static void assertLiteralExpressionSegment(final SQLCaseAssertContext assertContext, final PredicateInRightValue actual, final ExpectedPredicateInRightValue expected) {
        int count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof LiteralExpressionSegment) {
                ExpressionAssert.assertLiteralExpression(assertContext, (LiteralExpressionSegment) each, expected.getLiteralExpressions().get(count));
                count++;
            }
        }
    }
    
    private static void assertCommonExpressionSegment(final SQLCaseAssertContext assertContext, final PredicateInRightValue actual, final ExpectedPredicateInRightValue expected) {
        int count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof CommonExpressionSegment) {
                ExpressionAssert.assertCommonExpression(assertContext, (ComplexExpressionSegment) each, expected.getCommonExpressions().get(count));
                count++;
            }
        }
    }
    
    private static void assertSubqueryExpressionSegment(final SQLCaseAssertContext assertContext, final PredicateInRightValue actual, final ExpectedPredicateInRightValue expected) {
        int count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof SubqueryExpressionSegment) {
                ExpressionAssert.assertSubqueryExpression(assertContext, (SubqueryExpressionSegment) each, expected.getSubqueries().get(count));
                count++;
            }
        }
    }
    
    private static void assertBetweenRightValue(final SQLCaseAssertContext assertContext, final PredicateBetweenRightValue actual, final ExpectedPredicateBetweenRightValue expected) {
        assertNotNull(assertContext.getText("Expected predicate between right value can not be null"), expected);
        assertBetweenExpression(assertContext, actual.getBetweenExpression(), expected);
        assertAndExpression(assertContext, actual.getAndExpression(), expected);
    }
    
    private static void assertBetweenExpression(final SQLCaseAssertContext assertContext, final ExpressionSegment actual, final ExpectedPredicateBetweenRightValue expected) {
        if (actual instanceof ParameterMarkerExpressionSegment) {
            ExpressionAssert.assertParameterMarkerExpression(assertContext, (ParameterMarkerExpressionSegment) actual, expected.getBetweenParameterMarkerExpression());
        } else if (actual instanceof LiteralExpressionSegment) {
            ExpressionAssert.assertLiteralExpression(assertContext, (LiteralExpressionSegment) actual, expected.getBetweenLiteralExpression());
        } else if (actual instanceof CommonExpressionSegment) {
            ExpressionAssert.assertCommonExpression(assertContext, (ComplexExpressionSegment) actual, expected.getBetweenCommonExpression());
        } else if (actual instanceof SubqueryExpressionSegment) {
            ExpressionAssert.assertSubqueryExpression(assertContext, (SubqueryExpressionSegment) actual, expected.getBetweenSubquery());
        }
    }
    
    private static void assertAndExpression(final SQLCaseAssertContext assertContext, final ExpressionSegment actual, final ExpectedPredicateBetweenRightValue expected) {
        if (actual instanceof ParameterMarkerExpressionSegment) {
            ExpressionAssert.assertParameterMarkerExpression(assertContext, (ParameterMarkerExpressionSegment) actual, expected.getAndParameterMarkerExpression());
        } else if (actual instanceof LiteralExpressionSegment) {
            ExpressionAssert.assertLiteralExpression(assertContext, (LiteralExpressionSegment) actual, expected.getAndLiteralExpression());
        } else if (actual instanceof CommonExpressionSegment) {
            ExpressionAssert.assertCommonExpression(assertContext, (ComplexExpressionSegment) actual, expected.getAndCommonExpression());
        } else if (actual instanceof SubqueryExpressionSegment) {
            ExpressionAssert.assertSubqueryExpression(assertContext, (SubqueryExpressionSegment) actual, expected.getAndSubquery());
        }
    }
}
