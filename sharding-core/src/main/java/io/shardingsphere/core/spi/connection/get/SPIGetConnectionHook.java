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

package io.shardingsphere.core.spi.connection.get;

import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;

import java.util.ServiceLoader;

/**
 * Connection hook loader.
 *
 * @author zhangliang
 */
public final class SPIGetConnectionHook implements GetConnectionHook {
    
    private static final ServiceLoader<GetConnectionHook> SERVICE_LOADER;
    
    static {
        SERVICE_LOADER = ServiceLoader.load(GetConnectionHook.class);
    }
    
    @Override
    public void start(final String dataSourceName) {
        for (GetConnectionHook each : SERVICE_LOADER) {
            each.start(dataSourceName);
        }
    }
    
    @Override
    public void finishSuccess(final DataSourceMetaData dataSourceMetaData, final int connectionCount) {
        for (GetConnectionHook each : SERVICE_LOADER) {
            each.finishSuccess(dataSourceMetaData, connectionCount);
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        for (GetConnectionHook each : SERVICE_LOADER) {
            each.finishFailure(cause);
        }
    }
}
