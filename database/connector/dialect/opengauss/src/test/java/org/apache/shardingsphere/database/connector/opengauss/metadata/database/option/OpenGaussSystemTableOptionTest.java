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

package org.apache.shardingsphere.database.connector.opengauss.metadata.database.option;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussSystemTableOptionTest {
    
    @Test
    void assertIsSystemCatalogQueryExpressions() {
        assertTrue(new OpenGaussDriverQuerySystemCatalogOption().isSystemCatalogQueryExpressions("version()"));
        assertTrue(new OpenGaussDriverQuerySystemCatalogOption().isSystemCatalogQueryExpressions("intervaltonum(gs_password_deadline())"));
        assertTrue(new OpenGaussDriverQuerySystemCatalogOption().isSystemCatalogQueryExpressions("gs_password_notifytime()"));
        assertTrue(new OpenGaussDriverQuerySystemCatalogOption().isSystemCatalogQueryExpressions("Version()"));
        assertTrue(new OpenGaussDriverQuerySystemCatalogOption().isSystemCatalogQueryExpressions("Intervaltonum(gs_password_deadline())"));
        assertTrue(new OpenGaussDriverQuerySystemCatalogOption().isSystemCatalogQueryExpressions("Gs_password_notifytime()"));
        assertFalse(new OpenGaussDriverQuerySystemCatalogOption().isSystemCatalogQueryExpressions("invalid()"));
    }
}
