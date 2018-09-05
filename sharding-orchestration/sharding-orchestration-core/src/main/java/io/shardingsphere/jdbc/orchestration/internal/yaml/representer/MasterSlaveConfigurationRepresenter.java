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

package io.shardingsphere.jdbc.orchestration.internal.yaml.representer;

import io.shardingsphere.core.exception.ShardingException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Data source representer.
 *
 * @author panjuan
 */
public final class MasterSlaveConfigurationRepresenter extends Representer {
    
    private static Collection<String> eliminatedPropertyNames = new HashSet<>();
    
    static {
        eliminatedPropertyNames.add("configMap");
    }
    
    public MasterSlaveConfigurationRepresenter() {
        super();
        this.nullRepresenter = new NullRepresent();
    }
    
    @Override
    protected Set<Property> getProperties(final Class<?> type) {
        Set<Property> propertySet;
        try {
            propertySet = super.getProperties(type);
        } catch (IntrospectionException ex) {
            throw new ShardingException(ex);
        }
        Set<Property> filteredSet = new LinkedHashSet<>();
        for (Property prop : propertySet) {
            String name = prop.getName();
            if (!eliminatedPropertyNames.contains(name)) {
                filteredSet.add(prop);
            }
        }
        return filteredSet;
    }
    
    private class NullRepresent implements Represent {
        
        @Override
        public Node representData(final Object data) {
            return representScalar(Tag.NULL, "");
        }
    }
}
