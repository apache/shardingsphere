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

package org.apache.shardingsphere.sharding.algorithm.sharding.cosid;

import com.google.common.collect.Range;
import lombok.RequiredArgsConstructor;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.sharding.algorithm.constant.CosIdAlgorithmConstants;
import org.apache.shardingsphere.sharding.algorithm.keygen.CosIdSnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CosIdSnowflakeIntervalShardingAlgorithmTest {
    
    static CosIdSnowflakeIntervalShardingAlgorithm createShardingAlgorithm() {
        CosIdSnowflakeIntervalShardingAlgorithm result = new CosIdSnowflakeIntervalShardingAlgorithm();
        result.setProps(createAlgorithmProperties());
        result.init();
        return result;
    }
    
    private static Properties createAlgorithmProperties() {
        Properties result = new Properties();
        result.setProperty(CosIdIntervalShardingAlgorithm.ZONE_ID_KEY, "Asia/Shanghai");
        result.setProperty(CosIdAlgorithmConstants.LOGIC_NAME_PREFIX_KEY, CosIdIntervalShardingAlgorithmTest.LOGIC_NAME_PREFIX);
        result.setProperty(CosIdIntervalShardingAlgorithm.DATE_TIME_LOWER_KEY,
                CosIdIntervalShardingAlgorithmTest.LOWER_DATE_TIME.format(CosIdIntervalShardingAlgorithm.DEFAULT_DATE_TIME_FORMATTER));
        result.setProperty(CosIdIntervalShardingAlgorithm.DATE_TIME_UPPER_KEY,
                CosIdIntervalShardingAlgorithmTest.UPPER_DATE_TIME.format(CosIdIntervalShardingAlgorithm.DEFAULT_DATE_TIME_FORMATTER));
        result.setProperty(CosIdIntervalShardingAlgorithm.SHARDING_SUFFIX_FORMAT_KEY, CosIdIntervalShardingAlgorithmTest.SUFFIX_FORMATTER_STRING);
        result.setProperty(CosIdIntervalShardingAlgorithm.INTERVAL_UNIT_KEY, "MONTHS");
        result.put(CosIdIntervalShardingAlgorithm.INTERVAL_AMOUNT_KEY, 1);
        return result;
    }
    
    static long convertToSnowflakeId(final LocalDateTime dateTime) {
        long epochMilliseconds = dateTime.toInstant(CosIdIntervalShardingAlgorithmTest.ZONE_OFFSET_SHANGHAI).toEpochMilli();
        long timeDiff = epochMilliseconds - CosIdSnowflakeKeyGenerateAlgorithm.DEFAULT_EPOCH;
        return timeDiff << (MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT + MillisecondSnowflakeId.DEFAULT_MACHINE_BIT);
    }
    
    static Iterable<Object[]> preciseArgsProviderAsSnowflakeId() {
        return CosIdIntervalShardingAlgorithmTest.preciseArgsProvider(CosIdSnowflakeIntervalShardingAlgorithmTest::convertToSnowflakeId);
    }
    
    static Iterable<Object[]> rangeArgsProviderAsSnowflakeId() {
        return CosIdIntervalShardingAlgorithmTest.rangeArgsProvider(CosIdSnowflakeIntervalShardingAlgorithmTest::convertToSnowflakeId);
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class SnowflakeIdPreciseValueDoShardingTest {
        
        private final Long snowflakeId;
        
        private final String expected;
        
        private CosIdSnowflakeIntervalShardingAlgorithm shardingAlgorithm;
        
        @Before
        public void init() {
            shardingAlgorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return preciseArgsProviderAsSnowflakeId();
        }
        
        @Test
        public void assertDoSharding() {
            PreciseShardingValue shardingValue = new PreciseShardingValue<>(CosIdIntervalShardingAlgorithmTest.LOGIC_NAME,
                    CosIdIntervalShardingAlgorithmTest.COLUMN_NAME, new DataNodeInfo(CosIdIntervalShardingAlgorithmTest.LOGIC_NAME_PREFIX, 6, '0'), snowflakeId);
            String actual = shardingAlgorithm.doSharding(CosIdIntervalShardingAlgorithmTest.ALL_NODES, shardingValue);
            assertThat(actual, is(expected));
        }
    }
    
    @RunWith(Parameterized.class)
    @RequiredArgsConstructor
    public static class SnowflakeIdRangeValueDoShardingTest {
        
        private final Range<Long> rangeValue;
        
        private final Collection<String> expected;
        
        private CosIdSnowflakeIntervalShardingAlgorithm shardingAlgorithm;
        
        @Before
        public void init() {
            shardingAlgorithm = createShardingAlgorithm();
        }
        
        @Parameters
        public static Iterable<Object[]> argsProvider() {
            return rangeArgsProviderAsSnowflakeId();
        }
        
        @Test
        public void assertDoSharding() {
            RangeShardingValue shardingValue = new RangeShardingValue<>(CosIdIntervalShardingAlgorithmTest.LOGIC_NAME,
                    CosIdIntervalShardingAlgorithmTest.COLUMN_NAME, new DataNodeInfo(CosIdIntervalShardingAlgorithmTest.LOGIC_NAME_PREFIX, 6, '0'), rangeValue);
            assertThat(shardingAlgorithm.doSharding(CosIdIntervalShardingAlgorithmTest.ALL_NODES, shardingValue), is(expected));
        }
    }
}
