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

package org.apache.shardingsphere.infra.util.yaml.representer;

import org.apache.shardingsphere.infra.util.yaml.fixture.pojo.YamlConfigurationFixture;
import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShardingSphereYamlRepresenterTest {
    
    @Test
    public void assertToYamlWithoutContent() {
        YamlConfigurationFixture actual = new YamlConfigurationFixture();
        assertThat(new Yaml(new ShardingSphereYamlRepresenter(new DumperOptions())).dumpAsMap(actual), is("{}\n"));
    }
    
    @Test
    public void assertToYamlWithAllContents() {
        YamlConfigurationFixture actual = new YamlConfigurationFixture();
        actual.setValue("value");
        actual.setCollection(Arrays.asList("value1", "value2"));
        Map<String, String> map = new LinkedHashMap<>(2, 1);
        map.put("key1", "value1");
        map.put("key2", "value2");
        actual.setMap(map);
        actual.setEmbeddedMap(new LinkedHashMap<>());
        actual.getEmbeddedMap().put("embedded_map_1", new LinkedHashMap<>());
        actual.getEmbeddedMap().put("embedded_map_2", Collections.singletonMap("embedded_map_foo", "embedded_map_foo_value"));
        actual.setCustomizedTag("customized_tag");
        String expected = new Yaml(new ShardingSphereYamlRepresenter(new DumperOptions())).dumpAsMap(actual);
        assertThat(expected, containsString("collection:\n- value1\n- value2\n"));
        assertThat(expected, containsString("map:\n  key1: value1\n  key2: value2\n"));
        assertThat(expected, not(containsString("embedded_map_1")));
        assertThat(expected, containsString("embeddedMap:\n  embedded_map_2:\n    embedded_map_foo: embedded_map_foo_value\n"));
        assertThat(expected, containsString("value: value\n"));
        assertThat(expected, containsString("customizedTag: converted_customized_tag\n"));
    }
}
