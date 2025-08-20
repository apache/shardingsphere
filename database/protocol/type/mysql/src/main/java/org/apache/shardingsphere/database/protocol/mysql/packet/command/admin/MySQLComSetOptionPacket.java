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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.admin;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

/**
 * COM_SET_OPTION command packet for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_set_option.html">COM_SET_OPTION</a>
 */
@Getter
public final class MySQLComSetOptionPacket extends MySQLCommandPacket {
    
    public static final int MYSQL_OPTION_MULTI_STATEMENTS_ON = 0;
    
    public static final int MYSQL_OPTION_MULTI_STATEMENTS_OFF = 1;
    
    private final int value;
    
    public MySQLComSetOptionPacket(final MySQLPacketPayload payload) {
        super(MySQLCommandPacketType.COM_SET_OPTION);
        value = payload.readInt2();
    }
}
