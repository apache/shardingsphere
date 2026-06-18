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

package org.apache.shardingsphere.data.pipeline.cdc.client.config;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CDCClientConfigurationTest {
    
    @Test
    void assertNewSuccess() {
        CDCClientConfiguration actual = new CDCClientConfiguration("foo_host", 3307, 1000);
        assertThat(actual.getAddress(), is("foo_host"));
        assertThat(actual.getPort(), is(3307));
        assertThat(actual.getTimeoutMillis(), is(1000));
    }
    
    @Test
    void assertNewWithNullAddress() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new CDCClientConfiguration(null, 3307, 1000));
        assertThat(actual.getMessage(), is("The address parameter can't be null."));
    }
    
    @Test
    void assertNewWithInvalidPort() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new CDCClientConfiguration("foo_host", 0, 1000));
        assertThat(actual.getMessage(), is("The port must be greater than 0."));
    }
}
