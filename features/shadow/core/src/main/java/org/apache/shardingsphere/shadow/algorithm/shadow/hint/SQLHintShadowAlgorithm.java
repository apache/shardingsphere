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

import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.hint.PreciseHintShadowValue;

import java.util.Collection;

/**
 * SQL hint shadow algorithm.
 */
public final class SQLHintShadowAlgorithm implements HintShadowAlgorithm<String> {
    
    @Override
    public boolean isShadow(final Collection<String> shadowTableNames, final PreciseHintShadowValue<String> noteShadowValue) {
        if (ShadowOperationType.HINT_MATCH == noteShadowValue.getShadowOperationType() || shadowTableNames.contains(noteShadowValue.getLogicTableName())) {
            return SQLHintUtils.extractHint(noteShadowValue.getValue()).isShadow();
        }
        return false;
    }
    
    @Override
    public String getType() {
        return "SQL_HINT";
    }
}
