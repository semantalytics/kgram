package fr.inria.acacia.corese.exceptions;

/**
 * This class gathers all the exceptions that can be thrown in Corese
 * @author Virginie Bottollier
 */
public class EngineException extends Exception {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	protected EngineException() {
		super();
	}
	
	protected EngineException(Exception e) {
		super(e);
	}

	public EngineException(String message) {
		super(message);
	}

	protected EngineException(Error e) {
		super(e);
	}
	
}
