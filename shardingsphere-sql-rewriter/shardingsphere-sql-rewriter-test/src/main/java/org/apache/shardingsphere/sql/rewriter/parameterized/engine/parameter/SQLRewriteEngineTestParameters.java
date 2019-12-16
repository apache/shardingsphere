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

package org.apache.shardingsphere.sql.rewriter.parameterized.engine.parameter;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Test parameters for SQL rewrite engine.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLRewriteEngineTestParameters {
    
    private final String fileName;
    
    private final String ruleFile;
    
    private final String name;
    
    private final String inputSQL;
    
    private final List<Object> inputParameters;
    
    private final List<String> outputSQLs;
    
    private final List<List<String>> outputGroupedParameters;
    
    private final String databaseType;
    
    /**
     * To array.
     * 
     * @return array value of test parameters
     */
    public Object[] toArray() {
        Object[] result = new Object[8];
        result[0] = fileName;
        result[1] = ruleFile;
        result[2] = name;
        result[3] = inputSQL;
        result[4] = inputParameters;
        result[5] = outputSQLs;
        result[6] = outputGroupedParameters;
        result[7] = databaseType;
        return result;
    }
}
