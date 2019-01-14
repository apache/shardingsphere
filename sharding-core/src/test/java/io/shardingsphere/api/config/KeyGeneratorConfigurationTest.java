/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.api.config;

import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.keygen.generator.SnowflakeKeyGenerator;
import io.shardingsphere.core.keygen.generator.UUIDKeyGenerator;
import io.shardingsphere.core.yaml.sharding.YamlKeyGeneratorConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class KeyGeneratorConfigurationTest {
    
    @Test
    public void assertGetKeyGeneratorWithClassName() {
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration();
        keyGeneratorConfiguration.setClassName("io.shardingsphere.core.keygen.generator.SnowflakeKeyGenerator");
        assertThat(keyGeneratorConfiguration.getKeyGenerator().getClass().getName(), is(SnowflakeKeyGenerator.class.getName()));
    }
    
    @Test
    public void assertGetKeyGeneratorWithSnowflakeType() {
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("SNOWFLAKE");
        assertThat(keyGeneratorConfiguration.getKeyGenerator().getClass().getName(), is(SnowflakeKeyGenerator.class.getName()));
    }
    
    @Test
    public void assertGetKeyGeneratorWithoutTypeAndClassName() {
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration();
        assertThat(keyGeneratorConfiguration.getKeyGenerator().getClass().getName(), is(SnowflakeKeyGenerator.class.getName()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetKeyGeneratorClassNameWithLeaf() {
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("LEAF");
        keyGeneratorConfiguration.getKeyGenerator();
    }
    
    @Test
    public void assertGetKeyGeneratorClassNameWithUUID() {
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("UUID");
        assertThat(keyGeneratorConfiguration.getKeyGenerator().getClass().getName(), is(UUIDKeyGenerator.class.getName()));
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGetKeyGeneratorClassNameWithException() {
        KeyGeneratorConfiguration keyGeneratorConfiguration = new KeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("DEFAULT");
        keyGeneratorConfiguration.getKeyGenerator();
    }
    
    @Test
    public void assertGetKeyGeneratorVariables() {
        YamlKeyGeneratorConfiguration keyGeneratorConfiguration = new YamlKeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("SNOWFLAKE");
        keyGeneratorConfiguration.setClassName("io.shardingsphere.core.keygen.generator.SnowflakeKeyGenerator");
        keyGeneratorConfiguration.setColumn("order_id");
        assertThat(keyGeneratorConfiguration.getType(), is("SNOWFLAKE"));
        assertThat(keyGeneratorConfiguration.getClassName(), is("io.shardingsphere.core.keygen.generator.SnowflakeKeyGenerator"));
        assertThat(keyGeneratorConfiguration.getColumn(), is("order_id"));
    }
}
