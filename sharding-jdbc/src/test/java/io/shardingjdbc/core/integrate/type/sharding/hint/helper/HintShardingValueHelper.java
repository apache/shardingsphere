/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.integrate.type.sharding.hint.helper;

import io.shardingjdbc.core.api.HintManager;
import io.shardingjdbc.core.constant.ShardingOperator;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

public class HintShardingValueHelper implements AutoCloseable {
    
    @Getter(AccessLevel.PROTECTED)
    private final HintManager hintManager;
    
    public HintShardingValueHelper(final int userId, final int orderId) {
        hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_order", "user_id", userId);
        hintManager.addTableShardingValue("t_order", "order_id", orderId);
    }
    
    public HintShardingValueHelper(final List<Integer> userId, final ShardingOperator userIdOperator, final List<Integer> orderId, final ShardingOperator orderIdOperator) {
        hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_order", "user_id", userIdOperator, userId.toArray(new Comparable[userId.size()]));
        hintManager.addTableShardingValue("t_order", "order_id", orderIdOperator, orderId.toArray(new Comparable[orderId.size()]));
    }
    
    @Override
    public void close() {
        hintManager.close();
    }
}
