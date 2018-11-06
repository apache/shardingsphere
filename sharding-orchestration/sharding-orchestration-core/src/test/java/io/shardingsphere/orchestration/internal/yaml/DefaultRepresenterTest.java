/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.yaml;

import io.shardingsphere.orchestration.internal.yaml.fixture.DefaultRepresenterFixture;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DefaultRepresenterTest {
    
    @Test
    public void assertToYamlWithNull() {
        DefaultRepresenterFixture actual = new DefaultRepresenterFixture();
        assertThat(new Yaml(new DefaultRepresenter()).dumpAsMap(actual), is("{}\n"));
    }
    
    @Test
    public void assertToYamlWithEmpty() {
        DefaultRepresenterFixture actual = new DefaultRepresenterFixture();
        actual.setValue("");
        actual.setCollection(Collections.<String>emptyList());
        actual.setMap(Collections.<String, String>emptyMap());
        assertThat(new Yaml(new DefaultRepresenter()).dumpAsMap(actual), is("value: ''\n"));
    }
    
    @Test
    public void assertToYamlWithValue() {
        DefaultRepresenterFixture actual = new DefaultRepresenterFixture();
        actual.setValue("value");
        actual.setCollection(Arrays.asList("value1", "value2"));
        Map<String, String> map = new LinkedHashMap<>(2, 1);
        map.put("key1", "value1");
        map.put("key2", "value2");
        actual.setMap(map);
        String expected = new Yaml(new DefaultRepresenter()).dumpAsMap(actual);
        assertThat(expected, containsString("collection:\n- value1\n- value2\n"));
        assertThat(expected, containsString("map:\n  key1: value1\n  key2: value2\n"));
        assertThat(expected, containsString("value: value\n"));
    }
}
