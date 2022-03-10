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

import org.apache.shardingsphere.infra.hint.SQLHintPropertiesKey;
import org.apache.shardingsphere.traffic.api.traffic.hint.HintTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.hint.HintTrafficValue;

/**
 * Simple hint traffic algorithm.
 */
public final class SQLHintTrafficAlgorithm implements HintTrafficAlgorithm {
    
    @Override
    public void init() {
    }
    
    @Override
    public boolean match(final HintTrafficValue hintTrafficValue) {
        return hintTrafficValue.getSqlHintProps().getValue(SQLHintPropertiesKey.USE_TRAFFIC);
    }
    
    @Override
    public String getType() {
        return "SQL_HINT";
    }
}
