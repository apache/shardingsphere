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

package org.apache.shardingsphere.infra.route;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;
import org.apache.shardingsphere.infra.sql.LogicSQL;

/**
 * SQL Router.
 * 
 * @param <T> type of rule
 */
public interface SQLRouter<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Create route context.
     *
     * @param logicSQL logic SQL
     * @param schema ShardingSphere schema
     * @param rule rule
     * @param props configuration properties
     * @return route context
     */
    RouteContext createRouteContext(LogicSQL logicSQL, ShardingSphereSchema schema, T rule, ConfigurationProperties props);
    
    /**
     * Decorate route context.
     * 
     * @param routeContext route context
     * @param logicSQL logic SQL
     * @param schema ShardingSphere schema
     * @param rule rule
     * @param props configuration properties
     */
    void decorateRouteContext(RouteContext routeContext, LogicSQL logicSQL, ShardingSphereSchema schema, T rule, ConfigurationProperties props);
}
