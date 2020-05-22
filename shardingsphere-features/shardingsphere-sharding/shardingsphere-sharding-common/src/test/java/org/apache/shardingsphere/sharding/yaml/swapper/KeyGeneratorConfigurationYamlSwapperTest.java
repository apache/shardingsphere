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

package org.apache.shardingsphere.sharding.yaml.swapper;

import org.apache.shardingsphere.sharding.api.config.KeyGeneratorConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlKeyGeneratorConfiguration;
import org.apache.shardingsphere.sharding.spi.keygen.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class KeyGeneratorConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        KeyGenerateAlgorithm keyGenerateAlgorithm = TypedSPIRegistry.getRegisteredService(KeyGenerateAlgorithm.class, "UUID", new Properties());
        YamlKeyGeneratorConfiguration actual = new KeyGeneratorConfigurationYamlSwapper().swap(new KeyGeneratorConfiguration("id", keyGenerateAlgorithm));
        assertThat(actual.getType(), is("UUID"));
        assertThat(actual.getColumn(), is("id"));
        assertThat(actual.getProps(), is(new Properties()));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlKeyGeneratorConfiguration yamlConfiguration = new YamlKeyGeneratorConfiguration();
        yamlConfiguration.setType("UUID");
        yamlConfiguration.setColumn("id");
        KeyGeneratorConfiguration actual = new KeyGeneratorConfigurationYamlSwapper().swap(yamlConfiguration);
        assertThat(actual.getKeyGenerateAlgorithm().getType(), is("UUID"));
        assertThat(actual.getColumn(), is("id"));
        assertThat(actual.getKeyGenerateAlgorithm().getProperties(), is(new Properties()));
    }
}
