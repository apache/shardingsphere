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

package org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Data header packet.
 *
 * @author zhangyonglun
 * @author panjuan
 */
@Getter
public final class DataHeaderPacket implements DatabasePacket {
    
    private final int sequenceId;
    
    private final String schema;
    
    private final String table;
    
    private final String orgTable;
    
    private final String name;
    
    private final String orgName;
    
    private final int columnLength;
    
    private final Integer columnType;
    
    private final int decimals;
    
    public DataHeaderPacket(final int sequenceId, final ResultSetMetaData resultSetMetaData, final LogicSchema logicSchema, final int columnIndex) throws SQLException {
        this.sequenceId = sequenceId;
        String logicTableName = logicSchema instanceof ShardingSchema ? ((ShardingSchema) logicSchema).getShardingRule().getLogicTableNames(resultSetMetaData.getTableName(columnIndex)).iterator().next() : resultSetMetaData.getTableName(columnIndex);
        this(sequenceId, logicSchema.getName(), resultSetMetaData.getTableName(columnIndex), logicTableName,
            resultSetMetaData.getColumnLabel(columnIndex), resultSetMetaData.getColumnName(columnIndex), resultSetMetaData.getColumnDisplaySize(columnIndex),
            resultSetMetaData.getColumnType(columnIndex), resultSetMetaData.getScale(columnIndex));
    }
    
    public DataHeaderPacket(final int sequenceId, final String schema, final String table, final String orgTable,
                            final String name, final String orgName, final int columnLength, final Integer columnType, final int decimals) {
        this.sequenceId = sequenceId;
        this.schema = schema;
        this.table = table;
        this.orgTable = orgTable;
        this.name = name;
        this.orgName = orgName;
        this.columnLength = columnLength;
        this.columnType = columnType;
        this.decimals = decimals;
    }
}
