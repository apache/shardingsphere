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

package org.apache.shardingsphere.test.integration.framework.param.array;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.env.runtime.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Parameterized array factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterizedArrayFactory {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    /**
     * Get assertion parameterized array.
     *
     * @param sqlCommandType SQL command type
     * @return assertion parameterized array
     */
    public static Collection<AssertionParameterizedArray> getAssertionParameterized(final SQLCommandType sqlCommandType) {
        Collection<AssertionParameterizedArray> result = new LinkedList<>();
        for (String each : ENV.getRunModes()) {
            if ("Standalone".equalsIgnoreCase(each)) {
                result.addAll(StandaloneParameterizedArrayGenerator.getAssertionParameterized(sqlCommandType));
            } else if ("Cluster".equalsIgnoreCase(each)) {
                result.addAll(ClusterParameterizedArrayGenerator.getAssertionParameterized(sqlCommandType));
            }
        }
        return result;
    }
    
    /**
     * Get case parameterized array.
     *
     * @param sqlCommandType SQL command type
     * @return case parameterized array
     */
    public static Collection<ParameterizedArray> getCaseParameterized(final SQLCommandType sqlCommandType) {
        Collection<ParameterizedArray> result = new LinkedList<>();
        for (String each : ENV.getRunModes()) {
            if ("Standalone".equalsIgnoreCase(each)) {
                result.addAll(StandaloneParameterizedArrayGenerator.getCaseParameterized(sqlCommandType));
            } else if ("Cluster".equalsIgnoreCase(each)) {
                result.addAll(ClusterParameterizedArrayGenerator.getCaseParameterized(sqlCommandType));
            }
        }
        return result;
    }
}
