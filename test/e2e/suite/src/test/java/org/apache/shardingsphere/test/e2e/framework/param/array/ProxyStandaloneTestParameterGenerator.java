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

/**
 * PROXY test parameter generator for standalone mode.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyStandaloneTestParameterGenerator {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    /**
     * Get assertion test parameters.
     *
     * @param sqlCommandType SQL command type
     * @return assertion test parameters
     */
    public static Collection<AssertionTestParameter> getAssertionTestParameter(final SQLCommandType sqlCommandType) {
        return new E2ETestParameterGenerator(ENV.getClusterEnvironment().getAdapters(),
                ENV.getScenarios(), AdapterMode.STANDALONE.getValue(), ENV.getClusterEnvironment().getDatabaseTypes()).getAssertionTestParameter(sqlCommandType);
    }
    
    /**
     * Get case test parameters.
     *
     * @param sqlCommandType SQL command type
     * @return assertion test parameters
     */
    public static Collection<E2ETestParameter> getCaseTestParameter(final SQLCommandType sqlCommandType) {
        return new E2ETestParameterGenerator(ENV.getClusterEnvironment().getAdapters(),
                ENV.getScenarios(), AdapterMode.STANDALONE.getValue(), ENV.getClusterEnvironment().getDatabaseTypes()).getCaseTestParameter(sqlCommandType);
    }
}
