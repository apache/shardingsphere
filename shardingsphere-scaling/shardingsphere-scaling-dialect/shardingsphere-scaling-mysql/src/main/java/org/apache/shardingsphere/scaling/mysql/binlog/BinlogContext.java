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

package org.apache.shardingsphere.scaling.mysql.binlog;

import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Binlog context.
 */
@Setter
@Getter
public final class BinlogContext {
    
    private String fileName;
    
    private int checksumLength;
    
    private Map<Long, MySQLBinlogTableMapEventPacket> tableMap = new HashMap<>();
    
    /**
     * Cache table map event.
     *
     * @param tableId table id
     * @param tableMapEventPacket table map event
     */
    public void putTableMapEvent(final long tableId, final MySQLBinlogTableMapEventPacket tableMapEventPacket) {
        tableMap.put(tableId, tableMapEventPacket);
    }
    
    /**
     * Get table map event by table id.
     *
     * @param tableId table id
     * @return table map event
     */
    public MySQLBinlogTableMapEventPacket getTableMapEvent(final long tableId) {
        return tableMap.get(tableId);
    }
    
    /**
     * Get table name by table id.
     *
     * @param tableId table id
     * @return table name
     */
    public String getTableName(final long tableId) {
        return tableMap.get(tableId).getTableName();
    }
    
    /**
     * Get schema name by table id.
     *
     * @param tableId table id
     * @return schema name
     */
    public String getSchemaName(final long tableId) {
        return tableMap.get(tableId).getSchemaName();
    }
    
    /**
     * Get column defined by table id.
     *
     * @param tableId table id
     * @return array of column defined
     */
    public List<MySQLBinlogColumnDef> getColumnDefs(final long tableId) {
        return tableMap.get(tableId).getColumnDefs();
    }
}
