/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.algorithm.keygen;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.snowflake.ClockSyncSnowflakeId;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import me.ahoo.cosid.snowflake.SnowflakeId;
import me.ahoo.cosid.snowflake.StringSnowflakeId;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereInstanceRequiredAlgorithm;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.sharding.algorithm.sharding.cosid.CosIdAlgorithm;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import java.util.Properties;

/**
 * CosId snowflake key generate algorithm.
 */
public final class CosIdSnowflakeKeyGenerateAlgorithm implements KeyGenerateAlgorithm, ShardingSphereInstanceRequiredAlgorithm {
    
    public static final String TYPE = CosIdAlgorithm.TYPE_PREFIX + "SNOWFLAKE";
    
    public static final long DEFAULT_EPOCH = SnowflakeKeyGenerateAlgorithm.EPOCH;
    
    public static final String AS_STRING_KEY = "as-string";
    
    public static final String EPOCH_KEY = "epoch";
    
    private volatile SnowflakeId snowflakeId;
    
    private volatile boolean asString;
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
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
    public void init() {
        String asStringStr = getProps().getProperty(AS_STRING_KEY, Boolean.FALSE.toString());
        asString = Boolean.parseBoolean(asStringStr);
    }
    
    @Override
    public void setInstanceContext(final InstanceContext instanceContext) {
        long workerId = instanceContext.getWorkerId();
        long epoch = DEFAULT_EPOCH;
        if (props.containsKey(EPOCH_KEY)) {
            epoch = Long.parseLong(props.getProperty(EPOCH_KEY));
        }
        MillisecondSnowflakeId millisecondSnowflakeId =
                new MillisecondSnowflakeId(epoch, MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT, MillisecondSnowflakeId.DEFAULT_MACHINE_BIT, MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT, workerId);
        ClockSyncSnowflakeId clockSyncSnowflakeId = new ClockSyncSnowflakeId(millisecondSnowflakeId);
        snowflakeId = new StringSnowflakeId(clockSyncSnowflakeId, Radix62IdConverter.PAD_START);
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
