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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * MySQL binary string.
 *
 * <p>Since MySQL replication protocol handles BINARY/VARBINARY column as MYSQL_TYPE_VARCHAR(15),
 * and MySQLBinlogProtocolValue.read parameters have no real column metadata.
 * So this customized class object will be returned on parsing.</p>
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
public final class MySQLBinaryString implements Serializable {
    
    private static final long serialVersionUID = 2448062591593788665L;
    
    private final byte[] bytes;
}
