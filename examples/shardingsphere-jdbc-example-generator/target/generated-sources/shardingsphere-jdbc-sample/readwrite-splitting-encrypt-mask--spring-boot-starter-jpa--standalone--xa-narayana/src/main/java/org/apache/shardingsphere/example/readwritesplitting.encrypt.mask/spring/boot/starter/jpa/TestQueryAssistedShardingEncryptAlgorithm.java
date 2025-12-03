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

package org.apache.shardingsphere.example.readwritesplitting.encrypt.mask.spring.boot.starter.jpa;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;

import java.util.Properties;

@SuppressWarnings("LombokGetterMayBeUsed")
public final class TestQueryAssistedShardingEncryptAlgorithm implements EncryptAlgorithm {
    
    @Getter
    private Properties properties;

    @Getter
    private EncryptAlgorithmMetaData metaData;

    @Override
    public void init(final Properties props) {
        this.properties = props;
        EncryptAlgorithmMetaData algorithmMetaData = new EncryptAlgorithmMetaData(false, true, false);
        metaData = algorithmMetaData;
    }
    
    @Override
    public String encrypt(final Object plainValue, final AlgorithmSQLContext algorithmSQLContext) {
        return "assistedEncryptValue";
    }

    @Override
    public Object decrypt(final Object cipherValue, final AlgorithmSQLContext algorithmSQLContext) {
        throw new UnsupportedOperationException(String.format("Algorithm `%s` is unsupported to decrypt", getType()));
    }
    
    @Override
    public String getType() {
        return "assistedTest";
    }
}
