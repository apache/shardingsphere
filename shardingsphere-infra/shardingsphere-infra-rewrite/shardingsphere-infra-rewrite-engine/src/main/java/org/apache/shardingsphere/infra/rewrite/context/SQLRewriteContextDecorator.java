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

package org.apache.shardingsphere.infra.rewrite.context;

import org.apache.shardingsphere.infra.spi.order.OrderedSPI;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.route.context.RouteContext;

/**
 * SQL rewrite context decorator.
 *
 * @param <T> type of rule
 */
public interface SQLRewriteContextDecorator<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Decorate SQL rewrite context.
     *
     * @param rule rule
     * @param props ShardingSphere properties
     * @param sqlRewriteContext SQL rewrite context to be decorated
     * @param routeContext route context
     */
    void decorate(T rule, ConfigurationProperties props, SQLRewriteContext sqlRewriteContext, RouteContext routeContext);
}
