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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.AbstractDatabaseDiscoverySegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryConstructionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.PropertiesAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.ExpectedProperties;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedDatabaseDiscoveryConstructionRule;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedDatabaseDiscoveryDefinitionRule;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDatabaseDiscoveryConstructionRuleStatementTestCase;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rdl.create.CreateDatabaseDiscoveryDefinitionRuleStatementTestCase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Create database discovery rule statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateDatabaseDiscoveryRuleStatementAssert {
    
    /**
     * Assert create database discovery rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create database discovery rule statement
     * @param expected expected create database discovery rule statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateDatabaseDiscoveryRuleStatement actual, final SQLParserTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertDatabaseDiscoveryRules(assertContext, actual.getRules(), expected);
        }
    }
    
    private static void assertDatabaseDiscoveryRules(final SQLCaseAssertContext assertContext, final Collection<AbstractDatabaseDiscoverySegment> actual,
                                                     final SQLParserTestCase expected) {
        if (expected instanceof CreateDatabaseDiscoveryConstructionRuleStatementTestCase) {
            assertDiscoveryConstructionRule(assertContext, actual, ((CreateDatabaseDiscoveryConstructionRuleStatementTestCase) expected).getRules());
        } else if (expected instanceof CreateDatabaseDiscoveryDefinitionRuleStatementTestCase) {
            assertDiscoveryDefinitionRule(assertContext, actual, ((CreateDatabaseDiscoveryDefinitionRuleStatementTestCase) expected).getRules());
        }
    }
    
    private static void assertDiscoveryDefinitionRule(final SQLCaseAssertContext assertContext, final Collection<AbstractDatabaseDiscoverySegment> actual,
                                                      final List<ExpectedDatabaseDiscoveryDefinitionRule> expected) {
        assertThat(assertContext.getText(String.format("Actual database discovery rule size should be %s , but it was %s", expected.size(),
                actual.size())), actual.size(), is(expected.size()));
        Map<String, DatabaseDiscoveryDefinitionSegment> actualMap = actual.stream().map(each -> (DatabaseDiscoveryDefinitionSegment) each)
                .collect(Collectors.toMap(AbstractDatabaseDiscoverySegment::getName, each -> each));
        expected.forEach(each -> {
            DatabaseDiscoveryDefinitionSegment actualSegment = actualMap.get(each.getName());
            assertNotNull(actualSegment);
            assertThat(actualSegment.getName(), is(each.getName()));
            assertThat(actualSegment.getDataSources(), is(each.getDataSources()));
            PropertiesAssert.assertIs(assertContext, actualSegment.getDiscoveryHeartbeat(), each.getDiscoveryHeartbeat());
            AlgorithmAssert.assertIs(assertContext, actualSegment.getDiscoveryType(), each.getDiscoveryType());
        });
    }
    
    private static void assertDiscoveryConstructionRule(final SQLCaseAssertContext assertContext, final Collection<AbstractDatabaseDiscoverySegment> actual,
                                                        final Collection<ExpectedDatabaseDiscoveryConstructionRule> expected) {
        assertThat(assertContext.getText(String.format("Actual database discovery rule size should be %s , but it was %s", expected.size(),
                actual.size())), actual.size(), is(expected.size()));
        Map<String, DatabaseDiscoveryConstructionSegment> actualMap = actual.stream().map(each -> (DatabaseDiscoveryConstructionSegment) each)
                .collect(Collectors.toMap(AbstractDatabaseDiscoverySegment::getName, each -> each));
        expected.forEach(each -> {
            DatabaseDiscoveryConstructionSegment actualSegment = actualMap.get(each.getName());
            assertNotNull(actualSegment);
            assertThat(actualSegment.getName(), is(each.getName()));
            assertThat(actualSegment.getDataSources(), is(each.getDataSources()));
            assertThat(actualSegment.getDiscoveryHeartbeatName(), is(each.getDiscoveryHeartbeat()));
            assertThat(actualSegment.getDiscoveryTypeName(), is(each.getDiscoveryType()));
        });
    }
    
    private static void assertProps(final SQLCaseAssertContext assertContext, final Properties actual, final ExpectedProperties expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual properties should not exist"), actual);
        } else {
            assertNotNull(assertContext.getText("Actual properties should exist"), actual);
            PropertiesAssert.assertIs(assertContext, actual, expected);
        }
    }
}
