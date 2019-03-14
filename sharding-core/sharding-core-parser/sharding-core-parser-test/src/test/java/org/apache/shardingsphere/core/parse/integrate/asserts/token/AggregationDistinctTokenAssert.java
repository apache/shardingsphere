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

package org.apache.shardingsphere.core.parse.integrate.asserts.token;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedAggregationDistinctToken;
import org.apache.shardingsphere.core.parse.integrate.jaxb.token.ExpectedTokens;
import org.apache.shardingsphere.core.parsing.parser.token.AggregationDistinctToken;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Aggregation token assert.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
final class AggregationDistinctTokenAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertAggregationDistinctTokens(final Collection<SQLToken> actual, final ExpectedTokens expected) {
        List<AggregationDistinctToken> aggregationDistinctTokens = getAggregationDistinctTokens(actual);
        assertThat(assertMessage.getFullAssertMessage("Aggregation distinct tokens size error: "), aggregationDistinctTokens.size(), is(expected.getAggregationDistinctTokens().size()));
        int count = 0;
        for (ExpectedAggregationDistinctToken each : expected.getAggregationDistinctTokens()) {
            assertAggregationDistinctToken(aggregationDistinctTokens.get(count), each);
            count++;
        }
    }
    
    private void assertAggregationDistinctToken(final AggregationDistinctToken actual, final ExpectedAggregationDistinctToken expected) {
        assertThat(assertMessage.getFullAssertMessage("Aggregation distinct tokens start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertMessage.getFullAssertMessage("Aggregation distinct tokens stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
        assertThat(assertMessage.getFullAssertMessage("Aggregation distinct tokens column name assertion error: "), actual.getColumnName(), is(expected.getColumnName()));
    }
    
    private List<AggregationDistinctToken> getAggregationDistinctTokens(final Collection<SQLToken> actual) {
        List<AggregationDistinctToken> result = new ArrayList<>(actual.size());
        for (SQLToken each : actual) {
            if (each instanceof AggregationDistinctToken) {
                result.add((AggregationDistinctToken) each);
            }
        }
        return result;
    }
}
