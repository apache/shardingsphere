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

package org.apache.shardingsphere.mode.manager.standalone;

import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.manager.standalone.yaml.StandaloneYamlPersistRepositoryConfigurationSwapper;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandaloneYamlPersistRepositoryConfigurationSwapperTest {
    
    public static final String TEST_TYPE = "TEST_TYPE";
    
    @Test
    public void assertSwapToYamlConfiguration() {
        StandalonePersistRepositoryConfiguration standalonePersistRepositoryConfig = new StandalonePersistRepositoryConfiguration(TEST_TYPE, new Properties());
        YamlPersistRepositoryConfiguration actual = new StandaloneYamlPersistRepositoryConfigurationSwapper().swapToYamlConfiguration(standalonePersistRepositoryConfig);
        assertThat(actual.getProps(), is(new Properties()));
        assertThat(actual.getType(), is(TEST_TYPE));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlPersistRepositoryConfiguration yamlPersistRepositoryConfig = new YamlPersistRepositoryConfiguration();
        yamlPersistRepositoryConfig.setType(TEST_TYPE);
        StandalonePersistRepositoryConfiguration actual = new StandaloneYamlPersistRepositoryConfigurationSwapper().swapToObject(yamlPersistRepositoryConfig);
        assertThat(actual.getProps(), is(new Properties()));
        assertThat(actual.getType(), is(TEST_TYPE));
    }
}
