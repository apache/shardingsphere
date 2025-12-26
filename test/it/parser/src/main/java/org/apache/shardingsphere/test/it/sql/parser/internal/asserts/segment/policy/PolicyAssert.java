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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.policy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.policy.PolicyNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogProperties;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.policy.ExpectedPolicyName;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Policy assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PolicyAssert {
    
    /**
     * Assert policy name is correct with expected policy name.
     *
     * @param assertContext assert context
     * @param actual actual policy name segment
     * @param expected expected policy name
     */
    public static void assertPolicyName(final SQLCaseAssertContext assertContext, final PolicyNameSegment actual, final ExpectedPolicyName expected) {
        assertThat(assertContext.getText("Policy name assertion error: "), actual.getName(), is(expected.getName()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert properties are correct with expected properties.
     *
     * @param assertContext assert context
     * @param actual actual properties segment
     * @param expected expected properties
     */
    public static void assertProperties(final SQLCaseAssertContext assertContext, final PropertiesSegment actual, final ExpectedCatalogProperties expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        assertThat(assertContext.getText("Properties size assertion error: "), actual.getProperties().size(), is(expected.getProperties().size()));
        for (int i = 0; i < expected.getProperties().size(); i++) {
            assertProperty(assertContext, actual.getProperties().get(i), expected.getProperties().get(i));
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final ExpectedCatalogProperty expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
