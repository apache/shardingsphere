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

package org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Scenario common path.
 */
public final class ScenarioCommonPath {
    
    private static final String RULE_CONFIG_FILE = "rules.yaml";
    
    private static final String AUTHORITY_FILE = "authority.xml";
    
    private final String scenarioDirectory;
    
    public ScenarioCommonPath(final String scenario) {
        scenarioDirectory = String.join("/", "env", "scenario", scenario);
    }
    
    /**
     * Check folder existed.
     */
    public void checkFolderExisted() {
        assertNotNull(Thread.currentThread().getContextClassLoader().getResource(scenarioDirectory), String.format("Scenario folder `%s` must exist.", scenarioDirectory));
    }
    
    /**
     * Get rule configuration file.
     *
     * @param databaseType database type
     * @return rule configuration file
     */
    public String getRuleConfigurationFile(final DatabaseType databaseType) {
        String ruleConfigFileName = String.join("/", "jdbc", "conf", databaseType.getType().toLowerCase(), RULE_CONFIG_FILE);
        return isFileExisted(ruleConfigFileName) ? getFile(ruleConfigFileName) : getFile(RULE_CONFIG_FILE);
    }
    
    private boolean isFileExisted(final String fileName) {
        return null != Thread.currentThread().getContextClassLoader().getResource(String.join("/", scenarioDirectory, fileName));
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
        String scenarioFile = String.join("/", scenarioDirectory, fileName);
        URL url = Thread.currentThread().getContextClassLoader().getResource(scenarioFile);
        assertNotNull(url, String.format("File `%s` must exist.", scenarioFile));
        return url.getFile();
    }
}
