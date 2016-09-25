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

package org.harctoolbox.analyze;

import java.util.ArrayList;
import java.util.List;
import org.harctoolbox.irp.BitDirection;
import org.harctoolbox.irp.IrStreamItem;

public class PwmDecoder extends AbstractDecoder {

    private final Burst zero;
    private final Burst one;

    public PwmDecoder(Analyzer analyzer, int timebase, Burst zero, Burst one) {
        super(analyzer, timebase, mkBitSpec(zero, one, timebase));
        this.zero = zero;
        this.one = one;
    }

    public PwmDecoder(Analyzer analyzer, int timebase, int zeroFlash, int zeroGap, int oneFlash, int oneGap) {
        this(analyzer, timebase, new Burst(zeroFlash, zeroGap), new Burst(oneFlash, oneGap));
    }

    public PwmDecoder(Analyzer analyzer, int timebase, int a, int b) {
        this(analyzer, timebase, a, a, a, b);
    }

    @Override
    protected List<IrStreamItem> process(int beg, int length, BitDirection bitDirection, boolean useExtents, List<Integer> parameterWidths) {
        List<IrStreamItem> items = new ArrayList<>(16);
        ParameterData data = new ParameterData();
        for (int i = beg; i < beg + length - 1; i += 2) {
            int noBitsLimit = getNoBitsLimit(parameterWidths);
            int mark = analyzer.getCleanedTime(i);
            int space = analyzer.getCleanedTime(i + 1);
            Burst burst = new Burst(mark, space);
            if (burst.equals(zero)) {
                data.update(0);
            } else if (burst.equals(one)) {
                data.update(1);
            } else {
                if (!data.isEmpty()) {
                    saveParameter(data, items, bitDirection);
                    data = new ParameterData();
                }

                items.add(newFlash(mark));
                if (i == beg + length - 2 && useExtents)
                    items.add(newExtent(analyzer.getTotalDuration(beg, length)));
                else
                    items.add(newGap(space));
            }

            if (data.getNoBits() >= noBitsLimit) {
                saveParameter(data, items, bitDirection);
                data = new ParameterData();
            }
        }
        if (!data.isEmpty())
            saveParameter(data, items, bitDirection);

        return items;
    }
}