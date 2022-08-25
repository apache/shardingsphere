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

import com.google.common.base.Strings;
import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeIdStateParser;
import me.ahoo.cosid.snowflake.SnowflakeIdStateParser;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.cosid.algorithm.keygen.CosIdSnowflakeKeyGenerateAlgorithm;

import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Snowflake interval sharding algorithm with CosId.
 */
public final class CosIdSnowflakeIntervalShardingAlgorithm extends AbstractCosIdIntervalShardingAlgorithm<Comparable<?>> {
    
    public static final String EPOCH_KEY = "epoch";
    
    private SnowflakeIdStateParser snowflakeIdStateParser;
    
    @Override
    public void init(final Properties props) {
        super.init(props);
        snowflakeIdStateParser = getSnowflakeIdStateParser(props);
    }
    
    private SnowflakeIdStateParser getSnowflakeIdStateParser(final Properties props) {
        long epoch = Long.parseLong(props.getProperty(EPOCH_KEY, CosIdSnowflakeKeyGenerateAlgorithm.DEFAULT_EPOCH + ""));
        return new MillisecondSnowflakeIdStateParser(
                epoch, MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT, MillisecondSnowflakeId.DEFAULT_MACHINE_BIT, MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT, getZoneId());
    }
    
    @Override
    protected LocalDateTime convertShardingValue(final Comparable<?> shardingValue) {
        Long snowflakeId = convertToSnowflakeId(shardingValue);
        return snowflakeIdStateParser.parseTimestamp(snowflakeId);
    }
    
    private Long convertToSnowflakeId(final Comparable<?> shardingValue) {
        if (shardingValue instanceof Long) {
            return (Long) shardingValue;
        }
        if (shardingValue instanceof String) {
            String shardingValueStr = (String) shardingValue;
            return Radix62IdConverter.PAD_START.asLong(shardingValueStr);
        }
        throw new IllegalArgumentException(Strings.lenientFormat("The current shard type:[%s] is not supported!", shardingValue.getClass()));
    }
    
    @Override
    public String getType() {
        return CosIdAlgorithmConstants.TYPE_PREFIX + "INTERVAL_SNOWFLAKE";
    }
}
