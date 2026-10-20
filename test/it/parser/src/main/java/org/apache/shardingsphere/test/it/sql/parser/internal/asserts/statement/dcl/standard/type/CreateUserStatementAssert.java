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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dcl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.UserSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.CreateUserStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.user.ExpectedUser;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dcl.standard.CreateUserStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Create user statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateUserStatementAssert {
    
    /**
     * Assert create user statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create user statement
     * @param expected expected create user statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateUserStatement actual, final CreateUserStatementTestCase expected) {
        if (!expected.getUsers().isEmpty()) {
            assertThat(assertContext.getText("User count assertion error: "), actual.getUsers().size(), is(expected.getUsers().size()));
            int count = 0;
            for (UserSegment each : actual.getUsers()) {
                ExpectedUser expectedUser = expected.getUsers().get(count++);
                assertThat(assertContext.getText("User name assertion error: "), each.getUser(), is(expectedUser.getName()));
                assertThat(assertContext.getText("User host assertion error: "), each.getHost(), is(expectedUser.getHost()));
            }
        }
    }
}
