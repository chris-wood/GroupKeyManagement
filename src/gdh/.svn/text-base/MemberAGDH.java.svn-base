/*
 * MemberAGDH.java
 *
 */

import java.math.*;
import java.util.*;

/**
 * This class represents a member in authenticated version of Group Diffie-
 * Hellman(A-GDH).<br>
 * Constructor is initialized with several parameters such as group size, 
 * index of this member, safe prime p = 2*q + 1 where q is a large prime, etc.<br>
 * In A-GDH protocol, every member is assumed to have shared the long-term
 * public key with each other. However, this is not optimized so that the
 * long-term secret and its modulous inverse should be computed in upflow()
 * and finish().<br>
 * Once each member is created, upflow() method will be invoked in order to 
 * proceed the upflow stage in A-GDH protocol which is the same as non-
 * authneticated Group Diffie-Hellman except the group controller.
 * The group controller (last member in the protocol) hides the intermediate
 * values for other members using the long-term private key.
 * At last, finish() method will compute the group key from intermediate value
 * using the long-term private key shared with the group controller.
 *
 *
 * @author      Jisoo Kim(jsk4445@cs.rit.edu)
 * @version     18-May-2005
 *
 */

public class MemberAGDH extends MemberGDH implements GroupDiffieHellmanInterface
{
    
	//
	// protected variables 
	//    

	/**
	 *  Long-term secret of this member   
	 */
	protected BigInteger longtermSecret;

	/**
	 *  Array of long-term shared private keys
	 */
	protected BigInteger[] longtermKeys;


	/**
	 * Creates a group member with specific group parameters. pv and qv should
	 * satisfy the condition(safe prime) that pv = 2*qv + 1. 
	 * This contructor checks if pv = 2*qv + 1 but doesn't check if both are
	 * really primes.
	 *
	 * @param pv  a large prime where pv = 2*qv + 1
	 * @param qv  a large prime where pv = 2*qv + 1
	 * @param basev a generator of Zq* where q = qv
	 * @param intermv array of intermediate values
	 * @param longtermv array of long-term public keys
	 * @param indexv index of this member in the group protocol [0,groupsize-1]
	 * @param g_contv  indicates if this member is a group controller
	 * @exception IllegalGroupParameterException if these parameters don't 
	 *                        satisfy one of the followings<br>
	 *                        1. pv = 2*qv + 1<br>
	 *                        2. indexv should be positive and less than group 
	 *                           size<br>
	 *                        3. specified as a group controller, then indexv 
	 *                           should be <group size - 1><br>
	 */
	public MemberAGDH(BigInteger pv,
		BigInteger qv,
		BigInteger basev,
		BigInteger[] intermv,
		BigInteger[] longtermv,
		int indexv,
		boolean g_contv) throws IllegalGroupParameterException
	{
		super(pv,qv,basev,intermv,indexv,g_contv);
        
		this.longtermKeys = longtermv;
        
		// generating long-term secret
		longtermSecret = BigInteger.ZERO;
		while( longtermSecret.compareTo(BigInteger.ZERO) * 
			longtermSecret.compareTo(BigInteger.ONE) == 0 )
		{
			longtermSecret = new BigInteger(q.bitLength(),RANSTATE).mod(q);
		}
        
		// publish the long-term public key
		longtermKeys[index] = base.modPow(longtermSecret,p);
	}
    
    
	/**
	 * This method performs the group mapping function in authenticated versions of 
	 * Group Diffie-Hellman.<br>
	 * In this project, the group mapping function is defined as the following.<br>
	 * F(x) = ( x<=q ? x : p-x ) where p is a safe prime (p = 2*q + 1)
	 *
	 * @param sharedSecretK long-term secret to be used in the group mapping function
	 * @return long-term secret to be used in upflow() or finish()
	 */
	protected BigInteger functionF(BigInteger sharedSecretK)
	{
		if( sharedSecretK.compareTo(q) > 0 )
		{
			sharedSecretK = p.subtract(sharedSecretK).mod(q);
		}
		return sharedSecretK;
	}


	/**
	 * Perfoms upflow stage in the view of this member.
	 * This method returns the time elapsed to compute intermediate values during upflow
	 * process of this member
	 *
	 * @return time required to compute intermediate values
	 */
	public long upflow()
	{
		long start, end, timeElapsed=0;
		BigInteger sharedSecretK;
        
		// upflow stage is the same as GDH except for the group controller
		if(!g_cont)
			timeElapsed = super.upflow();
		else
		{	    
			for(int i=0 ; i<index ; i ++ )
			{
				start = System.currentTimeMillis();		
				sharedSecretK =
					functionF( longtermKeys[i].modPow(longtermSecret,p) );
				interm[i] 
					= interm[i].modPow(nounce.multiply(sharedSecretK).mod(q),p);
				end = System.currentTimeMillis();
				timeElapsed += end - start;
			}
                
		}
		return timeElapsed;
	}
    
	
	/**
	 * Now every member has got the intermediate value for itself in A-GDH protocol. 
	 * This method will calculate the group key in the view of this member and return 
	 * the time elapsed to calculate the group key from the final intermediate value.
	 *
	 * @return the time elapsed to calculate the group key from the final
	 *         intermediate value
	 */
	public long finish()
	{
		long start=0, end=0;
		BigInteger sharedSecretK,inverseK;
        
		if(g_cont)
		{
			return super.finish();
		}
		else
		{
			start = System.currentTimeMillis();
			sharedSecretK =
				functionF( longtermKeys[group_size-1].modPow(longtermSecret,p) );   
			try
			{
				inverseK = sharedSecretK.modInverse(q);
			}
			catch(ArithmeticException ae)   // no inverse exists
			{
				throw new ArithmeticException(sharedSecretK+" is not invertible");
			}
			interm[index] 
				= interm[index].modPow(nounce.multiply(inverseK).mod(q),p);
			end = System.currentTimeMillis();  
		
			return end-start;
		}
	}
    
    
	/**
	 * Return the long-term secret of this member. This is only for debugging.
	 *
	 * @return long-term private key of this member
	 */
	public BigInteger getLongtermSecret()
	{
		return longtermSecret;
	}
    
 
	/**
	 * Overrides <code>toString()</code> inherited from <code>MemberGDH</code> in order
	 * to show how this member is set up in the newer version.
	 *
	 * @return a value of type 'String'
	 */
	public String toString()
	{
		return "member[" + index + "]: " + base + "^" + nounce
			+ " <" + q + "> (mod " + p + "), long-term secret: " 
			+ longtermSecret + " => GC? " + (g_cont?"yes":"no");
	}	
    
} // MemberAGDH
