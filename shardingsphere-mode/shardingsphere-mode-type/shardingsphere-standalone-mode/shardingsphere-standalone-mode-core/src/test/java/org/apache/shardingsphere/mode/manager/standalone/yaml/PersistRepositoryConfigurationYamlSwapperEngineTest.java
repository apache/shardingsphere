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

package org.apache.shardingsphere.mode.manager.standalone.yaml;

import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.PersistRepositoryConfigurationYamlSwapperEngine;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PersistRepositoryConfigurationYamlSwapperEngineTest {

    @Test
    public void assertSwapToYamlConfiguration() {
        PersistRepositoryConfigurationYamlSwapperEngine persistRepositoryConfigurationYamlSwapperEngine = new PersistRepositoryConfigurationYamlSwapperEngine();
        String type = "Standalone";
        StandalonePersistRepositoryConfiguration standalonePersistRepositoryConfiguration = new StandalonePersistRepositoryConfiguration("Standalone", new Properties());
        YamlPersistRepositoryConfiguration yamlPersistRepositoryConfiguration = persistRepositoryConfigurationYamlSwapperEngine.swapToYamlConfiguration(type, standalonePersistRepositoryConfiguration);
        assertTrue(yamlPersistRepositoryConfiguration.getType().equalsIgnoreCase("Standalone"));
    }

    @Test
    public void assertSwapToObject() {
        PersistRepositoryConfigurationYamlSwapperEngine persistRepositoryConfigurationYamlSwapperEngine = new PersistRepositoryConfigurationYamlSwapperEngine();
        String type = "Standalone";
        YamlPersistRepositoryConfiguration yamlPersistRepositoryConfiguration = new YamlPersistRepositoryConfiguration();
        yamlPersistRepositoryConfiguration.setType("Standalone");
        PersistRepositoryConfiguration persistRepositoryConfiguration = persistRepositoryConfigurationYamlSwapperEngine.swapToObject(type, yamlPersistRepositoryConfiguration);
        assertThat(persistRepositoryConfiguration.getType(), is("Standalone"));
    }
}
