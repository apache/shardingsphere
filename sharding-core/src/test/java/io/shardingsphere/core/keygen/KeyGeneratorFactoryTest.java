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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class KeyGeneratorFactoryTest {
    
    @Test
    public void assertCreateKeyGeneratorSuccess() {
        assertThat(KeyGeneratorFactory.newInstance(DefaultKeyGenerator.class.getName()), instanceOf(DefaultKeyGenerator.class));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertCreateKeyGeneratorFailureWithInstantiationError() {
        KeyGeneratorFactory.newInstance(InstantiationKeyGenerator.class.getName());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertCreateKeyGeneratorFailureWithIllegalAccess() {
        KeyGeneratorFactory.newInstance(IllegalAccessKeyGenerator.class.getName());
    }
    
    @RequiredArgsConstructor
    public static final class InstantiationKeyGenerator implements KeyGenerator {
        
        private final int field;
        
        @Override
        public Number generateKey() {
            return null;
        }
    }
    
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class IllegalAccessKeyGenerator implements KeyGenerator {
        
        @Override
        public Number generateKey() {
            return null;
        }
    }
}
