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

package org.apache.shardingsphere.shadow.algorithm.shadow.column;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Properties;

/**
 * Column value matched shadow algorithm.
 */
public final class ColumnValueMatchedShadowAlgorithm extends AbstractColumnMatchedShadowAlgorithm {
    
    private static final String VALUE_PROPS_KEY = "value";
    
    private String shadowValue;
    
    @Override
    public void init(final Properties props) {
        super.init(props);
        shadowValue = getShadowValue(props);
    }
    
    private String getShadowValue(final Properties props) {
        String result = props.getProperty(VALUE_PROPS_KEY);
        ShardingSpherePreconditions.checkNotNull(result, () -> new AlgorithmInitializationException(this, "Column value match shadow algorithm value cannot be null."));
        return result;
    }
    
    @Override
    protected boolean matchesShadowValue(final Comparable<?> value) {
        return shadowValue.equals(String.valueOf(value));
    }
    
    @Override
    public String getType() {
        return "VALUE_MATCH";
    }
}
