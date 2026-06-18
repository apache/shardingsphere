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

package org.apache.shardingsphere.infra.util.yaml.fixture.representer;

import org.apache.shardingsphere.infra.util.yaml.representer.processor.ShardingSphereYamlTupleProcessor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

public final class YamlTupleProcessorFixture implements ShardingSphereYamlTupleProcessor {
    
    @Override
    public String getTupleName() {
        return "customizedTag";
    }
    
    @SuppressWarnings("ReturnOfNull")
    @Override
    public NodeTuple process(final NodeTuple nodeTuple) {
        String value = ((ScalarNode) nodeTuple.getValueNode()).getValue();
        return "null".equals(value) ? null : new NodeTuple(nodeTuple.getKeyNode(), new ScalarNode(Tag.STR, String.join("_", "converted", value), null, null, DumperOptions.ScalarStyle.PLAIN));
    }
}
