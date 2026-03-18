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

import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;

/**
 * Month temporal handler.
 */
public final class MonthTemporalHandler implements TemporalHandler<Month> {
    
    /*
     * After the sharding key is formatted as a {@link String}, if the length of the {@link String} is less than `datetime-pattern`, it usually means there is a problem with the sharding key.
     *
     * @param endpoint A class carrying time information with an unknown class name.
     *
     * @return {@link java.time.Month}
     */
    @Override
    public Month parse(final CharSequence text, final DateTimeFormatter formatter) {
        return Month.of(Integer.parseInt(text.toString()));
    }
    
    @Override
    public Month convertTo(final TemporalAccessor temporal) {
        return Month.from(temporal);
    }
    
    @Override
    public boolean isAfter(final Month temporal1, final Month temporal2, final int stepAmount) {
        return temporal1.getValue() > temporal2.getValue() || (temporal1.getValue() + stepAmount) > Month.DECEMBER.getValue();
    }
    
    @Override
    public Month add(final Month temporal, final long stepAmount, final TemporalUnit unit) {
        return temporal.plus(stepAmount);
    }
}
