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

package org.apache.shardingsphere.test.integration.env.container.atomic.storage.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.testcontainers.containers.BindMode;

import java.util.Optional;

/**
 * OpenGauss container.
 */
public final class OpenGaussContainer extends DockerStorageContainer {
    
    public OpenGaussContainer(final String dockerImageName, final String scenario) {
        super(DatabaseTypeFactory.getInstance("openGauss"), Strings.isNullOrEmpty(dockerImageName) ? "enmotech/opengauss:3.0.0" : dockerImageName, scenario);
    }
    
    @Override
    protected void configure() {
        withCommand("--max_connections=600");
        addEnv("GS_PASSWORD", getRootPassword());
        withClasspathResourceMapping("/env/postgresql/postgresql.conf", "/usr/local/opengauss/share/postgresql/postgresql.conf.sample", BindMode.READ_ONLY);
        withClasspathResourceMapping("/env/opengauss/pg_hba.conf", "/usr/local/opengauss/share/postgresql/pg_hba.conf.sample", BindMode.READ_ONLY);
        withPrivilegedMode(true);
        super.configure();
    }
    
    @Override
    public String getRootUsername() {
        return "root";
    }
    
    @Override
    public String getRootPassword() {
        return "Root@123";
    }
    
    @Override
    public String getTestCaseUsername() {
        return "scaling";
    }
    
    @Override
    public String getTestCasePassword() {
        return "Root@123";
    }
    
    @Override
    public int getPort() {
        return 5432;
    }
    
    @Override
    protected Optional<String> getDefaultDatabaseName() {
        return Optional.of("postgres");
    }
}
