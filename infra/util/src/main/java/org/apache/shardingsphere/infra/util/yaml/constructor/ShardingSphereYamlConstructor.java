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

package org.apache.shardingsphere.infra.util.yaml.constructor;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.shortcuts.ShardingSphereYamlShortcuts;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * ShardingSphere YAML constructor.
 */
public final class ShardingSphereYamlConstructor extends Constructor {
    
    private final Class<?> rootClass;
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public ShardingSphereYamlConstructor(final Class<?> rootClass) {
        super(rootClass, createLoaderOptions());
        Map<String, Class<?>> yamlShortcuts = new HashMap<>();
        ShardingSphereServiceLoader.getServiceInstances(ShardingSphereYamlShortcuts.class).stream().map(ShardingSphereYamlShortcuts::getYamlShortcuts).forEach(yamlShortcuts::putAll);
        yamlShortcuts.forEach((key, value) -> addTypeDescription(new TypeDescription(value, key)));
        this.rootClass = rootClass;
    }
    
    /**
     * Create loader options.
     *
     * @return loader options
     */
    public static LoaderOptions createLoaderOptions() {
        LoaderOptions result = new LoaderOptions();
        result.setMaxAliasesForCollections(1000);
        result.setCodePointLimit(Integer.MAX_VALUE);
        return result;
    }
    
    @Override
    protected Construct getConstructor(final Node node) {
        Optional<ShardingSphereYamlConstruct> construct = TypedSPILoader.findService(ShardingSphereYamlConstruct.class, node.getType());
        return construct.isPresent() ? construct.get() : super.getConstructor(node);
    }
    
    @Override
    public Object constructObject(final Node node) {
        Object result = super.constructObject(node);
        if (isMappingNode(node, result)) {
            getPropertyUtils().getProperties(result.getClass()).stream().filter(Property::isWritable).forEach(each -> setEmptyCollectionIfNull(result, each));
        }
        return result;
    }
    
    private static boolean isMappingNode(final Node node, final Object target) {
        return null != target && node instanceof MappingNode && !(target instanceof Map) && !(target instanceof Collection);
    }
    
    private void setEmptyCollectionIfNull(final Object target, final Property property) {
        try {
            Object value = property.get(target);
            if (null != value) {
                return;
            }
            Class<?> propertyType = property.getType();
            if (Properties.class.isAssignableFrom(propertyType)) {
                property.set(target, new Properties());
            } else if (Map.class.isAssignableFrom(propertyType)) {
                property.set(target, Collections.emptyMap());
            } else if (Set.class.isAssignableFrom(propertyType)) {
                property.set(target, Collections.emptySet());
            } else if (List.class.isAssignableFrom(propertyType)) {
                property.set(target, Collections.emptyList());
            } else if (Collection.class.isAssignableFrom(propertyType)) {
                property.set(target, Collections.emptyList());
            }
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    protected Class<?> getClassForName(final String className) throws ClassNotFoundException {
        Preconditions.checkArgument(className.equals(rootClass.getName()), "Class `%s` is not accepted", className);
        return super.getClassForName(className);
    }
}
