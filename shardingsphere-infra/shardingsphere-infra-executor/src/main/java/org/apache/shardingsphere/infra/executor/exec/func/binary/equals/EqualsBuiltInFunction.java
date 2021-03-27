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

package org.apache.shardingsphere.infra.executor.exec.func.binary.equals;

import org.apache.calcite.sql.SqlKind;
import org.apache.shardingsphere.infra.executor.exec.func.binary.BinaryBuiltinFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EqualsBuiltInFunction extends BinaryBuiltinFunction<Object, Boolean> {
    
    public static final EqualsBuiltInFunction INSTANCE = new EqualsBuiltInFunction();
    
    protected EqualsBuiltInFunction() {
        
    }
    
    @Override
    public String getFunctionName() {
        return SqlKind.EQUALS.name();
    }
    
    @Override
    public Boolean apply(final Object t1, final Object t2) {
        return Objects.equals(t1, t2);
    }
    
    @Override
    public List<String[]> getArgTypeNames() {
        List<String[]> argTypeNames = new ArrayList<>();
        argTypeNames.add(new String[]{"int", "int"});
        argTypeNames.add(new String[]{"long", "long"});
        argTypeNames.add(new String[]{"java.lang.Long", "java.lang.Long"});
        argTypeNames.add(new String[]{"java.lang.Integer", "java.lang.Integer"});
        argTypeNames.add(new String[]{"java.lang.String", "java.lang.String"});
        argTypeNames.add(new String[]{"java.lang.Object", "java.lang.Object"});
        return argTypeNames;
    }
}
