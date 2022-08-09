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

package org.apache.shardingsphere.mode.repository.standalone.jdbc.provider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * JDBC repository provider factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCRepositoryProviderFactory {
    
    static {
        ShardingSphereServiceLoader.register(JDBCRepositoryProvider.class);
    }
    
    /**
     * Get instance of JDBC repository provider.
     *
     * @param type JDBC repository type
     * @return got instance
     */
    public static JDBCRepositoryProvider getInstance(final Object type) {
        return null == type ? RequiredSPIRegistry.getRegisteredService(JDBCRepositoryProvider.class) : TypedSPIRegistry.getRegisteredService(JDBCRepositoryProvider.class, type.toString());
    }
}
