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

package org.apache.shardingsphere.test.e2e.mcp.env;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPE2ETestConfigurationTest {
    
    @Test
    void assertGetInstance() {
        assertNotNull(MCPE2ETestConfiguration.getInstance());
    }
    
    @Test
    void assertDefaultLaneValues() {
        MCPE2ETestConfiguration config = new MCPE2ETestConfiguration(new Properties());
        assertTrue(config.isContractEnabled());
        assertTrue(config.isProductionH2Enabled());
        assertFalse(config.isProductionMySQLEnabled());
        assertFalse(config.isProductionStdioEnabled());
        assertFalse(config.isProxyWorkflowEnabled());
        assertFalse(config.isDistributionEnabled());
        assertFalse(config.isLLMEnabled());
    }
    
    @Test
    void assertExplicitLaneOverrides() {
        Properties props = new Properties();
        props.setProperty("mcp.e2e.contract.enabled", "false");
        props.setProperty("mcp.e2e.production.h2.enabled", "false");
        props.setProperty("mcp.e2e.production.mysql.enabled", "true");
        props.setProperty("mcp.e2e.production.stdio.enabled", "true");
        props.setProperty("mcp.e2e.proxy.workflow.enabled", "true");
        props.setProperty("mcp.e2e.distribution.enabled", "true");
        props.setProperty("mcp.e2e.llm.enabled", "true");
        MCPE2ETestConfiguration config = new MCPE2ETestConfiguration(props);
        assertFalse(config.isContractEnabled());
        assertFalse(config.isProductionH2Enabled());
        assertTrue(config.isProductionMySQLEnabled());
        assertTrue(config.isProductionStdioEnabled());
        assertTrue(config.isProxyWorkflowEnabled());
        assertTrue(config.isDistributionEnabled());
        assertTrue(config.isLLMEnabled());
    }
    
    @Test
    void assertInvalidBooleanFallsBackToDefault() {
        Properties props = new Properties();
        props.setProperty("mcp.e2e.contract.enabled", "invalid");
        props.setProperty("mcp.e2e.production.mysql.enabled", "invalid");
        MCPE2ETestConfiguration config = new MCPE2ETestConfiguration(props);
        assertTrue(config.isContractEnabled());
        assertFalse(config.isProductionMySQLEnabled());
    }
    
    @Test
    void assertCompositeMySQLStdioCondition() {
        Properties mysqlOnlyProps = new Properties();
        mysqlOnlyProps.setProperty("mcp.e2e.production.mysql.enabled", "true");
        MCPE2ETestConfiguration mysqlOnlyConfig = new MCPE2ETestConfiguration(mysqlOnlyProps);
        Properties bothProps = new Properties();
        bothProps.setProperty("mcp.e2e.production.mysql.enabled", "true");
        bothProps.setProperty("mcp.e2e.production.stdio.enabled", "true");
        MCPE2ETestConfiguration bothConfig = new MCPE2ETestConfiguration(bothProps);
        assertFalse(MCPE2ECondition.isProductionMySQLStdioEnabled(mysqlOnlyConfig));
        assertTrue(MCPE2ECondition.isProductionMySQLStdioEnabled(bothConfig));
    }
}
