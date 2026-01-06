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

package org.apache.shardingsphere.infra.util.yaml.representer.processor;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultYamlTupleProcessorTest {
    
    private final DefaultYamlTupleProcessor processor = new DefaultYamlTupleProcessor();
    
    @Test
    void assertProcessWhenValueIsNullNode() {
        assertNull(processor.process(new NodeTuple(createScalar("key"), new ScalarNode(Tag.NULL, "null", null, null, DumperOptions.ScalarStyle.PLAIN))));
    }
    
    @Test
    void assertProcessWhenValueIsEmptySequence() {
        assertNull(processor.process(new NodeTuple(createScalar("key"), new SequenceNode(Tag.SEQ, Collections.emptyList(), DumperOptions.FlowStyle.BLOCK))));
    }
    
    @Test
    void assertProcessWhenValueIsEmptyMapping() {
        assertNull(processor.process(new NodeTuple(createScalar("key"), new MappingNode(Tag.MAP, Collections.emptyList(), DumperOptions.FlowStyle.BLOCK))));
    }
    
    @Test
    void assertProcessWhenMappingContainsNullValue() {
        NodeTuple mappingTuple = new NodeTuple(createScalar("inner"), new ScalarNode(Tag.NULL, "null", null, null, DumperOptions.ScalarStyle.PLAIN));
        MappingNode mappingNode = new MappingNode(Tag.MAP, Collections.singletonList(mappingTuple), DumperOptions.FlowStyle.BLOCK);
        assertNull(processor.process(new NodeTuple(createScalar("key"), mappingNode)));
    }
    
    @Test
    void assertProcessWhenValueIsNotUnset() {
        NodeTuple tuple = new NodeTuple(createScalar("key"), createScalar("value"));
        assertThat(processor.process(tuple), is(tuple));
    }
    
    @Test
    void assertProcessWhenSequenceNotEmpty() {
        SequenceNode sequenceNode = new SequenceNode(Tag.SEQ, Collections.singletonList(createScalar("v")), DumperOptions.FlowStyle.BLOCK);
        NodeTuple tuple = new NodeTuple(createScalar("key"), sequenceNode);
        assertThat(processor.process(tuple), is(tuple));
    }
    
    private ScalarNode createScalar(final String value) {
        return new ScalarNode(Tag.STR, value, null, null, DumperOptions.ScalarStyle.PLAIN);
    }
}
