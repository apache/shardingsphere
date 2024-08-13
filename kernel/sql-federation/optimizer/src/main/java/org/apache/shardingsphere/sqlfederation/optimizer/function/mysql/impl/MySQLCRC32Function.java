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

package org.apache.shardingsphere.sqlfederation.optimizer.function.mysql.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.zip.CRC32;

/**
 * MySQL crc32 function.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLCRC32Function {
    
    /**
     * Crc32.
     *
     * @param value value
     * @return crc32
     */
    @SuppressWarnings("unused")
    public static Long crc32(final Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof String && ((String) value).isEmpty()) {
            return 0L;
        }
        String stringValue = value instanceof Boolean ? ((Boolean) value) ? "1" : "0" : String.valueOf(value);
        CRC32 crc32 = new CRC32();
        crc32.update(stringValue.getBytes(), 0, stringValue.length());
        return crc32.getValue();
    }
}
