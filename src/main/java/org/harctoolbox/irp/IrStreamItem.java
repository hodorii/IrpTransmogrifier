/*
Copyright (C) 2017 Bengt Martensson.

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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.harctoolbox.ircore.IrSignal;

/**
 * This class is an abstract superclass of the things that make up an IRStream (see "Directly known subclasses").
 *
 * @author Bengt Martensson
 */
public abstract class IrStreamItem extends IrpObject {
    public static IrStreamItem newIrStreamItem(String str) {
        return newIrStreamItem((new ParserDriver(str)).getParser().irstream_item());
    }

    public static IrStreamItem newIrStreamItem(IrpParser.Irstream_itemContext ctx) {
        ParseTree child = ctx.getChild(0);
        return (child instanceof IrpParser.VariationContext) ? new Variation(((IrpParser.VariationContext) child))
                : (child instanceof IrpParser.BitfieldContext) ? BitField.newBitField((IrpParser.BitfieldContext) child)
                : (child instanceof IrpParser.AssignmentContext) ? new Assignment((IrpParser.AssignmentContext) child)
                //: (child instanceof IrpParser.ExtentContext) ? new Extent((IrpParser.ExtentContext) child)
                : (child instanceof IrpParser.DurationContext) ? Duration.newDuration((IrpParser.DurationContext) child)
                : (child instanceof IrpParser.IrstreamContext) ? new IrStream((IrpParser.IrstreamContext) child)
                : (child instanceof IrpParser.Bitspec_irstreamContext) ? new BitspecIrstream((IrpParser.Bitspec_irstreamContext) child)
                : null;
    }

    protected IrStreamItem() {
    }
    public abstract boolean isEmpty(NameEngine nameEngine) throws UnassignedException, IrpSemanticException;

    /**
     *
     * @param state
     * @param pass
     * @param nameEngine
     * @param generalSpec
     * @return EvaluatedIrStream or null if termination requested.
     * @throws InvalidArgumentException
     * @throws ArithmeticException
     * @throws UnassignedException
     * @throws IrpSyntaxException
     */
    abstract EvaluatedIrStream evaluate(IrSignal.Pass state, IrSignal.Pass pass, NameEngine nameEngine, GeneralSpec generalSpec)
            throws UnassignedException, InvalidNameException;

    int numberOfBitSpecs() {
        return 0;
    }

    /**
     *
     * @param nameEngine nameengine, or null
     * @param generalSpec generalspec, or null
     * @param last
     * @param gapFlashBitSpecs
     * @return
     */
    public abstract boolean interleavingOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs);

    public abstract boolean interleavingOk(DurationType toCheck, NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs);

    public abstract DurationType endingDurationType(DurationType last, boolean gapFlashBitSpecs);

    public abstract DurationType startingDuratingType(DurationType last, boolean gapFlashBitSpecs);

    abstract int numberOfBits() throws UnassignedException;

    abstract int numberOfBareDurations();

    public IrSignal.Pass stateWhenEntering(IrSignal.Pass pass) {
        return null;
    }

    public IrSignal.Pass stateWhenExiting(IrSignal.Pass pass) {
        return null;
    }

    abstract ParserRuleContext getParseTree();

    public abstract boolean recognize(RecognizeData recognizeData, IrSignal.Pass pass, List<BitSpec> bitSpecs)
            throws UnassignedException, InvalidNameException, NameConflictException, IrpSemanticException;

    public abstract boolean hasExtent();

    public abstract Set<String> assignmentVariables();

    public abstract Map<String, Object> propertiesMap(IrSignal.Pass state, IrSignal.Pass pass, GeneralSpec generalSpec, NameEngine nameEngine);

    protected Map<String, Object> propertiesMap(int noProperites) {
        return IrpUtils.propertiesMap(noProperites, this);
    }

    double microSeconds(NameEngine nameEngine, GeneralSpec generalSpec) throws IrpException {
        throw new IrpException(this.getClass().getSimpleName() + " does not implement microSeconds()");
    }
}
