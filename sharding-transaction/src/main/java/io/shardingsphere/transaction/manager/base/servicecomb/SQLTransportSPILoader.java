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

package io.shardingsphere.transaction.manager.base.servicecomb;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.servicecomb.saga.transports.SQLTransport;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * SQLTransport SPI loader.
 *
 * @author yangyi
 */
@Slf4j
public final class SQLTransportSPILoader {
    
    private static final SQLTransportSPILoader INSTANCE = new SQLTransportSPILoader();
    
    @Getter
    private final SQLTransport sqlTransport;
    
    private SQLTransportSPILoader() {
        sqlTransport = load();
    }
    
    private SQLTransport load() {
        Iterator<SQLTransport> sqlTransports = ServiceLoader.load(SQLTransport.class).iterator();
        if (!sqlTransports.hasNext()) {
            log.warn("There are no SQLTransport configured. BASE Saga Transaction cannot be used.");
            return new EmptySQLTransport();
        }
        SQLTransport result = sqlTransports.next();
        if (sqlTransports.hasNext()) {
            log.warn("There are more than one SQLTransport implement existing, chosen first one by default.");
        }
        return result;
    }
    
    /**
     * Get instance of SQLTransport SPI loader.
     *
     * @return instance of SQLTransport SPI loader
     */
    public static SQLTransportSPILoader getInstance() {
        return INSTANCE;
    }
}
