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

package io.shardingsphere.orchestration.internal.yaml.representer;

import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Collection;
import java.util.HashSet;

/**
 * Master slave configuration representer.
 *
 * @author panjuan
 */
public final class MasterSlaveConfigurationRepresenter extends Representer {
    
    private static Collection<String> eliminatedNodeNames = new HashSet<>();
    
    static {
        eliminatedNodeNames.add("configMap");
        eliminatedNodeNames.add("props");
    }
    
    public MasterSlaveConfigurationRepresenter() {
        super();
        nullRepresenter = new NullRepresent();
    }
    
    @Override
    protected NodeTuple representJavaBeanProperty(final Object javaBean, final Property property, final Object propertyValue, final Tag customTag) {
        NodeTuple tuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        return isUnwantedNodeTuple(tuple) ? null : tuple;
    }
    
    private boolean isUnwantedNodeTuple(final NodeTuple tuple) {
        return isEliminatedNode(tuple.getKeyNode()) || isNullNode(tuple.getValueNode()) || isEmptyCollectionNode(tuple.getValueNode());
    }
    
    private boolean isEliminatedNode(final Node keyNode) {
        return keyNode instanceof ScalarNode && eliminatedNodeNames.contains(((ScalarNode) keyNode).getValue());
    }
    
    private boolean isNullNode(final Node valueNode) {
        return Tag.NULL.equals(valueNode.getTag());
    }
    
    private boolean isEmptyCollectionNode(final Node valueNode) {
        return valueNode instanceof CollectionNode && (isEmptySequenceNode(valueNode) || isEmptyMappingNode(valueNode));
    }
    
    private boolean isEmptySequenceNode(final Node valueNode) {
        return Tag.SEQ.equals(valueNode.getTag()) && ((SequenceNode) valueNode).getValue().isEmpty();
    }
    
    private boolean isEmptyMappingNode(final Node valueNode) {
        return Tag.MAP.equals(valueNode.getTag()) && ((MappingNode) valueNode).getValue().isEmpty();
    }
    
    private class NullRepresent implements Represent {
        
        @Override
        public Node representData(final Object data) {
            return representScalar(Tag.NULL, "");
        }
    }
}
