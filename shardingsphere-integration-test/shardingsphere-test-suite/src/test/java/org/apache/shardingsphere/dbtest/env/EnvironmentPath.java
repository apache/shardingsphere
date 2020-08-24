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

package org.apache.shardingsphere.dbtest.env;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

/**
 * Environment path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentPath {
    
    private static final String DATABASE_ENVIRONMENT_RESOURCES_PATH = "integrate/env/%s/schema.xml";
    
    private static final String DATA_INITIALIZE_RESOURCES_PATH = "integrate/env/%s/dataset.xml";
    
    private static final String SHARDING_RULE_RESOURCES_PATH = "integrate/env/%s/sharding-rule.yaml";
    
    private static final String AUTHORITY_RESOURCES_PATH = "integrate/env/%s/authority.xml";
    
    /**
     * Get database environment resource File.
     * 
     * @param ruleType rule type
     * @return database environment resource file
     */
    public static String getDatabaseEnvironmentResourceFile(final String ruleType) {
        return getResourceFile(DATABASE_ENVIRONMENT_RESOURCES_PATH, ruleType);
    }
    
    /**
     * Get data initialize resource File.
     *
     * @param ruleType rule type
     * @return data initialize resource file
     */
    public static String getDataInitializeResourceFile(final String ruleType) {
        return getResourceFile(DATA_INITIALIZE_RESOURCES_PATH, ruleType);
    }
    
    /**
     * Get rule resource File.
     *
     * @param ruleType rule type
     * @return database environment resource file
     */
    public static String getRuleResourceFile(final String ruleType) {
        return getResourceFile(SHARDING_RULE_RESOURCES_PATH, ruleType);
    }
    
    private static String getResourceFile(final String resourcePath, final String ruleType) {
        URL resourceURL = EnvironmentPath.class.getClassLoader().getResource(String.format(resourcePath, ruleType));
        assertNotNull(resourceURL);
        return resourceURL.getFile();
    }
    
    /**
     * Get authority resource File.
     *
     * @param ruleType rule type
     * @return authority resource file
     */
    public static String getAuthorityResourcesPath(final String ruleType) {
        return getResourceFile(AUTHORITY_RESOURCES_PATH, ruleType);
    }
}
