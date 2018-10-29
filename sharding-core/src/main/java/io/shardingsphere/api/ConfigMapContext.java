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

package io.shardingsphere.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The config map context.
 *
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ConfigMapContext {
    
    private static final ConfigMapContext INSTANCE = new ConfigMapContext();
    
    private Map<String, Object> configMap = new ConcurrentHashMap<>();
    
    /**
     * Get a new instance for {@code ConfigMapContext}.
     *
     * @return  {@code ConfigMapContext} instance
     */
    public static ConfigMapContext getInstance() {
        return INSTANCE;
    }
}
