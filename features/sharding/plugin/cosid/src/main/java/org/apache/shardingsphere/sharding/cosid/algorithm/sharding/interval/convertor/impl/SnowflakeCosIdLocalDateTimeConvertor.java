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

package org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.convertor.impl;

import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.snowflake.SnowflakeIdStateParser;
import org.apache.shardingsphere.sharding.cosid.algorithm.sharding.interval.convertor.CosIdLocalDateTimeConvertor;
import org.apache.shardingsphere.sharding.exception.ShardingPluginException;

import java.time.LocalDateTime;

/**
 * Snowflake local date time convertor for Snowflake.
 */
@RequiredArgsConstructor
public final class SnowflakeCosIdLocalDateTimeConvertor implements CosIdLocalDateTimeConvertor {
    
    private final SnowflakeIdStateParser snowflakeIdStateParser;
    
    @Override
    public LocalDateTime toLocalDateTime(final Comparable<?> value) {
        return snowflakeIdStateParser.parseTimestamp(convertToSnowflakeId(value));
    }
    
    private Long convertToSnowflakeId(final Comparable<?> value) {
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof String) {
            return Radix62IdConverter.PAD_START.asLong((String) value);
        }
        throw new ShardingPluginException("Unsupported sharding value type `%s`.", value);
    }
}
