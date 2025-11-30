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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MySQL Column Field Detail Flag.
 * 
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__column__definition__flags.html">Column Definition Flags</a>
 * @see <a href="https://mariadb.com/kb/en/library/resultset/#field-detail-flag">Field detail flag</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLColumnDefinitionFlag {
    
    NOT_NULL(0x00000001),
    
    PRIMARY_KEY(0x00000002),
    
    UNIQUE_KEY(0x00000004),
    
    MULTIPLE_KEY(0x00000008),
    
    BLOB(0x00000010),
    
    UNSIGNED(0x00000020),
    
    ZEROFILL_FLAG(0x00000040),
    
    BINARY_COLLATION(0x00000080),
    
    ENUM(0x00000100),
    
    AUTO_INCREMENT(0x00000200),
    
    TIMESTAMP(0x00000400),
    
    SET(0x00000800),
    
    NO_DEFAULT_VALUE_FLAG(0x00001000),
    
    ON_UPDATE_NOW_FLAG(0x00002000),
    
    NUM_FLAG(0x00008000);
    
    private final int value;
}
