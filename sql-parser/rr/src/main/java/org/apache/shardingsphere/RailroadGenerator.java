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

package org.apache.shardingsphere;

import lombok.extern.slf4j.Slf4j;
import nl.bigo.rrdantlr4.ANTLRv4Lexer;
import nl.bigo.rrdantlr4.ANTLRv4Parser;
import nl.bigo.rrdantlr4.CommentsParser;
import nl.bigo.rrdantlr4.DiagramGenerator;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Railroad Generator
 */
@Slf4j
public class RailroadGenerator {

    private static final ScriptEngineManager MANAGER = new ScriptEngineManager();
    private static final ScriptEngine ENGINE = MANAGER.getEngineByName("graal.js");
    private static final String RAILROAD_SCRIPT = inputAsString(DiagramGenerator.class.getResourceAsStream("/railroad-diagram.js"));
    private static final String RAILROAD_CSS = inputAsString(DiagramGenerator.class.getResourceAsStream("/railroad-diagram.css"));
    private static final String HTML_TEMPLATE = inputAsString(DiagramGenerator.class.getResourceAsStream("/template.html"));
    private static final String HTML_SIMPLE_TEMPLATE = inputAsString(DiagramGenerator.class.getResourceAsStream("/template.simple.html"));
    private static final String CSS_TEMPLATE = inputAsString(DiagramGenerator.class.getResourceAsStream("/template.css"));
    private static final Pattern TEXT_PATTERN = Pattern.compile("(<text\\s+[^>]*?>\\s*(.+?)\\s*</text>)|[\\s\\S]");

    static {
        try {
            ENGINE.eval(RAILROAD_SCRIPT);
        }
        catch (ScriptException e) {
            log.error("could not evaluate script:\n{}", RAILROAD_SCRIPT);
            System.exit(1);
        }
    }
    private  Map<String, String> rules;
    private  Map<String, Set<String>> rulesRelation;
    private  Map<String, String> comments;

    public RailroadGenerator() {
        this.rules = new HashMap<>();
        this.comments = new HashMap<>();
        this.rulesRelation = new HashMap<>();
    }

    /**
     * parse the antlr4 grammar and get all the rules and rules relations.
     *
     * @param grammarFile grammar file
     * @throws IOException
     */
    public void parse(File grammarFile) throws IOException {

        InputStream input = new FileInputStream(grammarFile);

        ANTLRv4Lexer lexer = new ANTLRv4Lexer(new ANTLRInputStream(new BufferedInputStream(input)));
        ANTLRv4Parser parser = new ANTLRv4Parser(new CommonTokenStream(lexer));

        ParseTree tree = parser.grammarSpec();
        RailroadRuleVisitor visitor = new RailroadRuleVisitor();
        visitor.visit(tree);

        this.rules.putAll(visitor.getRules());
        this.comments.putAll(CommentsParser.commentsMap(inputAsString(new FileInputStream(grammarFile))));
        this.rulesRelation.putAll(visitor.getRulesRelation());
    }

