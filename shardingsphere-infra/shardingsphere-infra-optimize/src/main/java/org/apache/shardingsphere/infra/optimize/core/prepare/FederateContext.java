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

package org.apache.shardingsphere.infra.optimize.core.prepare;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalcitePrepare;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.tools.RelRunner;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public class FederateContext implements CalcitePrepare.Context {
    @Override
    public JavaTypeFactory getTypeFactory() {
        return new JavaTypeFactoryImpl();
    }

    @Override
    public CalciteSchema getRootSchema() {
        return null;
    }

    @Override
    public CalciteSchema getMutableRootSchema() {
        return null;
    }

    @Override
    public List<String> getDefaultSchemaPath() {
        return null;
    }

    @Override
    public CalciteConnectionConfig config() {
        return null;
    }

    @Override
    public CalcitePrepare.SparkHandler spark() {
        return null;
    }

    @Override
    public DataContext getDataContext() {
        return null;
    }

    @Override
    public @Nullable List<String> getObjectPath() {
        return null;
    }

    @Override
    public RelRunner getRelRunner() {
        return null;
    }
}
