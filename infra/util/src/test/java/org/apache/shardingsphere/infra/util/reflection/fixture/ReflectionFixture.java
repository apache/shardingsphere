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

package org.apache.shardingsphere.infra.util.reflection.fixture;

import lombok.Getter;

@Getter
public final class ReflectionFixture {
    
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    @Getter
    private static String staticValue = "static_value";
    
    private final String instanceValue = "instance_value";
    
    /**
     * Get contact value.
     *
     * @param val1 value 1
     * @param val2 value 2
     * @return contact value
     */
    public String getContactValue(final String val1, final String val2) {
        return String.join("_", val1, val2);
    }
    
}
