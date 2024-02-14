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

package org.apache.shardingsphere.driver.jdbc.core.driver.url.spi.absolutepath;

import org.apache.shardingsphere.driver.jdbc.core.driver.url.ShardingSphereURLManager;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class AbsolutePathWithEnvironmentURLProviderTest {
    
    @Test
    void assertGetContent() {
        String path = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        byte[] actual = new AbsolutePathWithEnvironmentURLProvider().getContent("jdbc:shardingsphere:absolutepath-environment:" + path, path);
        byte[] expected = ShardingSphereURLManager.getContent("jdbc:shardingsphere:absolutepath:" + path, "jdbc:shardingsphere:");
        assertThat(actual, is(expected));
    }
}
