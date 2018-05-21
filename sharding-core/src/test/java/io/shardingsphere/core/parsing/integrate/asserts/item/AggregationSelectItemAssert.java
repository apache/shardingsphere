/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.parsing.integrate.asserts.item;

import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.item.ExpectedAggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Aggregation select item assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class AggregationSelectItemAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertAggregationSelectItems(final Set<SelectItem> actual, final List<ExpectedAggregationSelectItem> expected) {
        List<AggregationSelectItem> aggregationSelectItems = getAggregationSelectItems(actual);
        assertThat(assertMessage.getFullAssertMessage("Table tokens size error: "), aggregationSelectItems.size(), is(expected.size()));
        int count = 0;
        for (AggregationSelectItem each : aggregationSelectItems) {
            assertAggregationSelectItem(each, expected.get(count));
            count++;
        }
    }
    
    private void assertAggregationSelectItem(final AggregationSelectItem actual, final ExpectedAggregationSelectItem expected) {
        assertThat(assertMessage.getFullAssertMessage("Aggregation select item aggregation type assertion error: "), actual.getType().name(), is(expected.getType()));
        assertThat(assertMessage.getFullAssertMessage("Aggregation select item inner expression assertion error: "), actual.getInnerExpression(), is(expected.getInnerExpression()));
        assertThat(assertMessage.getFullAssertMessage("Aggregation select item alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
        assertThat(assertMessage.getFullAssertMessage("Aggregation select item index assertion error: "), actual.getIndex(), is(expected.getIndex()));
        assertThat(assertMessage.getFullAssertMessage("Aggregation select item derived aggregation select items assertion error: "),
                actual.getDerivedAggregationSelectItems().size(), is(expected.getDerivedColumns().size()));
        int count = 0;
        for (AggregationSelectItem each : actual.getDerivedAggregationSelectItems()) {
            assertAggregationSelectItem(each, expected.getDerivedColumns().get(count));
            count++;
        }
    }
    
    private List<AggregationSelectItem> getAggregationSelectItems(final Set<SelectItem> actual) {
        List<AggregationSelectItem> result = new ArrayList<>(actual.size());
        for (SelectItem each : actual) {
            if (each instanceof AggregationSelectItem) {
                result.add((AggregationSelectItem) each);
            }
        }
        return result;
    }
}
