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
import lombok.Setter;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.hint.PreciseHintShadowValue;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Simple hint shadow algorithm.
 */
@Getter
@Setter
public final class SimpleHintShadowAlgorithm implements HintShadowAlgorithm<String> {
    
    private Properties props = new Properties();
    
    @Override
    public void init() {
        checkPropsSize();
    }
    
    private void checkPropsSize() {
        Preconditions.checkState(!props.isEmpty(), "Simple hint shadow algorithm props cannot be empty.");
    }
    
    @Override
    public boolean isShadow(final Collection<String> shadowTableNames, final PreciseHintShadowValue<String> noteShadowValue) {
        if (ShadowOperationType.HINT_MATCH != noteShadowValue.getShadowOperationType() && !shadowTableNames.contains(noteShadowValue.getLogicTableName())) {
            return false;
        }
        Optional<Map<String, String>> noteOptional = ShadowHintExtractor.extractSimpleHint(noteShadowValue.getValue());
        return noteOptional.filter(stringStringMap -> props.entrySet().stream().allMatch(entry -> Objects.equals(entry.getValue(), stringStringMap.get(String.valueOf(entry.getKey()))))).isPresent();
    }
    
    @Override
    public String getType() {
        return "SIMPLE_HINT";
    }
}
