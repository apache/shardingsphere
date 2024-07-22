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

package org.apache.shardingsphere.test.e2e.agent.metrics;

import org.apache.shardingsphere.test.e2e.agent.engine.env.props.AgentE2ETestConfiguration;
import org.apache.shardingsphere.test.e2e.agent.engine.env.AgentE2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.engine.framework.AgentE2ETestActionExtension;
import org.apache.shardingsphere.test.e2e.agent.engine.framework.AgentE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.MetricAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricE2ETestCase;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricE2ETestCases;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

@ExtendWith(AgentE2ETestActionExtension.class)
class MetricsPluginE2EIT {
    
    @EnabledIf("isEnabled")
    @ParameterizedTest
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertWithAgent(final MetricE2ETestCase metricTestCase) {
        MetricAssert.assertIs(AgentE2ETestEnvironment.getInstance().getAgentPluginURL(), metricTestCase);
    }
    
    private static boolean isEnabled() {
        return AgentE2ETestConfiguration.getInstance().containsTestParameter();
    }
    
    private static final class TestCaseArgumentsProvider extends AgentE2ETestCaseArgumentsProvider {
        
        private TestCaseArgumentsProvider() {
            super(MetricE2ETestCases.class);
        }
    }
}
