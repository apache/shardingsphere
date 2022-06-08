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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.parameter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Parameter marker assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterMarkerAssert {
    
    /**
     * Assert parameter markers count.
     * 
     * @param assertContext assert context
     * @param actual actual parameter markers count
     * @param expected expected parameter markers count
     */
    public static void assertCount(final SQLCaseAssertContext assertContext, final int actual, final int expected) {
        if (SQLCaseType.Placeholder == assertContext.getSqlCaseType()) {
            assertThat(assertContext.getText("Parameter markers count assertion error: "), actual, is(expected));
        } else {
            assertThat(assertContext.getText("Parameter markers count assertion error: "), actual, is(0));
        }
    }
}
