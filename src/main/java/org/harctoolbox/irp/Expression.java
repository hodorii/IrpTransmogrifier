/*
Copyright (C) 2016 Bengt Martensson.

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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IncompatibleArgumentException;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class corresponds to Chapter 9.
 * An expression is evaluated during execution time with the current name bindings.
 */
public class Expression extends PrimaryItem {

    private IrpParser.ExpressionContext parseTree;

    /**
     * Construct an Expression from the number supplied as argument.
     * @param value
     * @throws IrpSyntaxException
     */
    public Expression(long value) throws IrpSyntaxException { // FIXME: insanely inefficient
        this(new ParserDriver(Long.toString(value)));
    }

    /**
     * Construct an Expression by parsing the argument.
     * @param str
     * @throws IrpSyntaxException
     */
    public Expression(String str) throws IrpSyntaxException {
        this(new ParserDriver(str));
    }

    Expression(ParserDriver parserDriver) throws IrpSyntaxException {
        this(parserDriver.getParser().expression());
    }

    public Expression(IrpParser.Para_expressionContext ctx) {
        this(ctx.expression());
    }

    public Expression(IrpParser.ExpressionContext ctx) {
        this.parseTree = ctx;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.parseTree);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Expression))
            return false;

        Expression other = (Expression) obj;

        return toIrpString().equals(other.toIrpString());
    }

    @Override
    public String toString() {
        return toIrpString();
    }

    public String toStringTree(IrpParser parser) {
        return parseTree.toStringTree(parser);
    }

    public String toStringTree() {
        return parseTree.toStringTree();
    }

    @Override
    public long invert(long rhs) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {

        int noChilden = parseTree.getChildCount();
        long solution;
        String operator;

        switch (noChilden) {
            case 1:
                return rhs;
            case 2:
                operator = parseTree.getChild(0).getText();
                solution = operator.equals("~") ? ~rhs
                        : operator.equals("-") ? -rhs
                        : throwNewRuntimeException("Not implemented");
                break;
            case 3:
                // Solve the equation
                // exp1 op exp2 = rhs
                // for op: "+", "-", "*", and "/"
                operator = parseTree.getChild(1).getText();
                Expression operand = new Expression(parseTree.expression(1));
                long exp2 = operand.toNumber();
                solution
                        = operator.equals("+") ? rhs - exp2
                        : operator.equals("-") ? rhs + exp2
                        : operator.equals("*") ? rhs / exp2
                        : operator.equals("/") ? rhs * exp2
                        : throwNewRuntimeException("Not implemented");
                break;
            default:
                throw new UnsupportedOperationException("Not implemented");
        }

        Expression exp1 = new Expression(parseTree.expression(0)); // contains exactly one name
        return exp1.invert(solution);
    }

    @Override
    public boolean isUnary() {
        return parseTree.getChildCount() == 1;
    }

    public int numberNames() {
        if (parseTree.primary_item() != null)
            return newPrimaryItem(parseTree.primary_item()).toName() != null ? 1 : 0;

        int number = 0;
        return parseTree.expression().stream().map((expression) -> new Expression(expression).numberNames()).reduce(number, Integer::sum);
    }

    @Override
    public Name toName() {
        if (parseTree.primary_item() != null)
            return newPrimaryItem(parseTree.primary_item()).toName();

        int number = numberNames();
        return (number == 1 && parseTree.expression(0) != null)
                ? new Expression(parseTree.expression(0)).toName()
                : null;
    }

    public long toNumber() throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return toNumber(parseTree, new NameEngine());
    }

    @Override
    public long toNumber(NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        return toNumber(parseTree, nameEngine);
    }

    private long toNumber(IrpParser.ExpressionContext ctx, NameEngine nameEngine) throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        int noChilden = ctx.getChildCount();
        return noChilden == 1
                ? toNumberPrimary(ctx.getChild(0), nameEngine)
                : noChilden == 2 ? toNumberUnary(ctx.getChild(0).getText(), (IrpParser.ExpressionContext)ctx.getChild(1), nameEngine)
                : noChilden == 3 ? toNumberBinary((IrpParser.ExpressionContext) ctx.getChild(0), ctx.getChild(1).getText(),
                        (IrpParser.ExpressionContext) ctx.getChild(2), nameEngine)
                : noChilden == 5 ? toNumberTernary((IrpParser.ExpressionContext) ctx.getChild(0), (IrpParser.ExpressionContext) ctx.getChild(2),
                        (IrpParser.ExpressionContext) ctx.getChild(4), nameEngine)
                : throwNewRuntimeException();
    }

    private long toNumberPrimary(ParseTree child, NameEngine nameEngine) throws IrpSyntaxException, IncompatibleArgumentException, UnassignedException {
        return child instanceof IrpParser.Primary_itemContext
                ? newPrimaryItem((IrpParser.Primary_itemContext) child).toNumber(nameEngine)
                : child instanceof IrpParser.BitfieldContext ? BitField.newBitField((IrpParser.BitfieldContext) child).toNumber(nameEngine)
                : throwNewRuntimeException();
    }

    private long toNumberUnary(String operator, IrpParser.ExpressionContext expressionContext, NameEngine nameEngine)
            throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long operand = new Expression(expressionContext).toNumber(nameEngine);
        return operator.equals("!") ? (operand == 0L ? 1L : 0L)
                : operator.equals("#") ? Long.bitCount(operand)
                : operator.equals("-") ? - operand
                : operator.equals("~") ? ~ operand
                : throwNewRuntimeException("Unknown operator: " + operator);
    }

    private long toNumberBinary(IrpParser.ExpressionContext expressionContext, String operator,
            IrpParser.ExpressionContext expressionContext0, NameEngine nameEngine)
            throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long left = new Expression(expressionContext).toNumber(nameEngine);
        long right = new Expression(expressionContext0).toNumber(nameEngine);

        return    operator.equals("**") ? IrpUtils.power(left, right)
                : operator.equals("*")  ? left * right
                : operator.equals("/")  ? left / right
                : operator.equals("%")  ? left % right
                : operator.equals("+")  ? left + right
                : operator.equals("-")  ? left - right
                : operator.equals("<<") ? left << right
                : operator.equals(">>") ? left >> right
                : operator.equals("<=") ? cBoolean(left <= right)
                : operator.equals(">=") ? cBoolean(left >= right)
                : operator.equals("<")  ? cBoolean(left < right)
                : operator.equals(">")  ? cBoolean(left > right)
                : operator.equals("==") ? cBoolean(left == right)
                : operator.equals("!=") ? cBoolean(left != right)
                : operator.equals("&")  ? left & right
                : operator.equals("^")  ? left ^ right
                : operator.equals("|")  ? left | right
                : operator.equals("&&") ? (left != 0 ? right : 0L)
                : operator.equals("||") ? (left != 0 ? left : right)
                : throwNewRuntimeException("Unknown operator: " + operator);
    }

    private long toNumberTernary(IrpParser.ExpressionContext expressionContext, IrpParser.ExpressionContext trueExpContext,
            IrpParser.ExpressionContext falseExpContext, NameEngine nameEngine)
            throws UnassignedException, IrpSyntaxException, IncompatibleArgumentException {
        long ctrl = new Expression(expressionContext).toNumber(nameEngine);
        return new Expression(ctrl != 0L ? trueExpContext : falseExpContext).toNumber(nameEngine);
    }

    private long throwNewRuntimeException() {
        throw new RuntimeException("This cannot happen");
    }

    private long throwNewRuntimeException(String msg) {
        throw new RuntimeException(msg);
    }

    private long cBoolean(boolean x) {
        return x ? 1L : 0L;
    }

    /**
     * @return the parseTree
     */
    IrpParser.ExpressionContext getParseTree() {
        return parseTree;
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        Element op;
        switch (parseTree.children.size()) {
            case 1:
                ParseTree child = parseTree.children.get(0);

                try {
                    if (child instanceof IrpParser.Primary_itemContext)
                        element.appendChild(newPrimaryItem((IrpParser.Primary_itemContext) child).toElement(document));
                    else if (child instanceof IrpParser.BitfieldContext)
                        element.appendChild(BitField.newBitField((IrpParser.BitfieldContext) child).toElement(document));
                    else
                        ;
                } catch (IrpSyntaxException ex) {
                    throw new ThisCannotHappenException(ex);
                }
                break;

            case 2:
                op = document.createElement("UnaryOperator");
                op.setAttribute("type", parseTree.children.get(0).getText());
                element.appendChild(op);
                op.appendChild(new Expression(parseTree.expression(0)).toElement(document));
                break;
            case 3:
                op = document.createElement("BinaryOperator");
                op.setAttribute("type", parseTree.children.get(1).getText());
                element.appendChild(op);
                op.appendChild(new Expression(parseTree.expression(0)).toElement(document));
                op.appendChild(new Expression(parseTree.expression(1)).toElement(document));
                break;
            case 5:
                op = document.createElement("TernaryOperator");
                element.appendChild(op);
                op.appendChild(new Expression(parseTree.expression(0)).toElement(document));
                op.appendChild(new Expression(parseTree.expression(1)).toElement(document));
                op.appendChild(new Expression(parseTree.expression(2)).toElement(document));
                break;
            default:
                element.setTextContent("Unknown case in Expression.toElement");
        }
        return element;
    }

    @Override
    public String toIrpString() {
        return toIrpString(10);
    }

    @Override
    public String toIrpString(int radix) {
        if (parseTree == null)
            return null;

        switch (parseTree.children.size()) {
            case 1:
                ParseTree child = parseTree.children.get(0);

                try {
                    if (child instanceof IrpParser.Primary_itemContext)
                        return newPrimaryItem((IrpParser.Primary_itemContext) child).toIrpString(radix);
                    else if (child instanceof IrpParser.BitfieldContext)
                        return BitField.newBitField((IrpParser.BitfieldContext) child).toIrpString();
                    else
                        return null;
                } catch (IrpSyntaxException ex) {
                    Logger.getLogger(Expression.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case 2:
                return "(" + parseTree.children.get(0).getText() + new Expression(parseTree.expression(0)).toIrpString() + ")";

            case 3:
                return "("
                        + new Expression(parseTree.expression(0)).toIrpString(radix)
                        + parseTree.children.get(1).getText()
                        + new Expression(parseTree.expression(1)).toIrpString(radix)
                        + ")";
            case 5:
                return "("
                        + new Expression(parseTree.expression(0)).toIrpString(radix) + "?"
                        + new Expression(parseTree.expression(1)).toIrpString(radix) + ":"
                        + new Expression(parseTree.expression(2)).toIrpString(radix)
                        + ")";
            default:
                throw new ThisCannotHappenException("Unknown case in Expression.toElement");
        }
        return null;
    }

    @Override
    public int weight() {
        int weight = 0;
        weight = parseTree.children.stream().filter((child) -> (child instanceof IrpObject)).map((child) -> ((IrpObject) child).weight()).reduce(weight, Integer::sum);
        return weight;
    }

    @Override
    public String code(boolean eval, CodeGenerator codeGenerator) {
        if (parseTree == null)
            return "";

        switch (parseTree.children.size()) {
            case 1:
                ParseTree child = parseTree.children.get(0);

                try {
                    if (child instanceof IrpParser.Primary_itemContext)
                        return newPrimaryItem((IrpParser.Primary_itemContext) child).code(eval, codeGenerator);
                    else if (child instanceof IrpParser.BitfieldContext)
                        return BitField.newBitField((IrpParser.BitfieldContext) child).code(eval, codeGenerator);
                    else
                        return "";
                } catch (IrpSyntaxException ex) {
                    Logger.getLogger(Expression.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case 2: {

                String operator = parseTree.children.get(0).getText();
                String templateName = operator.equals("~") ? "BitInvert"
                        : operator.equals("!") ? "Negate"
                        : operator.equals("-") ? "UnaryMinus"
                        : operator.equals("#") ? "BitCount"
                        : "Error";
                ItemCodeGenerator itemGenerator = codeGenerator.newItemCodeGenerator(templateName);
                String arg = new Expression(parseTree.expression(0)).code(eval, codeGenerator);
                itemGenerator.addAttribute("arg", arg);
                return itemGenerator.render();

                //return "(" + parseTree.children.get(0).getText() + new Expression(parseTree.expression(0)).toIrpString() + ")";
            }
            case 3: {
                String operator = parseTree.children.get(1).getText();
                String templateName = operator.equals("**") ? "Exponentiate"
                        : operator.equals("+") ? "Add"
                        : operator.equals("-") ? "Subtract"
                        : operator.equals("*") ? "Multiply"
                        : operator.equals("/") ? "Divide"
                        : operator.equals("%") ? "Modulo"
                        : operator.equals("<<") ? "ShiftLeft"
                        : operator.equals(">>") ? "ShiftRight"
                        : operator.equals("<=") ? "LessOrEqual"
                        : operator.equals(">=") ? "GreaterOrEqual"
                        : operator.equals(">") ? "Greater"
                        : operator.equals("<") ? "Less"
                        : operator.equals("==") ? "Equals"
                        : operator.equals("!=") ? "NotEquals"
                        : operator.equals("&") ? "BitwiseAnd"
                        : operator.equals("|") ? "BitwiseOr"
                        : operator.equals("^") ? "BitwiseExclusiveOr"
                        : operator.equals("&&") ? "And"
                        : operator.equals("||") ? "Or"
                        : "Error";
                ItemCodeGenerator itemGenerator = codeGenerator.newItemCodeGenerator(templateName);
                String arg1 = new Expression(parseTree.expression(0)).code(eval, codeGenerator);
                String arg2 = new Expression(parseTree.expression(1)).code(eval, codeGenerator);
                itemGenerator.addAttribute("arg1", arg1);
                itemGenerator.addAttribute("arg2", arg2);
                return itemGenerator.render();

                //return "(" + parseTree.children.get(0).getText() + new Expression(parseTree.expression(0)).toIrpString() + ")";
            }
//                return "("
//                        + new Expression(parseTree.expression(0)).toIrpString(radix)
//                        + parseTree.children.get(1).getText()
//                        + new Expression(parseTree.expression(1)).toIrpString(radix)
//                        + ")";
            case 5: {
                ItemCodeGenerator itemGenerator = codeGenerator.newItemCodeGenerator("Conditional");
                String arg1 = new Expression(parseTree.expression(0)).code(eval, codeGenerator);
                String arg2 = new Expression(parseTree.expression(1)).code(eval, codeGenerator);
                String arg3 = new Expression(parseTree.expression(2)).code(eval, codeGenerator);
                itemGenerator.addAttribute("arg1", arg1);
                itemGenerator.addAttribute("arg2", arg2);
                itemGenerator.addAttribute("arg3", arg3);
                return itemGenerator.render();
            }
//                return "("
//                        + new Expression(parseTree.expression(0)).toIrpString(radix) + "?"
//                        + new Expression(parseTree.expression(1)).toIrpString(radix) + ":"
//                        + new Expression(parseTree.expression(2)).toIrpString(radix)
//                        + ")";
            default:
                throw new ThisCannotHappenException("Unknown case in Expression.toElement");
        }
        return "";
    }
}
