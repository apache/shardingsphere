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

package org.apache.shardingsphere.db.protocol.firebird.packet.handshake;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.firebird.exception.FirebirdProtocolException;
import org.firebirdsql.util.ByteArrayHelper;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;

/**
 * SRP authentication plugin data for Firebird.
 * Basically a copy of SrpClient implementation from Jaybird JDBC driver but for server side.
 *
 * @see <a href="http://srp.stanford.edu/design.html">SRP Protocol Design</a>
 * @see <a href="https://github.com/FirebirdSQL/jaybird/blob/master/src/main/org/firebirdsql/gds/ng/wire/auth/srp/SrpClient.java">Jaybird implementation</a>
 */
@Getter
public final class FirebirdSRPAuthenticationData {

    private static final int SRP_KEY_SIZE = 128;
    private static final int SRP_SALT_SIZE = 32;
    private static final int EXPECTED_AUTH_DATA_LENGTH = (SRP_SALT_SIZE + SRP_KEY_SIZE + 2) * 2;

    private static final BigInteger N = new BigInteger("E67D2E994B2F900C3F41F08F5BB2627ED0D49EE1FE767A52EFCD565CD6E768812C3E1E9CE8F0A8BEA6CB13CD29DDEBF7A96D4A93B55D488DF099A15C89DCB0640738EB2CBDD9A8F7BAB561AB1B0DC1C6CDABF303264A08D1BCA932D1F1EE428B619D970F342ABA9A65793B8B2F041AE5364350C16F735F56ECBCA87BD57B29E7", 16);
    private static final BigInteger g = new BigInteger("2");
    private static final BigInteger k = new BigInteger("1277432915985975349439481660349303019122249719989");

    private static final SecureRandom random = new SecureRandom();
    private static final byte SEPARATOR_BYTE = (byte) ':';

    private final MessageDigest sha1Md;
    private final String clientProofHashAlgorithm;
    private final BigInteger clientPublicKey; /* A */
    private final BigInteger publicKey;     /* B */
    private final BigInteger privateKey;    /* b */
    private final byte[] salt;
    private final BigInteger verifier;      /* v */
    private byte[] sessionKey;              /* K */

    @SneakyThrows
    public FirebirdSRPAuthenticationData(String hashAlgorithm, String username, String password, String userPublicKey) {
        sha1Md = MessageDigest.getInstance("SHA-1");
        clientProofHashAlgorithm = hashAlgorithm;
        clientPublicKey = new BigInteger(padHexBinary(userPublicKey), 16);
        privateKey = generateSecret();
        salt = generateSalt();
        verifier = g.modPow(getUserHash(normalizeLogin(username), password, salt), N);
        publicKey = getPublic();
    }

    private static BigInteger fromBigByteArray(byte[] b) {
        return new BigInteger(1, b);
    }

    private static byte[] toBigByteArray(BigInteger n) {
        return stripLeadingZeroes(n.toByteArray());
    }

    private static byte[] stripLeadingZeroes(byte[] b) {
        if (b[0] != 0) {
            return b;
        }
        int i = 1;
        while (b[i] == 0) {
            i++;
        }
        return Arrays.copyOfRange(b, i, b.length);
    }

    private static String padHexBinary(String hexString) {
        if (hexString.length() % 2 != 0) {
            return '0' + hexString;
        }
        return hexString;
    }

    private byte[] sha1(byte[] bytes) {
        try {
            return sha1Md.digest(bytes);
        } finally {
            sha1Md.reset();
        }
    }

    private byte[] sha1(byte[] bytes1, byte[] bytes2) {
        try {
            sha1Md.update(bytes1);
            return sha1Md.digest(bytes2);
        } finally {
            sha1Md.reset();
        }
    }

    private static byte[] pad(BigInteger n) {
        final byte[] bn = toBigByteArray(n);
        if (bn.length > SRP_KEY_SIZE) {
            return Arrays.copyOfRange(bn, bn.length - SRP_KEY_SIZE, bn.length);
        }
        return bn;
    }

