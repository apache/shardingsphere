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

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.engine.representer.processor.DefaultYamlTupleProcessor;
import org.apache.shardingsphere.infra.yaml.engine.representer.processor.ShardingSphereYamlTupleProcessor;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * ShardingSphere YAML representer.
 */
public final class ShardingSphereYamlRepresenter extends Representer {
    
    static {
        ShardingSphereServiceLoader.register(ShardingSphereYamlTupleProcessor.class);
    }
    
    public ShardingSphereYamlRepresenter() {
        YamlRuleConfigurationSwapperEngine.getYamlShortcuts().forEach((key, value) -> addClassTag(value, new Tag(key)));
    }
    
    @Override
    protected NodeTuple representJavaBeanProperty(final Object javaBean, final Property property, final Object propertyValue, final Tag customTag) {
        NodeTuple nodeTuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        for (ShardingSphereYamlTupleProcessor each : ShardingSphereServiceLoader.newServiceInstances(ShardingSphereYamlTupleProcessor.class)) {
            if (property.getName().equals(each.getTupleName())) {
                return each.process(nodeTuple);
            }
        }
        return new DefaultYamlTupleProcessor().process(nodeTuple);
    }
}
