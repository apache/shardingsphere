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

package org.apache.shardingsphere.infra.yaml.engine.representer;

import org.apache.shardingsphere.infra.yaml.engine.fixture.DefaultYamlRepresenterFixture;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.MethodProperty;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereYamlRepresenterTest {
    
    @Test
    public void assertToYamlWithNull() {
        DefaultYamlRepresenterFixture actual = new DefaultYamlRepresenterFixture();
        assertThat(new Yaml(new ShardingSphereYamlRepresenter()).dumpAsMap(actual), is("{}\n"));
    }
    
    @Test
    public void assertToYamlWithEmpty() {
        DefaultYamlRepresenterFixture actual = new DefaultYamlRepresenterFixture();
        actual.setValue("");
        actual.setCollection(Collections.emptyList());
        actual.setMap(Collections.emptyMap());
        assertThat(new Yaml(new ShardingSphereYamlRepresenter()).dumpAsMap(actual), is("value: ''\n"));
    }
    
    @Test
    public void assertToYamlWithValue() {
        DefaultYamlRepresenterFixture actual = new DefaultYamlRepresenterFixture();
        actual.setValue("value");
        actual.setCollection(Arrays.asList("value1", "value2"));
        Map<String, String> map = new LinkedHashMap<>(2, 1);
        map.put("key1", "value1");
        map.put("key2", "value2");
        actual.setMap(map);
        String expected = new Yaml(new ShardingSphereYamlRepresenter()).dumpAsMap(actual);
        assertThat(expected, containsString("collection:\n- value1\n- value2\n"));
        assertThat(expected, containsString("map:\n  key1: value1\n  key2: value2\n"));
        assertThat(expected, containsString("value: value\n"));
    }
    
    @Test
    public void assertRepresentJavaBeanProperty() throws IntrospectionException {
        ShardingSphereYamlRepresenter shardingSphereYamlRepresenter = new ShardingSphereYamlRepresenter();
        DefaultYamlRepresenterFixture javaBean = new DefaultYamlRepresenterFixture();
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor("collection", DefaultYamlRepresenterFixture.class);
        MethodProperty methodProperty = new MethodProperty(propertyDescriptor);
        List<String> propertyValue = new ArrayList<>(2);
        propertyValue.add("value1");
        propertyValue.add("value2");
        NodeTuple actual = shardingSphereYamlRepresenter.representJavaBeanProperty(javaBean, methodProperty, propertyValue, null);
        List<Node> nodes = ((SequenceNode) actual.getValueNode()).getValue();
        assertThat(nodes.size(), is(2));
        assertTrue(nodes.stream().map(node -> ((ScalarNode) node).getValue()).collect(Collectors.toList()).containsAll(propertyValue));
    }
}
