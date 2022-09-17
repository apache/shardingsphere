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

package org.apache.shardingsphere.infra.yaml.config.swapper.mode;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlModeConfigurationSwapperTest {
    
    private static final String TEST_TYPE = "TEST_TYPE";
    
    private final YamlModeConfigurationSwapper swapper = new YamlModeConfigurationSwapper();
    
    @Test
    public void swapToYamlConfiguration() {
        YamlModeConfiguration actual = swapper.swapToYamlConfiguration(new ModeConfiguration("TEST_TYPE", null, true));
        assertThat(actual.getType(), is(TEST_TYPE));
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void swapToObject() {
        YamlModeConfiguration yamlConfig = new YamlModeConfiguration();
        yamlConfig.setType(TEST_TYPE);
        yamlConfig.setOverwrite(false);
        ModeConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getType(), is(TEST_TYPE));
        assertFalse(actual.isOverwrite());
    }
}
