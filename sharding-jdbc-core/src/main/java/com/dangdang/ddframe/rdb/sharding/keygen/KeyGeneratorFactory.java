/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.keygen;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 主键生成器工厂.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyGeneratorFactory {
    
    /**
     * 创建主键生成器.
     * 
     * @param keyGeneratorClass 主键生成器类
     * @return 主键生成器实例
     */
    public static KeyGenerator createKeyGenerator(final Class<? extends KeyGenerator> keyGeneratorClass) {
        try {
            return keyGeneratorClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException(String.format("Class %s should have public privilege and no argument constructor", keyGeneratorClass.getName()));
        }
    }
}
