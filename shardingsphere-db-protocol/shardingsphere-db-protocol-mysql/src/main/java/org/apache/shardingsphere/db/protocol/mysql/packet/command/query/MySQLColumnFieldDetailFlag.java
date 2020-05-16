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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * MySQL Column Field Detail Flag.
 * 
 * @see <a href="https://mariadb.com/kb/en/library/resultset/#field-detail-flag">Field detail flag</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLColumnFieldDetailFlag {

    NOT_NULL(1),

    PRIMARY_KEY(2),

    UNIQUE_KEY(4),

    MULTIPLE_KEY(8),

    BLOB(16),

    UNSIGNED(32),

    ZEROFILL_FLAG(64),

    BINARY_COLLATION(128),

    ENUM(256),

    AUTO_INCREMENT(512),

    TIMESTAMP(1024),

    SET(2048),

    NO_DEFAULT_VALUE_FLAG(4096),

    ON_UPDATE_NOW_FLAG(8192),

    NUM_FLAG(32768);
    
    private final int value;
}
