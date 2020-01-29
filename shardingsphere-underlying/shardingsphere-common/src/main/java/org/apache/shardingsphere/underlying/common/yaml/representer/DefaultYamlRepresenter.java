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

import java.util.HashMap;
import java.util.Map;

import org.apache.shardingsphere.underlying.common.yaml.representer.processor.SkipUnsetTupleProcessor;
import org.apache.shardingsphere.underlying.common.yaml.representer.processor.TupleProcessor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Default YAML representer.
 *
 * @author panjuan
 * @author zhangliang
 * @author yangyi
 */
public final class DefaultYamlRepresenter extends Representer {
    
    private final Map<String, TupleProcessor> tupleProcessors = new HashMap<>();
    
    /**
     * Register new tuple processor into representer.
     *
     * @param tupleProcessor tuple processor
     */
    public void registerTupleProcessor(final TupleProcessor tupleProcessor) {
        tupleProcessors.put(tupleProcessor.getProcessedTupleName(), tupleProcessor);
    }
    
    @Override
    protected NodeTuple representJavaBeanProperty(final Object javaBean, final Property property, final Object propertyValue, final Tag customTag) {
        TupleProcessor tupleProcessor = tupleProcessors.containsKey(property.getName()) ? tupleProcessors.get(property.getName()) : new SkipUnsetTupleProcessor();
        return tupleProcessor.process(super.representJavaBeanProperty(javaBean, property, propertyValue, customTag));
    }
}
