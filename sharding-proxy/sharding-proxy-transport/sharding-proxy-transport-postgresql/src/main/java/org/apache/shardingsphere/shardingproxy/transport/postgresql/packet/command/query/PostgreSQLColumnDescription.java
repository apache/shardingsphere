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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.constant.PostgreSQLColumnType;

/**
 * PostgreSQL column description.
 *
 * @author zhangyonglun
 */
@Getter
@RequiredArgsConstructor
public final class PostgreSQLColumnDescription {
    
    private final String columnName;
    
    private final int tableOID = 0;
    
    private final int columnIndex;
    
    private final int typeOID;
    
    private final int columnLength;
    
    private final int typeModifier = -1;
    
    private final int dataFormat = 0;
    
    public PostgreSQLColumnDescription(final DataHeaderPacket dataHeaderPacket, final int columnIndex) {
        this.columnName = dataHeaderPacket.getName();
        this.columnIndex = columnIndex;
        this.typeOID = PostgreSQLColumnType.valueOfJDBCType(dataHeaderPacket.getColumnType()).getValue();
        this.columnLength = dataHeaderPacket.getColumnLength();
    }
}
