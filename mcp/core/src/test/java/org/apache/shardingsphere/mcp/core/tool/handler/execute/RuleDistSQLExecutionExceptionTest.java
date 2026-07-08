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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.junit.jupiter.api.Test;

import java.sql.SQLSyntaxErrorException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RuleDistSQLExecutionExceptionTest {
    
    @Test
    void assertRuleDistSQLExecutionException() {
        ClassificationResult classificationResult = new ClassificationResult(SupportedMCPStatement.DDL, "CREATE",
                "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'))", "", "");
        SQLSyntaxErrorException cause = new SQLSyntaxErrorException("syntax error");
        RuleDistSQLExecutionException actual = new RuleDistSQLExecutionException("sharding_db", classificationResult, cause);
        assertThat(actual.getMessage(),
                is("Rule DistSQL execution failed for database `sharding_db`; check MCP runtime capability and workflow guidance before asking for corrected SQL."));
        assertThat(actual.getDatabase(), is("sharding_db"));
        assertThat(actual.getClassificationResult(), is(classificationResult));
        assertThat(actual.getCause(), is(cause));
    }
}
