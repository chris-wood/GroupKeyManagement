/*
 * GroupDiffieHellman.java
 *
 */

/**
 * Provides the motheds for Group Diffie-Hellman protocol suites
 *
 * @author Jisoo Kim(jsk4445@cs.rit.edu)
 * @version 3-May-2005
 *
 */
public interface GroupDiffieHellmanInterface
{

	/**
	 * Performs the upflow stage of each member in Group Diffie-Hellman protocol suites.
	 * Computes the intermediate values for the next member in the protocol
	 *
	 * @return time elapsed to calculate intermediate values
	 */
	public long upflow();

	/**
	 * Computes the group key shared during Group Diffie-Hellman from the intermediate 
	 * value
	 *
	 * @return time elapsed to calculate the group key from the intermediate value
	 */
	public long finish();

}  // GroupDiffieHellmanInterface
