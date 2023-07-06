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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import org.apache.shardingsphere.driver.jdbc.exception.syntax.DriverURLProviderNotFoundException;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ConfigService.class)
class ShardingSphereDriverURLManagerTest {
    
    private final int fooDriverConfigLength = 999;
    
    private final String urlPrefix = "jdbc:shardingsphere:";
    
    @Test
    void assertNewConstructorWithEmptyURL() {
        assertThrows(DriverURLProviderNotFoundException.class, () -> ShardingSphereDriverURLManager.getContent("jdbc:shardingsphere:", urlPrefix));
    }
    
    @Test
    void assertToClasspathConfigurationFile() {
        byte[] actual = ShardingSphereDriverURLManager.getContent("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml", urlPrefix);
        assertThat(actual.length, is(fooDriverConfigLength));
    }
    
    @Test
    void assertToAbsolutePathConfigurationFile() {
        String absolutePath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        byte[] actual = ShardingSphereDriverURLManager.getContent("jdbc:shardingsphere:absolutepath:" + absolutePath, urlPrefix);
        assertThat(actual.length, is(fooDriverConfigLength));
    }
    
    @Test
    void assertToApolloConfigurationFile() {
        ConfigFile configFile = mock(ConfigFile.class);
        when(configFile.getContent()).thenReturn("config content");
        when(ConfigService.getConfigFile(anyString(), any(ConfigFileFormat.class))).thenReturn(configFile);
        String url = "jdbc:shardingsphere:apollo:namespace";
        byte[] content = ShardingSphereDriverURLManager.getContent(url, urlPrefix);
        assertThat("config content".getBytes(StandardCharsets.UTF_8), is(content));
    }
}
