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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rdl.alter.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryTypeSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.alter.AlterDatabaseDiscoveryTypeStatementTestCase;

import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Alter database discovery type statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterDatabaseDiscoveryTypeStatementAssert {
    
    /**
     * Alter database discovery type statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter database discovery type statement
     * @param expected expected alter database discovery type statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AlterDatabaseDiscoveryTypeStatement actual, final AlterDatabaseDiscoveryTypeStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            Map<String, DatabaseDiscoveryTypeSegment> actualMap = actual.getTypes().stream().collect(Collectors.toMap(DatabaseDiscoveryTypeSegment::getDiscoveryTypeName, each -> each));
            expected.getTypes().forEach(each -> {
                DatabaseDiscoveryTypeSegment actualSegment = actualMap.get(each.getDiscoveryTypeName());
                assertNotNull(actualSegment);
                assertThat(actualSegment.getDiscoveryTypeName(), is(each.getDiscoveryTypeName()));
                AlgorithmAssert.assertIs(assertContext, actualSegment.getAlgorithmSegment(), each.getAlgorithmSegment());
            });
        }
    }
}
