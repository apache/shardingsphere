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

package org.apache.shardingsphere.test.integration.env.runtime.scenario.path;

import lombok.RequiredArgsConstructor;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Scenario common path.
 */
@RequiredArgsConstructor
public final class ScenarioCommonPath {
    
    private static final String ROOT_PATH = "env/scenario";
    
    private static final String RULE_CONFIG_FILE = "rules.yaml";
    
    private static final String AUTHORITY_FILE = "authority.xml";
    
    private final String scenario;
    
    /**
     * Check folder exist.
     */
    public void checkFolderExist() {
        String scenarioDirectory = String.join("/", ROOT_PATH, scenario);
        assertNotNull(String.format("Scenario folder `%s` must exist", scenarioDirectory), ScenarioCommonPath.class.getClassLoader().getResource(scenarioDirectory));
    }
    
    /**
     * Get rule configuration file.
     *
     * @return rule configuration file
     */
    public String getRuleConfigurationFile() {
        return getFile(RULE_CONFIG_FILE);
    }
    
    /**
     * Get authority file.
     *
     * @return authority file
     */
    public String getAuthorityFile() {
        return getFile(AUTHORITY_FILE);
    }
    
    private String getFile(final String fileName) {
        String path = String.join("/", ROOT_PATH, scenario, fileName);
        URL url = ScenarioCommonPath.class.getClassLoader().getResource(path);
        assertNotNull(String.format("File `%s` must exist", path), url);
        return url.getFile();
    }
}
