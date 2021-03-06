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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class IrpObject implements XmlExport {

    public abstract String toIrpString();

    @Override
    public abstract boolean equals(Object obj);

    public int numberOfInfiniteRepeats() {
        return 0;
    }

    /**
     * Returns a (somewhat arbitrary) measure of the complexity of the object. Can be used
     * for determining if a decode is "simpler" than another decode.
     * @return non-negative integer.
     */
    public abstract int weight();

    @Override
    public abstract int hashCode();

    @Override
    public Element toElement(Document document) {
        return document.createElement(getClass().getSimpleName());
    }

    @Override
    public Document toDocument() {
        Document document = XmlUtils.newDocument();
        Element element = toElement(document);
        document.appendChild(element);
        return document;
    }
}
