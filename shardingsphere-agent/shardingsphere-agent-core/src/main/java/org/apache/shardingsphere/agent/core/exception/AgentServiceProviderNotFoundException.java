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

package org.apache.shardingsphere.agent.core.exception;

/**
 * Agent service provider not found exception.
 */
public final class AgentServiceProviderNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = -3730257541332863235L;
    
    public AgentServiceProviderNotFoundException(final Class<?> clazz) {
        super(String.format("No implementation class load from SPI `%s`.", clazz.getName()));
    }
    
    public AgentServiceProviderNotFoundException(final Class<?> clazz, final String type) {
        super(String.format("No implementation class load from SPI `%s` with type `%s`.", clazz.getName(), type));
    }
}
