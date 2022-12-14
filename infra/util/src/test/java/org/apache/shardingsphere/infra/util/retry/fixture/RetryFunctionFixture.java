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

package org.apache.shardingsphere.infra.util.retry.fixture;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class RetryFunctionFixture {
    
    private long target;
    
    private long step;
    
    private final Function<? super Long, Boolean> add = arg -> {
        step += arg;
        return step > target;
    };
    
    private final BiFunction<? super Long, ? super Long, Boolean> multi = (arg1, arg2) -> {
        step += arg1 * arg2;
        return step > target;
    };
    
    public RetryFunctionFixture(final long target) {
        this.target = target;
    }
    
    /**
     * Function of add.
     *
     * @return execute Function
     */
    public Function<? super Long, Boolean> moveAdd() {
        return add;
    }
    
    /**
     * BiFunction of multi.
     *
     * @return execute BiFunction
     */
    public BiFunction<? super Long, ? super Long, Boolean> moveMulti() {
        return multi;
    }
}
