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

package org.apache.shardingsphere.agent.api.advice;

/**
 * Wrapped class for target and provide a context to store variable during invocation.
 */
public interface AdviceTargetObject {
    
    /**
     * Get the variable from context.
     *
     * @return the attachment
     */
    Object getAttachment();
    
    /**
     * Store a variable into context.
     *
     * @param attachment what variable you want to store
     */
    void setAttachment(Object attachment);
}
