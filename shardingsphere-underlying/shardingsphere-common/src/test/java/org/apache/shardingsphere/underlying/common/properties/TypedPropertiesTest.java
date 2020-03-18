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

package org.apache.shardingsphere.underlying.common.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import java.util.Properties;

public class TypedPropertiesTest {

    @Test
    public void assertTypes() throws Exception {

        Properties props = new Properties();
        AProperties properties = new AProperties(APropertyKey.class, props);
        assertThat(properties.getValue(APropertyKey.A1), is(1));
        assertThat(properties.getValue(APropertyKey.A2), is(200L));
        assertThat(properties.getValue(APropertyKey.A3), is(Boolean.TRUE));
        assertThat(properties.getValue(APropertyKey.A4), is("a4"));
        assertThat(properties.getValue(APropertyKey.A5), is(1));
        assertThat(properties.getValue(APropertyKey.A6), is(200L));
        assertThat(properties.getValue(APropertyKey.A7), is(Boolean.TRUE));

        // test override
        props.put("a1", 11);
        props.put("a2", 222L);
        props.put("a3", Boolean.FALSE);
        props.put("a4", "a44");
        props.put("a5", 11);
        props.put("a6", 222L);
        props.put("a7", Boolean.FALSE);

        properties = new AProperties(APropertyKey.class, props);
        assertThat(properties.getValue(APropertyKey.A1), is(11));
        assertThat(properties.getValue(APropertyKey.A2), is(222L));
        assertThat(properties.getValue(APropertyKey.A3), is(Boolean.FALSE));
        assertThat(properties.getValue(APropertyKey.A4), is("a44"));
        assertThat(properties.getValue(APropertyKey.A5), is(11));
        assertThat(properties.getValue(APropertyKey.A6), is(222L));
        assertThat(properties.getValue(APropertyKey.A7), is(Boolean.FALSE));
    }

    @RequiredArgsConstructor
    @Getter
    private enum APropertyKey implements TypedPropertyKey {
        A1("a1", "1", int.class),
        A2("a2", "200", long.class),
        A3("a3", "true", boolean.class),
        A4("a4", "a4", String.class),
        A5("a5", "1", Integer.class),
        A6("a6", "200", Long.class),
        A7("a7", "true", Boolean.class);

        private final String key;

        private final String defaultValue;

        private final Class<?> type;
    }

    private static class AProperties extends TypedProperties<APropertyKey> {
        AProperties(final Class<APropertyKey> keyClass, final Properties props) {
            super(keyClass, props);
        }
    }
}
