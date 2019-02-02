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

package org.apache.shardingsphere.api.config.sharding;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.keygen.ShardingKeyGeneratorFactory;
import org.apache.shardingsphere.core.keygen.generator.ShardingKeyGenerator;

import java.util.Properties;

/**
 * Key generator configuration.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class KeyGeneratorConfiguration {
    
    private final String column;
    
    private final String type;
    
    private final Properties props;
    
    /**
     * Build key generator configuration.
     *
     * @return table rule configuration
     */
    public Optional<ShardingKeyGenerator> getKeyGenerator() {
        if (Strings.isNullOrEmpty(type)) {
            return Optional.absent();
        }
        ShardingKeyGenerator result = ShardingKeyGeneratorFactory.newInstance(type);
        result.setProperties(props);
        return Optional.of(result);
    }
}
