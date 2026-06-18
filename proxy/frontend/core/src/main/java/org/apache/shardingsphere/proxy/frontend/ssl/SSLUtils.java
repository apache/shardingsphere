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

package org.apache.shardingsphere.proxy.frontend.ssl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * SSL utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SSLUtils {
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    /**
     * Generate a 4096 RSA key pair.
     *
     * @return RSA key pair
     */
    @SneakyThrows({NoSuchProviderException.class, NoSuchAlgorithmException.class})
    public static KeyPair generateRSAKeyPair() {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        keyPairGenerator.initialize(4096, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }
    
    /**
     * Generate a self-signed X.509 certificate with provided key pair.
     *
     * @param keyPair key pair
     * @return self-signed X.509 certificate
     */
    @SneakyThrows({OperatorCreationException.class, CertificateException.class})
    public static X509Certificate generateSelfSignedX509Certificate(final KeyPair keyPair) {
        long now = System.currentTimeMillis();
        Date startDate = Date.from(LocalDateTime.ofInstant(Instant.ofEpochMilli(now - TimeUnit.DAYS.toMillis(1L)), ZoneId.systemDefault()).atZone(ZoneId.systemDefault()).toInstant());
        X500Name dnName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "").addRDN(BCStyle.OU, "").addRDN(BCStyle.O, "").addRDN(BCStyle.L, "").addRDN(BCStyle.ST, "").addRDN(BCStyle.C, "").addRDN(BCStyle.E, "").build();
        BigInteger certSerialNumber = new BigInteger(Long.toString(now));
        Date endDate = Date.from(
                LocalDateTime.ofInstant(Instant.ofEpochMilli(startDate.getTime() + TimeUnit.DAYS.toMillis(365L * 100L)), ZoneId.systemDefault()).atZone(ZoneId.systemDefault()).toInstant());
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic()).build(contentSigner));
    }
}
