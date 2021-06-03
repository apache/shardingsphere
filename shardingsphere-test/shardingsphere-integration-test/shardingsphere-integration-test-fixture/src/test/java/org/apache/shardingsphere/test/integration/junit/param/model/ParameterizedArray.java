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

package org.apache.shardingsphere.test.integration.junit.param.model;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.cases.IntegrationTestCaseContext;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.junit.compose.ContainerCompose;

/**
 * Parameterized array.
 */
public interface ParameterizedArray {
    
    /**
     * Get test case context.
     * 
     * @return test case context
     */
    IntegrationTestCaseContext getTestCaseContext();
    
    /**
     * Get adapter.
     * 
     * @return adapter
     */
    String getAdapter();
    
    /**
     * Get scenario.
     * 
     * @return scenario
     */
    String getScenario();
    
    /**
     * Get database type.
     *
     * @return database type
     */
    DatabaseType getDatabaseType();
    
    /**
     * Get SQL command type.
     *
     * @return SQL command type
     */
    SQLCommandType getSqlCommandType();
    
    /**
     * Get container compose.
     *
     * @return container compose
     */
    ContainerCompose getCompose();
    
    /**
     * Set container compose.
     *
     * @param compose container compose
     */
    void setCompose(ContainerCompose compose);
}
