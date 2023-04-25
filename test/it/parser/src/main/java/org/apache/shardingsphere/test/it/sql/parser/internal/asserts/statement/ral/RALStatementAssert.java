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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.pipeline.QueryablePipelineRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.pipeline.UpdatablePipelineRALStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.pipeline.QueryablePipelineRALStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.pipeline.UpdatablePipelineRALStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.QueryableRALStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ral.impl.UpdatableRALStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;

/**
 * RAL statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RALStatementAssert {
    
    /**
     * Assert RAL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual RAL statement
     * @param expected expected RAL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final RALStatement actual, final SQLParserTestCase expected) {
        if (actual instanceof QueryablePipelineRALStatement) {
            QueryablePipelineRALStatementAssert.assertIs(assertContext, (QueryablePipelineRALStatement) actual, expected);
        } else if (actual instanceof UpdatablePipelineRALStatement) {
            UpdatablePipelineRALStatementAssert.assertIs(assertContext, (UpdatablePipelineRALStatement) actual, expected);
        } else if (actual instanceof QueryableRALStatement) {
            QueryableRALStatementAssert.assertIs(assertContext, (QueryableRALStatement) actual, expected);
        } else if (actual instanceof UpdatableRALStatement) {
            UpdatableRALStatementAssert.assertIs(assertContext, (UpdatableRALStatement) actual, expected);
        }
    }
}
