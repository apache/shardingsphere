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

package org.apache.shardingsphere.sqltranslator.spi;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.algorithm.core.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqltranslator.context.SQLTranslatorContext;

import java.util.List;

/**
 * SQL translator.
 */
public interface SQLTranslator extends ShardingSphereAlgorithm {
    
    /**
     * Translate SQL.
     *
     * @param sql to be translated SQL
     * @param parameters to be translated parameters
     * @param queryContext query context
     * @param storageType storage type
     * @param database database
     * @param globalRuleMetaData global rule meta data
     * @return SQL translator context
     */
    SQLTranslatorContext translate(String sql, List<Object> parameters, QueryContext queryContext, DatabaseType storageType, ShardingSphereDatabase database, RuleMetaData globalRuleMetaData);
}
