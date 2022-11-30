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

package org.apache.shardingsphere.test.sql.parser.external.loader.strategy.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.test.sql.parser.external.loader.strategy.SQLCaseLoadStrategy;
import org.apache.shardingsphere.test.sql.parser.external.loader.summary.FileSummary;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

/**
 * SQL case loader with local file.
 */
public final class LocalFileSQLCaseLoadStrategy implements SQLCaseLoadStrategy {
    
    @SneakyThrows
    @Override
    public Collection<FileSummary> loadSQLCaseFileSummaries(final URI uri) {
        final Collection<FileSummary> result = new LinkedList<>();
        try (Stream<Path> stream = Files.walk(Paths.get(uri))) {
            stream.filter(each -> each.toString().endsWith(".sql"))
                    .forEach(each -> result.add(new FileSummary(each.getFileName().toString(), each.toUri().toString())));
        }
        return result;
    }
}
