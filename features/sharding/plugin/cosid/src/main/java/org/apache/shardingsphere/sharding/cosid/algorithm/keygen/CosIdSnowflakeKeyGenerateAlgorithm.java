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

package org.apache.shardingsphere.sharding.cosid.algorithm.keygen;

import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.snowflake.ClockSyncSnowflakeId;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import me.ahoo.cosid.snowflake.SnowflakeId;
import me.ahoo.cosid.snowflake.StringSnowflakeId;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.InstanceContextAware;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.exception.ShardingPluginException;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;

/**
 * CosId snowflake key generate algorithm.
 */
public final class CosIdSnowflakeKeyGenerateAlgorithm implements KeyGenerateAlgorithm, InstanceContextAware {
    
    public static final long DEFAULT_EPOCH;
    
    public static final String AS_STRING_KEY = "as-string";
    
    public static final String EPOCH_KEY = "epoch";
    
    private Properties props;
    
    private SnowflakeId snowflakeId;
    
    private boolean asString;
    
    private long epoch;
    
    static {
        DEFAULT_EPOCH = LocalDateTime.of(2016, 11, 1, 0, 0, 0).toInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())).toEpochMilli();
    }
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        asString = getAsString(props);
        epoch = getEpoch(props);
    }
    
    private boolean getAsString(final Properties props) {
        return Boolean.parseBoolean(props.getProperty(AS_STRING_KEY, Boolean.FALSE.toString()));
    }
    
    private long getEpoch(final Properties props) {
        long result = Long.parseLong(props.getProperty(EPOCH_KEY, String.valueOf(DEFAULT_EPOCH)));
        ShardingSpherePreconditions.checkState(result > 0L,
                () -> new ShardingPluginException("Key generate algorithm `%s` initialization failed, reason is: %s.", getType(), "Epoch must be positive."));
        return result;
    }
    
    @Override
    public void setInstanceContext(final InstanceContext instanceContext) {
        int workerId = instanceContext.generateWorkerId(props);
        MillisecondSnowflakeId millisecondSnowflakeId =
                new MillisecondSnowflakeId(epoch, MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT, MillisecondSnowflakeId.DEFAULT_MACHINE_BIT, MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT, workerId);
        snowflakeId = new StringSnowflakeId(new ClockSyncSnowflakeId(millisecondSnowflakeId), Radix62IdConverter.PAD_START);
    }
    
    @Override
    public Comparable<?> generateKey() {
        if (asString) {
            return getSnowflakeId().generateAsString();
        }
        return getSnowflakeId().generate();
    }
    
    private SnowflakeId getSnowflakeId() {
        ShardingSpherePreconditions.checkNotNull(snowflakeId, () -> new ShardingPluginException("Instance context not set yet."));
        return snowflakeId;
    }
    
    @Override
    public String getType() {
        return CosIdAlgorithmConstants.TYPE_PREFIX + "SNOWFLAKE";
    }
}
