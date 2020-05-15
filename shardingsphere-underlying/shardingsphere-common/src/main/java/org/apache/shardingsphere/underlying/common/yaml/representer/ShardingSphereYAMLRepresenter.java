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

package org.apache.shardingsphere.underlying.common.yaml.representer;

import lombok.SneakyThrows;
import org.apache.shardingsphere.sharding.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.underlying.common.yaml.representer.processor.ShardingSphereYAMLTupleProcessor;
import org.apache.shardingsphere.underlying.common.yaml.representer.processor.DefaultTupleProcessor;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlRuleConfigurationSwapper;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.lang.reflect.ParameterizedType;

/**
 * ShardingSphere YAML representer.
 */
public final class ShardingSphereYAMLRepresenter extends Representer {
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
        ShardingSphereServiceLoader.register(ShardingSphereYAMLTupleProcessor.class);
    }
    
    @SneakyThrows
    public ShardingSphereYAMLRepresenter() {
        for (YamlRuleConfigurationSwapper each : ShardingSphereServiceLoader.newServiceInstances(YamlRuleConfigurationSwapper.class)) {
            Class<?> yamlRuleConfiguration = Class.forName(((ParameterizedType) each.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].getTypeName());
            addClassTag(yamlRuleConfiguration, new Tag("!" + each.getRuleTagName()));
        }
    }
    
    @Override
    protected NodeTuple representJavaBeanProperty(final Object javaBean, final Property property, final Object propertyValue, final Tag customTag) {
        NodeTuple nodeTuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        for (ShardingSphereYAMLTupleProcessor each : ShardingSphereServiceLoader.newServiceInstances(ShardingSphereYAMLTupleProcessor.class)) {
            if (property.getName().equals(each.getTupleName())) {
                return each.process(nodeTuple);
            }
        }
        return new DefaultTupleProcessor().process(nodeTuple);
    }
}
