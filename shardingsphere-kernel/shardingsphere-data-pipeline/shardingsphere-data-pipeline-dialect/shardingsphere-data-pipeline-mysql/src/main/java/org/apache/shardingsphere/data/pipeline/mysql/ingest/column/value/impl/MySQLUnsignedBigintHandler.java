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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.impl;

import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.MySQLDataTypeHandler;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * MySQL unsigned bigint handler.
 */
public final class MySQLUnsignedBigintHandler implements MySQLDataTypeHandler {
    
    private static final BigInteger BIGINT_MODULO = new BigInteger("18446744073709551616");
    
    @Override
    public Serializable handle(final Serializable value) {
        if (null == value) {
            return null;
        }
        long longValue = (long) value;
        return 0 > longValue ? BIGINT_MODULO.add(BigInteger.valueOf(longValue)) : longValue;
    }
    
    @Override
    public String getType() {
        return "BIGINT UNSIGNED";
    }
}
