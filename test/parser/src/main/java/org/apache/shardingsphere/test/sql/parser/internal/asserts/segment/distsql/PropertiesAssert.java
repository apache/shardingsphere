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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.distsql.ExpectedProperties;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.distsql.ExpectedProperty;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Properties assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesAssert {
    
    /**
     * Assert properties is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual properties
     * @param expected expected properties test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Properties actual, final ExpectedProperties expected) {
        if (null == expected || null == expected.getProperties()) {
            assertTrue(assertContext.getText("Actual properties should not exist."), actual.isEmpty());
        } else {
            assertNotNull(assertContext.getText("Actual properties should exist."), actual);
            for (ExpectedProperty expectedProperty : expected.getProperties()) {
                PropertyAssert.assertIs(assertContext, actual, expectedProperty);
            }
        }
    }
}
