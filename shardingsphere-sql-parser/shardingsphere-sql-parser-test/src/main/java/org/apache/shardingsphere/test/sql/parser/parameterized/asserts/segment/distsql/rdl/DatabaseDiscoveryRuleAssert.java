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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.PropertiesAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.ExpectedProperties;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedDatabaseDiscoveryRule;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Database discovery rule assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseDiscoveryRuleAssert {
    
    /**
     * Assert database discovery rule is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual database discovery rule
     * @param expected expected database discovery rule test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DatabaseDiscoveryRuleSegment actual, final ExpectedDatabaseDiscoveryRule expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual should not exist"), actual);
        } else {
            assertNotNull(assertContext.getText("Actual should exist"), actual);
            assertDatabaseDiscoveryRule(assertContext, actual, expected);
        }
    }
    
    private static void assertDatabaseDiscoveryRule(final SQLCaseAssertContext assertContext, final DatabaseDiscoveryRuleSegment actual, final ExpectedDatabaseDiscoveryRule expected) {
        assertThat(assertContext.getText(String.format("`%s`'s discovery rule segment assertion error: ", actual.getClass().getSimpleName())), actual.getName(), is(expected.getName()));
        assertThat(assertContext.getText(String.format("`%s`'s discovery rule segment assertion error: ",
                actual.getClass().getSimpleName())), actual.getDiscoveryTypeName(), is(expected.getDiscoveryTypeName()));
        assertThat(assertContext.getText(String.format("`%s`'s discovery rule segment assertion error: ", actual.getClass().getSimpleName())), actual.getDataSources(), is(expected.getDataSources()));
        assertProps(assertContext, actual.getProps(), expected.getProps());
    }
    
    private static void assertProps(final SQLCaseAssertContext assertContext, final Properties actual, final ExpectedProperties expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual should not exist"), actual);
        } else {
            assertNotNull(assertContext.getText("Actual should exist"), actual);
            PropertiesAssert.assertIs(assertContext, actual, expected);
        }
    }
}
