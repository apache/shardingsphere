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

package io.shardingsphere.core.yaml.sharding;

import com.google.common.base.Strings;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.keygen.KeyGeneratorType;
import io.shardingsphere.core.keygen.KeyGenerator;
import io.shardingsphere.core.keygen.KeyGeneratorFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Properties;

/**
 * Yaml key generator configuration.
 *
 * @author panjuan
 */
@Getter
@Setter
public class YamlKeyGeneratorConfiguration {
    
    private String column;
    
    private String type;
    
    private String className;
    
    private Properties props = new Properties();
    
    /**
     * Build table rule configuration.
     *
     * @return table rule configuration
     */
    public KeyGenerator getKeyGenerator() {
        KeyGenerator result;
        if (!Strings.isNullOrEmpty(className)) {
            result = KeyGeneratorFactory.newInstance(className);
        } else if (!Strings.isNullOrEmpty(type)) {
            result = KeyGeneratorFactory.newInstance(getBuiltinKeyGeneratorClassName());
        } else {
            result = KeyGeneratorFactory.newInstance(KeyGeneratorType.SNOWFLAKE.getKeyGeneratorClassName());
        }
        result.setProperties(props);
        return result;
    }
    
    private String getBuiltinKeyGeneratorClassName() {
        if (type.equalsIgnoreCase(KeyGeneratorType.SNOWFLAKE.name())) {
            return KeyGeneratorType.SNOWFLAKE.getKeyGeneratorClassName();
        }
        if (type.equalsIgnoreCase(KeyGeneratorType.UUID.name())) {
            return KeyGeneratorType.UUID.getKeyGeneratorClassName();
        }
        if (type.equalsIgnoreCase(KeyGeneratorType.LEAF.name())) {
            return KeyGeneratorType.LEAF.getKeyGeneratorClassName();
        }
        throw new ShardingConfigurationException("Invalid built-in key generator type.");
    }
}
