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
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;

import java.util.Properties;

/**
 * Customized encrypt algorithm.
 */
@Getter
@Setter
public final class CustomizedEncryptAlgorithm implements EncryptAlgorithm<Integer, Integer>, SchemaMetaDataAware {
    
    private static final String TEST_KEY = "TEST";
    
    private Properties props = new Properties();
    
    private byte[] key = DigestUtils.sha256(TEST_KEY);
    
    private ShardingSphereSchema schema;
    
    @Override
    public void init() {
    }
    
    @Override
    public Integer encrypt(final Integer plainValue) {
        byte[] bytes = toBytes(plainValue);
        for (int index = 0; index < 32; index++) {
            bytes[index % 4] = (byte) (key[index] ^ bytes[index % 4]);
        }
        return toInt(bytes);
    }
    
    @Override
    public Integer decrypt(final Integer cipherValue) {
        byte[] bytes = toBytes(cipherValue);
        for (int index = 0; index < 32; index++) {
            bytes[index % 4] = (byte) (key[index] ^ bytes[index % 4]);
        }
        return toInt(bytes);
    }
    
    @Override
    public String getType() {
        return "CUSTOMIZED";
    }
    
    private int toInt(final byte[] bytes) {
        int result = 0;
        for (int index = 0; index < 4; index++) {
            result <<= 8;
            result |= bytes[index] & 0xff;
        }
        return result;
    }
    
    private byte[] toBytes(final int intValue) {
        byte[] result = new byte[4];
        result[0] = (byte) (intValue >>> 24);
        result[1] = (byte) (intValue >>> 16);
        result[2] = (byte) (intValue >>> 8);
        result[3] = (byte) intValue;
        return result;
    }
    
    @Override
    public void setSchema(final ShardingSphereSchema schema) {
        this.schema = schema;
    }
}
