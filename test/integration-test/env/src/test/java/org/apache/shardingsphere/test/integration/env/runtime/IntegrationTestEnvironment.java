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

package org.apache.shardingsphere.test.integration.env.runtime;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.test.integration.env.runtime.cluster.ClusterEnvironment;
import org.apache.shardingsphere.test.integration.env.runtime.scenario.path.ScenarioCommonPath;

import java.io.IOException;
import java.io.InputStream;
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
        Properties props = loadProperties();
        runModes = Splitter.on(",").trimResults().splitToList(props.getProperty("it.run.modes"));
        runAdditionalTestCases = Boolean.parseBoolean(props.getProperty("it.run.additional.cases"));
        scenarios = getScenarios(props);
        clusterEnvironment = new ClusterEnvironment(props);
    }
    
    @SuppressWarnings("AccessOfSystemProperties")
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = IntegrationTestEnvironment.class.getClassLoader().getResourceAsStream("env/it-env.properties")) {
            result.load(inputStream);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
    
    private Collection<String> getScenarios(final Properties props) {
        Collection<String> result = Splitter.on(",").trimResults().splitToList(props.getProperty("it.scenarios"));
        for (String each : result) {
            new ScenarioCommonPath(each).checkFolderExist();
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
