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

import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class SSLUtilsTest {
    
    @Test
    void assertGenerateKeyPair() {
        KeyPair actual = SSLUtils.generateRSAKeyPair();
        assertThat(actual.getPrivate().getAlgorithm(), is("RSA"));
        assertThat(actual.getPrivate().getFormat(), is("PKCS#8"));
        assertThat(actual.getPublic().getAlgorithm(), is("RSA"));
        assertThat(actual.getPublic().getFormat(), is("X.509"));
    }
    
    @Test
    void assertGenerateSelfSignedX509Certificate() {
        KeyPair keyPair = SSLUtils.generateRSAKeyPair();
        X509Certificate actual = SSLUtils.generateSelfSignedX509Certificate(keyPair);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, 99);
        try {
            actual.checkValidity(new Date());
            actual.checkValidity(calendar.getTime());
        } catch (CertificateExpiredException | CertificateNotYetValidException ex) {
            fail(ex);
        }
        try {
            actual.verify(keyPair.getPublic());
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex) {
            fail(ex);
        }
        assertThrows(SignatureException.class, () -> actual.verify(SSLUtils.generateRSAKeyPair().getPublic()));
    }
}
