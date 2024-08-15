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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.dumper.type.impl;

import org.apache.shardingsphere.data.pipeline.mysql.ingest.dumper.type.MySQLBinlogDataTypeHandler;

import java.io.Serializable;

/**
 * MySQL binlog unsigned int data type handler.
 */
public final class MySQLBinlogUnsignedIntHandler implements MySQLBinlogDataTypeHandler {
    
    private static final long INT_MODULO = 4294967296L;
    
    @Override
    public Serializable handle(final Serializable value) {
        if (null == value) {
            return null;
        }
        int intValue = (int) value;
        return intValue < 0 ? INT_MODULO + intValue : intValue;
    }
    
    @Override
    public String getType() {
        return "INT UNSIGNED";
    }
}
