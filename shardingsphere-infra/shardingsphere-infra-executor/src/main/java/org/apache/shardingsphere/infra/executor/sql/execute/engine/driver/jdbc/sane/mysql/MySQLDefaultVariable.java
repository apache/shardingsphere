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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane.mysql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * MySQL default variable.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLDefaultVariable {
    
    private static final Map<String, String> VARIABLES = new HashMap<>(20, 1);
    
    static {
        VARIABLES.put("auto_increment_increment", "1");
        VARIABLES.put("character_set_client", "utf8");
        VARIABLES.put("character_set_connection", "utf8");
        VARIABLES.put("character_set_results", "utf8");
        VARIABLES.put("character_set_server", "utf8");
        VARIABLES.put("collation_server", "utf8_general_ci");
        VARIABLES.put("collation_connection", "utf8_general_ci");
        VARIABLES.put("init_connect", "");
        VARIABLES.put("interactive_timeout", "28800");
        VARIABLES.put("license", "GPL");
        VARIABLES.put("lower_case_table_names", "2");
        VARIABLES.put("max_allowed_packet", "4194304");
        VARIABLES.put("net_buffer_length", "16384");
        VARIABLES.put("net_write_timeout", "60");
        VARIABLES.put("sql_mode", "STRICT_TRANS_TABLES");
        VARIABLES.put("system_time_zone", "CST");
        VARIABLES.put("time_zone", "SYSTEM");
        VARIABLES.put("transaction_isolation", "REPEATABLE-READ");
        VARIABLES.put("wait_timeout", "28800");
        VARIABLES.put("@@session.transaction_read_only", "0");
    }
    
    /**
     * Judge whether contains variable.
     * 
     * @param variableName variable name
     * @return contains variable or not
     */
    public static boolean containsVariable(final String variableName) {
        return VARIABLES.containsKey(variableName);
    }
    
    /**
     * Get variable value.
     *
     * @param variableName variable name
     * @return variable value
     */
    public static String getVariable(final String variableName) {
        return VARIABLES.get(variableName);
    }
}
