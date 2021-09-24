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

    public static final String TEST_TYPE = "TEST_TYPE";

    private StandalonePersistRepositoryConfigurationYamlSwapper swapper = new StandalonePersistRepositoryConfigurationYamlSwapper();

    @Test
    public void assertSwapToYamlConfiguration() {
        Properties testProps = new Properties();
        StandalonePersistRepositoryConfiguration standalonePersistRepositoryConfiguration = new StandalonePersistRepositoryConfiguration(TEST_TYPE, testProps);
        YamlPersistRepositoryConfiguration actual = swapper.swapToYamlConfiguration(standalonePersistRepositoryConfiguration);
        assertThat(actual.getProps(), is(testProps));
        assertThat(actual.getType(), is(TEST_TYPE));
    }

    @Test
    public void assertSwapToObject() {
        YamlPersistRepositoryConfiguration yamlPersistRepositoryConfiguration = new YamlPersistRepositoryConfiguration();
        yamlPersistRepositoryConfiguration.setType(TEST_TYPE);
        Properties testProps = new Properties();
        yamlPersistRepositoryConfiguration.setProps(testProps);
        StandalonePersistRepositoryConfiguration actual = swapper.swapToObject(yamlPersistRepositoryConfiguration);
        assertThat(actual.getProps(), is(testProps));
        assertThat(actual.getType(), is(TEST_TYPE));
    }

    @Test
    public void assertGetType() {
        assertThat(swapper.getType(), is("Standalone"));
    }
}
