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

package org.apache.shardingsphere.infra.executor.exec.func.binary;

import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.EvalBuiltinFunction;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class BinaryBuiltinFunction<T, R> implements EvalBuiltinFunction<T, R> {
    
    @Override
    public final R apply(final T[] args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException();
        }
        // TODO handle null values with nullPolicy if possible
        return apply(args[0], args[1]);
    }
    
    /**
     * parameters should not be null. 
     * @param t1 the first operand
     * @param t2 the second operand
     * @return the result of the function.
     */
    public abstract R apply(T t1, T t2);
    
    /**
     * Default implementation of {@link BuiltinFunction#getArgTypeNames()}.
     * @return A list of type names
     */
    @Override
    public List<String[]> getArgTypeNames() {
        String typeName = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
        List<String[]> argTypeNames = new ArrayList<>();
        argTypeNames.add(new String[]{typeName, typeName});
        return argTypeNames;
    }
}
