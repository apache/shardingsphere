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

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Environment path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentPath {
    
    private static final String ROOT_PATH = "integrate/env";
    
    private static final String SCHEMA_FILE = "schema.xml";
    
    private static final String DATA_FILE = "data.xml";
    
    private static final String RULES_CONFIG_FILE = "rules.yaml";
    
    private static final String AUTHORITY_FILE = "authority.xml";
    
    /**
     * Get schema file.
     * 
     * @param ruleType rule type
     * @return schema file
     */
    public static String getSchemaFile(final String ruleType) {
        return getFile(ruleType, SCHEMA_FILE);
    }
    
    /**
     * Get data file.
     *
     * @param ruleType rule type
     * @return data file
     */
    public static String getDataFile(final String ruleType) {
        return getFile(ruleType, DATA_FILE);
    }
    
    /**
     * Get rules configuration file.
     *
     * @param ruleType rule type
     * @return rules configuration file
     */
    public static String getRulesConfigurationFile(final String ruleType) {
        return getFile(ruleType, RULES_CONFIG_FILE);
    }
    
    /**
     * Get authority file.
     *
     * @param ruleType rule type
     * @return authority file
     */
    public static String getAuthorityFile(final String ruleType) {
        return getFile(ruleType, AUTHORITY_FILE);
    }
    
    private static String getFile(final String ruleType, final String fileName) {
        URL url = EnvironmentPath.class.getClassLoader().getResource(String.join(File.separator, ROOT_PATH, ruleType, fileName));
        assertNotNull(url);
        return url.getFile();
    }
}
