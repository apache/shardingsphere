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

package org.apache.shardingsphere.sqltranslator.spring.namespace.tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SQL translator rule bean definition tag constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLTranslatorRuleBeanDefinitionTag {
    
    /**
     * Root tag.
     */
    public static final String ROOT_TAG = "rule";
    
    /**
     * SQL translator type attribute.
     */
    public static final String SQL_TRANSLATOR_TYPE_ATTRIBUTE = "type";
    
    /**
     * Use original SQL when translating failed attribute.
     */
    public static final String SQL_USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED_ATTRIBUTE = "use-original-sql-when-translating-failed";
}
