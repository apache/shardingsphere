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

package org.apache.shardingsphere.db.protocol.postgresql.constant;

import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Character set of PostgreSQL.
 */
@Getter
public enum PostgreSQLCharacterSet {
    
    UNICODE("unicode", () -> StandardCharsets.UTF_8),
    UTF8("utf8", () -> StandardCharsets.UTF_8);
    
    private static final Map<String, PostgreSQLCharacterSet> CHARACTER_SET_MAP = Collections.unmodifiableMap(Arrays.stream(values()).collect(Collectors.toMap(each -> each.name, Function.identity())));
    
    private final String name;
    
    private final Charset charset;
    
    PostgreSQLCharacterSet(final String name, final Supplier<Charset> charsetSupplier) {
        this.name = name;
        Charset result = null;
        try {
            result = charsetSupplier.get();
        } catch (UnsupportedCharsetException ignored) {
        }
        charset = result;
    }
    
    /**
     * Get character set by value.
     *
     * @param value value
     * @return character set for PostgreSQL
     */
    public static Optional<Charset> findByValue(final String value) {
        PostgreSQLCharacterSet result = CHARACTER_SET_MAP.get(value.toLowerCase(Locale.ROOT));
        if (null == result) {
            return charsetForName(value);
        }
        if (null == result.getCharset()) {
            return charsetForName(value);
        }
        return Optional.of(result.getCharset());
    }
    
    private static Optional<Charset> charsetForName(final String value) {
        Charset result = null;
        try {
            result = Charset.forName(value);
        } catch (final UnsupportedCharsetException ignored) {
        }
        return null == result ? Optional.empty() : Optional.of(result);
    }
}
