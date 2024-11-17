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

package org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;

/**
 * Temporal handler.
 * 
 * @param <T> type of temporal
 */
public interface TemporalHandler<T extends TemporalAccessor> {
    
    /**
     * Parse from text.
     *
     * @param text temporal text
     * @param formatter date time formatter
     * @return parsed temporal
     */
    T parse(CharSequence text, DateTimeFormatter formatter);
    
    /**
     * Convert temporal.
     *
     * @param temporal to be converted temporal
     * @return converted temporal
     */
    T convertTo(TemporalAccessor temporal);
    
    /**
     * Judge whether temporal1 is after temporal2.
     *
     * @param temporal1 temporal1
     * @param temporal2 temporal2
     * @param stepAmount step amount
     * @return is temporal1 after temporal2 or not
     */
    boolean isAfter(T temporal1, T temporal2, int stepAmount);
    
    /**
     * Add temporal.
     *
     * @param temporal to be added temporal
     * @param stepAmount to be added step amount
     * @param unit temporal unit
     * @return added temporal
     */
    T add(T temporal, long stepAmount, TemporalUnit unit);
}
