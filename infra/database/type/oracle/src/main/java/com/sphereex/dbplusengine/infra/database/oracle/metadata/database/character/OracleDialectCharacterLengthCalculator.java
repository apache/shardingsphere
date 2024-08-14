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

package com.sphereex.dbplusengine.infra.database.oracle.metadata.database.character;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import com.sphereex.dbplusengine.infra.database.core.metadata.database.character.DialectCharacterLengthCalculator;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Oracle dialect character length calculator.
 */
public final class OracleDialectCharacterLengthCalculator implements DialectCharacterLengthCalculator {
    
    private static final Map<String, Integer> CHARSET_BYTE_LENGTH_MAP = new CaseInsensitiveMap<>(18, 1F);
    
    private static final Set<String> CHARACTER_TYPES = new CaseInsensitiveSet<>(Arrays.asList("CHAR", "NCHAR", "VARCHAR", "VARCHAR2", "NVARCHAR2"));
    
    private static final String DEFAULT_CHARSET = "AL32UTF8";
    
    private static final int CHAR_TYPE_COLUMN_BYTE_LIMIT = 4000;
    
    static {
        initCharsetByteLengthMap();
    }
        
    @Override
    public boolean isNeedCalculate() {
        return true;
    }
    
    @Override
    public int getCharsetCharToByteRatio(final String charset) {
        return CHARSET_BYTE_LENGTH_MAP.containsKey(charset) ? CHARSET_BYTE_LENGTH_MAP.get(charset) : CHARSET_BYTE_LENGTH_MAP.get(DEFAULT_CHARSET);
    }
    
    @Override
    public String getCharsetNameByCollation(final String collation) {
        return DEFAULT_CHARSET;
    }
    
    @Override
    public boolean isCharacterType(final String type) {
        return CHARACTER_TYPES.contains(type);
    }
    
    @Override
    public String getDefaultCharsetName() {
        return DEFAULT_CHARSET;
    }
    
    @Override
    public void checkColumnByteLength(final int columnByteLength, final String columnName) {
        ShardingSpherePreconditions.checkState(columnByteLength <= CHAR_TYPE_COLUMN_BYTE_LIMIT,
                () -> new IllegalArgumentException(String.format("Column `%s` byte length exceeds upper limit, current: %s, limit: %s", columnName, columnByteLength, CHAR_TYPE_COLUMN_BYTE_LIMIT)));
    }
    
    @Override
    public void checkRowByteLength(final int rowByteLength) {
        // No limit.
    }
    
    @Override
    public int calculateColumnByteLength(final int columnCharLength, final boolean notNull, final String dataType, final String columnName, final String columnCharset,
                                         final AtomicBoolean isNullCalculated) {
        return isCharacterType(dataType) ? columnCharLength * getCharsetCharToByteRatio(columnCharset) : 0;
    }
    
    @Override
    public int toCharacterLength(final int byteLength, final String charset) {
        Integer byteLengthRate = CHARSET_BYTE_LENGTH_MAP.containsKey(charset) ? CHARSET_BYTE_LENGTH_MAP.get(charset) : CHARSET_BYTE_LENGTH_MAP.get(DEFAULT_CHARSET);
        return 0 == byteLength % byteLengthRate ? byteLength / byteLengthRate : byteLength / byteLengthRate + 1;
    }
    
    @Override
    public boolean isSupportedColumnCharacterSetDefinition() {
        return false;
    }
    
    @Override
    public String getDefaultColumnLengthUnit() {
        return "BYTE";
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
    
    private static void initCharsetByteLengthMap() {
        CHARSET_BYTE_LENGTH_MAP.put("AL16UTF16", 4);
        CHARSET_BYTE_LENGTH_MAP.put("AL24UTFFSS", 4);
        CHARSET_BYTE_LENGTH_MAP.put("AL32UTF8", 4);
        CHARSET_BYTE_LENGTH_MAP.put("US7ASCII", 1);
        CHARSET_BYTE_LENGTH_MAP.put("UTF8", 4);
        CHARSET_BYTE_LENGTH_MAP.put("WE8EBCDIC1047", 1);
        CHARSET_BYTE_LENGTH_MAP.put("WE8EBCDIC1140", 1);
        CHARSET_BYTE_LENGTH_MAP.put("WE8ISO8859P1", 1);
        CHARSET_BYTE_LENGTH_MAP.put("WE8ISO8859P15", 1);
        CHARSET_BYTE_LENGTH_MAP.put("WE8MSWIN1252", 1);
        CHARSET_BYTE_LENGTH_MAP.put("ZHS16CGB231280", 2);
        CHARSET_BYTE_LENGTH_MAP.put("ZHS16CGB231280FIXED", 2);
        CHARSET_BYTE_LENGTH_MAP.put("ZHS16DBCS", 2);
        CHARSET_BYTE_LENGTH_MAP.put("ZHS16DBCSFIXED", 2);
        CHARSET_BYTE_LENGTH_MAP.put("ZHS16GBK", 2);
        CHARSET_BYTE_LENGTH_MAP.put("ZHS32GB18030", 4);
        CHARSET_BYTE_LENGTH_MAP.put("ZHT16BIG5", 2);
        CHARSET_BYTE_LENGTH_MAP.put("ZHT32EUC", 4);
    }
}
