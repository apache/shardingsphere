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

package org.apache.shardingsphere.core.parse.integrate.asserts.selectitem;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem.ExpectedAggregationDistinctItem;
import org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem.ExpectedAggregationItem;
import org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem.ExpectedColumnSelectItem;
import org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem.ExpectedExpressionItem;
import org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem.ExpectedSelectItem;
import org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem.ExpectedSelectItems;
import org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem.ExpectedShorthandSelectItem;
import org.apache.shardingsphere.core.parse.integrate.jaxb.selectitem.ExpectedTopSelectItem;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.top.TopSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *  Select item assert.
 *
 * @author zhaoyanan
 */
@RequiredArgsConstructor
public final class SelectItemAssert {

    private final SQLCaseType sqlCaseType;

    private final SQLStatementAssertMessage assertMessage;

    public void assertSelectItems(final SelectItemsSegment actual, final ExpectedSelectItems expectedSelectItems) {
        Collection<SelectItemSegment> actualItems = actual.getSelectItems();
        assertThat(assertMessage.getFullAssertMessage("Select items size error: "), actualItems.size(), is(expectedSelectItems.getSize()));
        Collection<ExpectedSelectItem> expectedBaseItems = new LinkedList<>();
        for (SelectItemSegment each : actualItems) {
            if (each instanceof ShorthandSelectItemSegment) {
                expectedBaseItems = expectedSelectItems.findExpectedSelectItems(ExpectedShorthandSelectItem.class);
            }
            if (each instanceof AggregationSelectItemSegment) {
                expectedBaseItems = expectedSelectItems.findExpectedSelectItems(ExpectedAggregationItem.class);
            }
            if (each instanceof AggregationDistinctSelectItemSegment) {
                expectedBaseItems = expectedSelectItems.findExpectedSelectItems(ExpectedAggregationDistinctItem.class);
            }
            if (each instanceof ColumnSelectItemSegment) {
                expectedBaseItems = expectedSelectItems.findExpectedSelectItems(ExpectedColumnSelectItem.class);
            }
            if (each instanceof ExpressionSelectItemSegment) {
                expectedBaseItems = expectedSelectItems.findExpectedSelectItems(ExpectedExpressionItem.class);
            }
            if (each instanceof TopSegment && SQLCaseType.Literal.equals(sqlCaseType)) {
                expectedBaseItems = expectedSelectItems.findExpectedSelectItems(ExpectedTopSelectItem.class);
            }
            if (!expectedBaseItems.isEmpty()) {
                assertSelectItem(each, expectedBaseItems);
            }
        }
    }

    private void assertSelectItem(final SelectItemSegment actual, final Collection<ExpectedSelectItem> expected) {
        String actualText = actual.getText();
        String expectedText = "";
        for (ExpectedSelectItem each: expected) {
            if (actualText.equals(each.getText())) {
                expectedText = each.getText();
                break;
            }
        }
        assertThat(assertMessage.getFullAssertMessage("selectItem text assert error: "), actualText, is(expectedText));
    }
}

