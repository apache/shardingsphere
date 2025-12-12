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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * MySQL constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLConstants {
    
    public static final AttributeKey<AtomicInteger> SEQUENCE_ID_ATTRIBUTE_KEY = AttributeKey.valueOf("MYSQL_SEQUENCE_ID");
    
    public static final AttributeKey<MySQLCharacterSets> CHARACTER_SET_ATTRIBUTE_KEY = AttributeKey.valueOf(MySQLCharacterSets.class.getName());
    
    public static final AttributeKey<Integer> OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY = AttributeKey.valueOf("MYSQL_OPTION_MULTI_STATEMENTS");
    
    /**
     * Protocol version is always 0x0A.
     */
    public static final int PROTOCOL_VERSION = 0x0A;
    
    public static final MySQLCharacterSets DEFAULT_CHARSET = MySQLCharacterSets.UTF8MB4_GENERAL_CI;
}
