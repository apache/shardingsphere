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

package org.apache.shardingsphere.infra.metadata.statistics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Row statistics.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class RowStatistics {
    
    @EqualsAndHashCode.Include
    private final String uniqueKey;
    
    private final List<Object> rows;
    
    public RowStatistics(final List<Object> rows) {
        uniqueKey = generateUniqueKey(rows);
        this.rows = rows;
    }
    
    private String generateUniqueKey(final List<Object> rows) {
        StringBuilder uniqueKeyText = new StringBuilder();
        for (Object each : rows) {
            if (null != each) {
                uniqueKeyText.append(each);
            }
            uniqueKeyText.append('|');
        }
        return useMd5GenerateUniqueKey(uniqueKeyText);
    }
    
    @SneakyThrows(NoSuchAlgorithmException.class)
    private String useMd5GenerateUniqueKey(final StringBuilder uniqueKeyText) {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(StandardCharsets.UTF_8.encode(uniqueKeyText.toString()));
        return String.format("%032x", new BigInteger(1, md5.digest()));
    }
}
