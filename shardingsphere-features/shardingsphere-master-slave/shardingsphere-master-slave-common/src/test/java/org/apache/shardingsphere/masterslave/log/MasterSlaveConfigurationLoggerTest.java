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

package org.apache.shardingsphere.masterslave.log;

import lombok.SneakyThrows;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public final class MasterSlaveConfigurationLoggerTest {
    
    @Mock
    private Logger log;
    
    @Before
    @SneakyThrows
    public void setLog() {
        setFinalStaticField(ConfigurationLogger.class.getDeclaredField("log"), log);
    }
    
    @SneakyThrows
    private void setFinalStaticField(final Field field, final Object newValue) {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
    
    @Test
    public void assertLogMasterSlaveRuleConfiguration() {
        String yaml = "rules:\n"
                + "- !MASTER_SLAVE\n"
                + "  dataSources:\n"
                + "    ms_ds:\n"
                + "      masterDataSourceName: master_ds\n"
                + "      name: ms_ds\n"
                + "      slaveDataSourceNames:\n"
                + "      - slave_ds_0\n"
                + "      - slave_ds_1\n";
        assertLogInfo(yaml);
        ConfigurationLogger.log(Collections.singletonList(getMasterSlaveRuleConfiguration()));
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        return new MasterSlaveRuleConfiguration(
                Collections.singleton(new MasterSlaveDataSourceRuleConfiguration("ms_ds", "master_ds", Arrays.asList("slave_ds_0", "slave_ds_1"), null)), Collections.emptyMap());
    }
    
    private void assertLogInfo(final String logContent) {
        doAnswer(invocationOnMock -> {
            assertThat(invocationOnMock.getArgument(1).toString(), is("Rule configurations: "));
            assertThat(invocationOnMock.getArgument(2).toString(), is(logContent));
            return null;
        }).when(log).info(anyString(), anyString(), anyString());
    }
}
