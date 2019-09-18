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

package org.apache.shardingsphere.core.route.router.sharding.validator.routingresult;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.route.router.sharding.validator.routingresult.impl.ComplexRoutingResultValidator;
import org.apache.shardingsphere.core.route.router.sharding.validator.routingresult.impl.StandardRoutingResultValidator;
import org.apache.shardingsphere.core.route.type.RoutingEngine;
import org.apache.shardingsphere.core.route.type.standard.StandardRoutingEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Routing result validator factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutingResultValidatorFactory {
    
    /**
     * New instance of sharding statement validator.
     * 
     * @param routingEngine routing engine
     * @param shardingRule sharding rule
     * @param metaData meta data of ShardingSphere
     * @return routing result validator
     */
    public static RoutingResultValidator newInstance(final RoutingEngine routingEngine, final ShardingRule shardingRule, final ShardingSphereMetaData metaData) {
        return routingEngine instanceof StandardRoutingEngine ? new StandardRoutingResultValidator(shardingRule, metaData) : new ComplexRoutingResultValidator(shardingRule, metaData);
    }
}
