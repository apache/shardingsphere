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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval;

import me.ahoo.cosid.sharding.LocalDateTimeConvertor;
import me.ahoo.cosid.sharding.StandardLocalDateTimeConvertor;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Interval sharding algorithm with CosId.
 */
public final class CosIdIntervalShardingAlgorithm extends AbstractCosIdIntervalShardingAlgorithm<Comparable<?>> {
    
    private static final String ZONE_ID_KEY = "zone-id";
    
    private static final String DATE_TIME_PATTERN_KEY = "datetime-pattern";
    
    private static final String TIMESTAMP_SECOND_UNIT = "SECOND";
    
    private static final String TIMESTAMP_UNIT_KEY = "ts-unit";
    
    @Override
    protected LocalDateTimeConvertor createLocalDateTimeConvertor(final Properties props) {
        ZoneId zoneId = props.containsKey(ZONE_ID_KEY) ? ZoneId.of(props.getProperty(ZONE_ID_KEY)) : ZoneId.systemDefault();
        boolean isSecondTs = props.containsKey(TIMESTAMP_UNIT_KEY) && TIMESTAMP_SECOND_UNIT.equalsIgnoreCase(props.getProperty(TIMESTAMP_UNIT_KEY));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(props.getProperty(DATE_TIME_PATTERN_KEY, DEFAULT_DATE_TIME_PATTERN));
        return new StandardLocalDateTimeConvertor(zoneId, isSecondTs, dateTimeFormatter);
    }
    
    @Override
    public String getType() {
        return CosIdAlgorithmConstants.TYPE_PREFIX + "INTERVAL";
    }
}
