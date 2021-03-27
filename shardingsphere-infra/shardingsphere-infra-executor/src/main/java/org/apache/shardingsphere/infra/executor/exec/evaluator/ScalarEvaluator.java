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
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.func.EvalBuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

public final class ScalarEvaluator extends AbstractEvaluator implements Evaluator {
    
    private Evaluator[] evaluatorArgs;
    
    private EvalBuiltinFunction function;
    
    protected ScalarEvaluator(final Evaluator[] evaluatorArgs, final BuiltinFunction function, final RelDataType retType) {
        super(retType);
        this.evaluatorArgs = evaluatorArgs;
        this.function = (EvalBuiltinFunction) function;
    }
    
    @Override
    public Object eval(final Row row) {
        Object[] args = new Object[evaluatorArgs.length];
        for (int i = 0; i < evaluatorArgs.length; i++) {
            Evaluator evaluator = evaluatorArgs[i];
            args[i] = evaluator.eval(row);
        }
        return function.apply(args);
    }
}
