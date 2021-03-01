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

package org.apache.shardingsphere.sharding.yaml.engine.representer.processor;

import org.apache.shardingsphere.infra.yaml.engine.representer.processor.ShardingSphereYamlTupleProcessor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * None YAML tuple processor.
 */
public final class NoneYamlTupleProcessor implements ShardingSphereYamlTupleProcessor {
    
    @Override
    public String getTupleName() {
        return "none";
    }
    
    @Override
    public NodeTuple process(final NodeTuple nodeTuple) {
        return isNullNode(nodeTuple.getValueNode()) ? null : processNoneTuple(nodeTuple);
    }
    
    private boolean isNullNode(final Node valueNode) {
        return Tag.NULL.equals(valueNode.getTag());
    }
    
    private NodeTuple processNoneTuple(final NodeTuple noneTuple) {
        return new NodeTuple(noneTuple.getKeyNode(), new ScalarNode(Tag.STR, "", null, null, null));
    }
}
