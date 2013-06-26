package fr.inria.acacia.corese.api;

import fr.inria.acacia.corese.cg.datatype.ICoresePolymorphDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * This is an interface for all Corese datatypes.<br />
 *
 * This is an interface for all xsd:datatypes: each has a normalized label and a
 * lower case label, that are comparable with an other datatype(instance). Each
 * can also have a value space (which is a string or not and so allow regular
 * expression matching) that have an order relation.
 *
 * @author Olivier Corby & Olivier Savoie & Virginie Bottollier
 */
public interface IDatatype
        extends ICoresePolymorphDatatype, Node, Entity {

    static final int LITERAL = 0;
    static final int STRING = 1;
    static final int XMLLITERAL = 2;
    static final int NUMBER = 3;
    static final int DATE = 4;
    static final int BOOLEAN = 5;
    static final int STRINGABLE = 6;
    static final int URI = 7;
    static final int UNDEF = 8;
    static final int BLANK = 9;
    static final int DOUBLE = 11;
    static final int FLOAT = 12;
    static final int DECIMAL = 13;
    static final int LONG = 14;
    static final int INTEGER = 15;
    // Pseudo codes (target is Integer or String ...)
    static final int DAY = 21;
    static final int MONTH = 22;
    static final int YEAR = 23;
    static final int DURATION = 24;
    static final int DATETIME = 25;

    /**
     * @return true if we have a blanknode
     */
    boolean isBlank();

    boolean isSkolem();

    boolean isXMLLiteral();

    boolean isArray();

    IDatatype[] getValues();

    IDatatype get(int n);

    int size();

    /**
     * @return true if we have a literal
     */
    boolean isLiteral();

    /**
     * Compare 2 datatypes
     *
     * @param dt2 another datatype
     * @return 0 if they are equals, an int > 0 if the datatype is greater than
     * dt2, an int < 0 if the datatype is lesser
     */
    int compareTo(IDatatype dt2);

    /**
     * Cast a value
     *
     * @param target the java type (ex: xsd:integer)
     * @param javaType the CoreseDatatype that corresponds (ex: CoreseInteger)
     * @return the datatype casted
     */
    IDatatype cast(IDatatype target, IDatatype javaType);

    /**
     * @return the lang as a datatype
     */
    IDatatype getDataLang();

    /**
     * @return the Sparql form of the datatype
     */
    String toSparql();

    String toSparql(boolean prefix);

    /**
     * @return true if we have an URI
     */
    boolean isURI();

    // Used by XMLLiteral to store a XML DOM 
    void setObject(Object obj);

    Object getObject();

    /**
     * ************************************************************************
     */
    /**
     * test if this.getLowerCaseLabel() contains iod.getLowerCaseLabel()
     *
     * @param iod the instance to be tested with
     * @return this.getLowerCaseLabel() contains iod.getLowerCaseLabel()
     */
    boolean contains(IDatatype iod);

    /**
     * test if this.getLowerCaseLabel() starts with iod.getLowerCaseLabel()
     *
     * @param iod the instance to be tested with
     * @return this.getLowerCaseLabel() starts with iod.getLowerCaseLabel()
     */
    boolean startsWith(IDatatype iod);

    /**
     * test the equality (by value) between two instances of datatype class
     *
     * @param iod the instance to be tested with this
     * @return true if the param has the same runtime class and if values are
     * equals, else false note: equals correponds to the SPARQL equals, with
     * type checking
     */
    boolean equals(IDatatype iod) throws CoreseDatatypeException;

    /**
     * test the equality (by value) between two instances of datatype class
     *
     * @param iod the instance to be tested with this
     * @return true if the param has the same runtime class and if values are
     * equals, else false
     */
    boolean sameTerm(IDatatype iod);

    /**
     *
     * @param iod
     * @return iod.getValue() < this.getValue() @throws Core
     * seDatatypeException
     */
    boolean less(IDatatype iod) throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() <= to this.getValue() @throws CoreseDa
     * tatypeException
     */
    boolean lessOrEqual(IDatatype iod)
            throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() > this.getValue()
     * @throws CoreseDatatypeException
     */
    boolean greater(IDatatype iod) throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() >= to this.getValue()
     * @throws CoreseDatatypeException
     */
    boolean greaterOrEqual(IDatatype iod)
            throws CoreseDatatypeException;

    /**
     *
     * @param iod
     * @return iod.getValue() + this.getValue()
     */
    IDatatype plus(IDatatype iod);

    /**
     *
     * @param iod
     * @return iod.getValue() - this.getValue()
     */
    IDatatype minus(IDatatype iod);

    /**
     *
     * @param iod
     * @return iod.getValue() * this.getValue()
     */
    IDatatype mult(IDatatype iod);

    /**
     *
     * @param iod
     * @return iod.getValue() / this.getValue()
     */
    IDatatype div(IDatatype iod);

    /**
     * ************************************************************************
     */
    /**
     * @return the datatype of this
     */
    IDatatype getDatatype();

    // same as getDatatype but URI return rdfs:Resource
    IDatatype getIDatatype();

    /**
     * @return the lang of this ('fr', 'en',...)
     */
    String getLang();

    /**
     * @return the datatype of this as a URI
     */
    String getDatatypeURI();

    /**
     * @return the string depending on the datatype
     * <br>representing the value of this
     */
    String getLabel();

    String getID();

    StringBuilder getStringBuilder();

    /**
     * @return true if this instance class is a number
     */
    boolean isNumber();

    double doubleValue();

    float floatValue();

    long longValue();

    int intValue();

    /**
     * ************************************************
     */
    @Deprecated
    double getDoubleValue();

    @Deprecated
    int getIntegerValue();

    @Deprecated
    int getiValue();

    @Deprecated
    double getdValue();

    @Deprecated
    String getNormalizedLabel();

    @Deprecated
    IDatatype getExtDatatype();

    @Deprecated
    String getLowerCaseLabel();
}
