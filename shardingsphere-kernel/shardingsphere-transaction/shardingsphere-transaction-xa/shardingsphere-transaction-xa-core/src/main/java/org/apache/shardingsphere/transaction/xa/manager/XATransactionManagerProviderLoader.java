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

package org.apache.shardingsphere.transaction.xa.manager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider;

import java.util.Properties;

/**
 * XA transaction manager provider loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XATransactionManagerProviderLoader {
    
    private static final XATransactionManagerProviderLoader INSTANCE = new XATransactionManagerProviderLoader();
    
    static {
        ShardingSphereServiceLoader.register(XATransactionManagerProvider.class);
    }
    
    /**
     * Get instance of XA transaction manager provider SPI loader.
     *
     * @return instance of XA transaction manager provider SPI loader
     */
    public static XATransactionManagerProviderLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get XA transaction manager provider.
     *
     * @param type XA transaction manager provider type
     * @return XA transaction manager provider
     */
    public XATransactionManagerProvider getXATransactionManagerProvider(final String type) {
        return null == type
                ? RequiredSPIRegistry.getRegisteredService(XATransactionManagerProvider.class) : TypedSPIRegistry.getRegisteredService(XATransactionManagerProvider.class, type, new Properties());
    }
}
