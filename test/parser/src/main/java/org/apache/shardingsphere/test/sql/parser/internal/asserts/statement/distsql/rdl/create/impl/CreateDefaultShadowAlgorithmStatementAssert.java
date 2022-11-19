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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.distsql.rdl.create.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.segment.impl.distsql.rdl.ExpectedShadowAlgorithm;
import org.apache.shardingsphere.test.sql.parser.internal.cases.parser.domain.statement.distsql.rdl.create.CreateDefaultShadowAlgorithmStatementTestCase;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Create default shadow algorithm statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateDefaultShadowAlgorithmStatementAssert {
    
    /**
     * Assert create default shadow algorithm statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create default shadow algorithm statement
     * @param expected expected create default shadow algorithm statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateDefaultShadowAlgorithmStatement actual, final CreateDefaultShadowAlgorithmStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            expected.getRules().forEach(each -> assertIsAlgorithmsSegment(assertContext, actual.getShadowAlgorithmSegment(), each));
        }
    }
    
    private static void assertIsAlgorithmsSegment(final SQLCaseAssertContext assertContext, final ShadowAlgorithmSegment actual, final ExpectedShadowAlgorithm expected) {
        assertNotNull(actual);
        AlgorithmAssert.assertIs(assertContext, actual.getAlgorithmSegment(), expected.getAlgorithmSegment());
    }
}
