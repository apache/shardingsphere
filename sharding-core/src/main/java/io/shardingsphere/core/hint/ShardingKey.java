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

package io.shardingsphere.core.hint;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Sharding key.
 *
 * @author zhangliang
 */
// TODO move to a suitable package
@EqualsAndHashCode
public final class ShardingKey {
    
    /**
     * Logic table name.
     */
    private final String logicTable;
    
    /**
     * Sharding column name.
     */
    @Getter
    private final String shardingColumn;
    
    public ShardingKey(final String logicTable, final String shardingColumn) {
        this.logicTable = logicTable.toLowerCase();
        this.shardingColumn = shardingColumn.toLowerCase();
    }
}
