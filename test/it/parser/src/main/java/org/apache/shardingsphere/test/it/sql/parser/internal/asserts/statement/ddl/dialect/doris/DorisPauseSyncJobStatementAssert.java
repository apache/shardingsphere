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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisPauseSyncJobStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisPauseSyncJobStatementTestCase;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

/**
 * Pause sync job statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisPauseSyncJobStatementAssert {
    
    /**
     * Assert pause sync job statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual pause sync job statement
     * @param expected expected pause sync job statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisPauseSyncJobStatement actual, final DorisPauseSyncJobStatementTestCase expected) {
        if (actual.getJobName().isPresent()) {
            MatcherAssert.assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().get().getIdentifier().getValue(), Matchers.is(expected.getJobName()));
            if (null != expected.getOwner()) {
                OwnerAssert.assertIs(assertContext, actual.getJobName().get().getOwner().orElse(null), expected.getOwner());
            }
        }
    }
}
