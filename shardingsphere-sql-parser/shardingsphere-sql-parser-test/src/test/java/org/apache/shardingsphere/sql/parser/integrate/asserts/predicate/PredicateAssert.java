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
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.expr.complex.ExpectedCommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.expr.complex.ExpectedSubquerySegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.expr.simple.ExpectedLiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.expr.simple.ExpectedParamMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.predicate.ExpectedAndPredicate;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.predicate.ExpectedColumnSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.predicate.ExpectedPredicateSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.predicate.ExpectedWhereSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.predicate.value.ExpectedPredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PredicateAssert {

    /**
     * Assert actual where segment is correct with expected where.
     * 
     * @param assertMessage assert message
     * @param actual actual where segment
     * @param expected expected where segment
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLStatementAssertMessage assertMessage, final WhereSegment actual, final ExpectedWhereSegment expected, final SQLCaseType sqlCaseType) {
        if (SQLCaseType.Placeholder == sqlCaseType) {
            assertThat(assertMessage.getText("parameters count assertion error:"), actual.getParametersCount(), is(expected.getParametersCount()));
        }
        assertAndPredicate(assertMessage, actual.getAndPredicates(), expected.getAndPredicates());
    }
    
    private static void assertAndPredicate(final SQLStatementAssertMessage assertMessage, final Collection<AndPredicate> actual, final List<ExpectedAndPredicate> expected) {
        assertThat(assertMessage.getText("and predicate size error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (AndPredicate each: actual) {
            assertThat(assertMessage.getText("predicate segment size error: "), each.getPredicates().size(), is(expected.get(count).getPredicates().size()));
            assertPredicateSegment(assertMessage, each.getPredicates(), expected.get(count).getPredicates());
            count++;
        }
    }
    
    private static void assertPredicateSegment(final SQLStatementAssertMessage assertMessage, final Collection<PredicateSegment> actual, final List<ExpectedPredicateSegment> expected) {
        int count = 0;
        for (PredicateSegment each: actual) {
            assertColumnSegment(assertMessage, each.getColumn(), expected.get(count).getColumn());
            if (each.getRightValue() instanceof PredicateCompareRightValue) {
                assertCompareRightValue(assertMessage, (PredicateCompareRightValue) each.getRightValue(), expected.get(count).findExpectedRightValue(ExpectedPredicateCompareRightValue.class));
            }
            //TODO add expr assertion
            count++;
        }
    }
    
    private static void assertColumnSegment(final SQLStatementAssertMessage assertMessage, final ColumnSegment actual, final ExpectedColumnSegment expected) {
        assertThat(assertMessage.getText("column segment name assertion error: "), actual.getName(), is(expected.getName()));
        assertThat(assertMessage.getText("column segment table name assertion error: "), 
                actual.getOwner().isPresent() ? actual.getOwner().get().getTableName() : null, is(expected.getOwner().getName()));
    }
    
    private static void assertCompareRightValue(final SQLStatementAssertMessage assertMessage, final PredicateCompareRightValue actual, final ExpectedPredicateCompareRightValue expected) {
        assertThat(assertMessage.getText("right value operator assertion error: "), actual.getOperator(), is(expected.getOperator()));
        if (actual.getExpression() instanceof ParameterMarkerExpressionSegment) {
            assertThat(assertMessage.getText("parameter marker expression parameter marker index assertion error"), 
                    ((ParameterMarkerExpressionSegment) actual.getExpression()).getParameterMarkerIndex(), 
                    is(expected.findExpectedExpression(ExpectedParamMarkerExpressionSegment.class).getParameterMarkerIndex()));
        }
        if (actual.getExpression() instanceof CommonExpressionSegment) {
            assertThat(assertMessage.getText("common expression text assertion error: "), ((ComplexExpressionSegment) actual.getExpression()).getText(), 
                    is(expected.findExpectedExpression(ExpectedCommonExpressionSegment.class).getText()));
        }
        if (actual.getExpression() instanceof SubquerySegment) {
            assertThat(assertMessage.getText("subquery segment text assertion error: "), ((ComplexExpressionSegment) actual.getExpression()).getText(), 
                    is(expected.findExpectedExpression(ExpectedSubquerySegment.class).getText()));
        }
        if (actual.getExpression() instanceof LiteralExpressionSegment) {
            assertThat(assertMessage.getText("literal assertion error:"), ((LiteralExpressionSegment) actual.getExpression()).getLiterals().toString(), 
                    is(expected.findExpectedExpression(ExpectedLiteralExpressionSegment.class).getLiterals().toString()));
        }
    }
}
