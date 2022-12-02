
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

package org.apache.shardingsphere;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Railroad maven plug-in to generate railroad diagram
 *
 */
@Slf4j
@Getter
@Setter
public class RailroadMojo extends AbstractMojo {

    /**
     * Provides an explicit list of all the grammars that should be included in
     * the generate phase of the plugin. Note that the plugin is smart enough to
     * realize that imported grammars should be included but not acted upon
     * directly by the ANTLR Tool.
     * <p>
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern
     * <code>**&#47;*.g4</code> is used to select grammar files.
     * </p>
     */
    @Parameter
    protected Set<String> includes = new HashSet<>();
    /**
     * A set of Ant-like exclusion patterns used to prevent certain files from
     * being processed. By default, this set is empty such that no files are
     * excluded.
     */
    @Parameter
    protected Set<String> excludes = new HashSet<>();

    /**
     * The directory where the ANTLR grammar files ({@code *.g4}) are located.
     */
    @Parameter(defaultValue = "${basedir}/src/main/antlr4")
    private File sourceDirectory;

    /**
     * Specify output directory where the railroad file are generated.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/rr")
    private File outputDirectory;

    /**
     * Specify output directory where the railroad file are generated.
     */
    @Parameter(defaultValue = "index.html")
    private String outputName;

    /**
     * Specify location of imported grammars and tokens files.
     */
    @Parameter(defaultValue = "${basedir}/src/main/antlr4/imports")
    private File libDirectory;

    @Parameter
    private String rootRule;

    @Override
    public void execute(){
        if (log.isDebugEnabled()) {
            for (String e : excludes) {
                log.debug("ANTLR: Exclude: " + e);
            }
            for (String e : includes) {
                log.debug("ANTLR: Include: " + e);
            }
            log.debug("RR: Output: " + outputDirectory);
            log.debug("RR: Library: " + libDirectory);
        }

        if (!sourceDirectory.isDirectory()) {
            log.info("No ANTLR 4 grammars to compile in " + sourceDirectory.getAbsolutePath());
            return;
        }

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        RailroadGenerator railroadGenerator = new RailroadGenerator();
        try {
            Set<File> grammarFiles =  getGrammarFiles();
            for(File grammar : grammarFiles){
                railroadGenerator.parse(grammar);
            }
            for(File importGrammar : getImportFiles()){
                railroadGenerator.parse(importGrammar);
            }

            if(grammarFiles.size() == 1){
                outputName = grammarFiles.toArray(new File[1])[0].getName();
                outputName = outputName.substring(0, outputName.lastIndexOf("."))+".html";
            }
            railroadGenerator.createHtml(outputDirectory.getAbsolutePath(), outputName, rootRule,true);
        } catch (InclusionScanException | IOException e) {
            throw new RailroadGeneratorException("generate railroad diagram fail.", e);
        }
    }
    private Set<File> getImportFiles() throws InclusionScanException {
        if (!libDirectory.exists()) {
            return Collections.emptySet();
        }
        Set<String> includes = new HashSet<>();
        includes.add("*.g4");
        includes.add("*.tokens");

        SourceInclusionScanner scan = new SimpleSourceInclusionScanner(includes, Collections.<String>emptySet());
        scan.addSourceMapping(new SuffixMapping("G4", "g4"));
        return scan.getIncludedSources(libDirectory, null);
    }

    private Set<File> getGrammarFiles() throws InclusionScanException
    {
        SourceMapping mapping = new SuffixMapping("g4", Collections.<String>emptySet());
        Set<String> includes = getIncludesPatterns();
        excludes.add("imports/**");
        SourceInclusionScanner scan = new SimpleSourceInclusionScanner(includes, excludes);
        scan.addSourceMapping(mapping);
        return scan.getIncludedSources(sourceDirectory, null);
    }

    private Set<String> getIncludesPatterns() {
        if (includes == null || includes.isEmpty()) {
            return Collections.singleton("**/*.g4");
        }
        return includes;
    }

    public static void main(String[] args) {
        RailroadMojo railroadMojo = new RailroadMojo();
        railroadMojo.setSourceDirectory(new File("/Users/hewenbin/github/shardingsphere/sql-parser/dialect/mysql/src/main/antlr4/org/apache/shardingsphere/sql/parser/autogen"));
        railroadMojo.setLibDirectory(new File("/Users/hewenbin/github/shardingsphere/sql-parser/dialect/mysql/src/main/antlr4/imports/mysql"));
        railroadMojo.setOutputDirectory(new File("/tmp"));
        railroadMojo.execute();
    }
}
