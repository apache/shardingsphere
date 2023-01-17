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

public final class ReflectionFixture {
    
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private static String staticValue = "foo";
    
    private String fooField = "foo_value";
    
    private String barField = "bar_value";
    
    /**
     * Get static value.
     * 
     * @return static value
     */
    public static String getStaticValue() {
        return staticValue;
    }
    
    /**
     * Get fooField value.
     *
     * @return fooField value
     */
    public String getFooField() {
        return fooField;
    }
    
    /**
     * Get barField value.
     *
     * @return barField value
     */
    public String getBarField() {
        return barField;
    }
    
    /**
     * Get contact value.
     *
     * @param val1 not null
     * @param val2 not null
     * @return contact value
     */
    public String getContactValue(final String val1, final String val2) {
        return String.join("_", val1, val2);
    }
    
}
