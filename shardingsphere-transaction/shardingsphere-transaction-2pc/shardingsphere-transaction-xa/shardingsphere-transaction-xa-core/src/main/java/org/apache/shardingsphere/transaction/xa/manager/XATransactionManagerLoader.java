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
import org.apache.shardingsphere.transaction.xa.atomikos.manager.AtomikosTransactionManagerProvider;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * XA transaction manager loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XATransactionManagerLoader {
    
    private static final XATransactionManagerLoader INSTANCE = new XATransactionManagerLoader();
    
    /**
     * Get instance of XA transaction manager SPI loader.
     *
     * @return instance of XA transaction manager SPI loader
     */
    public static XATransactionManagerLoader getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get XA transaction manager.
     *
     * @param type type
     * @return XA transaction manager
     */
    public XATransactionManagerProvider getXATransactionManager(final String type) {
        Iterator<XATransactionManagerProvider> xaTransactionManagers = ServiceLoader.load(XATransactionManagerProvider.class).iterator();
        if (!xaTransactionManagers.hasNext()) {
            return new AtomikosTransactionManagerProvider();
        }
        while (xaTransactionManagers.hasNext()) {
            XATransactionManagerProvider result = xaTransactionManagers.next();
            if (result.getType().equalsIgnoreCase(type)) {
                return result;
            }
        }
        return new AtomikosTransactionManagerProvider();
    }
}
