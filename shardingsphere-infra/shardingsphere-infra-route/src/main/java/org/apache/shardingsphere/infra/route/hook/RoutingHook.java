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

package org.apache.shardingsphere.infra.route.hook;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;

/**
 * Routing hook.
 */
public interface RoutingHook {
    
    /**
     * Handle when routing started.
     *
     * @param sql SQL to be routing
     */
    void start(String sql);
    
    /**
     * Handle when routing finished success.
     *
     * @param routeContext route context
     * @param schemaMetaData schema meta data
     */
    void finishSuccess(RouteContext routeContext, PhysicalSchemaMetaData schemaMetaData);
    
    /**
     * Handle when routing finished failure.
     * 
     * @param cause failure cause
     */
    void finishFailure(Exception cause);
}
