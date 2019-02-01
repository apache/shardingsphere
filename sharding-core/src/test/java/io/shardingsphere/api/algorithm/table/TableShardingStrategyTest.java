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

package io.shardingsphere.api.algorithm.table;

import com.google.common.collect.Sets;
import io.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.core.fixture.PreciseOrderShardingAlgorithm;
import io.shardingsphere.core.routing.strategy.standard.StandardShardingStrategy;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TableShardingStrategyTest {
    
    @Test
    public void assertTableShardingStrategyWithSingleShardingColumn() {
        assertThat(new StandardShardingStrategy(new StandardShardingStrategyConfiguration("shardingColumn", new PreciseOrderShardingAlgorithm(), null)).getShardingColumns(), 
                is((Collection<String>) Sets.newTreeSet(Collections.singleton("shardingColumn"))));
    }
}
