/**
 * MACErrorException.java
 *
 */

/**
 * Thrown to indicate that the Mac tag is corrupted or an error occurred during handling
 * Mac tags.
 *
 * @author      Jisoo Kim(jsk4445.cs.rit.edu)
 * @version	    3-May-2005
 *
 */

public class MACErrorException extends RuntimeException 
{

	/**
	 * Contructs an <code>MACErrorException</code> with no detail message
	 *
	 */
	public MACErrorException() 
	{
		super();
	}

	/**
	 * Contructs an <code>MACErrorException</code> with a specified message
	 *
	 * @param errorMessage the detail message
	 */
	public MACErrorException(String errorMessage) 
	{
		super(errorMessage);
	}
    
} // MACErrorException
