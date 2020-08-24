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

package org.apache.shardingsphere.example.proxy.hint;

import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public final class ModuloHintShardingAlgorithm implements HintShardingAlgorithm<String> {
    
    private Properties props = new Properties();
    
    @Override
    public void init() {
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final HintShardingValue<String> shardingValue) {
        Collection<String> result = new LinkedList<>();
        for (String each : availableTargetNames) {
            for (String value : shardingValue.getValues()) {
                if (each.endsWith(String.valueOf(Long.parseLong(value) % 2))) {
                    result.add(each);
                }
            }
        }
        return result;
    }
    
    @Override
    public Properties getProps() {
        return props;
    }
    
    @Override
    public void setProps(final Properties props) {
        this.props = props;
    }
    
    @Override
    public String getType() {
        return "HINT_TEST";
    }
}
