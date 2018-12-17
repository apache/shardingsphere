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

package io.shardingsphere.core.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadFactory;

/**
 * Sharding thread factory builder.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingThreadFactoryBuilder {
    
    private static final String NAME_FORMAT_PREFIX = "ShardingSphere-";
    
    private static final String DEFAULT_EXECUTOR_NAME_FORMAT = NAME_FORMAT_PREFIX + "%d";
    
    /**
     * Build default sharding thread factory.
     *
     * @return default sharding thread factory
     */
    public static ThreadFactory build() {
        return new ThreadFactoryBuilder().setDaemon(true).setNameFormat(DEFAULT_EXECUTOR_NAME_FORMAT).build();
    }
    
    /**
     * Build sharding thread factory.
     * 
     * @param nameFormat thread name format
     * @return sharding thread factory
     */
    public static ThreadFactory build(final String nameFormat) {
        return new ThreadFactoryBuilder().setDaemon(true).setNameFormat(NAME_FORMAT_PREFIX + nameFormat).build();
    }
}
