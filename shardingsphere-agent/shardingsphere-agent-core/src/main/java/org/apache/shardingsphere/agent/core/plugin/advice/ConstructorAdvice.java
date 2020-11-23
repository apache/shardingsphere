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
 *
 */

package org.apache.shardingsphere.agent.core.plugin.advice;

/**
 * Weaving the advice around the constructor of target class.
 */
public interface ConstructorAdvice {

    /**
     * Intercept the target's constructor. This method is weaved after the constructor execution.
     *
     * @param target intercepted target object
     * @param args all arguments of the intercepted constructor
     */
    void onConstructor(TargetObject target, Object[] args);
}
