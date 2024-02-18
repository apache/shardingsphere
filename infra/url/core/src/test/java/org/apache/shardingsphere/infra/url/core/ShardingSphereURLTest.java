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

package org.apache.shardingsphere.infra.url.core;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingSphereURLTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertParse(final String url, final String expectedSourceType, final String expectedConfigurationSubject, final Map<String, String> expectedProps) {
        ShardingSphereURL actual = ShardingSphereURL.parse(url);
        assertThat(actual.getSourceType(), is(expectedSourceType));
        assertThat(actual.getConfigurationSubject(), is(expectedConfigurationSubject));
        assertThat(actual.getQueryProps(), is(expectedProps));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Map<String, String> multiParams = new HashMap<>();
            multiParams.put("databaseName", "sharding_db");
            multiParams.put("placeholder-type", "none");
            return Stream.of(Arguments.of("absolutepath:/Users/shardingsphere/config.yaml", "absolutepath:", "/Users/shardingsphere/config.yaml", Collections.emptyMap()),
                    Arguments.of("absolutepath:C:\\Users\\shardingsphere\\config.yaml", "absolutepath:", "C:\\Users\\shardingsphere\\config.yaml", Collections.emptyMap()),
                    Arguments.of("absolutepath:/Users/configDirName?databaseName=sharding_db", "absolutepath:", "/Users/configDirName", Collections.singletonMap("databaseName", "sharding_db")),
                    Arguments.of("absolutepath:/Users/configDirName/?databaseName=sharding_db", "absolutepath:", "/Users/configDirName/", Collections.singletonMap("databaseName", "sharding_db")),
                    Arguments.of("classpath:config/shardingsphere/config.yml?databaseName=sharding_db&placeholder-type=none", "classpath:", "config/shardingsphere/config.yml", multiParams));
        }
    }
}
