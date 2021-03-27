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

package org.apache.shardingsphere.infra.executor.exec.evaluator;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

public class ConstantEvaluator extends AbstractEvaluator {
    
    private Object value;
    
    private Class<?> clazz;
    
    protected ConstantEvaluator(final Object value, final RelDataType retType) {
        super(retType);
        this.value = value;
    }
    
    @Override
    public final <T> T eval(final Row row) {
        return (T) value;
    }
}
