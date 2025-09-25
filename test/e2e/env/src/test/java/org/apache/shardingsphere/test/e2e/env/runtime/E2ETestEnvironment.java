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

package org.apache.shardingsphere.test.e2e.env.runtime;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.NativeStorageEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * E2E test environment.
 */
@Getter
public final class E2ETestEnvironment {
    
    private static final E2ETestEnvironment INSTANCE = new E2ETestEnvironment();
    
    private final Collection<String> scenarios;
    
    private final RunEnvironment runEnvironment;
    
    private final ArtifactEnvironment artifactEnvironment;
    
    private final NativeStorageEnvironment nativeStorageEnvironment;
    
    private E2ETestEnvironment() {
        Properties props = loadProperties();
        TimeZone.setDefault(TimeZone.getTimeZone(props.getProperty("e2e.timezone", "UTC")));
        scenarios = getScenarios(props);
        runEnvironment = new RunEnvironment(props);
        artifactEnvironment = new ArtifactEnvironment(props);
        nativeStorageEnvironment = new NativeStorageEnvironment(props);
    }
    
    @SneakyThrows(IOException.class)
    private Properties loadProperties() {
        Properties result = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("env/e2e-env.properties")) {
            result.load(inputStream);
        }
        for (String each : System.getProperties().stringPropertyNames()) {
            result.setProperty(each, System.getProperty(each));
        }
        return result;
    }
    
    private Collection<String> getScenarios(final Properties props) {
        Collection<String> result = Splitter.on(",").trimResults().splitToList(props.getProperty("e2e.scenarios", "")).stream().filter(each -> !each.isEmpty()).collect(Collectors.toList());
//        for (String each : result) {
//            new ScenarioCommonPath(each).checkFolderExist();
//        }
        return result;
    }
    
    /**
     * Judge whether valid E2E test environment.
     *
     * @return valid or invalid E2E test environment
     */
    public boolean isValid() {
        return !scenarios.isEmpty() && null != runEnvironment.getType()
                && !artifactEnvironment.getModes().isEmpty() && !artifactEnvironment.getAdapters().isEmpty() && !artifactEnvironment.getDatabaseTypes().isEmpty();
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static E2ETestEnvironment getInstance() {
        return INSTANCE;
    }
}
