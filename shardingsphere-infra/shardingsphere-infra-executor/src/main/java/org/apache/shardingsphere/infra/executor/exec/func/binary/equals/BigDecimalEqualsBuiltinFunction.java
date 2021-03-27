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

package org.apache.shardingsphere.infra.executor.exec.func.binary.equals;

import org.apache.calcite.sql.SqlKind;
import org.apache.shardingsphere.infra.executor.exec.func.binary.BinaryBuiltinFunction;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class BigDecimalEqualsBuiltinFunction extends BinaryBuiltinFunction<BigDecimal, Boolean> {
    
    public static final BigDecimalEqualsBuiltinFunction INSTANCE = new BigDecimalEqualsBuiltinFunction();
    
    @Override
    public Boolean apply(final BigDecimal t1, final BigDecimal t2) {
        return t1 == t2;
    }
    
    @Override
    public String getFunctionName() {
        return SqlKind.EQUALS.name();
    }
    
    @Override
    public List<String[]> getArgTypeNames() {
        String typeName = BigDecimal.class.getTypeName();
        return Collections.singletonList(new String[]{typeName, typeName});
    }
}
