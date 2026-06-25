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

package org.apache.shardingsphere.sqlfederation.compiler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.adapter.enumerable.EnumerableInterpretable;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRel.Prefer;
import org.apache.calcite.jdbc.CalcitePrepare.SparkHandler;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.runtime.Bindable;

import java.util.Map;

/**
 * SQL federation execution plan.
 */
@RequiredArgsConstructor
@Getter
public final class SQLFederationExecutionPlan {
    
    private final RelNode physicalPlan;
    
    private final RelDataType resultColumnType;
    
    /**
     * Create bindable execution plan.
     *
     * @param physicalPlan physical plan
     * @param internalParameters internal parameters
     * @param spark spark handler
     * @param prefer preferred Java row format
     * @return bindable execution plan
     */
    @SuppressWarnings("unchecked")
    public static synchronized Bindable<Object> toBindable(final RelNode physicalPlan, final Map<String, Object> internalParameters, final SparkHandler spark, final Prefer prefer) {
        return EnumerableInterpretable.toBindable(internalParameters, spark, (EnumerableRel) physicalPlan, prefer);
    }
}
