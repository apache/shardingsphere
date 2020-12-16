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

package org.apache.shardingsphere.transaction.core;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Transaction manager type.
 */
@RequiredArgsConstructor
@Getter
public enum TransactionManagerType {
    
    ATOMIKOS("Atomikos"),
    
    NARAYANA("Narayana"),
    
    BITRONIX("Bitronix"),
    
    SEATA("Seata");
    
    private final String type;
    
    /**
     * Value from transaction manager type.
     *
     * @param type value to be transaction manager type
     * @return value from transaction manager type
     */
    public static TransactionManagerType valueFrom(final String type) {
        return Arrays.stream(values()).filter(each -> each.type.equalsIgnoreCase(type)).findFirst().orElse(TransactionManagerType.ATOMIKOS);
    }
}
