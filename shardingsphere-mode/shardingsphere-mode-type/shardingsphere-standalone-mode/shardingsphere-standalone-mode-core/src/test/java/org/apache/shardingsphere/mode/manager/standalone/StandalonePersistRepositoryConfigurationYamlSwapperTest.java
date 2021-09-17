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
import org.apache.shardingsphere.mode.manager.standalone.yaml.StandalonePersistRepositoryConfigurationYamlSwapper;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandalonePersistRepositoryConfigurationYamlSwapperTest {

    @Test
    public void assertSwapToYamlConfiguration() {
        final String testType = "testType";
        Properties testProps = new Properties();
        StandalonePersistRepositoryConfiguration standalonePersistRepositoryConfiguration = new StandalonePersistRepositoryConfiguration(testType, testProps);
        StandalonePersistRepositoryConfigurationYamlSwapper standalonePersistRepositoryConfigurationYamlSwapper = new StandalonePersistRepositoryConfigurationYamlSwapper();
        YamlPersistRepositoryConfiguration result = standalonePersistRepositoryConfigurationYamlSwapper.swapToYamlConfiguration(standalonePersistRepositoryConfiguration);
        assertThat(result.getProps(), is(testProps));
        assertThat(result.getType(), is(testType));
    }

    @Test
    public void assertSwapToObject() {
        YamlPersistRepositoryConfiguration yamlPersistRepositoryConfiguration = new YamlPersistRepositoryConfiguration();
        final String testType = "testType";
        yamlPersistRepositoryConfiguration.setType(testType);
        Properties testProps = new Properties();
        yamlPersistRepositoryConfiguration.setProps(testProps);
        StandalonePersistRepositoryConfigurationYamlSwapper standalonePersistRepositoryConfigurationYamlSwapper = new StandalonePersistRepositoryConfigurationYamlSwapper();
        StandalonePersistRepositoryConfiguration result = standalonePersistRepositoryConfigurationYamlSwapper.swapToObject(yamlPersistRepositoryConfiguration);
        assertThat(result.getProps(), is(testProps));
        assertThat(result.getType(), is(testType));
    }

    @Test
    public void assertGetType() {
        StandalonePersistRepositoryConfigurationYamlSwapper standalonePersistRepositoryConfigurationYamlSwapper = new StandalonePersistRepositoryConfigurationYamlSwapper();
        assertThat(standalonePersistRepositoryConfigurationYamlSwapper.getType(), is("Standalone"));
    }
}
