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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.transaction.xa.atomikos.manager.AtomikosTransactionManager;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * XA transaction manager loader.
 */
@Getter
@Slf4j
public final class XATransactionManagerLoader {
    
    private static final XATransactionManagerLoader INSTANCE = new XATransactionManagerLoader();
    
    private final XATransactionManager transactionManager;
    
    private XATransactionManagerLoader() {
        transactionManager = load();
    }
    
    private XATransactionManager load() {
        Iterator<XATransactionManager> xaTransactionManagers = ServiceLoader.load(XATransactionManager.class).iterator();
        if (!xaTransactionManagers.hasNext()) {
            return new AtomikosTransactionManager();
        }
        XATransactionManager result = xaTransactionManagers.next();
        if (xaTransactionManagers.hasNext()) {
            log.warn("There are more than one transaction mangers existing, chosen first one by default.");
        }
        return result;
    }
    
    /**
     * Get instance of XA transaction manager SPI loader.
     * 
     * @return instance of XA transaction manager SPI loader
     */
    public static XATransactionManagerLoader getInstance() {
        return INSTANCE;
    }
}
