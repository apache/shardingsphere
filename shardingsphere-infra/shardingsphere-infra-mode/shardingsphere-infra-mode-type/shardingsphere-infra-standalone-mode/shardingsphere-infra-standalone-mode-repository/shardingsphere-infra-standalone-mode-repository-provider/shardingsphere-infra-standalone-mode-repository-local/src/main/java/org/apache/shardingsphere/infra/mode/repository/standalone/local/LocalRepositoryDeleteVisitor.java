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

package org.apache.shardingsphere.infra.mode.repository.standalone.local;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Local repository delete visitor.
 */
public final class LocalRepositoryDeleteVisitor implements FileVisitor {
    
    @Override
    public FileVisitResult preVisitDirectory(final Object dir, final BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(final Object file, final BasicFileAttributes attrs) throws IOException {
        Files.deleteIfExists((Path) file);
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(final Object file, final IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult postVisitDirectory(final Object dir, final IOException exc) throws IOException {
        Files.deleteIfExists((Path) dir);
        return FileVisitResult.CONTINUE;
    }
}
