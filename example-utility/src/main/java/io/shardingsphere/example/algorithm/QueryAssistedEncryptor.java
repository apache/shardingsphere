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

package io.shardingsphere.example.algorithm;

import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Query assisted encryptor.
 *
 * @author panjuan
 */
public final class QueryAssistedEncryptor implements ShardingQueryAssistedEncryptor {
    
    private final AtomicInteger count = new AtomicInteger();
    
    private Properties properties;
    
    @Override
    public String queryAssistedEncrypt(final String plaintext) {
        return "query_encrypt";
    }
    
    @Override
    public String encrypt(final Object plaintext) {
        return String.format("encrypt_%s", count.incrementAndGet());
    }
    
    @Override
    public Object decrypt(final String ciphertext) {
        return "decrypt";
    }
    
    @Override
    public String getType() {
        return "query";
    }
    
    @Override
    public Properties getProperties() {
        return properties;
    }
    
    @Override
    public void setProperties(final Properties properties) {
        this.properties = properties;
    }
}
