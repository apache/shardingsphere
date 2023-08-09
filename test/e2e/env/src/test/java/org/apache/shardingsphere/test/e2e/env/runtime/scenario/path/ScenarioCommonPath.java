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

package org.apache.shardingsphere.test.e2e.env.runtime.scenario.path;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertNotNull(Thread.currentThread().getContextClassLoader().getResource(scenarioDirectory), String.format("Scenario folder `%s` must exist.", scenarioDirectory));
    }
    
    /**
     * Get rule configuration file.
     *
     * @param databaseType database type
     * @return rule configuration file
     */
    public String getRuleConfigurationFile(final DatabaseType databaseType) {
        String databaseFileName = String.join("/", String.format("env/scenario/%s/jdbc/conf", scenario), databaseType.getType().toLowerCase(), RULE_CONFIG_FILE);
        return exists(databaseFileName) ? getFile(databaseFileName) : getFile(String.join("/", ROOT_PATH, scenario, RULE_CONFIG_FILE));
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
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        assertNotNull(url, String.format("File `%s` must exist.", fileName));
        return url.getFile();
    }
    
    private boolean exists(final String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        return null != url;
    }
}
