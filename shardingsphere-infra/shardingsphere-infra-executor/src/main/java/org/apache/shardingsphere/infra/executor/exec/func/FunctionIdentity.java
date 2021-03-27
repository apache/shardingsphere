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

package org.apache.shardingsphere.infra.executor.exec.func;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class FunctionIdentity {
    
    private String funcName;
    
    private List<String> argTypeNames;
    
    public FunctionIdentity(final String funcName, final List<String> argTypeNames) {
        this.funcName = funcName;
        this.argTypeNames = argTypeNames;
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FunctionIdentity that = (FunctionIdentity) o;
        return Objects.equals(funcName, that.funcName) && Objects.equals(argTypeNames, that.argTypeNames);
    }
    
    @Override
    public final int hashCode() {
        return Objects.hash(funcName, argTypeNames);
    }
}
