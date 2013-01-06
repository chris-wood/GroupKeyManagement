/*
 * OptimizedGDH.java
 *
 */

/**
 *  This interface contains a method used for optimized version of authenticated GDH.
 *
 * @author    Jisoo Kim(jsk4445@cs.rit.edu)
 * @version   3-May-2005
 *
 */
public interface OptimizedGDHInterface extends GroupDiffieHellmanInterface
{
    
	/**
	 * Pre-compute as many as parameters to be used in upflow() and finish()
	 *
	 */
	public void precomputeK();

} // OptimizedGDHInterface
