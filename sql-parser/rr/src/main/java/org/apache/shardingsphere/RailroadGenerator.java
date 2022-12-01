package org.apache.shardingsphere;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.PngImage;
import nl.bigo.rrdantlr4.ANTLRv4Lexer;
import nl.bigo.rrdantlr4.ANTLRv4Parser;
import nl.bigo.rrdantlr4.CommentsParser;
import nl.bigo.rrdantlr4.DiagramGenerator;
import nl.bigo.rrdantlr4.RuleVisitor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Railroad Generator
 */
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
            e.printStackTrace();
            System.err.println("could not evaluate script:\n" + RAILROAD_SCRIPT);
            System.exit(1);
        }
    }
    private  Map<String, String> rules;
    private  Map<String, String> rulesRelation;
    private  Map<String, String> comments;

    public RailroadGenerator() {
        this.rules = new HashMap<>();
        this.comments = new HashMap<>();
        this.rulesRelation = new HashMap<>();
    }

    /**
     * Parses `this.antlr4Grammar` and returns all parsed grammar rules.
     *
     * @return all parsed grammar rules.
     *
     * @throws IOException
     *         when the grammar could not be parsed.
     */
    public void parse(String antlr4Grammar) throws IOException {

        InputStream input = new FileInputStream(antlr4Grammar);

        // Now parse the grammar.
        ANTLRv4Lexer lexer = new ANTLRv4Lexer(new ANTLRInputStream(new BufferedInputStream(input)));
        ANTLRv4Parser parser = new ANTLRv4Parser(new CommonTokenStream(lexer));

        ParseTree tree = parser.grammarSpec();
        RailroadRuleVistor visitor = new RailroadRuleVistor();
        visitor.visit(tree);

        this.rules.putAll(visitor.getRules());
        this.comments.putAll(CommentsParser.commentsMap(inputAsString(new FileInputStream(antlr4Grammar))));
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

            // Evaluate the DSL that translates the input back to a SVG.
            String svg = (String) ENGINE.eval(dsl.toString());

            // Insert the proper namespaces and (custom) style sheet.
            svg = svg.replaceFirst("<svg ", "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
            svg = svg.replaceFirst("<g ", "<style type=\"text/css\">" + RAILROAD_CSS + "</style>\n<g ");

            return svg;
        }
        catch (ScriptException e) {
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
    public String getHtml(String fileName, boolean simpleHTML) {
        StringBuilder rows = new StringBuilder();

        for (String ruleName : this.rules.keySet()) {
            String svg = this.getSVG(ruleName);
            String ruleDescription = comments.get(ruleName);

            rows.append("<tr><td id=\"").append(antlr4GrammarFileName).append("_").append(ruleName).append("\"><h4>")
                .append(ruleName).append("</h4></td><td>").append(svg).append("</td></tr>");
            if (ruleDescription != null) {
                rows.append("<tr class=\"border-notop\"><td></td><td>" + ruleDescription.replaceAll("\n", "<br>") + "</td></tr>");
            }
        }

        if(simpleHTML) {
            final String template = HTML_SIMPLE_TEMPLATE
                .replace("${rows}", rows);
            return addLinks(antlr4GrammarFileName, template);
        }
        else {

            final String template = HTML_TEMPLATE
                .replace("${grammar}", antlr4GrammarFileName)
                .replace("${css}", CSS_TEMPLATE)
                .replace("${rows}", rows);

            return addLinks(antlr4GrammarFileName, template);
        }
    }

    /**
     * Creates a default (index.html) page containing all grammar rules.
     *
     * @return `true` iff the creation of the html page was successful.
     */
    public boolean createHtml() {
        return createHtml("index.html", false);
    }

    /**
     * Creates an html page containing all grammar rules.
     *
     * @param fileName
     *         the file name of the generated html page.
     *
     * @return `true` iff the creation of the html page was successful.
     */
    public boolean createHtml(String fileName, boolean simpleHTML) {

        String html = this.getHtml(fileName, simpleHTML);

        PrintWriter out = null;

        try {
            out = new PrintWriter(new File(this.outputDir, fileName));
            out.write(html);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }



    /**
     * Returns an HTML template containing SVG text-tags that
     * will be wrapped with '<a xlink:href=...' to make the grammar
     * rules clickable inside the HTML page.
     *
     * @param antlr4GrammarFileName
     *         the name of the parsed grammar.
     * @param template
     *         the template whose text-tags need to be linked.
     *
     * @return an HTML template containing SVG text-tags that
     * will be wrapped with '<a xlink:href=...' to make the grammar
     * rules clickable inside the HTML page.
     */
    private String addLinks(String antlr4GrammarFileName, String template) {

        StringBuilder builder = new StringBuilder();
        Matcher m = TEXT_PATTERN.matcher(template);

        while (m.find()) {

            if (m.group(1) == null) {
                // We didn't match a text-tag, just append whatever we did match.
                builder.append(m.group());
            }
            else {
                // We found an SVG text tag.
                String textTag = m.group(1);
                String rule = m.group(2);

                // The rule does not match any of the parser rules (one of:
                // epsilon/not/comment/literal tags probably). Do not link
                // but just add it back in the builder.
                if (!this.rules.containsKey(rule)) {
                    builder.append(textTag);
                }
                else {
                    // Yes, the rule matches with a parsed rule, add a link
                    // around it.
                    builder.append("<a xlink:href=\"").append("#").append(antlr4GrammarFileName).append("_").append(rule).append("\">")
                            .append(textTag).append("</a>");
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
