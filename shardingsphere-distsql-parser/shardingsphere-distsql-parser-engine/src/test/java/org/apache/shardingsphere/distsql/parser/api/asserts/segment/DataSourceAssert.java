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

package org.apache.shardingsphere.distsql.parser.api.asserts.segment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.segment.impl.ExpectedDataSource;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Data source assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceAssert {

    /**
     * Assert RQL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual RQL statement
     * @param expected      expected RQL statement test case
     */
    public static void assertIs(SQLCaseAssertContext assertContext, DataSourceSegment actual, ExpectedDataSource expected) {
        if (null != expected) {
            assertDataSourceName(assertContext, actual.getName(), expected);
            assertHostName(assertContext, actual.getHostName(), expected);
            assertPort(assertContext, actual.getPort(), expected);
            assertUser(assertContext, actual.getUser(), expected);
            assertDB(assertContext, actual.getDb(), expected);
            assertPassword(assertContext, actual.getPassword(), expected);
        } else {
            assertFalse(assertContext.getText("Actual dataSource should not exit."), actual != null);
        }
    }

    private static void assertDataSourceName(SQLCaseAssertContext assertContext, String actual, ExpectedDataSource expected) {
        if (null != expected.getName()) {
            String name = expected.getName().getName();
            assertThat(assertContext.getText(String.format("The dataSource name should be %s, but it was %s", actual, name)), actual, is(name));
        } else {
            assertNull(assertContext.getText("A dataSource name should not exit."), actual);
        }
    }

    private static void assertHostName(SQLCaseAssertContext assertContext, String actual, ExpectedDataSource expected) {
        if (null != expected.getHostName()) {
            String hostname = expected.getHostName().getName();
            assertThat(assertContext.getText(String.format("The hostname should be %s, but it was %s", actual, hostname)), actual, is(hostname));
        } else {
            assertNull(assertContext.getText("Expect hostname should not exit."), actual);
        }
    }

    private static void assertPort(SQLCaseAssertContext assertContext, String actual, ExpectedDataSource expected) {
        if (null != expected.getPort()) {
            String port = expected.getPort().getName();
            assertThat(assertContext.getText(String.format("The port should be %s, but it was %s", actual, expected.getPort())), actual, is(port));
        } else {
            assertNull(assertContext.getText("Expect port should not exit."), actual);
        }
    }

    private static void assertUser(SQLCaseAssertContext assertContext, String actual, ExpectedDataSource expected) {
        if (null != expected.getUser()) {
            String user = expected.getUser().getName();
            assertThat(assertContext.getText(String.format("The user should be %s, but it was %s", actual, expected.getUser())), actual, is(user));
        } else {
            assertNull(assertContext.getText("Expect user should not exit."), actual);
        }
    }

    private static void assertDB(SQLCaseAssertContext assertContext, String actual, ExpectedDataSource expected) {
        if (null != expected.getDb()) {
            String db = expected.getDb().getName();
            assertThat(assertContext.getText(String.format("The db should be %s, but it was %s", actual, expected.getDb())), actual, is(db));
        } else {
            assertNull(assertContext.getText("Expect db should not exit."), actual);
        }
    }

    private static void assertPassword(SQLCaseAssertContext assertContext, String actual, ExpectedDataSource expected) {
        if (null != expected.getPassword()) {
            String password = expected.getPassword().getName();
            assertThat(assertContext.getText(String.format("The password should be %s, but it was %s", actual, expected.getPassword())), actual, is(password));
        } else {
            assertThat(assertContext.getText("Expect password should not exit."), actual, is(""));
        }
    }
}
