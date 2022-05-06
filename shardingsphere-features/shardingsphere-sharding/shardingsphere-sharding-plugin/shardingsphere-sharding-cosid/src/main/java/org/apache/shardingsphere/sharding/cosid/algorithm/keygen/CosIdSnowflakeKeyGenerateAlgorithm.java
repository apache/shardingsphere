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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.snowflake.ClockSyncSnowflakeId;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import me.ahoo.cosid.snowflake.SnowflakeId;
import me.ahoo.cosid.snowflake.StringSnowflakeId;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereInstanceAwareAlgorithm;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.sharding.cosid.algorithm.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Calendar;
import java.util.Properties;

/**
 * CosId snowflake key generate algorithm.
 */
public final class CosIdSnowflakeKeyGenerateAlgorithm implements KeyGenerateAlgorithm, ShardingSphereInstanceAwareAlgorithm {
    
    public static final String TYPE = CosIdAlgorithmConstants.TYPE_PREFIX + "SNOWFLAKE";
    
    public static final long DEFAULT_EPOCH;
    
    public static final String AS_STRING_KEY = "as-string";
    
    public static final String EPOCH_KEY = "epoch";
    
    @Getter
    @Setter
    private Properties props;
    
    private volatile SnowflakeId snowflakeId;
    
    private volatile boolean asString;
    
    private volatile long epoch;
    
    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.NOVEMBER, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        DEFAULT_EPOCH = calendar.getTimeInMillis();
    }
    
    @Override
    public void init(final Properties props) {
        asString = getAsString(props);
        epoch = getEpoch(props);
    }
    
    private boolean getAsString(final Properties props) {
        return Boolean.parseBoolean(props.getProperty(AS_STRING_KEY, Boolean.FALSE.toString()));
    }
    
    private long getEpoch(final Properties props) {
        return props.containsKey(EPOCH_KEY) ? Long.parseLong(props.get(EPOCH_KEY).toString()) : DEFAULT_EPOCH;
    }
    
    @Override
    public Comparable<?> generateKey() {
        if (asString) {
            return getSnowflakeId().generateAsString();
        }
        return getSnowflakeId().generate();
    }
    
    private SnowflakeId getSnowflakeId() {
        Preconditions.checkNotNull(snowflakeId, "Instance context not set yet.");
        return snowflakeId;
    }
    
    @Override
    public void setInstanceContext(final InstanceContext instanceContext) {
        long workerId = instanceContext.getWorkerId();
        MillisecondSnowflakeId millisecondSnowflakeId =
                new MillisecondSnowflakeId(epoch, MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT, MillisecondSnowflakeId.DEFAULT_MACHINE_BIT, MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT, workerId);
        snowflakeId = new StringSnowflakeId(new ClockSyncSnowflakeId(millisecondSnowflakeId), Radix62IdConverter.PAD_START);
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
