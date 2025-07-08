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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.mysql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.component.MySQLUninstallComponentStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.component.ExpectedComponent;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.component.MySQLUninstallComponentStatementTestCase;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Uninstall component statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLUninstallComponentStatementAssert {
    
    /**
     * Assert uninstall component statement is correct with expected uninstall component statement test case.
     *
     * @param assertContext assert context
     * @param actual actual uninstall component statement
     * @param expected expected uninstall component statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLUninstallComponentStatement actual, final MySQLUninstallComponentStatementTestCase expected) {
        assertThat(assertContext.getText("Actual components size assertion error: "), actual.getComponents().size(), is(expected.getComponents().size()));
        assertComponents(assertContext, actual.getComponents(), expected.getComponents());
    }
    
    private static void assertComponents(final SQLCaseAssertContext assertContext, final List<String> actual, final List<ExpectedComponent> expected) {
        int count = 0;
        for (String each : actual) {
            assertThat(assertContext.getText("Actual component value does not match: "), each, is(expected.get(count).getName()));
            count++;
        }
    }
}
