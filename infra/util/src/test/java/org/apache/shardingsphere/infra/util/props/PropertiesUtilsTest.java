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

package org.apache.shardingsphere.infra.util.props;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PropertiesUtilsTest {
    
    @Test
    void assertToStringWithEmptyProperties() {
        assertThat(PropertiesUtils.toString(new Properties()), is(""));
    }
    
    @Test
    void assertToStringWithSingleKey() {
        assertThat(PropertiesUtils.toString(PropertiesBuilder.build(new Property("key", "value"))), is("'key'='value'"));
    }
    
    @Test
    void assertToStringWithMultipleKeys() {
        assertThat(PropertiesUtils.toString(PropertiesBuilder.build(new Property("key1", "value1"), new Property("key2", "value2"))), is("'key1'='value1', 'key2'='value2'"));
    }
}
