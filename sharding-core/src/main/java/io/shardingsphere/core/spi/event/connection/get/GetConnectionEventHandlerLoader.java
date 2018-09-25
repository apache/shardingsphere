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

package io.shardingsphere.core.spi.event.connection.get;

import io.shardingsphere.core.spi.event.ShardingEventHandlerLoader;

import java.util.ServiceLoader;

/**
 * Connection event handler loader.
 *
 * @author zhangliang
 */
public final class GetConnectionEventHandlerLoader implements ShardingEventHandlerLoader<GetConnectionStartEvent, GetConnectionFinishEvent> {
    
    private static final GetConnectionEventHandlerLoader INSTANCE = new GetConnectionEventHandlerLoader();
    
    private final ServiceLoader<GetConnectionEventHandler> serviceLoader;
    
    private GetConnectionEventHandlerLoader() {
        serviceLoader = ServiceLoader.load(GetConnectionEventHandler.class);
    }
    
    /**
     * Get instance.
     * 
     * @return instance
     */
    public static GetConnectionEventHandlerLoader getInstance() {
        return INSTANCE;
    }
    
    @Override
    public void start(final GetConnectionStartEvent event) {
        for (GetConnectionEventHandler each : serviceLoader) {
            each.start(event);
        }
    }
    
    @Override
    public void finish(final GetConnectionFinishEvent event) {
        for (GetConnectionEventHandler each : serviceLoader) {
            each.finish(event);
        }
    }
}
