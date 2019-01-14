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

package io.shardingsphere.core.keygen;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.keygen.generator.KeyGenerator;
import io.shardingsphere.spi.NewInstanceServiceLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Key generator factory.
 * 
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyGeneratorFactory {
    
    /**
     * Create key generator.
     * 
     * @param keyGeneratorType key generator type
     * @return key generator instance
     */
    public static KeyGenerator newInstance(final String keyGeneratorType) {
        Collection<KeyGenerator> keyGenerators = NewInstanceServiceLoader.load(KeyGenerator.class);
        if (!isValid(keyGeneratorType, keyGenerators)) {
            throw new ShardingConfigurationException("Invalid key generator type.");
        }
        return keyGenerators.iterator().next();
    }
    
    private static boolean isValid(final String keyGeneratorType, final Collection<KeyGenerator> keyGenerators) {
        return !Collections2.filter(keyGenerators, new Predicate<KeyGenerator>() {
            
            @Override
            public boolean apply(final KeyGenerator input) {
                return keyGeneratorType.equals(input.getType());
            }
        }).isEmpty();
    }
}
