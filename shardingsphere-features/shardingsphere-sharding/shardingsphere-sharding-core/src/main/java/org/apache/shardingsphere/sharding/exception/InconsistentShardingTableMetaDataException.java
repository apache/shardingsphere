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

package org.apache.shardingsphere.sharding.exception;

import org.apache.shardingsphere.infra.util.exception.external.sql.type.feature.FeatureSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.sharding.metadata.TableMetaDataViolation;

import java.util.Collection;

/**
 * Inconsistent sharding table meta data exception.
 */
public final class InconsistentShardingTableMetaDataException extends FeatureSQLException {
    
    private static final long serialVersionUID = -5450346946223396192L;
    
    public InconsistentShardingTableMetaDataException(final String logicTableName, final Collection<TableMetaDataViolation> violations) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 1, createReason(violations), logicTableName);
    }
    
    private static String createReason(final Collection<TableMetaDataViolation> violations) {
        StringBuilder result = new StringBuilder(
                "Can not get uniformed table structure for logic table `%s`, it has different meta data of actual tables are as follows: ").append(System.lineSeparator());
        for (TableMetaDataViolation each : violations) {
            result.append("actual table: ").append(each.getActualTableName()).append(", meta data: ").append(each.getTableMetaData()).append(System.lineSeparator());
        }
        return result.toString();
    }
}
