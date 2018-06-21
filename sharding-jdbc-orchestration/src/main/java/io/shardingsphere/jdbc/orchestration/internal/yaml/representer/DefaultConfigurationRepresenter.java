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

import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Sharding or master-slave rule config representer.
 *
 * @author panjuan
 */
public class DefaultConfigurationRepresenter extends Representer {
    
    public DefaultConfigurationRepresenter() {
        super();
        this.nullRepresenter = new DefaultConfigurationRepresenter.NullRepresent();
    }
    
    private class NullRepresent implements Represent {
        public Node representData(final Object data) {
            return representScalar(Tag.NULL, "");
        }
    }
}
