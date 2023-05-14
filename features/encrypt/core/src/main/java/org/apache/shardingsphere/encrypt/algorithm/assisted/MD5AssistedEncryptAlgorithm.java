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

package org.apache.shardingsphere.encrypt.algorithm.assisted;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.api.encrypt.assisted.AssistedEncryptAlgorithm;

import java.util.Properties;

/**
 * MD5 assisted encrypt algorithm.
 */
public final class MD5AssistedEncryptAlgorithm implements AssistedEncryptAlgorithm<Object, String> {
    
    private static final String SALT_KEY = "salt";
    
    private String salt;
    
    @Override
    public void init(final Properties props) {
        this.salt = props.getProperty(SALT_KEY, "");
    }
    
    @Override
    public String encrypt(final Object plainValue, final EncryptContext encryptContext) {
        return null == plainValue ? null : DigestUtils.md5Hex(plainValue + salt);
    }
    
    @Override
    public String getType() {
        return "MD5";
    }
}
