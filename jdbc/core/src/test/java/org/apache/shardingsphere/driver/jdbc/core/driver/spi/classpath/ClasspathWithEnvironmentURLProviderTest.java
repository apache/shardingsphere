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

package org.apache.shardingsphere.driver.jdbc.core.driver.spi.classpath;

import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLManager;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ClasspathWithEnvironmentURLProviderTest {
    
    @Test
    void assertReplaceEnvironmentVariables() {
        final String urlPrefix = "jdbc:shardingsphere:";
        ClasspathWithEnvironmentURLProvider spy = spy(new ClasspathWithEnvironmentURLProvider());
        when(spy.getEnvironmentVariables("FIXTURE_JDBC_URL")).thenReturn("jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        when(spy.getEnvironmentVariables("FIXTURE_USERNAME")).thenReturn("sa");
        byte[] actual = spy.getContent("jdbc:shardingsphere:classpath-environment:config/driver/foo-driver-environment-variables-fixture.yaml", urlPrefix);
        byte[] actualOrigin = ShardingSphereURLManager.getContent("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml", urlPrefix);
        assertThat(actual, is(actualOrigin));
    }
}
