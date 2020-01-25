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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.predicate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedAndPredicate;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedColumn;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedOperator;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedPredicate;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedWhere;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.value.ExpectedPredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.value.ExpectedPredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.value.ExpectedPredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 *  Where assert.
 *
 * @author zhaoyanan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WhereAssert {
    
    /**
     * Assert actual where segment is correct with expected where.
     * 
     * @param assertMessage assert message
     * @param actual actual where segment
     * @param expected expected where segment
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLCaseAssertMessage assertMessage, final WhereSegment actual, final ExpectedWhere expected, final SQLCaseType sqlCaseType) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getText("Parameters count in where clause assertion error: "), actual.getParametersCount(), is(expected.getParametersCount()));
        }
        assertAndPredicates(assertMessage, actual.getAndPredicates(), expected.getAndPredicates(), sqlCaseType);
    }
    
    private static void assertAndPredicates(final SQLCaseAssertMessage assertMessage, 
                                            final Collection<AndPredicate> actual, final List<ExpectedAndPredicate> expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("And predicate size assertion error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (AndPredicate each: actual) {
            Collection<PredicateSegment> actualPredicates = each.getPredicates();
            List<ExpectedPredicate> expectedPredicates = expected.get(count).getPredicates();
            assertThat(assertMessage.getText("Predicates size assertion error: "), actualPredicates.size(), is(expectedPredicates.size()));
            assertPredicates(assertMessage, actualPredicates, expectedPredicates, sqlCaseType);
            count++;
        }
    }
    
    private static void assertPredicates(final SQLCaseAssertMessage assertMessage, 
                                         final Collection<PredicateSegment> actual, final List<ExpectedPredicate> expected, final SQLCaseType sqlCaseType) {
        int count = 0;
        for (PredicateSegment each: actual) {
            ExpectedPredicate expectedPredicate = expected.get(count);
            // TODO assert other type of left value 
            assertColumn(assertMessage, each.getColumn(), expectedPredicate.getColumnLeftValue(), sqlCaseType);
            if (each.getRightValue() instanceof ColumnSegment) {
                assertColumn(assertMessage, (ColumnSegment) each.getRightValue(), expectedPredicate.getColumnRightValue(), sqlCaseType);
            } else if (each.getRightValue() instanceof PredicateCompareRightValue) {
                assertOperator(assertMessage, each, expectedPredicate.getOperator());
                assertCompareRightValue(assertMessage, (PredicateCompareRightValue) each.getRightValue(), expectedPredicate.getCompareRightValue(), sqlCaseType);
            } else if (each.getRightValue() instanceof PredicateInRightValue) {
                assertOperator(assertMessage, each, expectedPredicate.getOperator());
                assertInRightValue(assertMessage, (PredicateInRightValue) each.getRightValue(), expectedPredicate.getInRightValue(), sqlCaseType);
            } else if (each.getRightValue() instanceof PredicateBetweenRightValue) {
                assertOperator(assertMessage, each, expectedPredicate.getOperator());
                assertBetweenRightValue(assertMessage, (PredicateBetweenRightValue) each.getRightValue(), expectedPredicate.getBetweenRightValue(), sqlCaseType);
            }
            // TODO add other right value assertion
            SQLSegmentAssert.assertIs(assertMessage, each, expectedPredicate, sqlCaseType);
            count++;
        }
    }
    
    private static void assertColumn(final SQLCaseAssertMessage assertMessage, final ColumnSegment actual, final ExpectedColumn expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Column name assertion error: "), actual.getName(), is(expected.getName()));
        if (actual.getOwner().isPresent()) {
            OwnerAssert.assertTable(assertMessage, actual.getOwner().get(), expected.getOwner(), sqlCaseType);
        } else {
            assertNull(expected.getOwner());
        }
        assertThat(assertMessage.getText("Column start delimiter assertion error: "), actual.getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Column end delimiter assertion error: "), actual.getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertOperator(final SQLCaseAssertMessage assertMessage, final PredicateSegment actual, final ExpectedOperator expected) {
        if (actual.getRightValue() instanceof PredicateCompareRightValue) {
            assertNotNull(assertMessage.getText("Operator assertion error: "), expected);
            assertThat(assertMessage.getText("Operator assertion error: "), ((PredicateCompareRightValue) actual.getRightValue()).getOperator(), is(expected.getType()));
        }
        // TODO assert operator start index and stop index
    }
    
    private static void assertCompareRightValue(final SQLCaseAssertMessage assertMessage, 
                                                final PredicateCompareRightValue actual, final ExpectedPredicateCompareRightValue expected, final SQLCaseType sqlCaseType) {
        if (actual.getExpression() instanceof ParameterMarkerExpressionSegment) {
            ExpressionAssert.assertParameterMarkerExpression(assertMessage, (ParameterMarkerExpressionSegment) actual.getExpression(), expected.getParameterMarkerExpression(), sqlCaseType);
        } else if (actual.getExpression() instanceof LiteralExpressionSegment) {
            ExpressionAssert.assertLiteralExpression(assertMessage, (LiteralExpressionSegment) actual.getExpression(), expected.getLiteralExpression(), sqlCaseType);
        } else if (actual.getExpression() instanceof CommonExpressionSegment) {
            ExpressionAssert.assertCommonExpression(assertMessage, (ComplexExpressionSegment) actual.getExpression(), expected.getCommonExpression(), sqlCaseType);
        } else if (actual.getExpression() instanceof SubquerySegment) {
            ExpressionAssert.assertSubquery(assertMessage, (ComplexExpressionSegment) actual.getExpression(), expected.getSubquery(), sqlCaseType);
        }
        // TODO assert start index and stop index
//        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertInRightValue(final SQLCaseAssertMessage assertMessage,
                                           final PredicateInRightValue actual, final ExpectedPredicateInRightValue expected, final SQLCaseType sqlCaseType) {
        assertNotNull(assertMessage.getText("Expected predicate in right value can not be null"), expected);
        int count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                ExpressionAssert.assertParameterMarkerExpression(assertMessage, (ParameterMarkerExpressionSegment) each, expected.getParameterMarkerExpressions().get(count), sqlCaseType);
                count++;
            }
        }
        count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof LiteralExpressionSegment) {
                ExpressionAssert.assertLiteralExpression(assertMessage, (LiteralExpressionSegment) each, expected.getLiteralExpressions().get(count), sqlCaseType);
                count++;
            }
        }
        count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof CommonExpressionSegment) {
                ExpressionAssert.assertCommonExpression(assertMessage, (ComplexExpressionSegment) each, expected.getCommonExpressions().get(count), sqlCaseType);
                count++;
            }
        }
        count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof SubquerySegment) {
                ExpressionAssert.assertSubquery(assertMessage, (ComplexExpressionSegment) each, expected.getSubqueries().get(count), sqlCaseType);
                count++;
            }
        }
        // TODO assert start index and stop index
//        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertBetweenRightValue(final SQLCaseAssertMessage assertMessage,
                                                final PredicateBetweenRightValue actual, final ExpectedPredicateBetweenRightValue expected, final SQLCaseType sqlCaseType) {
        assertNotNull(assertMessage.getText("Expected predicate between right value can not be null"), expected);
        assertBetweenExpression(assertMessage, actual.getBetweenExpression(), expected, sqlCaseType);
        assertAndExpression(assertMessage, actual.getAndExpression(), expected, sqlCaseType);
        // TODO assert start index and stop index
//        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertBetweenExpression(final SQLCaseAssertMessage assertMessage, 
                                                final ExpressionSegment actual, final ExpectedPredicateBetweenRightValue expected, final SQLCaseType sqlCaseType) {
        if (actual instanceof ParameterMarkerExpressionSegment) {
            assertNotNull(assertMessage.getText("Expected between parameter marker expression can not be null"), expected.getBetweenParameterMarkerExpression());
            ExpressionAssert.assertParameterMarkerExpression(assertMessage, (ParameterMarkerExpressionSegment) actual, expected.getBetweenParameterMarkerExpression(), sqlCaseType);
        } else if (actual instanceof LiteralExpressionSegment) {
            assertNotNull(assertMessage.getText("Expected between literal expression can not be null"), expected.getBetweenParameterMarkerExpression());
            ExpressionAssert.assertLiteralExpression(assertMessage, (LiteralExpressionSegment) actual, expected.getBetweenLiteralExpression(), sqlCaseType);
        } else if (actual instanceof CommonExpressionSegment) {
            assertNotNull(assertMessage.getText("Expected between common expression can not be null"), expected.getBetweenParameterMarkerExpression());
            ExpressionAssert.assertCommonExpression(assertMessage, (ComplexExpressionSegment) actual, expected.getBetweenCommonExpression(), sqlCaseType);
        } else if (actual instanceof SubquerySegment) {
            assertNotNull(assertMessage.getText("Expected between subquery expression can not be null"), expected.getBetweenParameterMarkerExpression());
            ExpressionAssert.assertSubquery(assertMessage, (ComplexExpressionSegment) actual, expected.getBetweenSubquery(), sqlCaseType);
        }
    }
    
    private static void assertAndExpression(final SQLCaseAssertMessage assertMessage, 
                                            final ExpressionSegment actual, final ExpectedPredicateBetweenRightValue expected, final SQLCaseType sqlCaseType) {
        if (actual instanceof ParameterMarkerExpressionSegment) {
            assertNotNull(assertMessage.getText("Expected and parameter marker expression can not be null"), expected.getBetweenParameterMarkerExpression());
            ExpressionAssert.assertParameterMarkerExpression(assertMessage, (ParameterMarkerExpressionSegment) actual, expected.getAndParameterMarkerExpression(), sqlCaseType);
        } else if (actual instanceof LiteralExpressionSegment) {
            assertNotNull(assertMessage.getText("Expected and literal expression can not be null"), expected.getBetweenParameterMarkerExpression());
            ExpressionAssert.assertLiteralExpression(assertMessage, (LiteralExpressionSegment) actual, expected.getAndLiteralExpression(), sqlCaseType);
        } else if (actual instanceof CommonExpressionSegment) {
            assertNotNull(assertMessage.getText("Expected and common expression can not be null"), expected.getBetweenParameterMarkerExpression());
            ExpressionAssert.assertCommonExpression(assertMessage, (ComplexExpressionSegment) actual, expected.getAndCommonExpression(), sqlCaseType);
        } else if (actual instanceof SubquerySegment) {
            assertNotNull(assertMessage.getText("Expected and subquery expression can not be null"), expected.getBetweenParameterMarkerExpression());
            ExpressionAssert.assertSubquery(assertMessage, (ComplexExpressionSegment) actual, expected.getAndSubquery(), sqlCaseType);
        }
    }
}
