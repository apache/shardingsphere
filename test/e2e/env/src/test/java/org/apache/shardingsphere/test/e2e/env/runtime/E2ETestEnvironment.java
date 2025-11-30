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

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.env.runtime.type.ArtifactEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.DockerEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.NativeDatabaseEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioCommonPath;

import java.util.Collection;
import java.util.Properties;
import java.util.TimeZone;

/**
 * E2E test environment.
 */
@Getter
public final class E2ETestEnvironment {
    
    private static final E2ETestEnvironment INSTANCE = new E2ETestEnvironment();
    
    private final Collection<String> scenarios;
    
    private final RunEnvironment runEnvironment;
    
    private final ArtifactEnvironment artifactEnvironment;
    
    private final DockerEnvironment dockerEnvironment;
    
    private final NativeDatabaseEnvironment nativeDatabaseEnvironment;
    
    private E2ETestEnvironment() {
        Properties props = EnvironmentPropertiesLoader.loadProperties();
        TimeZone.setDefault(TimeZone.getTimeZone(props.getProperty("e2e.timezone", "UTC")));
        scenarios = EnvironmentPropertiesLoader.getListValue(props, "e2e.scenarios");
        scenarios.forEach(each -> new ScenarioCommonPath(each).checkFolderExisted());
        runEnvironment = new RunEnvironment(props);
        artifactEnvironment = new ArtifactEnvironment(props);
        dockerEnvironment = new DockerEnvironment(props);
        nativeDatabaseEnvironment = new NativeDatabaseEnvironment(props);
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
