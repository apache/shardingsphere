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

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.util.Collection;
import java.util.Set;

/**
 * Yaml representer for simple type.
 *
 * @author zhangliang
 */
public final class SimpleTypeRepresenter extends DefaultRepresenter {
    
    private static final Collection<Class<?>> GENERAL_CLASS_TYPE;
    
    private final Collection<String> skippedPropertyNames;
    
    public SimpleTypeRepresenter(final String... skippedPropertyNames) {
        this.skippedPropertyNames = Sets.newHashSet(skippedPropertyNames);
    }
    
    static {
        GENERAL_CLASS_TYPE = Sets.<Class<?>>newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class);
    }
    
    @Override
    protected MappingNode representJavaBean(final Set<Property> properties, final Object javaBean) {
        return super.representJavaBean(Sets.filter(properties, new Predicate<Property>() {
    
            @Override
            public boolean apply(final Property input) {
                return !skippedPropertyNames.contains(input.getName()) && GENERAL_CLASS_TYPE.contains(input.getType());
            }
        }), javaBean);
    }
}
