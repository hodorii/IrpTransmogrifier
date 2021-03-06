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
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.harctoolbox.ircore.IrCoreUtils;
import org.harctoolbox.ircore.IrSignal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NamedProtocol extends Protocol {
    private final static Logger logger = Logger.getLogger(Protocol.class.getName());

    public static Document toDocument(Iterable<NamedProtocol> protocols) {
        Document document = XmlUtils.newDocument();
        Element root = document.createElement("NamedProtocols");
        document.appendChild(root);
        for (NamedProtocol protocol : protocols) {
            Element el = protocol.toElement(document);
            root.appendChild(el);
        }
        return document;
    }

    private static void putParameter(Map<String, Object> map, String parameterName, Double userValue, Double databaseValue) {
        if (userValue != null)
            map.put(parameterName, userValue);
        else if (databaseValue != null)
            map.put(parameterName, databaseValue);
        else
            ;
    }

    private final String irp; // original one on input, not canonicalized
    private final String name;
    private final String documentation;
    private final Double absoluteTolerance;
    private final Double relativeTolerance;
    private final Double frequencyTolerance;
    private final boolean decodable;
    private final List<String> preferOver;

    public NamedProtocol(String name, String irp, String documentation, String frequencyTolerance,
            String absoluteTolerance, String relativeTolerance, String decodable,
            List<String> preferOver)
            throws IrpSemanticException, InvalidNameException, UnassignedException {
        super(irp);
        this.irp = irp;
        this.name = name;
        this.documentation = documentation;
        this.frequencyTolerance = frequencyTolerance != null ? Double.parseDouble(frequencyTolerance) : null;
        this.absoluteTolerance = absoluteTolerance != null ? Double.parseDouble(absoluteTolerance) : null;
        this.relativeTolerance = relativeTolerance != null ? Double.parseDouble(relativeTolerance) : null;
        this.decodable = decodable == null || Boolean.parseBoolean(decodable);
        this.preferOver = preferOver;
    }

    public NamedProtocol(String name, String irp, String documentation) throws IrpSemanticException, InvalidNameException, UnassignedException {
        this(name, irp, documentation, null, null, null, null, null);
    }

    public Map<String, Long> recognize(IrSignal irSignal, boolean keepDefaulted,
            Double userFrequencyTolerance, Double userAbsoluteTolerance, Double userRelativeTolerance) {
        if (!isDecodeable()) {
            logger.log(Level.FINE, "Protocol {0} is not decodeable, skipped", getName());
            return null;
        }
        return super.recognize(irSignal, keepDefaulted,
                getFrequencyTolerance(userFrequencyTolerance),
                getAbsoluteTolerance(userAbsoluteTolerance), getRelativeTolerance(userRelativeTolerance));

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.irp);
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.documentation);
        hash = 41 * hash + Objects.hashCode(this.absoluteTolerance);
        hash = 41 * hash + Objects.hashCode(this.relativeTolerance);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NamedProtocol))
            return false;

        NamedProtocol other = (NamedProtocol) obj;
        return super.equals(obj)
                && irp.equals(other.irp)
                && name.equals(other.name)
                && documentation.equals(other.documentation)
                && Double.compare(absoluteTolerance, other.absoluteTolerance) == 0
                && Double.compare(relativeTolerance, other.relativeTolerance) == 0;
    }

    @Override
    public String toString() {
        return name + ": " + super.toString();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the documentation
     */
    public String getDocumentation() {
        return documentation;
    }

    public String getIrp() {
        return irp;
    }

    public boolean isDecodeable() {
        return decodable;
    }

    private double getDoubleWithSubstitute(Double userValue, Double standardValue, double fallback) {
        return userValue != null ? userValue
                : standardValue != null ? standardValue
                : fallback;
    }

    public double getRelativeTolerance(Double userValue) throws NumberFormatException {
        return getDoubleWithSubstitute(userValue, relativeTolerance, IrCoreUtils.defaultRelativeTolerance);
    }

    public double getAbsoluteTolerance(Double userValue) throws NumberFormatException {
        return getDoubleWithSubstitute(userValue, absoluteTolerance, IrCoreUtils.defaultAbsoluteTolerance);
    }

    public double getFrequencyTolerance(Double userValue) throws NumberFormatException {
        return getDoubleWithSubstitute(userValue, frequencyTolerance, IrCoreUtils.defaultFrequencyTolerance);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    List<String> getPreferOver() {
        return preferOver;
    }

    @Override
    public Document toDocument() {
        return toDocument(false);
    }

    public Document toDocument(boolean split) {
        Document document = XmlUtils.newDocument();
        document.appendChild(toElement(document, split));
        return document;
    }

    @Override
    public Element toElement(Document document) {
        return toElement(document, false);
    }

    @Override
    public Element toElement(Document document, boolean split) {
        Element root = super.toElement(document, split);
        root.setAttribute("name", getName());

        Element docu = document.createElement("Documentation");
        docu.appendChild(document.createTextNode(getDocumentation()));
        root.appendChild(docu);

        Element irpElement = document.createElement("Irp");
        irpElement.appendChild(document.createTextNode(getIrp()));
        root.appendChild(irpElement);

        return root;
    }

    ItemCodeGenerator code(CodeGenerator codeGenerator, Map<String, String> parameters, Double absoluteTolerance, Double relativeTolerance, Double frequencyTolerance) {
        ItemCodeGenerator template = codeGenerator.newItemCodeGenerator(this);
        template.addAggregateList("metaData", metaDataPropertiesMap(parameters, absoluteTolerance, relativeTolerance, frequencyTolerance));
        template.addAggregateList("generalSpec", getGeneralSpec(), getGeneralSpec(), getDefinitions());
        template.addAggregateList("parameterSpecs", getParameterSpecs(), getGeneralSpec(), getDefinitions());
        Set<String> variables = getBitspecIrstream().assignmentVariables();
        Set<String> params = getParameterSpecs().getNames();
        variables.removeAll(params);
        template.addAttribute("assignmentVariables", variables);
        template.addAggregateList("definitions", getDefinitions(), getGeneralSpec(), getDefinitions());

        template.addAggregateList("code", getBitspecIrstream().getIrStream(), getGeneralSpec(), getDefinitions());
        template.addAggregateList("bitSpec", getBitspecIrstream().getBitSpec(), getGeneralSpec(), getDefinitions());
        return template;
    }

    private Map<String, Object> metaDataPropertiesMap(Map<String, String> parameters, Double userAbsoluteTolerance, Double userRelativeTolerance, Double userFrequencyTolerance) {
        Map<String, Object> map = IrpUtils.propertiesMap(parameters.size() + 11, this);
        map.put("protocolName", getName());
        map.put("cProtocolName", IrpUtils.toCIdentifier(getName()));
        map.put("irp", getIrp());
        map.put("documentation", IrCoreUtils.javaifyString(getDocumentation()));
        map.put("sonyType", isSonyType());
        map.put("interleavingOk", interleavingOk());
        map.put("interleavingFlashOk", interleavingFlashOk());
        map.put("interleavingGapOk", interleavingGapOk());
        putParameter(map, "relativeTolerance", userRelativeTolerance, relativeTolerance);
        putParameter(map, "absoluteTolerance", userAbsoluteTolerance, absoluteTolerance);
        putParameter(map, "frequencyTolerance", userFrequencyTolerance, frequencyTolerance);
        map.putAll(parameters);
        return map;
    }
}
