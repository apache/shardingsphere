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

package org.apache.shardingsphere.mcp.protocol;

import org.apache.shardingsphere.mcp.tool.response.SQLExecutionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLExecutionResponseTest {
    
    @Test
    void assertResultSet() {
        SQLExecutionResponse actual = SQLExecutionResponse.resultSet(List.of(new ExecuteQueryColumnDefinition("order_id", "INT", "INT", false)), List.of(List.of(1)), true);
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.RESULT_SET));
        assertThat(actual.getColumns().size(), is(1));
        assertThat(actual.getRows().size(), is(1));
        assertTrue(actual.isTruncated());
    }
    
    @Test
    void assertUpdateCount() {
        SQLExecutionResponse actual = SQLExecutionResponse.updateCount("UPDATE", 2);
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.UPDATE_COUNT));
        assertThat(actual.getStatementType(), is("UPDATE"));
        assertThat(actual.getAffectedRows(), is(2));
        assertThat(actual.getStatus(), is("OK"));
    }
    
    @Test
    void assertStatementAck() {
        SQLExecutionResponse actual = SQLExecutionResponse.statementAck("COMMIT", "Transaction committed.");
        assertThat(actual.getResultKind(), is(ExecuteQueryResultKind.STATEMENT_ACK));
        assertThat(actual.getStatementType(), is("COMMIT"));
        assertThat(actual.getMessage(), is("Transaction committed."));
    }
    
    @Test
    void assertResultSetWithNullRows() {
        assertNull(SQLExecutionResponse.resultSet(List.of(), null, false).getRows());
    }
}
