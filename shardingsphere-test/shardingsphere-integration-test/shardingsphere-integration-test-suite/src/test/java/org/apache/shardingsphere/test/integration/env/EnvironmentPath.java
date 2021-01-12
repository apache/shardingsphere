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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Environment path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentPath {
    
    private static final String ROOT_PATH = "env";
    
    private static final String SCHEMA_FILE = "schema.xml";
    
    private static final String DATASET_FILE = "dataset.xml";
    
    private static final String RULES_CONFIG_FILE = "rules.yaml";
    
    private static final String AUTHORITY_FILE = "authority.xml";
    
    /**
     * Get schema file.
     * 
     * @param scenario scenario
     * @return schema file
     */
    public static String getSchemaFile(final String scenario) {
        return getFile(scenario, SCHEMA_FILE);
    }
    
    /**
     * Get data set file.
     *
     * @param scenario scenario
     * @return data set file
     */
    public static String getDataSetFile(final String scenario) {
        return getFile(scenario, DATASET_FILE);
    }
    
    /**
     * Get rules configuration file.
     *
     * @param scenario scenario
     * @return rules configuration file
     */
    public static String getRulesConfigurationFile(final String scenario) {
        return getFile(scenario, RULES_CONFIG_FILE);
    }
    
    /**
     * Get authority file.
     *
     * @param scenario scenario
     * @return authority file
     */
    public static String getAuthorityFile(final String scenario) {
        return getFile(scenario, AUTHORITY_FILE);
    }
    
    private static String getFile(final String scenario, final String fileName) {
        URL url = EnvironmentPath.class.getClassLoader().getResource(String.join("/", ROOT_PATH, scenario, fileName));
        assertNotNull(url);
        return url.getFile();
    }
}
