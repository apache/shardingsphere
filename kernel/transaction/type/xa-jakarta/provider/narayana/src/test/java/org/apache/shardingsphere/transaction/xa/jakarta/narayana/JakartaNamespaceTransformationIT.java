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

package org.apache.shardingsphere.transaction.xa.jakarta.narayana;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the Eclipse Transformer has rewritten the Jakarta EE 8 {@code javax.transaction} namespace to
 * {@code jakarta.transaction} in the classes produced for this module. The JDK-provided {@code javax.transaction.xa}
 * subpackage ({@code XAResource}, {@code Xid}, {@code XAException}) must remain unchanged, as it is part of the
 * {@code java.transaction.xa} module and is not renamed by the Jakarta EE 9 transition.
 */
class JakartaNamespaceTransformationIT {
    
    private static final byte[] JAVAX_TRANSACTION = "javax/transaction/".getBytes(StandardCharsets.UTF_8);
    
    private static final byte[] JAKARTA_TRANSACTION = "jakarta/transaction/".getBytes(StandardCharsets.UTF_8);
    
    @Test
    void assertNoJakartaEE8NamespaceRemains() throws IOException {
        Path artifact = findMainArtifact();
        try (JarFile jarFile = new JarFile(artifact.toFile())) {
            Collection<byte[]> actualClassBytes = jarFile.stream().filter(entry -> entry.getName().endsWith(".class")).map(entry -> readEntry(jarFile, entry)).collect(Collectors.toList());
            assertFalse(actualClassBytes.isEmpty());
            assertTrue(actualClassBytes.stream().anyMatch(bytes -> indexOf(bytes, JAKARTA_TRANSACTION, 0) >= 0));
            assertTrue(actualClassBytes.stream().noneMatch(JakartaNamespaceTransformationIT::hasJakartaEE8Reference));
        }
    }
    
    private Path findMainArtifact() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get("target"))) {
            return stream.filter(path -> path.toString().endsWith(".jar"))
                    .filter(path -> !path.toString().contains("-sources"))
                    .filter(path -> !path.toString().contains("-javadoc"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No main artifact jar was found in target"));
        }
    }
    
    private static byte[] readEntry(final JarFile jarFile, final JarEntry jarEntry) {
        try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] block = new byte[8192];
            int read;
            while ((read = inputStream.read(block)) != -1) {
                buffer.write(block, 0, read);
            }
            return buffer.toByteArray();
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    private static boolean hasJakartaEE8Reference(final byte[] bytes) {
        int idx = indexOf(bytes, JAVAX_TRANSACTION, 0);
        while (idx >= 0) {
            int after = idx + JAVAX_TRANSACTION.length;
            if (after < bytes.length && bytes[after] >= 'A' && bytes[after] <= 'Z') {
                return true;
            }
            idx = indexOf(bytes, JAVAX_TRANSACTION, after);
        }
        return false;
    }
    
    private static int indexOf(final byte[] haystack, final byte[] needle, final int from) {
        for (int i = from; i <= haystack.length - needle.length; i++) {
            boolean match = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
        }
        return -1;
    }
}
