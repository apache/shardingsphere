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

package org.apache.shardingsphere.driver.jdbc.core.driver.spi.absolutepath;

import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLManager;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class AbsolutePathWithEnvironmentURLProviderTest {
    
    @Test
    void assertGetContent() {
        assertThat(getActual(createURLProvider()), is(getExpected()));
    }
    
    private AbsolutePathWithEnvironmentURLProvider createURLProvider() {
        AbsolutePathWithEnvironmentURLProvider result = spy(new AbsolutePathWithEnvironmentURLProvider());
        when(result.getEnvironmentVariables("FIXTURE_JDBC_URL")).thenReturn("jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        when(result.getEnvironmentVariables("FIXTURE_USERNAME")).thenReturn("sa");
        return result;
    }
    
    private byte[] getActual(final AbsolutePathWithEnvironmentURLProvider urlProvider) {
        String absoluteActualPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("config/driver/foo-driver-environment-variables-fixture.yaml")).getPath();
        return urlProvider.getContent("jdbc:shardingsphere:absolutepath-environment:" + absoluteActualPath, absoluteActualPath);
    }
    
    private byte[] getExpected() {
        String absoluteExpectedPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        return ShardingSphereURLManager.getContent("jdbc:shardingsphere:absolutepath:" + absoluteExpectedPath, "jdbc:shardingsphere:");
    }
}
