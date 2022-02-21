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

package org.apache.shardingsphere.traffic.algorithm.traffic.hint;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.traffic.api.traffic.hint.HintTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.hint.HintTrafficValue;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

/**
 * Simple hint traffic algorithm.
 */
@Getter
@Setter
public final class SQLHintTrafficAlgorithm implements HintTrafficAlgorithm<String> {
    
    private Properties props = new Properties();
    
    @Override
    public void init() {
        Preconditions.checkState(!props.isEmpty(), "Simple hint traffic algorithm props cannot be empty.");
    }
    
    @Override
    public boolean match(final HintTrafficValue<String> hintTrafficValue) {
        Properties sqlHintProps = SQLHintUtils.getSQLHintProps(hintTrafficValue.getValue());
        for (Entry<Object, Object> each : props.entrySet()) {
            if (!Objects.equals(each.getValue(), sqlHintProps.get(String.valueOf(each.getKey())))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getType() {
        return "SQL_HINT";
    }
}
