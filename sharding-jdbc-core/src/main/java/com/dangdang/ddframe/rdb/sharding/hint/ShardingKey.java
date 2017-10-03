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

package com.dangdang.ddframe.rdb.sharding.hint;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Sharding key.
 *
 * @author zhangliang
 */
// TODO move to a suitable package
@RequiredArgsConstructor
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
    
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ShardingKey)) {
            return false;
        } else {
            ShardingKey other = (ShardingKey) o;
            String this$logicTable = this.logicTable;
            String other$logicTable = other.logicTable;
            if (this$logicTable == null) {
                if (other$logicTable != null) {
                    return false;
                }
            } else if (!this$logicTable.equalsIgnoreCase(other$logicTable)) {
                return false;
            }

            String this$shardingColumn = this.getShardingColumn();
            String other$shardingColumn = other.getShardingColumn();
            if (this$shardingColumn == null) {
                if (other$shardingColumn != null) {
                    return false;
                }
            } else if (!this$shardingColumn.equalsIgnoreCase(other$shardingColumn)) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        int result = 1;
        String $logicTable = this.logicTable;
        result = result * 59 + ($logicTable == null ? 0 : $logicTable.toLowerCase().hashCode());
        String $shardingColumn = this.getShardingColumn();
        result = result * 59 + ($shardingColumn == null ? 0 : $shardingColumn.toLowerCase().hashCode());
        return result;
    }
}
