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

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;

/**
 * Year month temporal handler.
 */
public final class YearMonthTemporalHandler implements TemporalHandler<YearMonth> {
    
    @Override
    public YearMonth parse(final CharSequence text, final DateTimeFormatter formatter) {
        return YearMonth.parse(text, formatter);
    }
    
    @Override
    public YearMonth convertTo(final TemporalAccessor temporal) {
        return temporal.query(YearMonth::from);
    }
    
    @Override
    public boolean isAfter(final YearMonth temporal1, final YearMonth temporal2, final int stepAmount) {
        return temporal1.isAfter(temporal2);
    }
    
    @Override
    public YearMonth add(final YearMonth temporal, final long stepAmount, final TemporalUnit unit) {
        return temporal.plus(stepAmount, unit);
    }
}
