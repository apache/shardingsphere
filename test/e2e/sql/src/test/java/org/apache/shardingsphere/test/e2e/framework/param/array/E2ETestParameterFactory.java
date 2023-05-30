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

package org.apache.shardingsphere.test.e2e.framework.param.array;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.runtime.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.framework.param.model.E2ETestParameter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * E2E test parameter factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class E2ETestParameterFactory {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    /**
     * Get assertion test parameters.
     *
     * @param sqlCommandType SQL command type
     * @return assertion test parameters
     */
    public static Collection<AssertionTestParameter> getAssertionTestParameters(final SQLCommandType sqlCommandType) {
        Collection<AssertionTestParameter> result = new LinkedList<>();
        for (String each : ENV.getRunModes()) {
            if (AdapterMode.STANDALONE.getValue().equalsIgnoreCase(each)) {
                result.addAll(isDistSQLCommandType(sqlCommandType)
                        ? ProxyStandaloneTestParameterGenerator.getAssertionTestParameter(sqlCommandType)
                        : JdbcStandaloneTestParameterGenerator.getAssertionTestParameter(sqlCommandType));
            } else if (AdapterMode.CLUSTER.getValue().equalsIgnoreCase(each)) {
                result.addAll(ClusterTestParameterArrayGenerator.getAssertionTestParameter(sqlCommandType));
            }
        }
        return result;
    }
    
    /**
     * Get case test parameters.
     *
     * @param sqlCommandType SQL command type
     * @return case test parameters
     */
    public static Collection<E2ETestParameter> getCaseTestParameters(final SQLCommandType sqlCommandType) {
        Collection<E2ETestParameter> result = new LinkedList<>();
        for (String each : ENV.getRunModes()) {
            if (AdapterMode.STANDALONE.getValue().equalsIgnoreCase(each)) {
                result.addAll(isDistSQLCommandType(sqlCommandType)
                        ? ProxyStandaloneTestParameterGenerator.getCaseTestParameter(sqlCommandType)
                        : JdbcStandaloneTestParameterGenerator.getCaseTestParameter(sqlCommandType));
            } else if (AdapterMode.CLUSTER.getValue().equalsIgnoreCase(each)) {
                result.addAll(ClusterTestParameterArrayGenerator.getCaseTestParameter(sqlCommandType));
            }
        }
        return result;
    }
    
    private static boolean isDistSQLCommandType(final SQLCommandType sqlCommandType) {
        return SQLCommandType.RDL == sqlCommandType || SQLCommandType.RAL == sqlCommandType || SQLCommandType.RQL == sqlCommandType;
    }
    
    /**
     * Judge whether contains test parameter.
     * 
     * @return contains or not
     */
    public static boolean containsTestParameter() {
        return IntegrationTestEnvironment.getInstance().getRunModes().stream()
                .anyMatch(each -> AdapterMode.STANDALONE.getValue().equalsIgnoreCase(each) || AdapterMode.CLUSTER.getValue().equalsIgnoreCase(each));
    }
}
