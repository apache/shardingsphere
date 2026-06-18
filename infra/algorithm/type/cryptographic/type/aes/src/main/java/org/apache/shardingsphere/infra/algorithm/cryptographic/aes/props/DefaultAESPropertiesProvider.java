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

package org.apache.shardingsphere.infra.algorithm.cryptographic.aes.props;

import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicPropertiesProvider;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

/**
 * Provider of default AES cryptographic properties.
 */
public final class DefaultAESPropertiesProvider implements CryptographicPropertiesProvider {
    
    private static final String AES_KEY = "aes-key-value";
    
    private static final String DIGEST_ALGORITHM_NAME = "digest-algorithm-name";
    
    @Getter
    private byte[] secretKey;
    
    @Getter
    private String mode;
    
    @Getter
    private String padding;
    
    @Getter
    private byte[] ivParameter;
    
    @Getter
    private String encoder;
    
    @Override
    public void init(final Properties props) {
        secretKey = getSecretKey(props);
        mode = "";
        padding = "";
        ivParameter = new byte[0];
        encoder = "BASE64";
    }
    
    private byte[] getSecretKey(final Properties props) {
        String aesKey = props.getProperty(AES_KEY);
        ShardingSpherePreconditions.checkNotEmpty(aesKey, () -> new AlgorithmInitializationException(this, "%s can not be null or empty", AES_KEY));
        String digestAlgorithm = props.getProperty(DIGEST_ALGORITHM_NAME);
        ShardingSpherePreconditions.checkNotEmpty(digestAlgorithm, () -> new AlgorithmInitializationException(this, "%s can not be null or empty", DIGEST_ALGORITHM_NAME));
        return Arrays.copyOf(DigestUtils.getDigest(digestAlgorithm.toUpperCase()).digest(aesKey.getBytes(StandardCharsets.UTF_8)), 16);
    }
    
    @Override
    public String getType() {
        return "DEFAULT";
    }
}
