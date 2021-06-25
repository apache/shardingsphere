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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.ExpectedAlgorithm;
import org.hamcrest.CoreMatchers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;

/**
 * Algorithm assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlgorithmAssert {
    
    /**
     * Assert function is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual function
     * @param expected expected function test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlgorithmSegment actual, final ExpectedAlgorithm expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual dataSource should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual dataSource should exist."), actual);
            assertThat(assertContext.getText(String.format("`%s`'s function segment assertion error: ", actual.getClass().getSimpleName())),
                    actual.getName(), CoreMatchers.is(expected.getName()));
            PropertiesAssert.assertIs(assertContext, actual.getProps(), expected.getProps());
        }
    }
}
