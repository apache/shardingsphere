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
import org.yaml.snakeyaml.nodes.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    protected Class<?> getClassForName(final String className) throws ClassNotFoundException {
        Preconditions.checkArgument(className.equals(rootClass.getName()), "Class `%s` is not accepted", className);
        return super.getClassForName(className);
    }
}
