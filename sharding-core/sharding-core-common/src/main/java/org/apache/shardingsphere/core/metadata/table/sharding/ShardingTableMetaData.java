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

package org.apache.shardingsphere.core.metadata.table.sharding;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.metadata.table.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Table meta data for sharding.
 *
 * @author zhangliang
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ShardingTableMetaData extends TableMetaData {
    
    private final Collection<String> logicIndexes;
    
    public ShardingTableMetaData(final Collection<ColumnMetaData> columnMetaDataList, final Collection<String> logicIndexes) {
        super(columnMetaDataList);
        this.logicIndexes = new CopyOnWriteArraySet<>(logicIndexes);
    }
    
    /**
     * Judge contains index or not.
     * 
     * @param logicIndexName logic index name
     * @return contains index or not
     */
    public boolean containsIndex(final String logicIndexName) {
        return logicIndexes.contains(logicIndexName);
    }
}
