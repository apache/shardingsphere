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

package org.apache.shardingsphere.core.parse.integrate.asserts.limit;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.limit.ExpectedLimit;
import org.apache.shardingsphere.core.parsing.parser.context.limit.Limit;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Limit assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class LimitAssert {
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert limit.
     * 
     * @param actual actual limit
     * @param expected expected limit
     */
    public void assertLimit(final Limit actual, final ExpectedLimit expected) {
        if (null == actual) {
            assertNull(assertMessage.getFullAssertMessage("Limit should not exist: "), expected);
            return;
        }
        if (SQLCaseType.Placeholder == sqlCaseType) {
            if (null != actual.getOffset()) {
                assertThat(assertMessage.getFullAssertMessage("Limit offset index assertion error: "), actual.getOffset().getIndex(), is(expected.getOffsetParameterIndex()));
            }
            if (null != actual.getRowCount()) {
                assertThat(assertMessage.getFullAssertMessage("Limit row count index assertion error: "), actual.getRowCount().getIndex(), is(expected.getRowCountParameterIndex()));
            }
        } else {
            if (null != actual.getOffset()) {
                assertThat(assertMessage.getFullAssertMessage("Limit offset value assertion error: "), actual.getOffset().getValue(), is(expected.getOffset()));
            }
            if (null != actual.getRowCount()) {
                assertThat(assertMessage.getFullAssertMessage("Limit row count value assertion error: "), actual.getRowCount().getValue(), is(expected.getRowCount()));
            }
        }
    }
}
