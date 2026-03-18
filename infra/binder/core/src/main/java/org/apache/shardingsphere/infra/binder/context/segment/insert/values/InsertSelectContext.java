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

package org.apache.shardingsphere.infra.binder.context.segment.insert.values;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Insert select context.
 */
@Getter
@ToString
public final class InsertSelectContext {
    
    private final List<Object> parameters;
    
    private final SelectStatementContext selectStatementContext;
    
    public InsertSelectContext(final SelectStatementContext selectStatementContext, final List<Object> params, final int parametersOffset) {
        this.selectStatementContext = selectStatementContext;
        parameters = getParameters(params, parametersOffset);
    }
    
    private List<Object> getParameters(final List<Object> params, final int parametersOffset) {
        int parameterCount = selectStatementContext.getSqlStatement().getParameterCount();
        if (params.isEmpty() || 0 == parameterCount) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(parameterCount);
        result.addAll(params.subList(parametersOffset, parametersOffset + parameterCount));
        return result;
    }
}
