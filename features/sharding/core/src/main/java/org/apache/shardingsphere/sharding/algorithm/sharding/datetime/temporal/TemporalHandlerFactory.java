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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalDateTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalDateTimeTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.LocalTimeTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.MonthTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.YearMonthTemporalHandler;
import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type.YearTemporalHandler;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Temporal handler factory.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class TemporalHandlerFactory {
    
    /**
     * Create new instance of temporal handler.
     *
     * @param temporal temporal
     * @return temporal handler
     */
    @SuppressWarnings("rawtypes")
    public static TemporalHandler newInstance(final TemporalAccessor temporal) {
        if (!temporal.isSupported(ChronoField.NANO_OF_DAY)) {
            if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
                return new LocalDateTemporalHandler();
            }
            if (temporal.isSupported(ChronoField.YEAR) && temporal.isSupported(ChronoField.MONTH_OF_YEAR)) {
                return new YearMonthTemporalHandler();
            }
            if (temporal.isSupported(ChronoField.YEAR)) {
                return new YearTemporalHandler();
            }
            if (temporal.isSupported(ChronoField.MONTH_OF_YEAR)) {
                return new MonthTemporalHandler();
            }
        }
        return temporal.isSupported(ChronoField.EPOCH_DAY) ? new LocalDateTimeTemporalHandler() : new LocalTimeTemporalHandler();
    }
}
