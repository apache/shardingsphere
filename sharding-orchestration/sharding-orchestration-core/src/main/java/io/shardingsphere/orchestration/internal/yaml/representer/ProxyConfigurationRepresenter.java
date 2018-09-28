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

import lombok.SneakyThrows;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Proxy configuration representer.
 *
 * @author panjuan
 */
public final class ProxyConfigurationRepresenter extends Representer {
    
    private static Collection<String> eliminatedPropertyNames = new HashSet<>();
    
    static {
        eliminatedPropertyNames.add("dataSources");
        eliminatedPropertyNames.add("orchestration");
    }
    
    public ProxyConfigurationRepresenter() {
        super();
        nullRepresenter = new NullRepresent();
    }
    
    @SneakyThrows
    @Override
    protected Set<Property> getProperties(final Class<?> type) {
        Set<Property> result = new LinkedHashSet<>();
        for (Property each : super.getProperties(type)) {
            if (!eliminatedPropertyNames.contains(each.getName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private class NullRepresent implements Represent {
        public Node representData(final Object data) {
            return representScalar(Tag.NULL, "");
        }
    }
}
