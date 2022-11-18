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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.generic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.segment.impl.generic.ExpectedDataType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *  Data type assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataTypeAssert {
    
    /**
     * Assert actual data type segment is correct with expected date type.
     *
     * @param assertContext assert context
     * @param actual actual data type statement
     * @param expected expected data type
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DataTypeSegment actual, final ExpectedDataType expected) {
        assertThat(assertContext.getText(String.format("%s name assertion error: ", "dataType")),
                actual.getDataTypeName(), is(expected.getValue()));
        assertThat(assertContext.getText(String.format("%s start index assertion error: ", "dataType")),
                actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertContext.getText(String.format("%s end index assertion error: ", "dataType")),
                actual.getStopIndex(), is(expected.getStopIndex()));
    }
}
