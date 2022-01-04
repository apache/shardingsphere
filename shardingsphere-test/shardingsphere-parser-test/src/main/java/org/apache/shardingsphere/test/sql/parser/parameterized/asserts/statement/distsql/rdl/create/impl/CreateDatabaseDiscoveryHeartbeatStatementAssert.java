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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.create.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryHeartbeatSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryHeartbeatStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.PropertiesAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDatabaseDiscoveryHeartbeatStatementTestCase;

import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Create database discovery heartbeat statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateDatabaseDiscoveryHeartbeatStatementAssert {
    
    /**
     * Assert create database discovery heartbeat statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create database discovery heartbeat statement
     * @param expected expected create database discovery heartbeat statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateDatabaseDiscoveryHeartbeatStatement actual, final CreateDatabaseDiscoveryHeartbeatStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            Map<String, DatabaseDiscoveryHeartbeatSegment> actualMap = actual.getHeartbeats().stream().collect(Collectors.toMap(DatabaseDiscoveryHeartbeatSegment::getHeartbeatName, each -> each));
            expected.getHeartbeats().forEach(each -> {
                DatabaseDiscoveryHeartbeatSegment actualSegment = actualMap.get(each.getName());
                assertNotNull(actualSegment);
                assertThat(actualSegment.getHeartbeatName(), is(each.getName()));
                PropertiesAssert.assertIs(assertContext, actualSegment.getProperties(), each.getProperties());
            });
        }
    }
}
