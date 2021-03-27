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

package org.apache.shardingsphere.infra.executor.exec.func.implementor;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;

/**
 * Interface to convert Calcite functions call(e.g. {@link org.apache.calcite.rex.RexCall},
 * {@link org.apache.calcite.rel.core.AggregateCall}) to {@link BuiltinFunction} 
 * @param <T> Calcite function call
 * @param <R> implemented BuiltinFunction
 */
public interface RexCallToFunctionImplementor<T, R extends BuiltinFunction> {
    
    /**
     * Implement the sub-class of BuiltinFunction from parameter rexCall.
     * @param call to be implemented
     * @param argTypes argument type for <code>BuiltinFunction</code>  
     * @return sub-class of <code>BuiltinFunction</code> instance
     */
    R implement(T call, RelDataType[] argTypes);
}
