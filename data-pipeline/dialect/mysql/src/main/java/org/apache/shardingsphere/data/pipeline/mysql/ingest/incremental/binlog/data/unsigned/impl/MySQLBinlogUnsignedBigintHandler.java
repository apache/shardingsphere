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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.unsigned.impl;

import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.unsigned.MySQLBinlogUnsignedNumberHandler;

import java.math.BigInteger;

/**
 * MySQL binlog unsigned bigint handler.
 */
public final class MySQLBinlogUnsignedBigintHandler implements MySQLBinlogUnsignedNumberHandler<Long> {
    
    private static final BigInteger BIGINT_MODULO = new BigInteger("18446744073709551616");
    
    @Override
    public Number handle(final Long value) {
        return value < 0L ? BIGINT_MODULO.add(BigInteger.valueOf(value)) : BigInteger.valueOf(value);
    }
}
