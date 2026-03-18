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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterStoragePolicyStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.policy.PolicyAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterStoragePolicyStatementTestCase;

/**
 * Alter storage policy statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAlterStoragePolicyStatementAssert {
    
    /**
     * Assert alter storage policy statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter storage policy statement
     * @param expected expected alter storage policy statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAlterStoragePolicyStatement actual, final DorisAlterStoragePolicyStatementTestCase expected) {
        assertPolicyName(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
    }
    
    private static void assertPolicyName(final SQLCaseAssertContext assertContext, final DorisAlterStoragePolicyStatement actual, final DorisAlterStoragePolicyStatementTestCase expected) {
        if (null != expected.getPolicyName()) {
            PolicyAssert.assertPolicyName(assertContext, actual.getPolicyName(), expected.getPolicyName());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final DorisAlterStoragePolicyStatement actual, final DorisAlterStoragePolicyStatementTestCase expected) {
        if (null != expected.getProperties()) {
            PolicyAssert.assertProperties(assertContext, actual.getProperties(), expected.getProperties());
        }
    }
}
