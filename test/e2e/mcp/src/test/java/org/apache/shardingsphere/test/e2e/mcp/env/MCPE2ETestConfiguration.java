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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;

import java.util.Locale;
import java.util.Properties;

@RequiredArgsConstructor
final class MCPE2ETestConfiguration {
    
    private static final MCPE2ETestConfiguration INSTANCE = new MCPE2ETestConfiguration(EnvironmentPropertiesLoader.loadProperties("env/e2e-env.properties"));
    
    private final Properties props;
    
    static MCPE2ETestConfiguration getInstance() {
        return INSTANCE;
    }
    
    boolean isContractEnabled() {
        return getBoolean("mcp.e2e.contract.enabled", true);
    }
    
    boolean isProductionH2Enabled() {
        return getBoolean("mcp.e2e.production.h2.enabled", true);
    }
    
    boolean isProductionMySQLEnabled() {
        return getBoolean("mcp.e2e.production.mysql.enabled", false);
    }
    
    boolean isProductionStdioEnabled() {
        return getBoolean("mcp.e2e.production.stdio.enabled", false);
    }
    
    boolean isProxyWorkflowEnabled() {
        return getBoolean("mcp.e2e.proxy.workflow.enabled", false);
    }
    
    boolean isDistributionEnabled() {
        return getBoolean("mcp.e2e.distribution.enabled", false);
    }
    
    boolean isLLMEnabled() {
        return getBoolean("mcp.e2e.llm.enabled", false);
    }
    
    private boolean getBoolean(final String key, final boolean defaultValue) {
        String value = props.getProperty(key);
        if (null == value) {
            return defaultValue;
        }
        String trimmedValue = value.trim().toLowerCase(Locale.ENGLISH);
        if ("true".equals(trimmedValue)) {
            return true;
        }
        if ("false".equals(trimmedValue)) {
            return false;
        }
        return defaultValue;
    }
}
