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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

/**
 * Time value utility of MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLTimeValueUtil {
    
    public static final String ZERO_OF_TIME = "00:00:00";
    
    public static final String ZERO_OF_DATE = "0000-00-00";
    
    public static final String YEAR_OF_ZERO = "0000";
    
    public static final String DATETIME_OF_ZERO = "0000-00-00 00:00:00";
    
    private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMAT = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    
    /**
     * Get simple date format for current thread.
     *
     * @return simple date format
     */
    public static SimpleDateFormat getSimpleDateFormat() {
        return TIMESTAMP_FORMAT.get();
    }
}
