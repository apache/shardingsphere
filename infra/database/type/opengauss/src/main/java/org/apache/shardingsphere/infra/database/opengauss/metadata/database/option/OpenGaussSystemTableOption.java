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

package org.apache.shardingsphere.infra.database.opengauss.metadata.database.option;

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.table.DialectSystemTableOption;

import java.util.Collection;

/**
 * System table option for openGauss.
 */
public final class OpenGaussSystemTableOption implements DialectSystemTableOption {
    
    private static final Collection<String> SYSTEM_CATALOG_QUERY_EXPRESSIONS = new CaseInsensitiveSet<>(3, 1F);
    
    static {
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("version()");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("intervaltonum(gs_password_deadline())");
        SYSTEM_CATALOG_QUERY_EXPRESSIONS.add("gs_password_notifytime()");
    }
    
    @Override
    public boolean isDriverQuerySystemCatalog() {
        return true;
    }
    
    @Override
    public boolean isSystemCatalogQueryExpressions(final String projectionExpression) {
        return SYSTEM_CATALOG_QUERY_EXPRESSIONS.contains(projectionExpression);
    }
}
