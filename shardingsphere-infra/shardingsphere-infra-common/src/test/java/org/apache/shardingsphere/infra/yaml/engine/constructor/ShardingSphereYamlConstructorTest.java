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

package org.apache.shardingsphere.infra.yaml.engine.constructor;

import org.apache.shardingsphere.infra.yaml.engine.fixture.DefaultYamlRepresenterFixture;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class ShardingSphereYamlConstructorTest {
    
    @Test
    public void assertToObject() {
        String yamlString = ""
                + "collection:\n"
                + "- value1\n"
                + "- value2\n"
                + "map:\n"
                + "  key1: value1\n"
                + "  key2: value2\n"
                + "value: value\n"
                + "customClass:";
        DefaultYamlRepresenterFixture actual = new Yaml(new ShardingSphereYamlConstructor(DefaultYamlRepresenterFixture.class)).loadAs(yamlString, DefaultYamlRepresenterFixture.class);
        assertThat(actual.getValue(), is("value"));
        assertThat(actual.getCollection().size(), is(2));
        Iterator<String> iterator = actual.getCollection().iterator();
        assertThat(iterator.next(), is("value1"));
        assertThat(iterator.next(), is("value2"));
        assertThat(actual.getMap().size(), is(2));
        assertThat(actual.getMap().get("key1"), is("value1"));
        assertThat(actual.getMap().get("key2"), is("value2"));
        assertNotNull(actual.getCustomClass());
    }
}
