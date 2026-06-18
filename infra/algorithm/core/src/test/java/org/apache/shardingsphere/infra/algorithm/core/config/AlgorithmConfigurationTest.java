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

package org.apache.shardingsphere.infra.algorithm.core.config;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgorithmConfigurationTest {
    
    @Test
    void assertConstruct() {
        Properties props = new Properties();
        AlgorithmConfiguration actual = new AlgorithmConfiguration("INLINE", props);
        assertThat(actual.getType(), is("INLINE"));
        assertThat(actual.getProps(), is(props));
    }
    
    @Test
    void assertConstructWithNullProps() {
        AlgorithmConfiguration actual = new AlgorithmConfiguration("INLINE", null);
        assertThat(actual.getType(), is("INLINE"));
        assertTrue(actual.getProps().isEmpty());
    }
    
    @Test
    void assertConstructWithEmptyType() {
        assertThat(assertThrows(IllegalArgumentException.class, () -> new AlgorithmConfiguration("", new Properties())).getMessage(), is("Type is required."));
    }
}
