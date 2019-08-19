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

package org.apache.shardingsphere.core.optimize.encrypt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptTransparentOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.engine.dml.EncryptInsertOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.engine.dml.EncryptWhereOptimizeEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;

/**
 * Optimize engine factory for encrypt.
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptOptimizeEngineFactory {
    
    /**
     * Create encrypt optimize engine instance.
     *
     * @param sqlStatement SQL statement
     * @return encrypt optimize engine instance
     */
    public static EncryptOptimizeEngine newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            return new EncryptInsertOptimizeEngine();
        }
        if (sqlStatement instanceof SelectStatement || sqlStatement instanceof UpdateStatement || sqlStatement instanceof DeleteStatement) {
            return new EncryptWhereOptimizeEngine();
        }
        return new EncryptTransparentOptimizeEngine();
    }
}
