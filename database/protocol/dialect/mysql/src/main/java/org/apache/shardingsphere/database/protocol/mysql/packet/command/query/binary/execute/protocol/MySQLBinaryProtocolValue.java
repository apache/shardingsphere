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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol;

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

import java.sql.SQLException;

/**
 * Binary protocol value for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_binary_resultset.html#sect_protocol_binary_resultset_row_value">Binary Protocol Value</a>
 */
public interface MySQLBinaryProtocolValue {
    
    /**
     * Read binary protocol value.
     *
     * @param payload payload operation for MySQL packet
     * @param unsigned is unsigned value
     * @return binary value result
     * @throws SQLException SQL exception
     */
    Object read(MySQLPacketPayload payload, boolean unsigned) throws SQLException;
    
    /**
     * Write binary protocol value.
     *
     * @param payload payload operation for MySQL packet
     * @param value value to be written
     */
    void write(MySQLPacketPayload payload, Object value);
}
