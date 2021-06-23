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

package org.apache.shardingsphere.scaling.mysql.component.column.value;

import java.io.Serializable;

public final class UnsignedTinyintHandler implements ValueHandler {
    
    private static final int TINYINT_MODULO = 256;
    
    @Override
    public String getTypeName() {
        return "TINYINT UNSIGNED";
    }
    
    @Override
    public Serializable handle(final Serializable value) {
        byte byteValue = (byte) value;
        return 0 > byteValue ? TINYINT_MODULO + byteValue : byteValue;
    }
}