    /**
     * Returns the SVG railroad diagram corresponding to the provided grammar rule.
     *
     * @param ruleName
     *         the grammar rule to get the SVG railroad diagram from.
     *
     * @return the SVG railroad diagram corresponding to the provided grammar rule.
     */
    public String getSVG(String ruleName) {
        try {
            CharSequence dsl = rules.get(ruleName);
            if (dsl == null) {
                throw new RuntimeException("no such rule found: " + ruleName);
            }
            String svg = (String) ENGINE.eval(dsl.toString());
            svg = svg.replaceFirst("<svg ", "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
            svg = svg.replaceFirst("<g ", "<style type=\"text/css\">" + RAILROAD_CSS + "</style>\n<g ");
            return svg;
        }
        catch (ScriptException e) {
            log.error("get svg of rule {} fail", ruleName);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an html page as a string of all grammar rules.
     *
     * @param fileName
     *         the name of the html page.
     *
     * @return an html page as a string of all grammar rules.
     */
    public String getHtml(String fileName,String rootRule, boolean simpleHTML) {
        StringBuilder rows = new StringBuilder();

        for (String ruleName : iterateRules(rootRule)) {
            String svg = this.getSVG(ruleName);
            String ruleDescription = comments.get(ruleName);

            rows.append("<tr><td id=\"").append(fileName).append("_").append(ruleName).append("\"><h4>").append(ruleName).append("</h4></td><td>").append(svg).append("</td></tr>");
            if (ruleDescription != null) {
                rows.append("<tr class=\"border-notop\"><td></td><td>" + ruleDescription.replaceAll("\n", "<br>") + "</td></tr>");
            }
        }

        final String template;
        if(simpleHTML) {
            template = HTML_SIMPLE_TEMPLATE.replace("${rows}", rows);
        }
        else {
            template = HTML_TEMPLATE.replace("${grammar}", fileName).replace("${css}", CSS_TEMPLATE).replace("${rows}", rows);
        }
        return addLinks(fileName, template);
    }

    private Collection<String> iterateRules(String rootRule){
        if(null == rootRule){
            return this.rules.keySet();
        }else {
            return iterateRulesBroadcast(rootRule);
        }
    }

    private Collection<String> iterateRulesBroadcast(String rootRule){
        Collection<String> rules = new ArrayList<>(this.rules.size());
        Collection<String> rulesProcessed = new HashSet<>(this.rules.size());
        rules.add(rootRule);
        rulesProcessed.add(rootRule);

        Queue<String> ruleQueue = new LinkedList<>();
        ruleQueue.addAll(this.rulesRelation.get(rootRule));
        rulesProcessed.addAll(this.rulesRelation.get(rootRule));

        while (!ruleQueue.isEmpty()){
            String rule = ruleQueue.poll();
            rules.add(rule);
            Collection<String> childRules = this.rulesRelation.get(rule);
            if(null != childRules && !childRules.isEmpty()){
                childRules.forEach(
                        childRule -> {
                            if(!rulesProcessed.contains(childRule)){
                                rulesProcessed.add(childRule);
                                ruleQueue.add(childRule);
                            }
                        }
                );
            }
        }
        return rules;
    }

    /**
     * Creates an html page containing all grammar rules.
     *
     * @param dir
     *          output dir
     * @param fileName
     *          output fine name
     * @param simpleHTML
     *          simple html or not
     * @return`true` iff the creation of the html page was successful.
     */
    public boolean createHtml(String dir,String fileName, String rootRule, boolean simpleHTML) {
        String html = this.getHtml(fileName,rootRule, simpleHTML);
        PrintWriter out = null;

        try {
            out = new PrintWriter(new File(dir+"/"+fileName));
            out.write(html);
            return true;
        }
        catch (IOException e) {
            log.error("create html fail,Exception:{}", e.getMessage());
            return false;
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Returns an HTML template containing SVG text-tags that will be wrapped with '<a xlink:href=...' to make the grammar rules clickable inside the HTML page.
     * @param fileName
     *          the name of the parsed grammar.
     * @param template
     *          the template whose text-tags need to be linked.
     * @return an HTML template containing SVG text-tags that will be wrapped with '<a xlink:href=...' to make the grammar rules clickable inside the HTML page.
     */
    private String addLinks(String fileName, String template) {
        StringBuilder builder = new StringBuilder();
        Matcher m = TEXT_PATTERN.matcher(template);
        while (m.find()) {
            if (m.group(1) == null) {
                builder.append(m.group());
            }
            else {
                String textTag = m.group(1);
                String rule = m.group(2);
                if (!this.rules.containsKey(rule)) {
                    builder.append(textTag);
                }
                else {
                    builder.append("<a xlink:href=\"").append("#").append(fileName).append("_").append(rule).append("\">").append(textTag).append("</a>");
                }
            }
        }
        return builder.toString();
    }

    private static String inputAsString(InputStream input) {
        final StringBuilder builder = new StringBuilder();
        final Scanner scan = new Scanner(input);

        while (scan.hasNextLine()) {
            builder.append(scan.nextLine()).append(scan.hasNextLine() ? "\n" : "");
        }

        return builder.toString();
    }
}
