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

package io.shardingsphere.transaction.xa.manager;

import io.shardingsphere.transaction.xa.spi.XATransactionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * XA transaction manager SPI loader.
 *
 * @author zhangliang
 * @author zhaojun
 */
@Getter
@Slf4j
public final class XATransactionManagerSPILoader {
    
    private static final XATransactionManagerSPILoader INSTANCE = new XATransactionManagerSPILoader();
    
    private final XATransactionManager transactionManager;
    
    private XATransactionManagerSPILoader() {
        transactionManager = load();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            
            @Override
            public void run() {
                transactionManager.destroy();
            }
        }));
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
    public static XATransactionManagerSPILoader getInstance() {
        return INSTANCE;
    }
}
