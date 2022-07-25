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

package org.apache.shardingsphere.test.integration.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.integration.env.runtime.IntegrationTestEnvironment;

/**
 * Total test suites count calculator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TotalSuitesCountCalculator {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private static final int GENERIC_SUITES_COUNT = 4;
    
    private static final int ADDITIONAL_SUITES_COUNT = 2;
    
    private static final int DCL_SUITES_COUNT = 1;
    
    private static final int DAL_SUITES_COUNT = 1;
    
    private static final int DISTSQL_SUITES_COUNT = 3;
    
    /**
     * Calculate total test suites count.
     * 
     * @return total test suites count
     */
    public static int calculate() {
        int result = GENERIC_SUITES_COUNT;
        if (ENV.isRunAdditionalTestCases()) {
            result += ADDITIONAL_SUITES_COUNT;
        }
        if (isRunDCL()) {
            result += DCL_SUITES_COUNT;
        }
        if (isRunProxy()) {
            result += DAL_SUITES_COUNT;
            result += DISTSQL_SUITES_COUNT;
        }
        return result;
    }
    
    private static boolean isRunDCL() {
        return ENV.getRunModes().contains("Cluster");
    }
    
    private static boolean isRunProxy() {
        return ENV.getRunModes().contains("Cluster") && ENV.getClusterEnvironment().getAdapters().contains("proxy");
    }
}
