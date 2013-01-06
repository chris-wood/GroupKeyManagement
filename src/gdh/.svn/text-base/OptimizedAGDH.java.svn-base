/*
 * OptimizedAGDH.java
 *
 */

import java.math.*;
import java.util.*;

/**
 * This class is an optimzed version of <code>MemberAGDH</code>.<br>
 * Constructor is initialized with several parameters such as group size, 
 * index of this member, safe prime p = 2*q + 1 where q is a large prime, etc.<br>
 * In A-GDH protocol, every member is assumed to have shared the long-term
 * public key with each other. This optimized version performs the precomputation of shared
 * private key(s) and the group inverse used to compute the final group key in the method
 * precomputeK().<br>
 * upflow() and finish() are doing the same as in <code>MemberAGDH</code>.
 *
 *
 * @author      Jisoo Kim(jsk4445@cs.rit.edu)
 * @version     3-May-2005
 *
 */

public class OptimizedAGDH extends MemberAGDH implements OptimizedGDHInterface
{

	//
	// protected variables
	//

	/**
	 *  Array of the long-term shared key K for pre-computation
	 */
	protected BigInteger[] sharedSecretK;

	/**
	 *	Array of K^-1 for pre-computation
	 */
	protected BigInteger[] inverseOfK;
    
    
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
	public OptimizedAGDH(BigInteger pv,
		BigInteger qv,
		BigInteger basev,
		BigInteger[] intermv,
		BigInteger[] longtermv,
		int indexv,
		boolean g_contv) throws IllegalGroupParameterException
	{
		super(pv,qv,basev,intermv,longtermv,indexv,g_contv);
	}

	/**
	 * Pre-computes the long-term private key and its inverse of group operation.
	 * This should be always invoked before upflow().
	 *
	 */
	public void precomputeK()
	{
        
		if(g_cont) // if this member is the group controller
		{                       
			sharedSecretK = new BigInteger[group_size-1];
			inverseOfK = null;    // not necessary for GC in AGDH                    
			for(int i=0 ; i<group_size-1 ; i++)
			{
				sharedSecretK[i] 
					= functionF( longtermKeys[i].modPow(longtermSecret,p) );
				// finally, sharedSecretK[i] = nounce*K[i]
				sharedSecretK[i]
					= nounce.multiply(sharedSecretK[i]).mod(q);  
			}
		}
		else       // if not group controller
		{   
			sharedSecretK = new BigInteger[1];
			inverseOfK = new BigInteger[1];
                
			sharedSecretK[0] = functionF( longtermKeys[group_size-1].
				modPow(longtermSecret,p) );
			try
			{
				inverseOfK[0] = sharedSecretK[0].modInverse(q);
				// finally, inverseOfK = nounce * K^-1
				inverseOfK[0] = nounce.multiply(inverseOfK[0]).mod(q);
			}
			catch(ArithmeticException ae)   // no inverse of K exists
			{   
				throw ae;
			}
		}
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
        
		// upflow stage is the same as GDH except for the group controller
		if(!g_cont)
			timeElapsed = super.upflow();
		else
		{	    
			for(int i=0 ; i<index ; i ++ )
			{
				start = System.currentTimeMillis();		
				interm[i] = interm[i].modPow( sharedSecretK[i], p );
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
		long start, end, timeElapsed=0;
        
		if(g_cont)
		{
			timeElapsed = super.finish();
		}
		else
		{
			start = System.currentTimeMillis();
			interm[index] = interm[index].modPow(inverseOfK[0],p);
			end = System.currentTimeMillis();  
			timeElapsed = end - start;
		}
        
		return timeElapsed;
	}

} // OptimizedAGDH
