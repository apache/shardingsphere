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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAlterResourceStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisAlterResourceStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Alter resource statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAlterResourceStatementAssert {
    
    /**
     * Assert alter resource statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter resource statement
     * @param expected expected alter resource statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAlterResourceStatement actual, final DorisAlterResourceStatementTestCase expected) {
        if (null != expected.getResourceName()) {
            assertThat(assertContext.getText("resource name does not match: "), actual.getResourceName(), is(expected.getResourceName()));
        }
        assertNotNull(actual.getProperties(), assertContext.getText("properties should not be null"));
        if (!expected.getProperties().isEmpty()) {
            assertThat(assertContext.getText("properties size does not match: "), actual.getProperties().size(), is(expected.getProperties().size()));
            expected.getProperties().forEach(property -> {
                String actualValue = actual.getProperties().getProperty(property.getKey());
                assertNotNull(actualValue, assertContext.getText("property key '" + property.getKey() + "' should exist"));
                assertThat(assertContext.getText("property value for key '" + property.getKey() + "' does not match: "), actualValue, is(property.getValue()));
            });
        }
    }
}
