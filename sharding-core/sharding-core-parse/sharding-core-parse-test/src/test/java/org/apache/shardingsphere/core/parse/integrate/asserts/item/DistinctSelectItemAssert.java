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

package org.apache.shardingsphere.core.parse.integrate.asserts.item;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.item.ExpectedDistinctSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.DistinctSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.SelectItem;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Distinct select item assert.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
final class DistinctSelectItemAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    void assertDistinctSelectItems(final Set<SelectItem> actual, final ExpectedDistinctSelectItem expected) {
        Optional<DistinctSelectItem> distinctSelectItem = getDistinctSelectItem(actual);
        if (distinctSelectItem.isPresent()) {
            assertDistinctSelectItem(distinctSelectItem.get(), expected);
        } else {
            assertNull(assertMessage.getFullAssertMessage("distinct select item should not exist: "), expected);
        }
    }
    
    private void assertDistinctSelectItem(final DistinctSelectItem actual, final ExpectedDistinctSelectItem expected) {
        assertThat(assertMessage.getFullAssertMessage("Distinct select item alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
        assertThat(assertMessage.getFullAssertMessage("Distinct select item distinct column name size assertion error: "),
                actual.getDistinctColumnNames().size(), is(expected.getDistinctColumnNames().size()));
        assertTrue(assertMessage.getFullAssertMessage("Distinct select item distinct column name assertion error: "),
                actual.getDistinctColumnNames().containsAll(expected.getDistinctColumnNames()) && expected.getDistinctColumnNames().containsAll(actual.getDistinctColumnNames()));
    }
    
    private Optional<DistinctSelectItem> getDistinctSelectItem(final Set<SelectItem> actual) {
        Set<SelectItem> distinctItems = Sets.filter(actual, new Predicate<SelectItem>() {
            @Override
            public boolean apply(final SelectItem input) {
                return input instanceof DistinctSelectItem;
            }
        });
        return distinctItems.isEmpty() ? Optional.<DistinctSelectItem>absent() : Optional.of((DistinctSelectItem) distinctItems.iterator().next());
    }
}
