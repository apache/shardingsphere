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

package org.apache.shardingsphere.sql.parser.integrate.asserts.predicate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.complex.ExpectedCommonExpression;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.complex.ExpectedSubquery;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.simple.ExpectedLiteralExpression;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.simple.ExpectedParameterMarkerExpression;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.owner.ExpectedTableOwner;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedAndPredicate;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedColumn;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedOperator;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedPredicate;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.ExpectedWhere;
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
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
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
    public static void assertIs(final SQLStatementAssertMessage assertMessage, final WhereSegment actual, final ExpectedWhere expected, final SQLCaseType sqlCaseType) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getText("Parameters count in where clause assertion error: "), actual.getParametersCount(), is(expected.getParametersCount()));
        }
        assertAndPredicates(assertMessage, actual.getAndPredicates(), expected.getAndPredicates(), sqlCaseType);
    }
    
    private static void assertAndPredicates(final SQLStatementAssertMessage assertMessage, 
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
    
    private static void assertPredicates(final SQLStatementAssertMessage assertMessage, 
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
            }
            // TODO add other right value assertion
            SQLSegmentAssert.assertIs(assertMessage, each, expectedPredicate, sqlCaseType);
            count++;
        }
    }
    
    private static void assertColumn(final SQLStatementAssertMessage assertMessage, final ColumnSegment actual, final ExpectedColumn expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Column name assertion error: "), actual.getName(), is(expected.getName()));
        if (actual.getOwner().isPresent()) {
            assertOwner(assertMessage, actual.getOwner().get(), expected.getOwner(), sqlCaseType);
        } else {
            assertNull(expected.getOwner());
        }
        assertThat(assertMessage.getText("Column start delimiter assertion error: "), actual.getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Column end delimiter assertion error: "), actual.getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertOwner(final SQLStatementAssertMessage assertMessage, final TableSegment actual, final ExpectedTableOwner expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Column owner name assertion error: "), actual.getTableName(), is(expected.getName()));
        assertThat(assertMessage.getText("Column owner name start delimiter assertion error: "), actual.getTableQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Column owner name end delimiter assertion error: "), actual.getTableQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertOperator(final SQLStatementAssertMessage assertMessage, final PredicateSegment actual, final ExpectedOperator expected) {
        assertNotNull(assertMessage.getText("Operator assertion error: "), expected);
        if (actual.getRightValue() instanceof PredicateCompareRightValue) {
            assertThat(assertMessage.getText("Operator assertion error: "), ((PredicateCompareRightValue) actual.getRightValue()).getOperator(), is(expected.getType()));
        } else if (actual.getRightValue() instanceof PredicateInRightValue) {
            assertThat(assertMessage.getText("Operator assertion error: "), "IN", is(expected.getType()));
        }
        
        // TODO assert operator start index and stop index
    }
    
    private static void assertCompareRightValue(final SQLStatementAssertMessage assertMessage, 
                                                final PredicateCompareRightValue actual, final ExpectedPredicateCompareRightValue expected, final SQLCaseType sqlCaseType) {
        if (actual.getExpression() instanceof ParameterMarkerExpressionSegment) {
            assertThat(assertMessage.getText("Parameter marker index assertion error: "), 
                    ((ParameterMarkerExpressionSegment) actual.getExpression()).getParameterMarkerIndex(), is(expected.getParameterMarkerExpression().getValue()));
        } else if (actual.getExpression() instanceof LiteralExpressionSegment) {
            assertThat(assertMessage.getText("Literal assertion error: "),
                    ((LiteralExpressionSegment) actual.getExpression()).getLiterals().toString(), is(expected.getLiteralExpression().getValue()));
        } else if (actual.getExpression() instanceof CommonExpressionSegment) {
            assertThat(assertMessage.getText("Common expression text assertion error: "), 
                    ((ComplexExpressionSegment) actual.getExpression()).getText(), is(expected.getCommonExpression().getText()));
        } else if (actual.getExpression() instanceof SubquerySegment) {
            assertThat(assertMessage.getText("Subquery text assertion error: "), 
                    ((ComplexExpressionSegment) actual.getExpression()).getText(), is(expected.getSubquery().getText()));
        }
//        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertInRightValue(final SQLStatementAssertMessage assertMessage,
                                           final PredicateInRightValue actual, final ExpectedPredicateInRightValue expected, final SQLCaseType sqlCaseType) {
        int count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                ExpectedParameterMarkerExpression expectedExpression = expected.getParameterMarkerExpressions().get(count);
                assertThat(assertMessage.getText("Parameter marker index assertion error: "), ((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex(), is(expectedExpression.getValue()));
                //        SQLSegmentAssert.assertIs(assertMessage, each, expectedExpression, sqlCaseType);
                count++;
            }
        }
        count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof LiteralExpressionSegment) {
                ExpectedLiteralExpression expectedExpression = expected.getLiteralExpressions().get(count);
                assertThat(assertMessage.getText("Literal assertion error: "), ((LiteralExpressionSegment) each).getLiterals().toString(), is(expectedExpression.getValue()));
                //        SQLSegmentAssert.assertIs(assertMessage, each, expectedExpression, sqlCaseType);
                count++;
            }
        }
        count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof CommonExpressionSegment) {
                ExpectedCommonExpression expectedExpression = expected.getCommonExpressions().get(count);
                assertThat(assertMessage.getText("Common expression text assertion error: "), ((ComplexExpressionSegment) each).getText(), is(expectedExpression.getText()));
                //        SQLSegmentAssert.assertIs(assertMessage, each, expectedExpression, sqlCaseType);
                count++;
            }
        }
        count = 0;
        for (ExpressionSegment each : actual.getSqlExpressions()) {
            if (each instanceof SubquerySegment) {
                ExpectedSubquery expectedExpression = expected.getSubqueries().get(count);
                assertThat(assertMessage.getText("Subquery text assertion error: "), ((ComplexExpressionSegment) each).getText(), is(expectedExpression.getText()));
                //        SQLSegmentAssert.assertIs(assertMessage, each, expectedExpression, sqlCaseType);
                count++;
            }
        }
    }
}
