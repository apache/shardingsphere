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

package org.apache.shardingsphere.proxy.backend.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class only records variables need to be replayed on connections.
 */
public final class RequiredSessionVariableRecorder {
    
    private static final String DEFAULT = "DEFAULT";
    
    private final Map<String, String> sessionVariables = new ConcurrentHashMap<>();
    
    /**
     * Set variable.
     *
     * @param variableName variable name
     * @param variableValue variable value
     */
    public void setVariable(final String variableName, final String variableValue) {
        sessionVariables.put(variableName, variableValue);
    }
    
    /**
     * Return true if no session variable was set.
     *
     * @return true if no session variable was set
     */
    public boolean isEmpty() {
        return sessionVariables.isEmpty();
    }
    
    /**
     * Get set SQLs for database.
     *
     * @param databaseType database type
     * @return set SQLs
     */
    public List<String> toSetSQLs(final String databaseType) {
        if (sessionVariables.isEmpty()) {
            return Collections.emptyList();
        }
        // TODO Refactor the following switch by SPI if we support more database in future
        switch (databaseType) {
            case "MySQL":
                return Collections.singletonList(aggregateToMySQLSetSQL());
            case "PostgreSQL":
                return convertToPostgreSQLSetSQLs();
            default:
                return Collections.emptyList();
        }
    }
    
    private String aggregateToMySQLSetSQL() {
        StringJoiner result = new StringJoiner(",", "SET ", "");
        for (Entry<String, String> stringStringEntry : sessionVariables.entrySet()) {
            String s = stringStringEntry.getKey() + "=" + stringStringEntry.getValue();
            result.add(s);
        }
        return result.toString();
    }
    
    private List<String> convertToPostgreSQLSetSQLs() {
        List<String> result = new ArrayList<>(sessionVariables.size());
        for (Entry<String, String> entry : sessionVariables.entrySet()) {
            result.add("SET " + entry.getKey() + "=" + entry.getValue());
        }
        return result;
    }
    
    /**
     * Get reset SQLs for database.
     *
     * @param databaseType database type
     * @return reset SQLs
     */
    public List<String> toResetSQLs(final String databaseType) {
        if (sessionVariables.isEmpty()) {
            return Collections.emptyList();
        }
        // TODO Refactor the following switch by SPI if we support more database in future
        switch (databaseType) {
            case "MySQL":
                return Collections.singletonList(aggregateToMySQLSetDefaultSQLs());
            case "PostgreSQL":
                return Collections.singletonList("RESET ALL");
            default:
                return Collections.emptyList();
        }
    }
    
    private String aggregateToMySQLSetDefaultSQLs() {
        StringJoiner result = new StringJoiner(",", "SET ", "");
        for (String each : sessionVariables.keySet()) {
            result.add(each + "=" + DEFAULT);
        }
        return result.toString();
    }
    
    /**
     * Remove variables with default value.
     */
    public void removeVariablesWithDefaultValue() {
        sessionVariables.entrySet().removeIf(entry -> DEFAULT.equalsIgnoreCase(entry.getValue()));
    }
}
