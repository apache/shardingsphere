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

package org.apache.shardingsphere.core.parse.integrate.asserts.predicate;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.ExpectedExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.complex.ExpectedCommonExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.complex.ExpectedSubquerySegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.simple.ExpectedLiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.simple.ExpectedParamMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.ExpectedAndPredicate;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.ExpectedColumnSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.ExpectedPredicateSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.ExpectedWhereSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.value.ExpectedPredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *  where predicate assert.
 *
 * @author zhaoyanan
 */
@RequiredArgsConstructor
public final class PredicateAssert {

    private final SQLCaseType sqlCaseType;

    private final SQLStatementAssertMessage assertMessage;

    /**
     * assert predicate
     * @param actual actual where segment
     * @param expected expected where segment
     */
    public void assertPredicate(final WhereSegment actual, final ExpectedWhereSegment expected) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getFullAssertMessage("parameters count assertion error:"), actual.getParametersCount(), is(expected.getParametersCount()));
        }
        assertAndPredicate(actual.getAndPredicates(),expected.getAndPredicates());
    }

    /**
     * assert and predicate
     * @param actual actual and predicate
     * @param expected expected and predicate
     */
    private void assertAndPredicate(final Collection<AndPredicate> actual, final List<ExpectedAndPredicate> expected) {
        assertThat(assertMessage.getFullAssertMessage("and predicate size error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (AndPredicate each: actual) {
            assertThat(assertMessage.getFullAssertMessage("predicate segment size error: "), each.getPredicates().size(), is(expected.get(count).getPredicates().size()));
            assertPredicateSegment(each.getPredicates(),expected.get(count).getPredicates());
            count++;
        }
    }

    /**
     * assert predicate segment
     * @param actual actual predicate segment
     * @param expected expected predicate segment
     */
    private void assertPredicateSegment(final Collection<PredicateSegment> actual, final List<ExpectedPredicateSegment> expected) {
        int count = 0;
        for (PredicateSegment each: actual) {
            assertColumnSegment(each.getColumn(),expected.get(count).getColumn());
            if (each.getRightValue() instanceof PredicateCompareRightValue) {
                assertCompareRightValue((PredicateCompareRightValue) each.getRightValue(), expected.get(count).findExpectedRightValue(ExpectedPredicateCompareRightValue.class));
            }
            //TODO add expr assertion
            count++;
        }
    }

    /**
     * assert column segment
     * @param actual actual column segment
     * @param expected expected column segment
     */
    private void assertColumnSegment(final ColumnSegment actual, final ExpectedColumnSegment expected) {
        assertThat(assertMessage.getFullAssertMessage("column segment name assertion error: "), actual.getName(), is(expected.getName()));
        assertThat(assertMessage.getFullAssertMessage("column segment table name assertion error: "), actual.getOwner().isPresent() ? actual.getOwner().get().getTableName() : null, is(expected.getOwner().getName()));
    }

    /**
     * assert expr and operation
     * @param actual
     * @param expected
     */
    private void assertCompareRightValue(final PredicateCompareRightValue actual, final ExpectedPredicateCompareRightValue expected) {
        assertThat(assertMessage.getFullAssertMessage("right value operator assertion error: "),actual.getOperator(),is(expected.getOperator()));
        if (actual.getExpression() instanceof ParameterMarkerExpressionSegment) {
            assertThat(assertMessage.getFullAssertMessage("parameter marker expression parameterMarkerindex assertion error"),((ParameterMarkerExpressionSegment) actual.getExpression()).getParameterMarkerIndex(),is(expected.findExpectedExpression(ExpectedParamMarkerExpressionSegment.class).getParameterMarkerIndex()));
        }
        if (actual.getExpression() instanceof CommonExpressionSegment) {
            assertThat(assertMessage.getFullAssertMessage("common expression text assertion error: "),((ComplexExpressionSegment) actual.getExpression()).getText(),is(expected.findExpectedExpression(ExpectedCommonExpressionSegment.class).getText()));
        }
        if (actual.getExpression() instanceof SubquerySegment) {
            assertThat(assertMessage.getFullAssertMessage("subquery segment text assertion error: "),((ComplexExpressionSegment) actual.getExpression()).getText(),is(expected.findExpectedExpression(ExpectedSubquerySegment.class).getText()));
        }
        if (actual.getExpression() instanceof LiteralExpressionSegment) {
            assertThat(assertMessage.getFullAssertMessage("literal assertion error:"),((LiteralExpressionSegment) actual.getExpression()).getLiterals().toString(),is(expected.findExpectedExpression(ExpectedLiteralExpressionSegment.class).getLiterals().toString()));
        }
    }

}
