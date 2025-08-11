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

import org.apache.shardingsphere.test.infra.util.PropertiesBuilder;
import org.apache.shardingsphere.test.infra.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class MultiSourcePropertiesTest {
    
    private MultiSourceProperties multiSourceProperties;
    
    @BeforeEach
    void setUp() {
        Properties propsA = PropertiesBuilder.build(new Property("keyA", "valueA"));
        Properties propsB = new Properties(PropertiesBuilder.build(new Property("keyA", "valueB"), new Property("keyB", "valueB")));
        multiSourceProperties = new MultiSourceProperties(propsA, propsB);
        multiSourceProperties.setProperty("originalKey", "originalValue");
    }
    
    @Test
    void assertGetProperty() {
        assertThat(multiSourceProperties.getProperty("originalKey"), is("originalValue"));
        assertThat(multiSourceProperties.getProperty("keyA"), is("valueA"));
        assertThat(multiSourceProperties.getProperty("keyB"), is("valueB"));
        assertNull(multiSourceProperties.getProperty("keyC"));
    }
    
    @Test
    void assertGetPropertyWithDefaultValue() {
        assertThat(multiSourceProperties.getProperty("keyA", "defaultValue"), is("valueA"));
        assertThat(multiSourceProperties.getProperty("keyB", "defaultValue"), is("valueB"));
        assertThat(multiSourceProperties.getProperty("keyC", "defaultValue"), is("defaultValue"));
    }
}