    private BigInteger getScramble(BigInteger x, BigInteger y) {
        return fromBigByteArray(sha1(pad(x), pad(y)));
    }

    private static BigInteger generateSecret() {
        return new BigInteger(SRP_KEY_SIZE, random);
    }

    private static byte[] generateSalt() {
        byte[] b = new byte[SRP_SALT_SIZE];
        random.nextBytes(b);
        return b;
    }

    private BigInteger getPublic() {
        final BigInteger gb = g.modPow(privateKey, N);
        final BigInteger kv = k.multiply(verifier).mod(N);
        return kv.add(gb).mod(N);
    }

    private BigInteger getUserHash(String user, String password, byte[] salt) {
        final byte[] hash1;
        try {
            sha1Md.update(user.getBytes(StandardCharsets.UTF_8));
            sha1Md.update(SEPARATOR_BYTE);
            hash1 = sha1Md.digest(password.getBytes(StandardCharsets.UTF_8));
        } finally {
            sha1Md.reset();
        }
        final byte[] hash2 = sha1(salt, hash1);
        return fromBigByteArray(hash2);
    }

    private byte[] getServerSessionKey() {
        final BigInteger u = getScramble(clientPublicKey, publicKey);
        final BigInteger vu = verifier.modPow(u, N);
        final BigInteger Avu = clientPublicKey.multiply(vu).mod(N);
        final BigInteger sessionSecret = Avu.modPow(privateKey, N);
        return sha1(toBigByteArray(sessionSecret));
    }

    public byte[] serverProof(String user) {
        final byte[] K = getServerSessionKey();
        final BigInteger n1 = fromBigByteArray(sha1(toBigByteArray(N)));
        final BigInteger n2 = fromBigByteArray(sha1(toBigByteArray(g)));
        final byte[] M = clientProofHash(
                toBigByteArray(n1.modPow(n2, N)),
                stripLeadingZeroes(sha1(normalizeLogin(user).getBytes(StandardCharsets.UTF_8))),
                salt,
                toBigByteArray(clientPublicKey),
                toBigByteArray(publicKey),
                K);

        sessionKey = K;
        return M;
    }

    public String getPublicKeyHex() {
        return ByteArrayHelper.toHexString(pad(publicKey));
    }

    private byte[] clientProofHash(byte[]... ba) throws FirebirdProtocolException {
        try {
            MessageDigest md = MessageDigest.getInstance(clientProofHashAlgorithm);
            for (byte[] b : ba) {
                md.update(b);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new FirebirdProtocolException("Unrecognised hash algorithm `%s`.", clientProofHashAlgorithm);
        }
    }

    /**
     * Normalizes a login by uppercasing unquoted usernames, or stripping and unescaping (double) quoted user names.
     *
     * @param login login to process
     * @return normalized login
     */
    static String normalizeLogin(String login) {
        if (login == null || login.isEmpty()) {
            return login;
        }
        // Contrary to Firebird, check if login is enclosed in double quotes, not just starting with a double quote
        if (login.length() > 2 && login.charAt(0) == '"' && login.charAt(login.length() - 1) == '"') {
            return normalizeQuotedLogin(login);
        }
        return login.toUpperCase(Locale.ROOT);
    }

    private static String normalizeQuotedLogin(String login) {
        final StringBuilder sb = new StringBuilder(login.length() - 2);
        sb.append(login, 1, login.length() - 1);

        for (int idx = 0; idx < sb.length(); idx++) {
            // Double double quotes ("") escape a double quote in a quoted string
            if (sb.charAt(idx) == '"') {
                // Strip double quote escape
                sb.deleteCharAt(idx);
                if (idx < sb.length() && sb.charAt(idx) == '"') {
                    // Retain escaped double quote
                    idx += 1;
                } else {
                    // The character after escape is not a double quote, we terminate the conversion and truncate.
                    // Firebird does this as well (see common/utils.cpp#dpbItemUpper)
                    sb.setLength(idx);
                    return sb.toString();
                }
            }
        }

        return sb.toString();
    }
}
