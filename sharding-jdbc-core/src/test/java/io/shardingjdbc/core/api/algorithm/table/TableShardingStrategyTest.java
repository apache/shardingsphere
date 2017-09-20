/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.api.algorithm.table;

import io.shardingjdbc.core.routing.strategy.standard.StandardShardingStrategy;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TableShardingStrategyTest {
    
    @Test
    public void assertTableShardingStrategyWithSingleShardingColumn() {
        assertThat(new StandardShardingStrategy("shardingColumn", null).getShardingColumns(), is((Collection<String>) Sets.newTreeSet(Collections.singleton("shardingColumn"))));
    }
}
