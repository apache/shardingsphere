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

package org.apache.shardingsphere.test.e2e.sql.framework.param.model;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment.Mode;
import org.apache.shardingsphere.test.e2e.sql.cases.casse.SQLE2ETestCaseContext;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;

/**
 * E2E test parameter.
 */
public interface E2ETestParameter {
    
    /**
     * Get test case context.
     *
     * @return test case context
     */
    SQLE2ETestCaseContext getTestCaseContext();
    
    /**
     * Get database type.
     *
     * @return database type
     */
    DatabaseType getDatabaseType();
    
    /**
     * Get scenario.
     *
     * @return scenario
     */
    String getScenario();
    
    /**
     * Get adapter.
     *
     * @return adapter
     */
    String getAdapter();
    
    /**
     * Get mode.
     *
     * @return mode
     */
    Mode getMode();
    
    /**
     * Get sql command type.
     *
     * @return sql command type
     */
    SQLCommandType getSqlCommandType();
    
    /**
     * Get key.
     *
     * @return key of test parameter
     */
    default String getKey() {
        return String.join("-", getSqlCommandType().toString(), getScenario(), getAdapter(), getDatabaseType().getType());
    }
}
