/*
Copyright (C) 2011, 2012, 2015 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
 */

package org.harctoolbox.irp;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Definitions in Chapter 10 and Assignments in Chapter 11; these are not independent objects.
 *
 */

// TODO: There are probably too many accessing functions here.
// Clean up by eliminating and making private.

public class NameEngine {

    private HashMap<String, Expression> map;

    public NameEngine() {
        map = new LinkedHashMap<>();
    }

    public NameEngine(String str) throws IrpSyntaxException {
        this();
        ParserDriver parserDriver = new ParserDriver(str);
        parseDefinitions(parserDriver.definitions());
    }

    private void define(String name, String value) throws IrpSyntaxException {
        Expression exp = new Expression(value);
        define(name, exp.getParseTree());
    }

    private void define(String name, IrpParser.Bare_expressionContext ctx) throws IrpSyntaxException {
        if (!Name.validName(name))
            throw new IrpSyntaxException("Invalid name: " + name);
        Expression expression = new Expression(ctx);
        map.put(name, expression);
    }

    public void define(String name, long value) throws IrpSyntaxException {
        define(name, Long.toString(value));
    }

    /**
     * Invoke the parser on the supplied argument, and stuff the result into the name engine.
     *
     * @param str String to be parsed, like "{C = F*4 + D + 3}".
     * @throws org.harctoolbox.irp.IrpSyntaxException
     */
    public void parseDefinitions(String str) throws IrpSyntaxException {
        ParserDriver parserDriver = new ParserDriver(str);
        parseDefinitions(parserDriver.definitions());
    }

    public final void parseDefinitions(IrpParser.DefinitionsContext ctx /* DEFINITIONS */) throws IrpSyntaxException {
        for (IrpParser.DefinitionContext definition : ctx.definitions_list().definition())
            parseDefinition(definition);
    }

    private void parseDefinition(IrpParser.DefinitionContext ctx /* DEFINITION */) throws IrpSyntaxException {
        define(ctx.name().getText(), ctx.bare_expression());
    }

    /**
     * Set names according to the content of the default values supplies in the first argument.
     *
     * @param parameterSpecs from where the default values are taken
     * @param initial If false, Parameters with memory (state variables) are not reset.
     * /
    public void loadDefaults(ParameterSpecs parameterSpecs, boolean initial) {
        for (ParameterSpec param : parameterSpecs.getParams()) {
            if ((initial || ! param.hasMemory()) && param.getDefault() != null) {
                //System.out.println(">>>>>" + param.getName());
                map.put(param.getName(), param.getDefault());
            }
        }
    }

    public void loadActualParameters(HashMap<String, Long> ivs, ParameterSpecs paramSpecs) throws DomainViolationException {
        for (Entry<String, Long> kvp : ivs.entrySet()) {
            String name = kvp.getKey();
            // if no Parameter Specs, do not annoy the user; he has been warned already.
            if (!paramSpecs.isEmpty()) {
                ParameterSpec ps = paramSpecs.getParameterSpec(name);
                if (ps == null) {
                    UserComm.warning("Parameter `" + name + "' unknown in ParameterSpecs.");
                } else if (!ps.isOK(kvp.getValue())) {
                    throw new DomainViolationException("Parameter " + name + " = " + kvp.getValue() + " outside of allowed domain (" + ps.domainAsString() + ").");
                }
            }
            assign(name, kvp.getValue());
        }
    }

    public void checkAssignments(ParameterSpecs paramSpecs) throws UnassignedException {
        for (String name : paramSpecs.getNames()) {
            if (!map.containsKey(name)) {
                throw new UnassignedException("Parameter `" + name + "' has not been assigned.");
            }
        }
    }*/

    /**
     * Returns the expression associated to the name given as parameter.
     * @param name
     * @return
     * @throws org.harctoolbox.irp.UnassignedException
     */
    public Expression get(String name) throws UnassignedException {
        //Debug.debugNameEngine("NameEngine: " + name + (map.containsKey(name) ? (" = " + map.get(name).toStringTree()) : "-"));
        if (!map.containsKey(name))
            throw new UnassignedException("Name " + name + " not defined");
        return map.get(name);
    }

    public long toNumber(String name) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        Expression expression = get(name);
        return expression.toNumber(this);
    }

    private ParseTree toParseTree(String name) throws UnassignedException {
        return get(name).getParseTree();
    }

    /* *
     *
     * @param name Input name
     * @return StringTree of the value.
     * @throws UnassignedException
     * /
    public IrpParser.Bare_expressionContext evaluate(String name) throws UnassignedException {
        if (map.containsKey(name))
            return map.get(name);
        else
            throw new UnassignedException("Name `" + name + "' not defined.");
    }

    public IrpParser.Bare_expressionContext evaluate(IrpParser.NameContext ctx) throws UnassignedException {
        return evaluate(ctx.getText());
    }*/

    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    /*public String tryEvaluate(String name) {
        IrpParser.Bare_expressionContext result ;
        try {
            result = evaluate(name);
        } catch (UnassignedException ex) {
            System.err.println(ex.getMessage());
        }
        return result;
    }*/

    public String toString(IrpParser parser) {
        StringBuilder str = new StringBuilder();
        for (String name : map.keySet()) {
            str.append(name).append("=").append(map.get(name).getParseTree().toStringTree(parser)).append(",");
        }
        return "{" + (str.length() == 0 ? "" : str.substring(0, str.length()-1)) + "}";
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (String name : map.keySet()) {
            str.append(name).append("=").append(map.get(name).toString()).append(",");
        }
        return "{" + (str.length() == 0 ? "" : str.substring(0, str.length()-1)) + "}";
    }

    /**
     * Creates consisting of parameter values that can be used as part of filenames etc.
     * Roughly, is a "pretty" variant of toString().
     *
     * @param equals String between name and value, often "=",
     * @param separator String between name-value pairs, often ",".
     * @return String
     * @throws org.harctoolbox.irp.UnassignedException
     */
    public String notationString(String equals, String separator) throws UnassignedException {
        StringBuilder str = new StringBuilder();
        for (String name : map.keySet()) {
            if (!name.startsWith("$") && !toParseTree(name).toStringTree().startsWith("("))
                str.append(name).append(equals).append(toParseTree(name).toStringTree()).append(separator);
        }
        return (str.length() == 0 ? "" : str.substring(0, str.length()-1));
    }

    public Element toElement(Document document) {
        Element root = document.createElement("definitions");
        return root;
    }

    /*private static void usage(int code) {
        System.err.println("Usage:");
        System.err.println("\tNameEngine [<name>=<value>|{<name>=<expression>}]+");
        System.exit(code);
    }

    /**
     * Just for testing purposes.
     *
     * @param args the command line arguments
     * /
    public static void main(String[] args) {
        try {
            NameEngine nameEngine = new NameEngine("{answer = 42, C = F*4 + D + 3}");
            System.out.println(nameEngine);
        } catch (IrpSyntaxException ex) {
            Logger.getLogger(NameEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    */
}
