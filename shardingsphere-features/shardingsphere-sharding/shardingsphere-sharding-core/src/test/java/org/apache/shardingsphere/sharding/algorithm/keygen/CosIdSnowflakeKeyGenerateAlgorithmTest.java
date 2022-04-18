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

import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeIdStateParser;
import me.ahoo.cosid.snowflake.SnowflakeIdState;
import me.ahoo.cosid.snowflake.SnowflakeIdStateParser;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.sharding.algorithm.keygen.fixture.WorkerIdGeneratorFixture;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class CosIdSnowflakeKeyGenerateAlgorithmTest {
    
    private static final long FIXTURE_WORKER_ID = 0;
    
    private final SnowflakeIdStateParser snowflakeIdStateParser = new MillisecondSnowflakeIdStateParser(
            CosIdSnowflakeKeyGenerateAlgorithm.DEFAULT_EPOCH,
            MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT,
            MillisecondSnowflakeId.DEFAULT_MACHINE_BIT,
            MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT
    );
    
    @Test
    public void assertGenerateKey() {
        CosIdSnowflakeKeyGenerateAlgorithm cosIdSnowflakeKeyGenerateAlgorithm = new CosIdSnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        cosIdSnowflakeKeyGenerateAlgorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(FIXTURE_WORKER_ID),
                new ModeConfiguration("Memory", null, false), mock(LockContext.class)));
        cosIdSnowflakeKeyGenerateAlgorithm.setProps(properties);
        cosIdSnowflakeKeyGenerateAlgorithm.init();
        long firstActualKey = (Long) cosIdSnowflakeKeyGenerateAlgorithm.generateKey();
        long secondActualKey = (Long) cosIdSnowflakeKeyGenerateAlgorithm.generateKey();
        SnowflakeIdState firstActualState = snowflakeIdStateParser.parse(firstActualKey);
        SnowflakeIdState secondActualState = snowflakeIdStateParser.parse(secondActualKey);
        assertThat(firstActualState.getMachineId(), is(FIXTURE_WORKER_ID));
        assertThat(firstActualState.getSequence(), is(0L));
        assertThat(secondActualState.getMachineId(), is(FIXTURE_WORKER_ID));
        long expectedSecondSequence = secondActualState.getTimestamp().isAfter(firstActualState.getTimestamp()) ? 0L : 1L;
        assertThat(secondActualState.getSequence(), is(expectedSecondSequence));
    }
    
    @Test
    public void assertGenerateKeyAsString() {
        CosIdSnowflakeKeyGenerateAlgorithm cosIdSnowflakeKeyGenerateAlgorithm = new CosIdSnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        properties.setProperty(CosIdSnowflakeKeyGenerateAlgorithm.AS_STRING_KEY, "true");
        cosIdSnowflakeKeyGenerateAlgorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(FIXTURE_WORKER_ID),
                new ModeConfiguration("Memory", null, false), mock(LockContext.class)));
        cosIdSnowflakeKeyGenerateAlgorithm.setProps(properties);
        cosIdSnowflakeKeyGenerateAlgorithm.init();
        Comparable<?> actualKey = cosIdSnowflakeKeyGenerateAlgorithm.generateKey();
        assertThat(actualKey, Matchers.instanceOf(String.class));
        String actualStringKey = (String) actualKey;
        assertThat(actualStringKey.length(), is(Radix62IdConverter.MAX_CHAR_SIZE));
        long actualLongKey = Radix62IdConverter.PAD_START.asLong(actualStringKey);
        SnowflakeIdState actualState = snowflakeIdStateParser.parse(actualLongKey);
        assertThat(actualState.getMachineId(), is(FIXTURE_WORKER_ID));
        assertThat(actualState.getSequence(), is(0L));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertGenerateKeyWhenNoneInstanceContext() {
        CosIdSnowflakeKeyGenerateAlgorithm cosIdSnowflakeKeyGenerateAlgorithm = new CosIdSnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        cosIdSnowflakeKeyGenerateAlgorithm.setProps(properties);
        cosIdSnowflakeKeyGenerateAlgorithm.init();
        cosIdSnowflakeKeyGenerateAlgorithm.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGenerateKeyWhenNegative() {
        CosIdSnowflakeKeyGenerateAlgorithm cosIdSnowflakeKeyGenerateAlgorithm = new CosIdSnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        cosIdSnowflakeKeyGenerateAlgorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(-1),
                new ModeConfiguration("Memory", null, false), mock(LockContext.class)));
        cosIdSnowflakeKeyGenerateAlgorithm.setProps(properties);
        cosIdSnowflakeKeyGenerateAlgorithm.init();
        cosIdSnowflakeKeyGenerateAlgorithm.generateKey();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGenerateKeyWhenGreaterThen1023() {
        CosIdSnowflakeKeyGenerateAlgorithm cosIdSnowflakeKeyGenerateAlgorithm = new CosIdSnowflakeKeyGenerateAlgorithm();
        Properties properties = new Properties();
        cosIdSnowflakeKeyGenerateAlgorithm.setInstanceContext(new InstanceContext(new ComputeNodeInstance(mock(InstanceDefinition.class)), new WorkerIdGeneratorFixture(1024),
                new ModeConfiguration("Memory", null, false), mock(LockContext.class)));
        cosIdSnowflakeKeyGenerateAlgorithm.setProps(properties);
        cosIdSnowflakeKeyGenerateAlgorithm.init();
        cosIdSnowflakeKeyGenerateAlgorithm.generateKey();
    }
}
