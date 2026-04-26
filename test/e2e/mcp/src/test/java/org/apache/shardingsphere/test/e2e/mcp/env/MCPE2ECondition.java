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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPE2ECondition {
    
    public static boolean isContractEnabled() {
        return isContractEnabled(MCPE2ETestConfiguration.getInstance());
    }
    
    static boolean isContractEnabled(final MCPE2ETestConfiguration config) {
        return config.isContractEnabled();
    }
    
    public static boolean isProductionH2Enabled() {
        return isProductionH2Enabled(MCPE2ETestConfiguration.getInstance());
    }
    
    static boolean isProductionH2Enabled(final MCPE2ETestConfiguration config) {
        return config.isProductionH2Enabled();
    }
    
    public static boolean isProductionMySQLEnabled() {
        return isProductionMySQLEnabled(MCPE2ETestConfiguration.getInstance());
    }
    
    static boolean isProductionMySQLEnabled(final MCPE2ETestConfiguration config) {
        return config.isProductionMySQLEnabled();
    }
    
    public static boolean isProductionStdioEnabled() {
        return isProductionStdioEnabled(MCPE2ETestConfiguration.getInstance());
    }
    
    static boolean isProductionStdioEnabled(final MCPE2ETestConfiguration config) {
        return config.isProductionStdioEnabled();
    }
    
    public static boolean isProductionMySQLStdioEnabled() {
        return isProductionMySQLStdioEnabled(MCPE2ETestConfiguration.getInstance());
    }
    
    static boolean isProductionMySQLStdioEnabled(final MCPE2ETestConfiguration config) {
        return config.isProductionMySQLEnabled() && config.isProductionStdioEnabled();
    }
    
    public static boolean isProxyWorkflowEnabled() {
        return isProxyWorkflowEnabled(MCPE2ETestConfiguration.getInstance());
    }
    
    static boolean isProxyWorkflowEnabled(final MCPE2ETestConfiguration config) {
        return config.isProxyWorkflowEnabled();
    }
    
    public static boolean isDistributionEnabled() {
        return isDistributionEnabled(MCPE2ETestConfiguration.getInstance());
    }
    
    static boolean isDistributionEnabled(final MCPE2ETestConfiguration config) {
        return config.isDistributionEnabled();
    }
    
    public static boolean isLLMEnabled() {
        return isLLMEnabled(MCPE2ETestConfiguration.getInstance());
    }
    
    static boolean isLLMEnabled(final MCPE2ETestConfiguration config) {
        return config.isLLMEnabled();
    }
}
