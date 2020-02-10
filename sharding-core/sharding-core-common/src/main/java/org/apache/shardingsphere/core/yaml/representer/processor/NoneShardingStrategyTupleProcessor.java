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

package org.apache.shardingsphere.core.yaml.representer.processor;

import org.apache.shardingsphere.underlying.common.yaml.representer.processor.TupleProcessor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * NoneShardingStrategy YAML tuple processor.
 *
 * @author yangyi
 */
public final class NoneShardingStrategyTupleProcessor implements TupleProcessor {
    
    @Override
    public String getProcessedTupleName() {
        return "none";
    }
    
    @Override
    public NodeTuple process(final NodeTuple nodeTuple) {
        return isNullNode(nodeTuple.getValueNode()) ? null : processNoneShardingStrategyTuple(nodeTuple);
    }
    
    private boolean isNullNode(final Node valueNode) {
        return Tag.NULL.equals(valueNode.getTag());
    }
    
    private NodeTuple processNoneShardingStrategyTuple(final NodeTuple noneShardingStrategyTuple) {
        Node newValueNode = new ScalarNode(Tag.STR, "", null, null, null);
        return new NodeTuple(noneShardingStrategyTuple.getKeyNode(), newValueNode);
    }
}
