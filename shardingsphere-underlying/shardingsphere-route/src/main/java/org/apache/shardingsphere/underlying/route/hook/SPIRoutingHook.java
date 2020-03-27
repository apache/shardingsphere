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

package org.apache.shardingsphere.underlying.route.hook;

import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.underlying.route.context.RouteContext;

import java.util.Collection;

/**
 * Routing hook for SPI.
 */
public final class SPIRoutingHook implements RoutingHook {
    
    private final Collection<RoutingHook> routingHooks = NewInstanceServiceLoader.newServiceInstances(RoutingHook.class);
    
    static {
        NewInstanceServiceLoader.register(RoutingHook.class);
    }
    
    @Override
    public void start(final String sql) {
        for (RoutingHook each : routingHooks) {
            each.start(sql);
        }
    }
    
    @Override
    public void finishSuccess(final RouteContext routeContext, final SchemaMetaData schemaMetaData) {
        for (RoutingHook each : routingHooks) {
            each.finishSuccess(routeContext, schemaMetaData);
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        for (RoutingHook each : routingHooks) {
            each.finishFailure(cause);
        }
    }
}
