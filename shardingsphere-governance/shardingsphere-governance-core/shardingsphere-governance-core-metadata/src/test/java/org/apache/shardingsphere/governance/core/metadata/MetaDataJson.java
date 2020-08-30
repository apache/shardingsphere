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

package org.apache.shardingsphere.governance.core.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataJson {
    
    public static final String META_DATA = "configuredSchemaMetaData:\n"
            + "  tables:\n"
            + "    t_order:\n"
            + "      columns:\n"
            + "        id:\n"
            + "          caseSensitive: false\n"
            + "          dataType: 0\n"
            + "          generated: false\n"
            + "          name: id\n"
            + "          primaryKey: true\n"
            + "      indexes:\n"
            + "         primary:\n"
            + "           name: PRIMARY          \n"
            + "unconfiguredSchemaMetaDataMap:\n"
            + "  ds_0:\n"
            + "    tables:\n"
            + "      t_user:\n"
            + "        columns:\n"
            + "          id:\n"
            + "            caseSensitive: false\n"
            + "            dataType: 0\n"
            + "            generated: false\n"
            + "            name: id\n"
            + "            primaryKey: true\n"
            + "        indexes:\n"
            + "          primary:\n"
            + "            name: PRIMARY";
}
