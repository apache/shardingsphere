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

package org.apache.shardingsphere.example.engine;

import lombok.AllArgsConstructor;

/**
 * Framework type.
 */
@AllArgsConstructor
public enum FrameworkType {
    JDBC("jdbc"),
    SPRING_BOOT_STARTER_JDBC("spring-boot-starter-jdbc"),
    SPRING_BOOT_STARTER_JPA("spring-boot-starter-jpa"),
    SPRING_BOOT_STARTER_MYBATIS("spring-boot-starter-mybatis"),
    SPRING_BOOT_NAMESPACE_JDBC("spring-namespace-jdbc"),
    SPRING_BOOT_NAMESPACE_JPA("spring-namespace-jpa"),
    SPRING_BOOT_NAMESPACE_MYBATIS("spring-namespace-mybatis");
    
    private final String framework;
    
    public String getFramework() {
        return framework;
    }
}
