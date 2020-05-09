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

package org.apache.shardingsphere.underlying.common.yaml.constructor;

import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract type constructor.
 */
public abstract class AbstractTypeConstructor extends Constructor {
    
    private final Map<Class, Construct> typeConstructs = new HashMap<>();
    
    protected AbstractTypeConstructor(final Class<?> rootClass) {
        super(rootClass);
    }
    
    /**
     * register construct for class.
     *
     * @param typeClass type class
     * @param classConstruct class construct
     */
    protected final void registerConstruct(final Class typeClass, final Construct classConstruct) {
        typeConstructs.put(typeClass, classConstruct);
    }
    
    @Override
    protected final Construct getConstructor(final Node node) {
        if (typeConstructs.containsKey(node.getType())) {
            return typeConstructs.get(node.getType());
        }
        return super.getConstructor(node);
    }
}
