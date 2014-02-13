//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.24 at 10:15:40 AM CEST 
//


package fr.inria.edelweiss.rif.xml.schema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for And-meta.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="And-meta.type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="formula" type="{http://www.w3.org/2007/rif#}formula-meta.type" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "And-meta.type", propOrder = {
    "formula"
})
public class AndMetaType {

    protected List<FormulaMetaType> formula;

    /**
     * Gets the value of the formula property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formula property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormula().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FormulaMetaType }
     * 
     * 
     */
    public List<FormulaMetaType> getFormula() {
        if (formula == null) {
            formula = new ArrayList<FormulaMetaType>();
        }
        return this.formula;
    }

	public fr.inria.edelweiss.rif.ast.And<fr.inria.edelweiss.rif.ast.Frame> XML2AST() {
		fr.inria.edelweiss.rif.ast.And<fr.inria.edelweiss.rif.ast.Frame> and = fr.inria.edelweiss.rif.ast.And.create() ;
		for(FormulaMetaType fmt : this.getFormula()) {
			and.add(fmt.getFrame().XML2AST()) ;
		}
		return and ;
	}

}
