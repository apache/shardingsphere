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

package org.apache.shardingsphere.infra.url.core.arg;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class URLArgumentPlaceholderTypeFactoryTest {
    
    @Test
    void assertValueOfWithValidQueryProperties() {
        Properties props = new Properties();
        props.setProperty("placeholder-type", "environment");
        assertThat(URLArgumentPlaceholderTypeFactory.valueOf(props), is(URLArgumentPlaceholderType.ENVIRONMENT));
    }
    
    @Test
    void assertValueOfWithInvalidQueryProperties() {
        Properties props = new Properties();
        props.setProperty("placeholder-type", "invalid");
        assertThat(URLArgumentPlaceholderTypeFactory.valueOf(props), is(URLArgumentPlaceholderType.NONE));
    }
    
    @Test
    void assertValueOfWithEmptyQueryProperties() {
        assertThat(URLArgumentPlaceholderTypeFactory.valueOf(new Properties()), is(URLArgumentPlaceholderType.NONE));
    }
}
