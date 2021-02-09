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

package org.apache.shardingsphere.infra.yaml.engine.constructor;

import java.util.Collection;

/**
 * ShardingSphere filter constructor for YAML.
 */
public final class ShardingSphereFilterYamlConstructor extends ShardingSphereYamlConstructor {
    
    private final Collection<Class<?>> acceptedClasses;
    
    public ShardingSphereFilterYamlConstructor(final Class<?> rootClass, final Collection<Class<?>> acceptedClasses) {
        super(rootClass);
        this.acceptedClasses = acceptedClasses;
    }
    
    public ShardingSphereFilterYamlConstructor(final Collection<Class<?>> acceptedClasses) {
        super(Object.class);
        this.acceptedClasses = acceptedClasses;
    }
    
    @Override
    protected Class<?> getClassForName(final String className) throws ClassNotFoundException {
        for (Class<?> each : acceptedClasses) {
            if (className.equals(each.getName())) {
                return super.getClassForName(className);
            }
        }
        throw new IllegalArgumentException(String.format("Class is not accepted: %s", className));
    }
}
