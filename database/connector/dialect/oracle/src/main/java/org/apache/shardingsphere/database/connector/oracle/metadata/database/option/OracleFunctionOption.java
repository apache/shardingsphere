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

package org.apache.shardingsphere.database.connector.oracle.metadata.database.option;

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.function.DialectFunctionOption;

import java.util.Arrays;
import java.util.Collection;

/**
 * Oracle function option.
 */
public final class OracleFunctionOption implements DialectFunctionOption {
    
    // TODO remove ROWNUM_ and ROW_NUMBER, move DAY to anthor method @duanzhengqiang
    private static final Collection<String> UNPARENTHESIZED_FUNCTION_NAMES = new CaseInsensitiveSet<>(Arrays.asList(
            "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURRVAL", "DAY", "DBTIMEZONE", "LEVEL", "LOCALTIME", "LOCALTIMESTAMP",
            "NEXTVAL", "ORA_ROWSCN", "ROWID", "ROWNUM", "ROWNUM_", "ROW_NUMBER", "SESSIONTIMEZONE", "SESSION_USER", "SYSDATE", "SYSTIMESTAMP", "UID", "USER"));
    
    @Override
    public String getIfNullFunctionName() {
        return "NVL";
    }
    
    @Override
    public Collection<String> getUnparenthesizedFunctionNames() {
        return UNPARENTHESIZED_FUNCTION_NAMES;
    }
}
