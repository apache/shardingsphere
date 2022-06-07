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

package org.apache.shardingsphere.shadow.algorithm.shadow.hint;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.hint.PreciseHintShadowValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Simple hint shadow algorithm.
 */
public final class SimpleHintShadowAlgorithm implements HintShadowAlgorithm<String> {
    
    @Getter
    private Properties props;
    
    private Map<String, String> simpleHint;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        checkPropsSize(props);
        simpleHint = initSimpleHint(props);
    }
    
    private Map<String, String> initSimpleHint(final Properties props) {
        Map<String, String> result = new HashMap<>(props.size(), 1.0f);
        Set<String> strings = props.stringPropertyNames();
        for (String each : strings) {
            result.put(each, props.getProperty(each));
        }
        return result;
    }
    
    private void checkPropsSize(final Properties props) {
        Preconditions.checkState(!props.isEmpty(), "Simple hint shadow algorithm props cannot be empty.");
    }
    
    @Override
    public boolean isShadow(final Collection<String> shadowTableNames, final PreciseHintShadowValue<String> noteShadowValue) {
        if (ShadowOperationType.HINT_MATCH != noteShadowValue.getShadowOperationType() && !shadowTableNames.contains(noteShadowValue.getLogicTableName())) {
            return false;
        }
        return ShadowHintExtractor.extractSimpleHint(noteShadowValue.getValue()).filter(this::containsHint).isPresent();
    }
    
    private boolean containsHint(final Map<String, String> preciseHint) {
        for (Entry<String, String> entry : simpleHint.entrySet()) {
            if (!entry.getValue().equals(preciseHint.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getType() {
        return "SIMPLE_HINT";
    }
}
