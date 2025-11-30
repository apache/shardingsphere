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
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.trigger.MySQLShowCreateTriggerStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.mysql.show.trigger.MySQLShowCreateTriggerStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Show create trigger statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLShowCreateTriggerStatementAssert {
    
    /**
     * Assert show create trigger statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show create trigger statement
     * @param expected expected show create trigger statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLShowCreateTriggerStatement actual, final MySQLShowCreateTriggerStatementTestCase expected) {
        assertNotNull(expected.getTrigger(), assertContext.getText("expected show create trigger should be not null"));
        assertThat(assertContext.getText("trigger name does not match: "), actual.getName(), is(expected.getTrigger().getName()));
    }
}
