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

package org.apache.shardingsphere.distsql.parser.statement.ral.common.authority.enumeration;

import org.apache.shardingsphere.distsql.parser.operation.DistSQLOperationTypeEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public enum DistSQLStatementType {
    
    RQL {
        @Override
        public Collection<DistSQLOperationTypeEnum> getPrivilegeTypes() {
            return Collections.singletonList(DistSQLOperationTypeEnum.SHOW);
        }
    },
    RDL {
        @Override
        public Collection<DistSQLOperationTypeEnum> getPrivilegeTypes() {
            return Arrays.asList(DistSQLOperationTypeEnum.ADD, DistSQLOperationTypeEnum.ALTER, DistSQLOperationTypeEnum.CREATE, DistSQLOperationTypeEnum.DROP);
        }
    },
    RAL {
        @Override
        public Collection<DistSQLOperationTypeEnum> getPrivilegeTypes() {
            return Collections.singletonList(DistSQLOperationTypeEnum.RAL);
        }
    };
    
    /**
     * Get the corresponding privilege type.
     * @return privilege type
     */
    public abstract Collection<DistSQLOperationTypeEnum> getPrivilegeTypes();
    
    /**
     * Returns the statement type of the specified variable name.
     *
     * @param statementTypeName statement type name
     * @return statement type constant
     */
    public static DistSQLStatementType getValueOf(final String statementTypeName) {
        try {
            return valueOf(statementTypeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException(String.format("Unsupported statement type `%s`", statementTypeName));
        }
    }
}
