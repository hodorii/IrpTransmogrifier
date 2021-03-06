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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.IrSignal;
import org.harctoolbox.ircore.OddSequenceLenghtException;
import org.harctoolbox.ircore.ThisCannotHappenException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements BitSpecs, as described in Chapter 7.
 *
 */
public class BitSpec extends IrpObject implements AggregateLister {

    // Computes the upper integer part of the 2-logarithm of the integer n.
    // Treat n = 0 and n = 1 differently, since coding on a zero or one-letter alphaber is ... special.
    private static int computeNoBits(int n) {
        if (n == 0)
            return 0;
        if (n == 1)
            return 1;
        int x = n-1;
        int m;
        for (m = 0; x != 0; m++)
            x >>= 1;
        return m;
    }

    private static List<BareIrStream> parse(List<IrpParser.Bare_irstreamContext> list) {
        List<BareIrStream> result = new ArrayList<>(list.size());
        list.stream().forEach((bareIrStreamCtx) -> {
            result.add(new BareIrStream(bareIrStreamCtx));
        });

        return result;
    }

    // Number of bits encoded
    private int chunkSize;

    private List<BareIrStream> bitCodes;

    public BitSpec(String str) {
        this(new ParserDriver(str).getParser().bitspec());
    }

    public BitSpec(IrpParser.BitspecContext ctx) {
        this(parse(ctx.bare_irstream()));
    }


    public BitSpec(List<BareIrStream> list) {
        chunkSize = computeNoBits(list.size());
        bitCodes = list;
    }

    public BitSpec() {
        chunkSize = 0;
        bitCodes = new ArrayList<>(2);
    }

    public int noDurations() {
        int result = -1;
        for (BareIrStream bitCode : bitCodes) {
            if (bitCode.numberOfBitSpecs() > 0)
                return -1;
            int curr = bitCode.numberOfBareDurations();
            if (result > 0 && result != curr)
                return -1;
            result = curr;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BitSpec))
            return false;

        BitSpec other = (BitSpec) obj;
        if (chunkSize != other.getChunkSize())
            return false;

        if (bitCodes.size() != other.bitCodes.size())
            return false;

