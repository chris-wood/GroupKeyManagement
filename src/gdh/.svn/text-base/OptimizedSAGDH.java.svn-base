/*
 * OptimizedSAGDH.java
 *
 */

import java.math.*;
import java.util.*;

/**
 * This class is an optimized version of <code>MemberSAGDH</code>.
 * Constructor is initialized with several parameters such as group size, 
 * index of this member, safe prime p = 2*q + 1 where q is a large prime, etc.
 * In SA-GDH protocol, every member is assumed to have shared the long-term
 * public key with each other. This optimized version performs the precomputation of shared
 * private key(s) and the group inverse used to compute the final group key in the method
 * precomputeK().<br>
 * Once each member is created, upflow() method will be invoked in order to 
 * proceed the upflow stage in SA-GDH protocol using the shared long-term secret.
 * In SA-GDH, every member implicitly authenticates every other member.<br>
 * At last, finish() method will compute the group key from intermediate value
 * using the long-term private keys shared with other members.
 *
 * @author     Jisoo Kim
 * @version    3-May-2005
 *
 */
public class OptimizedSAGDH extends OptimizedAGDH implements OptimizedGDHInterface 
{

	/**
	 * Creates a new <code>OptimizedSAGDH</code> instance with specific group parameters.
	 *
	 */
	public OptimizedSAGDH(BigInteger pv,
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
	 * Perfoms upflow stage in the view of this member. 
	 * This should be invoked after the method <code>precomputeK()</code>.
	 * This method returns the time elapsed to compute intermediate values during upflow
	 * process of this member
	 *
	 * @return time required to compute intermediate values
	 */ 
	public long upflow() 
	{
		long start, end, timeElapsed=0;
		BigInteger sharedSecret;
        
		for(int i=0 ; i<group_size ; i++)
		{
			if( i != index )
			{
				start = System.currentTimeMillis();
				interm[i] = interm[i].modPow(sharedSecretK[i],p);
				end = System.currentTimeMillis();
				timeElapsed += end - start;
			}
		}
        
		return timeElapsed;
	}

	/**
	 * Now every member has got the intermediate value for itself in SA-GDH protocol. 
	 * This method will calculate the group key in the view of this member and return 
	 * the time elapsed to calculate the group key from the final intermediate value.
	 *
	 * @return the time elapsed to calculate the group key from the final
	 *         intermediate value
	 */
	public long finish() 
	{
		long start=0, end=0;
        
		start = System.currentTimeMillis();
		interm[index] = interm[index].modPow(inverseOfK[index],p);
		end = System.currentTimeMillis();
        
		return end-start;
	}
 
	/**
	 * Pre-computes the long-term private key and its inverse of group operation.
	 * This should be always invoked before upflow().
	 *
	 */
	public void precomputeK() 
	{
		sharedSecretK = new BigInteger[group_size];
		inverseOfK = new BigInteger[group_size];
        
		for(int i=0 ; i<group_size ; i++)
		{
			if( i != index )
			{
				sharedSecretK[i] 
					= functionF( longtermKeys[i].modPow(longtermSecret,p) );
				try
				{
					inverseOfK[i]
						= sharedSecretK[i].modInverse(q);
				}
				catch(ArithmeticException ae)   // no inverse exists
				{
					throw ae;
				}
			}
		}
        
		inverseOfK[index] = nounce;
		for(int i=0 ; i<group_size ; i++)
		{
			if( i != index )
			{
				// finally, sharedSecretK[i] = nounce * K
				// will be used in upflow()
				sharedSecretK[i] 
					= nounce.multiply(sharedSecretK[i]).mod(q);
                        
				// inverseOfK[index] = nounce * K[1]^-1 * ... * K[n]^-1 
				// will be used in finish()
				inverseOfK[index] 
					= inverseOfK[index].multiply(inverseOfK[i]).mod(q);                        
			}
		}
                
	}

} // OptimizedSAGDH
