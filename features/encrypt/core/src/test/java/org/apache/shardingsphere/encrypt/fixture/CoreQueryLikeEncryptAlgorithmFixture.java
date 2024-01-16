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

package org.apache.shardingsphere.encrypt.fixture;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;

import java.util.Properties;

@Getter
public final class CoreQueryLikeEncryptAlgorithmFixture implements EncryptAlgorithm {
    
    private EncryptAlgorithmMetaData metaData;
    
    @Override
    public void init(final Properties props) {
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData();
        encryptAlgorithmMetaData.setSupportLike(true);
        encryptAlgorithmMetaData.setSupportDecrypt(false);
        encryptAlgorithmMetaData.setSupportEquivalentFilter(false);
        metaData = encryptAlgorithmMetaData;
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        return "likeEncryptValue";
    }
    
    @Override
    public Object decrypt(final Object cipherValue, final EncryptContext encryptContext) {
        throw new UnsupportedOperationException(String.format("Algorithm `%s` is unsupported to decrypt", getType()));
    }
    
    @Override
    public String getType() {
        return "CORE.QUERY_LIKE.FIXTURE";
    }
}
