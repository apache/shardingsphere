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

package org.apache.shardingsphere.core.encrypt;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;

import java.util.Collection;
import java.util.Properties;

/**
 * Sharding encryptor factory.
 * 
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingEncryptorFactory {
    
    static {
        NewInstanceServiceLoader.register(ShardingEncryptor.class);
    }
    
    /**
     * Create sharding encryptor.
     * 
     * @param type encryptor type
     * @param props encryptor properties
     * @return encryptor instance
     */
    public static ShardingEncryptor newInstance(final String type, final Properties props) {
        Collection<ShardingEncryptor> shardingEncryptors = loadEncryptors(type);
        if (shardingEncryptors.isEmpty()) {
            throw new ShardingConfigurationException("Invalid encryptor type.");
        }
        ShardingEncryptor result = shardingEncryptors.iterator().next();
        result.setProperties(props);
        return result;
    }
    
    private static Collection<ShardingEncryptor> loadEncryptors(final String encryptorType) {
        return Collections2.filter(NewInstanceServiceLoader.newServiceInstances(ShardingEncryptor.class), new Predicate<ShardingEncryptor>() {
            
            @Override
            public boolean apply(final ShardingEncryptor input) {
                return encryptorType.equalsIgnoreCase(input.getType());
            }
        });
    }
}
