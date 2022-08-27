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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;

import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeIdStateParser;
import me.ahoo.cosid.snowflake.SnowflakeIdState;
import me.ahoo.cosid.snowflake.SnowflakeIdStateParser;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.sharding.cosid.algorithm.keygen.fixture.WorkerIdGeneratorFixture;
import org.apache.shardingsphere.sharding.factory.KeyGenerateAlgorithmFactory;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class CosIdSnowflakeKeyGenerateAlgorithmTest {
    
    private static final long FIXTURE_WORKER_ID = 0;
    
    private final SnowflakeIdStateParser snowflakeIdStateParser = new MillisecondSnowflakeIdStateParser(
            CosIdSnowflakeKeyGenerateAlgorithm.DEFAULT_EPOCH,
            MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT,
            MillisecondSnowflakeId.DEFAULT_MACHINE_BIT,
            MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT);
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    @Test
    public void assertGenerateKey() {
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) KeyGenerateAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("COSID_SNOWFLAKE", new Properties()));
        algorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(FIXTURE_WORKER_ID),
                new ModeConfiguration("Standalone", null, false), mock(LockContext.class), eventBusContext));
        long firstActualKey = (Long) algorithm.generateKey();
        long secondActualKey = (Long) algorithm.generateKey();
        SnowflakeIdState firstActualState = snowflakeIdStateParser.parse(firstActualKey);
        SnowflakeIdState secondActualState = snowflakeIdStateParser.parse(secondActualKey);
        assertThat(firstActualState.getMachineId(), is(FIXTURE_WORKER_ID));
        assertThat(firstActualState.getSequence(), is(1L));
        assertThat(secondActualState.getMachineId(), is(FIXTURE_WORKER_ID));
        long expectedSecondSequence = 2L;
        assertThat(secondActualState.getSequence(), is(expectedSecondSequence));
    }
    
    @Test
    public void assertGenerateKeyModUniformity() {
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) KeyGenerateAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("COSID_SNOWFLAKE", new Properties()));
        algorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(FIXTURE_WORKER_ID),
                new ModeConfiguration("Standalone", null, false), mock(LockContext.class), eventBusContext));
        int divisor = 4;
        int total = 99999;
        int avg = total / divisor;
        double tolerance = avg * .0015;
        
        int mod0Counter = 0;
        int mod1Counter = 0;
        int mod2Counter = 0;
        int mod3Counter = 0;
        for (int i = 0; i < total; i++) {
            long id = (Long) algorithm.generateKey();
            int mod = (int) (id % divisor);
            switch (mod) {
                case 0:
                    mod0Counter++;
                    break;
                case 1:
                    mod1Counter++;
                    break;
                case 2:
                    mod2Counter++;
                    break;
                case 3:
                    mod3Counter++;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + mod);
            }
            int wait = ThreadLocalRandom.current().nextInt(10, 1000);
            LockSupport.parkNanos(wait);
        }
        MatcherAssert.assertThat((double) mod0Counter, closeTo(avg, tolerance));
        MatcherAssert.assertThat((double) mod1Counter, closeTo(avg, tolerance));
        MatcherAssert.assertThat((double) mod2Counter, closeTo(avg, tolerance));
        MatcherAssert.assertThat((double) mod3Counter, closeTo(avg, tolerance));
    }
    
    @Test
    public void assertGenerateKeyAsString() {
        Properties props = new Properties();
        props.setProperty(CosIdSnowflakeKeyGenerateAlgorithm.AS_STRING_KEY, Boolean.TRUE.toString());
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) KeyGenerateAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("COSID_SNOWFLAKE", props));
        algorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)),
                new WorkerIdGeneratorFixture(FIXTURE_WORKER_ID), new ModeConfiguration("Standalone", null, false), mock(LockContext.class), eventBusContext));
        Comparable<?> actualKey = algorithm.generateKey();
        assertThat(actualKey, instanceOf(String.class));
        String actualStringKey = (String) actualKey;
        assertThat(actualStringKey.length(), is(Radix62IdConverter.MAX_CHAR_SIZE));
        long actualLongKey = Radix62IdConverter.PAD_START.asLong(actualStringKey);
        SnowflakeIdState actualState = snowflakeIdStateParser.parse(actualLongKey);
        assertThat(actualState.getMachineId(), is(FIXTURE_WORKER_ID));
        assertThat(actualState.getSequence(), is(1L));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGenerateKeyWhenNoneInstanceContext() {
        KeyGenerateAlgorithmFactory.newInstance(new AlgorithmConfiguration("COSID_SNOWFLAKE", new Properties())).generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGenerateKeyWhenNegative() {
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) KeyGenerateAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("COSID_SNOWFLAKE", new Properties()));
        algorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(-1),
                new ModeConfiguration("Standalone", null, false), mock(LockContext.class), eventBusContext));
        algorithm.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGenerateKeyWhenGreaterThen1023() {
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) KeyGenerateAlgorithmFactory.newInstance(
                new AlgorithmConfiguration("COSID_SNOWFLAKE", new Properties()));
        algorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new WorkerIdGeneratorFixture(1024),
                new ModeConfiguration("Standalone", null, false), mock(LockContext.class), eventBusContext));
        algorithm.generateKey();
    }
}
