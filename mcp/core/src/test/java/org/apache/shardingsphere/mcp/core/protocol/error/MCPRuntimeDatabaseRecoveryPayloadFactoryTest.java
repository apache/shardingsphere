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

package org.apache.shardingsphere.mcp.core.protocol.error;

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPRuntimeDatabaseRecoveryPayloadFactoryTest {
    
    @Test
    void assertCreateValidationRecoveryWithoutDatabase() {
        Map<String, Object> actual = MCPRuntimeDatabaseRecoveryPayloadFactory.create("", RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION);
        assertThat(actual.get("category"), is("invalid_configuration"));
        assertThat(actual.get("recovery_category"), is("unavailable_runtime"));
        assertFalse(actual.containsKey("database"));
        assertFalse(actual.containsKey("request_id"));
    }
    
    @Test
    void assertCreateValidationRecoveryForInvisibleDatabase() {
        Map<String, Object> actual = MCPRuntimeDatabaseRecoveryPayloadFactory.create("logic_db", RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE);
        assertThat(actual.get("database"), is("logic_db"));
        assertThat(actual.get("recovery_category"), is("validation"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("resources_to_read")).get(1)).get("uri"), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("next_actions")).get(2)).get("depends_on"), is(List.of(2)));
    }
    
    @Test
    void assertCreateExceptionRecoveryPreservesDatabase() {
        Map<String, Object> actual = MCPRuntimeDatabaseRecoveryPayloadFactory.create(
                new RuntimeDatabaseConnectionException("", RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED, new SQLException("Connection failed.")));
        assertTrue(actual.containsKey("database"));
        assertThat(actual.get("database"), is(""));
    }
}
