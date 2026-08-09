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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.charset;

import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * MySQL session character set context.
 */
@Getter
public final class MySQLSessionCharsetContext {
    
    public static final AttributeKey<MySQLSessionCharsetContext> ATTRIBUTE_KEY = AttributeKey.valueOf(MySQLSessionCharsetContext.class.getName());
    
    private final MySQLCharacterSets clientCharacterSet;
    
    private final Optional<MySQLCharacterSets> resultCharacterSet;
    
    private final Optional<String> resultCharacterSetName;
    
    private final MySQLCharacterSets connectionCollation;
    
    private MySQLSessionCharsetContext(final MySQLCharacterSets clientCharacterSet, final Optional<MySQLCharacterSets> resultCharacterSet,
                                       final Optional<String> resultCharacterSetName, final MySQLCharacterSets connectionCollation) {
        this.clientCharacterSet = clientCharacterSet;
        this.resultCharacterSet = resultCharacterSet;
        this.resultCharacterSetName = resultCharacterSetName;
        this.connectionCollation = connectionCollation;
    }
    
    /**
     * Create MySQL session character set context.
     *
     * @param characterSet initial character set and collation
     * @return MySQL session character set context
     */
    public static MySQLSessionCharsetContext create(final MySQLCharacterSets characterSet) {
        return new MySQLSessionCharsetContext(characterSet, Optional.of(characterSet), Optional.of(characterSet.getCharacterSetName()), characterSet);
    }
    
    /**
     * Get MySQL session character set context.
     *
     * @param attributeMap attribute map
     * @return MySQL session character set context
     */
    public static MySQLSessionCharsetContext get(final AttributeMap attributeMap) {
        MySQLSessionCharsetContext result = attributeMap.attr(ATTRIBUTE_KEY).get();
        return null == result ? create(MySQLConstants.DEFAULT_CHARSET) : result;
    }
    
    /**
     * Create a context with the specified client character set.
     *
     * @param characterSet client character set
     * @return updated context
     */
    public MySQLSessionCharsetContext withClientCharacterSet(final MySQLCharacterSets characterSet) {
        return new MySQLSessionCharsetContext(characterSet, resultCharacterSet, resultCharacterSetName, connectionCollation);
    }
    
    /**
     * Create a context with the specified result character set.
     *
     * @param characterSet result character set
     * @return updated context
     */
    public MySQLSessionCharsetContext withResultCharacterSet(final MySQLCharacterSets characterSet) {
        return new MySQLSessionCharsetContext(clientCharacterSet, Optional.of(characterSet), Optional.of(characterSet.getCharacterSetName()), connectionCollation);
    }
    
    /**
     * Create a context without result conversion for a NULL result character set.
     *
     * @return updated context
     */
    public MySQLSessionCharsetContext withoutResultConversion() {
        return new MySQLSessionCharsetContext(clientCharacterSet, Optional.empty(), Optional.empty(), connectionCollation);
    }
    
    /**
     * Create a context without result conversion for the binary result character set.
     *
     * @return updated context
     */
    public MySQLSessionCharsetContext withBinaryResult() {
        return new MySQLSessionCharsetContext(clientCharacterSet, Optional.empty(), Optional.of("binary"), connectionCollation);
    }
    
    /**
     * Create a context with the default collation of the specified connection character set.
     *
     * @param characterSet connection character set and its default collation
     * @return updated context
     */
    public MySQLSessionCharsetContext withConnectionCharacterSet(final MySQLCharacterSets characterSet) {
        return new MySQLSessionCharsetContext(clientCharacterSet, resultCharacterSet, resultCharacterSetName, characterSet);
    }
    
    /**
     * Create a context with the specified connection collation.
     *
     * @param collation connection collation
     * @return updated context
     */
    public MySQLSessionCharsetContext withConnectionCollation(final MySQLCharacterSets collation) {
        return new MySQLSessionCharsetContext(clientCharacterSet, resultCharacterSet, resultCharacterSetName, collation);
    }
    
    /**
     * Get result encoding character set.
     *
     * @return result encoding character set
     */
    public Charset getResultCharset() {
        return resultCharacterSet.orElse(MySQLConstants.DEFAULT_CHARSET).getCharset();
    }
    
    /**
     * Get result metadata collation.
     *
     * @return result metadata collation
     */
    public MySQLCharacterSets getResultCollation() {
        return resultCharacterSet.orElse(MySQLConstants.DEFAULT_CHARSET);
    }
    
    /**
     * Get client character set name.
     *
     * @return client character set name
     */
    public String getClientCharacterSetName() {
        return clientCharacterSet.getCharacterSetName();
    }
    
    /**
     * Get connection character set name.
     *
     * @return connection character set name
     */
    public String getConnectionCharacterSetName() {
        return connectionCollation.getCharacterSetName();
    }
    
    /**
     * Get connection collation name.
     *
     * @return connection collation name
     */
    public String getConnectionCollationName() {
        return connectionCollation.getCollationName();
    }
    
    /**
     * Apply context to protocol attributes.
     *
     * @param attributeMap attribute map
     */
    public void apply(final AttributeMap attributeMap) {
        attributeMap.attr(ATTRIBUTE_KEY).set(this);
        attributeMap.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(clientCharacterSet.getCharset());
        attributeMap.attr(MySQLConstants.RESULT_CHARSET_ATTRIBUTE_KEY).set(getResultCharset());
        attributeMap.attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).set(getResultCollation());
    }
}
