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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckIgnoredType;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlTableDataConsistencyCheckResultSwapperTest {
    
    private YamlTableDataConsistencyCheckResultSwapper yamlTableDataConsistencyCheckResultSwapper;
    
    @BeforeEach
    void setUp() {
        yamlTableDataConsistencyCheckResultSwapper = new YamlTableDataConsistencyCheckResultSwapper();
    }
    
    @Test
    void assertSwapToYamlConfigurationWithTableDataConsistencyCheckIgnoredType() {
        TableDataConsistencyCheckResult data = new TableDataConsistencyCheckResult(TableDataConsistencyCheckIgnoredType.NO_UNIQUE_KEY);
        YamlTableDataConsistencyCheckResult result = yamlTableDataConsistencyCheckResultSwapper.swapToYamlConfiguration(data);
        assertThat(result.getIgnoredType(), is("NO_UNIQUE_KEY"));
        assertFalse(result.isMatched());
    }
    
    @Test
    void assertSwapToYamlConfigurationWithTableDataConsistencyCheckResultMatched() {
        TableDataConsistencyCheckResult data = new TableDataConsistencyCheckResult(true);
        YamlTableDataConsistencyCheckResult result = yamlTableDataConsistencyCheckResultSwapper.swapToYamlConfiguration(data);
        assertNull(result.getIgnoredType());
        assertTrue(result.isMatched());
    }
    
    @Test
    void assertSwapToObjectWithYamlTableDataConsistencyCheckResultIgnoredType() {
        YamlTableDataConsistencyCheckResult yamlConfig = new YamlTableDataConsistencyCheckResult();
        yamlConfig.setIgnoredType("NO_UNIQUE_KEY");
        TableDataConsistencyCheckResult result = yamlTableDataConsistencyCheckResultSwapper.swapToObject(yamlConfig);
        assertThat(result.getIgnoredType(), is(TableDataConsistencyCheckIgnoredType.NO_UNIQUE_KEY));
        assertFalse(result.isMatched());
    }
    
    @Test
    void assertSwapToObjectWithYamlTableDataConsistencyCheckResultMatched() {
        YamlTableDataConsistencyCheckResult yamlConfig = new YamlTableDataConsistencyCheckResult(true);
        TableDataConsistencyCheckResult result = yamlTableDataConsistencyCheckResultSwapper.swapToObject(yamlConfig);
        assertNull(result.getIgnoredType());
        assertTrue(result.isMatched());
    }
    
    @Test
    void assertSwapToObjectWithNullYamlTableDataConsistencyCheckResult() {
        assertNull(yamlTableDataConsistencyCheckResultSwapper.swapToObject((YamlTableDataConsistencyCheckResult) null));
    }
    
    @Test
    void assertSwapToObjectWithNullYamlTableDataConsistencyCheckResultIgnoredType() {
        YamlTableDataConsistencyCheckResult yamlConfig = new YamlTableDataConsistencyCheckResult();
        yamlConfig.setIgnoredType(null);
        TableDataConsistencyCheckResult result = yamlTableDataConsistencyCheckResultSwapper.swapToObject(yamlConfig);
        assertNull(result.getIgnoredType());
        assertFalse(result.isMatched());
    }
    
    @Test
    void assertSwapToObjectWithEmptyYamlTableDataConsistencyCheckResultMatched() {
        YamlTableDataConsistencyCheckResult yamlConfig = new YamlTableDataConsistencyCheckResult();
        yamlConfig.setIgnoredType("");
        TableDataConsistencyCheckResult result = yamlTableDataConsistencyCheckResultSwapper.swapToObject(yamlConfig);
        assertNull(result.getIgnoredType());
        assertFalse(result.isMatched());
    }
    
    @Test
    void assertSwapToObjectWithString() {
        TableDataConsistencyCheckResult result = yamlTableDataConsistencyCheckResultSwapper.swapToObject("ignoredType: NO_UNIQUE_KEY");
        assertThat(result.getIgnoredType(), is(TableDataConsistencyCheckIgnoredType.NO_UNIQUE_KEY));
        assertFalse(result.isMatched());
    }
    
    @Test
    void assertSwapToObjectWithEmptyString() {
        assertNotNull(yamlTableDataConsistencyCheckResultSwapper.swapToObject(""));
    }
    
    @Test
    void assertSwapToObjectWithBlankString() {
        assertNotNull(yamlTableDataConsistencyCheckResultSwapper.swapToObject(" "));
    }
}
