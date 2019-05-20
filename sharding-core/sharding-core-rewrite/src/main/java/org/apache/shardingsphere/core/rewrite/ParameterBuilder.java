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

package org.apache.shardingsphere.core.rewrite;

import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResult;
import org.apache.shardingsphere.core.optimize.result.insert.InsertOptimizeResultUnit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Parameter builder.
 *
 * @author panjuan
 */
public final class ParameterBuilder {
    
    private final List<Object> originalParameters;
    
    private final Map<Integer, Object> assistedIndexAndParametersForUpdate;
    
    private final List<InsertParameterUnit> insertParameterUnits;
    
    public ParameterBuilder(final List<Object> parameters, final InsertOptimizeResult insertOptimizeResult) {
        originalParameters = parameters;
        assistedIndexAndParametersForUpdate = new HashMap<>();
        insertParameterUnits = createInsertParameterUnits(insertOptimizeResult);
    }
    
    private List<InsertParameterUnit> createInsertParameterUnits(final InsertOptimizeResult insertOptimizeResult) {
        List<InsertParameterUnit> result = new LinkedList<>();
        if (null == insertOptimizeResult) {
            return result;
        }
        for (InsertOptimizeResultUnit each : insertOptimizeResult.getUnits()) {
            result.add(new InsertParameterUnit(Arrays.asList(each.getParameters()), each.getDataNodes()));
        }
        return result;
    }
}
