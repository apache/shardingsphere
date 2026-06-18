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

package org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.type;

import org.apache.shardingsphere.sharding.algorithm.sharding.datetime.temporal.TemporalHandler;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;

/**
 * Local time temporal handler.
 */
public final class LocalTimeTemporalHandler implements TemporalHandler<LocalTime> {
    
    @Override
    public LocalTime parse(final CharSequence text, final DateTimeFormatter formatter) {
        return LocalTime.parse(text, formatter);
    }
    
    @Override
    public LocalTime convertTo(final TemporalAccessor temporal) {
        return temporal.query(TemporalQueries.localTime());
    }
    
    @Override
    public boolean isAfter(final LocalTime temporal1, final LocalTime temporal2, final int stepAmount) {
        return temporal1.isAfter(temporal2);
    }
    
    @Override
    public LocalTime add(final LocalTime temporal, final long stepAmount, final TemporalUnit unit) {
        return temporal.plus(stepAmount, unit);
    }
}
