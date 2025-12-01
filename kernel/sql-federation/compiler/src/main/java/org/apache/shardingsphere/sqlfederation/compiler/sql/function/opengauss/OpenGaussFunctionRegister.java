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

package org.apache.shardingsphere.sqlfederation.compiler.sql.function.opengauss;

import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.SQLFederationFunctionRegister;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.opengauss.impl.OpenGaussSystemFunction;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.postgresql.PostgreSQLFunctionRegister;

/**
 * Opengauss function register.
 */
public final class OpenGaussFunctionRegister implements SQLFederationFunctionRegister {
    
    private final SQLFederationFunctionRegister delegate = new PostgreSQLFunctionRegister();
    
    @Override
    public void registerFunction(final SchemaPlus schemaPlus, final String schemaName) {
        if ("pg_catalog".equalsIgnoreCase(schemaName)) {
            schemaPlus.add("gs_password_deadline", ScalarFunctionImpl.create(OpenGaussSystemFunction.class, "gsPasswordDeadline"));
            schemaPlus.add("intervaltonum", ScalarFunctionImpl.create(OpenGaussSystemFunction.class, "intervalToNum"));
            schemaPlus.add("gs_password_notifyTime", ScalarFunctionImpl.create(OpenGaussSystemFunction.class, "gsPasswordNotifyTime"));
            schemaPlus.add("version", ScalarFunctionImpl.create(OpenGaussSystemFunction.class, "version"));
            schemaPlus.add("opengauss_version", ScalarFunctionImpl.create(OpenGaussSystemFunction.class, "openGaussVersion"));
        }
        delegate.registerFunction(schemaPlus, schemaName);
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
