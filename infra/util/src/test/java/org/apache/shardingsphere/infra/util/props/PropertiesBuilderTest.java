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

import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class PropertiesBuilderTest {
    
    @SuppressWarnings("UseOfPropertiesAsHashtable")
    @Test
    void assertBuild() {
        Properties props = PropertiesBuilder.build(new Property("string_key", "string_value"), new Property("int_key", 1), new Property("list_key", Arrays.asList(10, 11)));
        assertThat(props.size(), is(3));
        assertThat(props.getProperty("string_key"), is("string_value"));
        assertNull(props.getProperty("int_key"));
        assertThat(props.get("int_key"), is(1));
        assertNull(props.getProperty("list_key"));
        assertThat(props.get("list_key"), is(Arrays.asList(10, 11)));
    }
}
