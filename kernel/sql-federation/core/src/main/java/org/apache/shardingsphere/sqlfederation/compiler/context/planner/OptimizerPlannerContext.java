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

package org.apache.shardingsphere.sqlfederation.compiler.context.planner;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;

import java.util.Map;

/**
 * Optimize planner context.
 */
@RequiredArgsConstructor
public final class OptimizerPlannerContext {
    
    private final Map<String, SqlValidator> validators;
    
    private final Map<String, SqlToRelConverter> converters;
    
    /**
     * Get validator.
     * 
     * @param schemaName schema name
     * @return validator
     */
    public SqlValidator getValidator(final String schemaName) {
        return validators.get(schemaName.toLowerCase());
    }
    
    /**
     * Get converter.
     *
     * @param schemaName schema name
     * @return converter
     */
    public SqlToRelConverter getConverter(final String schemaName) {
        return converters.get(schemaName.toLowerCase());
    }
}
