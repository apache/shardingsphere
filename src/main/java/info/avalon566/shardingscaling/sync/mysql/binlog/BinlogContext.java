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

package info.avalon566.shardingscaling.sync.mysql.binlog;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.ColumnDef;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.TableMapEvent;
import lombok.Data;
import lombok.var;
import java.util.HashMap;
import java.util.Map;

/**
 * Binlog context.
 *
 * @author avalon566
 */
@Data
public class BinlogContext {

    private String fileName;

    private int checksumLength;

    private Map<Long, TableMapEvent> tableMap = new HashMap<>();
    
    /**
     * Cache table map event.
     *
     * @param tableId table id
     * @param tableMapEvent table map event
     */
    public void putTableMapEvent(final long tableId, final TableMapEvent tableMapEvent) {
        tableMap.put(tableId, tableMapEvent);
    }
    
    /**
     * Get full table name by table id.
     *
     * @param tableId table id
     * @return full table name
     */
    public String getFullTableName(final long tableId) {
        var tableMapEvent = tableMap.get(tableId);
        return String.format("%s.%s", tableMapEvent.getSchemaName(), tableMapEvent.getTableName());
    }
    
    /**
     * Get column defined by table id.
     *
     * @param tableId table id
     * @return array of column defined
     */
    public ColumnDef[] getColumnDefs(final long tableId) {
        return tableMap.get(tableId).getColumnDefs();
    }
}
