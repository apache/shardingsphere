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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.exception.syntax.DriverURLProviderNotFoundException;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

/**
 * ShardingSphere driver URL manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereDriverURLManager {
    
    /**
     * Get config content from URL.
     *
     * @param url driver URL
     * @param urlPrefix url prefix
     * @return configuration content
     * @throws DriverURLProviderNotFoundException driver URL provider not found exception
     */
    public static byte[] getContent(final String url, final String urlPrefix) {
        for (ShardingSphereDriverURLProvider each : ShardingSphereServiceLoader.getServiceInstances(ShardingSphereDriverURLProvider.class)) {
            if (each.accept(url)) {
                return each.getContent(url, urlPrefix);
            }
        }
        throw new DriverURLProviderNotFoundException(url);
    }
}
