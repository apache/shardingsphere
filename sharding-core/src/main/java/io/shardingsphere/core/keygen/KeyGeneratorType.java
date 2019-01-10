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

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Key generator type.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public enum KeyGeneratorType {
    
    SNOWFLAKE("io.shardingsphere.core.keygen.DefaultKeyGenerator"),
    UUID(""),
    LEAF("");
    
    private final String keyGeneratorClassName;
    
    /**
     * Get key generator type.
     * 
     * @param keyGeneratorClassName key generator class name
     * @return key generator type
     */
    public static Optional<KeyGeneratorType> getKeyGeneratorType(final String keyGeneratorClassName) {
        for (KeyGeneratorType each : KeyGeneratorType.values()) {
            if (each.getKeyGeneratorClassName().equals(keyGeneratorClassName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