        for (int i = 0; i < bitCodes.size(); i++)
            if (!bitCodes.get(i).equals(other.bitCodes.get(i)))
                return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.chunkSize;
        hash = 53 * hash + Objects.hashCode(this.bitCodes);
        return hash;
    }

    public int size() {
        return bitCodes.size();
    }

    @Override
    public int numberOfInfiniteRepeats() {
        int sum = 0;
        sum = bitCodes.stream().map((code) -> code.numberOfInfiniteRepeats()).reduce(sum, Integer::sum);
        return sum;
    }

    public BareIrStream get(int index) {
        if (index >= bitCodes.size())
            throw new ThisCannotHappenException("Cannot encode " + index + " with current bitspec.");
        return bitCodes.get(index);
    }

    @Override
    public String toString() {
        if (bitCodes.isEmpty())
            return "<null>";

        StringBuilder s = new StringBuilder(bitCodes.size()*10);
        s.append("<").append(bitCodes.get(0));
        for (int i = 1; i < bitCodes.size(); i++) {
            s.append("|").append(bitCodes.get(i));
        }
        return s.append(">").toString();
    }

    @Override
    public String toIrpString() {
        StringBuilder s = new StringBuilder(bitCodes.size()*10);
        s.append("<");
        List<String> list = new ArrayList<>(bitCodes.size() * 20);
        bitCodes.stream().forEach((bitCode) -> {
            list.add(bitCode.toIrpString());
        });

        return s.append(String.join("|", list)).append(">").toString();
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public boolean isEmpty() {
        return bitCodes.isEmpty();
    }

    public int numberOfBitspecDurations() {
        int numberDurations = -1;
        for (BareIrStream bitCode : bitCodes) {
            int n = bitCode.numberOfBareDurations();
            if (numberDurations == -1)
                numberDurations = n;
            else if (numberDurations == n) {
                // ok
            } else {
                return -1;
            }
        }
        return numberDurations;
    }

   /**
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isPWM(NameEngine nameEngine, GeneralSpec generalSpec) {
        return isPWM(2, nameEngine, generalSpec);
    }

    /**
     * @param length
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isPWM(int length, NameEngine nameEngine, GeneralSpec generalSpec) {
        return bitCodes.size() == length && isTwoLengthGapFlash(nameEngine, generalSpec);
    }

    private boolean isTwoLengthGapFlash(NameEngine nameEngine, GeneralSpec generalSpec) {
        for (BareIrStream bitCode : bitCodes) {
            try {
                // toIrSequence throws exception if not positive, negative
                IrSequence irSequence = bitCode.evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec).toIrSequence();
                if (irSequence.getLength() != 2)
                    return false;
            } catch (IrpException | ArithmeticException | OddSequenceLenghtException ex) {
                return false;
            }
        }
        return true;
    }

    boolean isSonyType(NameEngine nameEngine, GeneralSpec generalSpec) {
        try {
            return isPWM(nameEngine, generalSpec)
                    && ! IrCoreUtils.approximatelyEquals(bitCodes.get(0).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec),
                            bitCodes.get(1).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec));
        } catch (IrpException ex) {
            return false;
        }
    }

    /**
     * Checks if the BitSpec is of type &lt;a,-a|-a,a&gt; (a != 0)
     * @param nameEngine
     * @param generalSpec
     * @return
     */
    public boolean isStandardBiPhase(NameEngine nameEngine, GeneralSpec generalSpec) {
        if (bitCodes.size() != 2)
            return false;

        Double a = null;
        for (BareIrStream bitCode : bitCodes) {
            try {
                EvaluatedIrStream on = bitCode.evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec);
                if (on.getLenght() != 2)
                    return false;
                if (a == null)
                    a = on.get(0);
                if (! (IrCoreUtils.approximatelyEquals(a, on.get(0), 1, 0) && IrCoreUtils.approximatelyEquals(-a, on.get(1), 1, 0)))
                    return false;
                a = -a;
            } catch (UnassignedException | InvalidNameException | IrpSemanticException ex) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the BitSpec is of type &lt;a|-a&gt; (a != 0)
     * @param nameEngine
     * @param generalSpec
     * @param inverted
     * @return
     */
    public boolean isTrivial(NameEngine nameEngine, GeneralSpec generalSpec, boolean inverted) {
        if (bitCodes.size() != 2)
            return false;
        try {
            EvaluatedIrStream off = bitCodes.get(0).evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec);
            EvaluatedIrStream on = bitCodes.get(1).evaluate(IrSignal.Pass.intro, IrSignal.Pass.intro, nameEngine, generalSpec);
            if (on.getLenght() != 1 || off.getLenght() != 1)
                return false;

            boolean sign = off.get(0) > 0;
            return IrCoreUtils.approximatelyEquals(on.get(0), -off.get(0)) && (sign == inverted);
        } catch (UnassignedException | InvalidNameException | IrpSemanticException ex) {
            return false;
        }
    }

    @Override
    public Element toElement(Document document) {
        Element element = super.toElement(document);
        element.setAttribute("size", Integer.toString(bitCodes.size()));
        element.setAttribute("chunkSize", Integer.toString(chunkSize));
        element.setAttribute("bitMask", Integer.toString(IrCoreUtils.ones(chunkSize)));
        element.setAttribute("pwm2", Boolean.toString(isPWM(2, new NameEngine(), new GeneralSpec())));
        element.setAttribute("standardBiPhase", Boolean.toString(isStandardBiPhase(new NameEngine(), new GeneralSpec())));
        if (noDurations() > 0)
            element.setAttribute("noDurations", Integer.toString(noDurations()));
        bitCodes.forEach((bitCode) -> {
            element.appendChild(bitCode.toElement(document));
        });
        return element;
    }

    boolean interleaveOk(NameEngine nameEngine, GeneralSpec generalSpec, DurationType last, boolean gapFlashBitSpecs) {
        if (!gapFlashBitSpecs && (isPWM(2, nameEngine, generalSpec)
                || isPWM(4, nameEngine, generalSpec)))
            return true;

        return bitCodes.stream().noneMatch((bareIrStream) -> (bareIrStream.getIrStreamItems().size() < 2 || !bareIrStream.interleavingOk(nameEngine, generalSpec, last, gapFlashBitSpecs)));
    }

    @Override
    public int weight() {
        int weight = 0;
        weight = bitCodes.stream().map((bitCode) -> bitCode.weight()).reduce(weight, Integer::sum);
        // if weight is 2, then it is something like <a|-b> (serial) very nasty, penalize that nasty bitspec!
        return weight != 2 ? weight : 100;
    }

    public boolean hasExtent() {
        return bitCodes.stream().anyMatch((bitCode) -> (bitCode.hasExtent()));
    }

    private List<Map<String, Object>> propertiesMap(boolean reverse, GeneralSpec generalSpec, NameEngine nameEngine) {
        List<Map<String, Object>> list = new ArrayList<>(bitCodes.size());
        bitCodes.stream().map((bitCode) -> bitCode.propertiesMap(IrSignal.Pass.intro, IrSignal.Pass.intro, generalSpec, nameEngine)).forEach((map) -> {
            list.add(map);
        });

        return list;
    }

    Set<String> assignmentVariables() {
        Set<String> list = new HashSet<>(1);
        bitCodes.stream().forEach((bitCode) -> {
            list.addAll(bitCode.assignmentVariables());
        });
        return list;
    }

    @Override
    public Map<String, Object> propertiesMap(GeneralSpec generalSpec, NameEngine nameEngine) {
        Map<String, Object> map = new HashMap<>(17);
        if (chunkSize > 1)
            map.put("chunkSize", chunkSize);
        map.put("bitMask", IrCoreUtils.ones(chunkSize));
        map.put("size", bitCodes.size());
        if (isPWM(2, new NameEngine(), new GeneralSpec())) {
            map.put("pwm2", true);
            try {
                map.put("zeroGap",   bitCodes.get(0).getIrStreamItems().get(1).microSeconds(nameEngine, generalSpec));
                map.put("zeroFlash", bitCodes.get(0).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec));
                map.put("oneGap",    bitCodes.get(1).getIrStreamItems().get(1).microSeconds(nameEngine, generalSpec));
                map.put("oneFlash",  bitCodes.get(1).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec));
                map.put("flashesDiffer", !IrCoreUtils.approximatelyEquals(
                        bitCodes.get(0).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec),
                        bitCodes.get(0).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec)));
            } catch (IrpException ex) {
                Logger.getLogger(BitSpec.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (isPWM(4, new NameEngine(), new GeneralSpec())) {
            map.put("pwm4", true);
            try {
                map.put("zeroGap",   bitCodes.get(0).getIrStreamItems().get(1).microSeconds(nameEngine, generalSpec));
                map.put("zeroFlash", bitCodes.get(0).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec));
                map.put("oneGap",    bitCodes.get(1).getIrStreamItems().get(1).microSeconds(nameEngine, generalSpec));
                map.put("oneFlash",  bitCodes.get(1).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec));
                map.put("twoGap",    bitCodes.get(2).getIrStreamItems().get(1).microSeconds(nameEngine, generalSpec));
                map.put("twoFlash",  bitCodes.get(2).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec));
                map.put("threeGap",  bitCodes.get(3).getIrStreamItems().get(1).microSeconds(nameEngine, generalSpec));
                map.put("threeFlash",bitCodes.get(3).getIrStreamItems().get(0).microSeconds(nameEngine, generalSpec));
            } catch (IrpException ex) {
                Logger.getLogger(BitSpec.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (isStandardBiPhase(new NameEngine(), new GeneralSpec())) {
            map.put("standardBiPhase", true);
            try {
                map.put("biPhaseHalfPeriod", averageDuration(nameEngine, generalSpec));
            } catch (IrpException ex) {
            }
            map.put("biPhaseInverted", bitCodes.get(0).getIrStreamItems().get(0) instanceof Flash);
        }
        map.put("lsbFirst", generalSpec.getBitDirection() == BitDirection.lsb);
        if (noDurations() > 0)
            map.put("noDurations", noDurations());
        map.put("list", propertiesMap(false, generalSpec, nameEngine));
        return map;
    }

    double averageDuration(NameEngine nameEngine, GeneralSpec generalSpec) throws IrpException {
        double sum = 0;
        for (BareIrStream bitCode : bitCodes) {
            sum += bitCode.averageDuration(nameEngine, generalSpec);
        }
        return sum / bitCodes.size();
    }
}
