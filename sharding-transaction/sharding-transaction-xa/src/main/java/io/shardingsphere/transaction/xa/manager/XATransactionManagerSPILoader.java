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

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.spi.NewInstanceServiceLoader;
import io.shardingsphere.transaction.manager.xa.XATransactionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

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
    
    private final Collection<XATransactionManager> xaTransactionManagers = NewInstanceServiceLoader.load(XATransactionManager.class);
    
    private final XATransactionManager transactionManager;
    
    private XATransactionManagerSPILoader() {
        transactionManager = load();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (null != transactionManager) {
                    transactionManager.destroy();
                }
            }
        }));
    }
    
    private XATransactionManager load() {
        try {
            if (xaTransactionManagers.size() > 1) {
                log.warn("There are more than one transaction mangers existing, chosen first one by default.");
            }
            if (xaTransactionManagers.isEmpty()) {
                return new AtomikosTransactionManager();
            }
            return xaTransactionManagers.iterator().next();
            // CHECKSTYLE:OFF
        } catch (Exception ex) {
            // CHECKSTYLE:ON
            throw new ShardingException("Can not initialize the xaTransaction manager failed with " + ex);
        }
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
