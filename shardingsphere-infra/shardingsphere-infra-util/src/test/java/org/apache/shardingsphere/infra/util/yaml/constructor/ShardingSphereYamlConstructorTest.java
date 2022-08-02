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

package org.apache.shardingsphere.infra.util.yaml.constructor;

import org.apache.shardingsphere.infra.util.yaml.fixture.pojo.YamlConfigurationFixture;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class ShardingSphereYamlConstructorTest {
    
    @Test
    public void assertToObject() throws IOException {
        try (InputStream inputStream = ShardingSphereYamlConstructorTest.class.getClassLoader().getResourceAsStream("yaml/customized-obj.yaml")) {
            assertYamlObject(new Yaml(new ShardingSphereYamlConstructor(YamlConfigurationFixture.class)).loadAs(inputStream, YamlConfigurationFixture.class));
        }
    }
    
    private void assertYamlObject(final YamlConfigurationFixture actual) {
        assertThat(actual.getValue(), is("value"));
        assertThat(actual.getCollection().size(), is(2));
        assertThat(actual.getCollection(), is(Arrays.asList("value1", "value2")));
        assertThat(actual.getMap().size(), is(2));
        assertThat(actual.getMap().get("key1"), is("value1"));
        assertThat(actual.getMap().get("key2"), is("value2"));
        assertNotNull(actual.getCustomizedClass());
    }
    
    @Test(expected = ConstructorException.class)
    public void assertToObjectWithNotAcceptClass() throws IOException {
        try (InputStream inputStream = ShardingSphereYamlConstructorTest.class.getClassLoader().getResourceAsStream("yaml/accepted-class.yaml")) {
            new Yaml(new ShardingSphereYamlConstructor(Object.class)).loadAs(inputStream, Object.class);
        }
    }
}
