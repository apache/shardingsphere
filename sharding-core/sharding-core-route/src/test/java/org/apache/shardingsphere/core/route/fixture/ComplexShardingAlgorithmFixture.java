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

package org.apache.shardingsphere.core.route.fixture;

import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.Collection;
import java.util.Collections;

/**
 * Complex sharding algorithm for @{@link org.apache.shardingsphere.core.route.type.standard.StandardRoutingEngineTest}.
 *
 * @author Rimal
 */
public final class ComplexShardingAlgorithmFixture implements ComplexKeysShardingAlgorithm<Long> {

    private Long userIdValue;

    private Long orderIdValue;

    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames,
                                         final ComplexKeysShardingValue<Long> shardingValue) {
        Collection<Long> userIdValueCollection = shardingValue.getColumnNameAndShardingValuesMap().get("user_id");
        if (null != userIdValueCollection && userIdValueCollection.size() > 0) {
            userIdValue = userIdValueCollection.iterator().next();
            if (userIdValue < 100) {
                return Collections.singletonList("ds_0");
            } else {
                return Collections.singletonList("ds_1");
            }
        }
        Collection<Long> orderIdValueCollection = shardingValue.getColumnNameAndShardingValuesMap().get("order_id");
        if (null != orderIdValueCollection && orderIdValueCollection.size() > 0) {
            orderIdValue = orderIdValueCollection.iterator().next();
            if (orderIdValue < 100) {
                return Collections.singletonList("t_order_0");
            } else {
                return Collections.singletonList("t_order_1");
            }
        }
        return null;
    }
}
