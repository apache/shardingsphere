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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Operation;
import org.apache.hadoop.hbase.client.Put;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * HBase database insert operation adapter.
 */
@RequiredArgsConstructor
@Getter
public final class HBaseInsertOperationAdapter extends Operation {
    
    private final List<Put> puts;
    
    @Override
    public Map<String, Object> getFingerprint() {
        return new TreeMap<>();
    }
    
    @Override
    public Map<String, Object> toMap(final int i) {
        return new TreeMap<>();
    }
}
