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

package org.apache.shardingsphere.test.integration.env;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.test.integration.env.cluster.ClusterEnvironment;
import org.apache.shardingsphere.test.integration.env.props.EnvironmentProperties;

import java.util.Collection;
import java.util.Properties;

/**
 * Integration test environment.
 */
@Getter
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final Collection<String> runModes;
    
    private final boolean runAdditionalTestCases;
    
    private final Collection<String> scenarios;
    
    private final ClusterEnvironment clusterEnvironment;
    
    private IntegrationTestEnvironment() {
        Properties envProps = EnvironmentProperties.loadProperties("env/engine-env.properties");
        runModes = Splitter.on(",").trimResults().splitToList(envProps.getProperty("it.run.modes"));
        runAdditionalTestCases = Boolean.parseBoolean(envProps.getProperty("it.run.additional.cases"));
        scenarios = getScenarios(envProps);
        clusterEnvironment = new ClusterEnvironment(envProps);
    }
    
    private Collection<String> getScenarios(final Properties envProps) {
        Collection<String> result = Splitter.on(",").trimResults().splitToList(envProps.getProperty("it.scenarios"));
        for (String each : result) {
            EnvironmentPath.assertScenarioDirectoryExisted(each);
        }
        return result;
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
}
