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
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.ExpectedProperty;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Property assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertyAssert {
    
    /**
     * Assert property is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual property
     * @param expected expected property test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Properties actual, final ExpectedProperty expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual properties should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual properties should exist."), actual);
            assertThat(assertContext.getText("properties assertion error: "), actual.getProperty(expected.getKey()), is(expected.getValue()));
        }
    }
}
