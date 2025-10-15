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

package org.apache.shardingsphere.encrypt.sm.algorithm;

import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.FPEForSM4;
import org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util.FPEConstants;
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.bouncycastle.crypto.util.BasicAlphabetMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Properties;

/**
 * TDE (Transparent Data Encryption) encrypt algorithm implementation using Format Preserving Encryption with SM4.
 * This implementation supports multiple character types including numbers, English letters, and Chinese characters.
 * The algorithm preserves the original format of the data while providing encryption based on SM4.
 */
public final class TDEEncryptAlgorithm implements StandardEncryptAlgorithm<Object, Object> {
    
    private static final String FPE_KEY = "fpe-key-value";
    private static final String FPE_NUMBER_MAPPER = "fpe-number-mapper";
    private FPEForSM4 fpe;
    
    public TDEEncryptAlgorithm() {
    }
    
    public void init(Properties props) {
        this.fpe = this.createFPE(props);
    }
    
    private FPEForSM4 createFPE(Properties props) {
        byte[] keyBytes = props.getProperty("fpe-key-value").getBytes(StandardCharsets.UTF_8);
        String mapperType = props.getProperty("fpe-number-mapper");
        String mapper = this.getNumberMapping(mapperType);
        BasicAlphabetMapper numberMapper = new BasicAlphabetMapper(mapper);
        FPEForSM4 fpe = new FPEForSM4(FPEForSM4.FPEMode.FF3_1, keyBytes, numberMapper, (byte[]) null, mapper);
        return fpe;
    }
    
    public String encrypt(Object plainValue, EncryptContext encryptContext) {
        return null == plainValue ? null : this.fpe.encrypt(String.valueOf(plainValue));
    }
    
    public Object decrypt(Object cipherValue, EncryptContext encryptContext) {
        return null == cipherValue ? null : this.fpe.decrypt(String.valueOf(cipherValue));
    }
    
    public String getType() {
        return "FPE";
    }
    
    private String getNumberMapping(String encryptRuleNumberMapping) {
        String[] split = encryptRuleNumberMapping.split(",");
        StringBuffer sb = new StringBuffer();
        
        for (String s : split) {
            sb.append(FPEConstants.FPEEncryptCharacterSet.getFPEEncryptCharacterSet(Integer.valueOf(s)));
        }
        
        return sb.toString();
    }
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}