package io.shardingsphere.core.util;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Script cache
 * @author Pramy
 */
public class GroovyUtil {


    private final static Map<String, Script> scripts = new ConcurrentHashMap<>();

    private final static GroovyShell shell = new GroovyShell();

    public static Object getResult(final String expression){
        Script script;
        if (scripts.containsKey(expression)) {
            script = scripts.get(expression);
        }
        else {
            script = shell.parse(expression);
            scripts.put(expression, script);
        }
        return script.run();
    }
}

