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

package org.apache.shardingsphere.infra.log;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigurationLoggerTest {
    
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
    public void assertLogEmptyRuleConfigurations() {
        String yaml = "{}\n"; 
        assertLogInfo("Rule configurations: ", yaml);
        ConfigurationLogger.log(Collections.emptyList());
    }
    
    @Test
    public void assertLogAuthentication() {
        String yaml = ""
                + "users:\n"
                + "  root:\n"
                + "    authorizedSchemas: sharding_db\n"
                + "    password: '123456'\n";
        assertLogInfo(Authentication.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getAuthentication());
    }
    
    private Authentication getAuthentication() {
        Authentication result = new Authentication();
        result.getUsers().put("root", new ProxyUser("123456", Collections.singletonList("sharding_db")));
        return result;
    }
    
    @Test
    public void assertLogProperties() {
        String yaml = ""
                + "sql.simple: 'true'\n"
                + "sql.show: 'true'\n";
        assertLogInfo(Properties.class.getSimpleName(), yaml);
        ConfigurationLogger.log(getProperties());
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        result.put(ConfigurationPropertyKey.SQL_SIMPLE.getKey(), Boolean.TRUE.toString());
        return result;
    }
    
    private void assertLogInfo(final String type, final String logContent) {
        doAnswer(invocationOnMock -> {
            assertThat(invocationOnMock.getArgument(1).toString(), is(type));
            assertThat(invocationOnMock.getArgument(2).toString(), is(logContent));
            return null;
        }).when(log).info(anyString(), anyString(), anyString());
    }
}
