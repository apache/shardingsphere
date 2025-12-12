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
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class YamlModeConfigurationSwapperTest {
    
    private static final String TEST_TYPE = "TEST_TYPE";
    
    private final YamlModeConfigurationSwapper swapper = new YamlModeConfigurationSwapper();
    
    @Test
    void assertSwapToYamlConfigurationWithNullRepository() {
        YamlModeConfiguration actual = swapper.swapToYamlConfiguration(new ModeConfiguration("TEST_TYPE", null));
        assertThat(actual.getType(), is(TEST_TYPE));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithNotNullRepository() {
        YamlPersistRepositoryConfiguration yamlRepoConfig = new YamlPersistRepositoryConfiguration();
        yamlRepoConfig.setType(TEST_TYPE);
        try (MockedStatic<TypedSPILoader> mockedLoader = mockStatic(TypedSPILoader.class)) {
            YamlPersistRepositoryConfigurationSwapper<?> mockSwapper = mock(YamlPersistRepositoryConfigurationSwapper.class);
            when(mockSwapper.swapToYamlConfiguration(any())).thenReturn(yamlRepoConfig);
            when(mockSwapper.getType()).thenReturn(TEST_TYPE);
            mockedLoader.when(() -> TypedSPILoader.getService(YamlPersistRepositoryConfigurationSwapper.class, TEST_TYPE)).thenReturn(mockSwapper);
            ModeConfiguration modeConfig = new ModeConfiguration(TEST_TYPE, mock(PersistRepositoryConfiguration.class));
            YamlModeConfiguration actual = swapper.swapToYamlConfiguration(modeConfig);
            assertThat(actual.getType(), is(TEST_TYPE));
            assertThat(actual.getRepository(), is(yamlRepoConfig));
        }
    }
    
    @Test
    void assertSwapToObjectWithNullRepository() {
        YamlModeConfiguration yamlConfig = new YamlModeConfiguration();
        yamlConfig.setType(TEST_TYPE);
        ModeConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getType(), is(TEST_TYPE));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertSwapToObjectWithNotNullRepository() {
        PersistRepositoryConfiguration mockRepoConfig = mock(PersistRepositoryConfiguration.class);
        try (MockedStatic<TypedSPILoader> mockedLoader = mockStatic(TypedSPILoader.class)) {
            YamlPersistRepositoryConfigurationSwapper<PersistRepositoryConfiguration> mockSwapper = mock(YamlPersistRepositoryConfigurationSwapper.class);
            when(mockSwapper.swapToObject(any())).thenReturn(mockRepoConfig);
            when(mockSwapper.getType()).thenReturn(TEST_TYPE);
            mockedLoader.when(() -> TypedSPILoader.getService(YamlPersistRepositoryConfigurationSwapper.class, TEST_TYPE)).thenReturn(mockSwapper);
            YamlPersistRepositoryConfiguration yamlRepoConfig = new YamlPersistRepositoryConfiguration();
            yamlRepoConfig.setType(TEST_TYPE);
            YamlModeConfiguration yamlConfig = new YamlModeConfiguration();
            yamlConfig.setType(TEST_TYPE);
            yamlConfig.setRepository(yamlRepoConfig);
            ModeConfiguration actual = swapper.swapToObject(yamlConfig);
            assertThat(actual.getType(), is(TEST_TYPE));
            assertThat(actual.getRepository(), is(mockRepoConfig));
        }
    }
}
