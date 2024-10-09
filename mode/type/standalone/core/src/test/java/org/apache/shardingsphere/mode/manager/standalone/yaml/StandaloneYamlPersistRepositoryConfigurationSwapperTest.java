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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.YamlPersistRepositoryConfigurationSwapper;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings({"unchecked", "rawtypes"})
class StandaloneYamlPersistRepositoryConfigurationSwapperTest {
    
    private final YamlPersistRepositoryConfigurationSwapper swapper = TypedSPILoader.getService(YamlPersistRepositoryConfigurationSwapper.class, "Standalone");
    
    @Test
    void assertSwapToYamlConfiguration() {
        StandalonePersistRepositoryConfiguration standalonePersistRepositoryConfig = new StandalonePersistRepositoryConfiguration("TEST", new Properties());
        YamlPersistRepositoryConfiguration actual = (YamlPersistRepositoryConfiguration) swapper.swapToYamlConfiguration(standalonePersistRepositoryConfig);
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getProps(), is(new Properties()));
    }
    
    @Test
    void assertSwapToObject() {
        YamlPersistRepositoryConfiguration yamlPersistRepositoryConfig = new YamlPersistRepositoryConfiguration();
        yamlPersistRepositoryConfig.setType("TEST");
        StandalonePersistRepositoryConfiguration actual = (StandalonePersistRepositoryConfiguration) swapper.swapToObject(yamlPersistRepositoryConfig);
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getProps(), is(new Properties()));
    }
}
