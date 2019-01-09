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
import io.shardingsphere.core.keygen.BuiltinKeyGeneratorType;
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
@NoArgsConstructor
@Getter
@Setter
public class YamlKeyGeneratorConfiguration {
    
    private String keyGeneratorColumnName;
    
    private String keyGeneratorType;
    
    private String keyGeneratorClassName;
    
    private Properties props = new Properties();
    
    /**
     * Build table rule configuration.
     *
     * @return table rule configuration
     */
    public KeyGenerator getKeyGenerator() {
        KeyGenerator result;
        if (!Strings.isNullOrEmpty(keyGeneratorClassName)) {
            result = KeyGeneratorFactory.newInstance(keyGeneratorClassName);
        } else if (!Strings.isNullOrEmpty(keyGeneratorType)) {
            result = KeyGeneratorFactory.newInstance(getKeyGeneratorClassName());
        } else {
            result = KeyGeneratorFactory.newInstance(BuiltinKeyGeneratorType.SNOWFLAKE.getKeyGeneratorClassName());
        }
        result.setProperties(props);
        return result;
    }
}
