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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesUsedResourceStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.schema.SchemaAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowRulesUsedResourceStatementTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Show rules used resource statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowRulesUsedResourceStatementAssert {
    
    /**
     * Assert show rules used resource statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show rules used resource statement
     * @param expected expected show rules used resource statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowRulesUsedResourceStatement actual, final ShowRulesUsedResourceStatementTestCase expected) {
        if (null != expected.getSchema()) {
            assertTrue(assertContext.getText("Actual schema should exist."), actual.getSchema().isPresent());
            SchemaAssert.assertIs(assertContext, actual.getSchema().get(), expected.getSchema());
        } else {
            assertFalse(assertContext.getText("Actual schema should not exist."), actual.getSchema().isPresent());
        }
        if (!Strings.isNullOrEmpty(expected.getResourceName())) {
            assertTrue(assertContext.getText("Actual resource should exist."), actual.getResourceName().isPresent());
        } else {
            assertFalse(assertContext.getText("Actual resource should not exist."), actual.getResourceName().isPresent());
        }
    }
}
