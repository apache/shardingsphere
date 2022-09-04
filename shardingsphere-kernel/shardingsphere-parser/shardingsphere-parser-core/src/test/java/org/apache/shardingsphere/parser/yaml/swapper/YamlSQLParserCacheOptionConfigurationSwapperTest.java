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

package org.apache.shardingsphere.parser.yaml.swapper;

import static org.junit.Assert.assertEquals;

import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserCacheOptionRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Before;
import org.junit.Test;

public final class YamlSQLParserCacheOptionConfigurationSwapperTest {
    
    private final YamlSQLParserCacheOptionConfigurationSwapper configurationSwapper = new YamlSQLParserCacheOptionConfigurationSwapper();
    
    private final CacheOption expectedData = new CacheOption(2, 5);
    
    private final YamlSQLParserCacheOptionRuleConfiguration expectedConfiguration = new YamlSQLParserCacheOptionRuleConfiguration();
    
    @Before
    public void setup() {
        expectedConfiguration.setInitialCapacity(expectedData.getInitialCapacity());
        expectedConfiguration.setMaximumSize(expectedData.getMaximumSize());
    }
    
    @Test
    public void testSwapToYamlConfiguration() {
        YamlSQLParserCacheOptionRuleConfiguration actualResponse = configurationSwapper.swapToYamlConfiguration(expectedData);
        assertEquals(expectedConfiguration.getInitialCapacity(), actualResponse.getInitialCapacity());
        assertEquals(expectedConfiguration.getMaximumSize(), actualResponse.getMaximumSize());
    }
    
    @Test
    public void testSwapToObject() {
        CacheOption actualResponse = configurationSwapper.swapToObject(expectedConfiguration);
        assertEquals(expectedData.getInitialCapacity(), actualResponse.getInitialCapacity());
        assertEquals(expectedData.getMaximumSize(), actualResponse.getMaximumSize());
    }
}
