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

package org.apache.shardingsphere.infra.util.datetime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

/**
 * Date time formatter factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeFormatterFactory {
    
    private static final DateTimeFormatter STANDARD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private static final DateTimeFormatter SHORT_MILLS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    
    private static final DateTimeFormatter LONG_MILLS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Get standard date time formatter.
     * 
     * @return standard date time formatter
     */
    public static DateTimeFormatter getStandardFormatter() {
        return STANDARD;
    }
    
    /**
     * Get date formatter.
     *
     * @return date formatter
     */
    public static DateTimeFormatter getDateFormatter() {
        return DATE;
    }
    
    /**
     * Get time formatter.
     *
     * @return time formatter
     */
    public static DateTimeFormatter getTimeFormatter() {
        return TIME;
    }
    
    /**
     * Get short mills date time formatter.
     *
     * @return short mills date time formatter
     */
    public static DateTimeFormatter getShortMillsFormatter() {
        return SHORT_MILLS;
    }
    
    /**
     * Get long mills date time formatter.
     *
     * @return long mills date time formatter
     */
    public static DateTimeFormatter getLongMillsFormatter() {
        return LONG_MILLS;
    }
}
