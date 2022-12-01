package org.apache.shardingsphere;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Railroad maven plug-in to generate railroad diagram
 *
 */
@Slf4j
public class RailroadMojo extends AbstractMojo {


    /**
     * The directory where the ANTLR grammar files ({@code *.g4}) are located.
     */
    @Parameter(defaultValue = "${basedir}/src/main/antlr4")
    @Getter
    private File sourceDirectory;

    /**
     * Specify output directory where the railroad file are generated.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/rr")
    @Getter
    private File outputDirectory;

    /**
     * Specify location of imported grammars and tokens files.
     */
    @Parameter(defaultValue = "${basedir}/src/main/antlr4/imports")
    @Getter
    private File libDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("RR: Output: " + outputDirectory);
            log.debug("RR: Library: " + libDirectory);
        }

        if (!sourceDirectory.isDirectory()) {
            log.info("No ANTLR 4 grammars to compile in " + sourceDirectory.getAbsolutePath());
            return;
        }


    }

    public static void main(String[] args) throws IOException {
        RailroadGenerator generator = new RailroadGenerator();
        generator.parse("/Users/hewenbin/github/shardingsphere/sql-parser/dialect/mysql/src/main/antlr4/imports/mysql/Alphabet.g4");
        generator.parse("/Users/hewenbin/github/shardingsphere/sql-parser/dialect/mysql/src/main/antlr4/imports/mysql/BaseRule.g4");
        generator.parse("/Users/hewenbin/github/shardingsphere/sql-parser/dialect/mysql/src/main/antlr4/imports/mysql/Keyword.g4");
        generator.parse("/Users/hewenbin/github/shardingsphere/sql-parser/dialect/mysql/src/main/antlr4/org/apache/shardingsphere/sql/parser/autogen/MySQLStatement.g4");
        generator.createHtml();
    }
}
