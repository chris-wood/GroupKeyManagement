/*
 * MemberGDH.java
 *
 */

//package jisoo.msproject.gdh;

import java.math.*;
import java.util.*;

/**
 * This class represents a member in non-authenticated version of Group Diffie-
 * Hellman.
 * Constructor is initialized with several parameters such as group size, 
 * index of this member, safe prime p = 2*q + 1 where q is a large prime, etc.
 * Once each member is created, upflow() method will be invoked in order to 
 * proceed the upflow stage in Group Diffie-Hellman protocol.
 * At last, finish() method will compute the group key in the view of this 
 * member from intermediate value tossed by the group controller. 
 * If this member is the group controller, there is nothing to do in upflow 
 * stage. 
 * <br>The array values of forth argument should be initialized as base number
 * (generator) before the first member performs the upflow stage.
 *
 *
 * @author	Jisoo Kim(jsk4445@cs.rit.edu)
 * @version     18-May-2005
 *
 */
public class MemberGDH implements GroupDiffieHellmanInterface
{

	// static variables for generating random numbers
	public static final long RANSEED = 1234;
	public static final Random RANSTATE = new Random( RANSEED );
    
    
	//
	// protected variables
	//
	protected BigInteger p;
	protected BigInteger q;
	protected BigInteger base;
	protected BigInteger[] interm; 
	protected BigInteger nounce;
	protected int index;
	protected int group_size;    
	protected boolean g_cont;
    

	/**
	 * Creates a group member with specific group parameters. pv and qv should
	 * satisfy the condition(safe prime) that pv = 2*qv + 1. 
	 * This contructor checks if pv = 2*qv + 1 but doesn't check if both are
	 * really primes.
	 *
	 * @param pv a large prime where pv = 2*qv + 1
	 * @param qv a large prime where pv = 2*qv + 1
	 * @param basev a generator of Zq* where q = qv
	 * @param intermv array of intermediate values
	 * @param indexv index of this member in the group protocol [0,groupsize-1]
	 * @param g_contv indicates if this member is a group controller
	 * @exception IllegalGroupParameterException if these parameters don't 
	 *                        satisfy one of the followings
	 *                        1. pv = 2*qv + 1
	 *                        2. indexv should be positive and less than group 
	 *                           size
	 *                        3. specified as a group controller, then indexv 
	 *                           should be <group size - 1>
	 */
	public MemberGDH(BigInteger pv,
		BigInteger qv,
		BigInteger basev,
		BigInteger[] intermv,
		int indexv,
		boolean g_contv) throws IllegalGroupParameterException
	{
		if( !pv.equals(qv.multiply(new BigInteger("2")).add(BigInteger.ONE)) )
			throw new IllegalGroupParameterException
				("The first two parameters in the contructor of member[" +
				indexv + "] doesn't satisfy that p = 2*q + 1");
        
		if( indexv < 0 || index >= intermv.length )
			throw new IllegalGroupParameterException
				("Illegal index for member[" + indexv + "]"); 

		if( g_contv && indexv != intermv.length-1 )
			throw new IllegalGroupParameterException
				("Member[" + indexv + "] is specified as a group controller "
				+ "but the index is not <group size - 1>."); 

		p = pv;
		q = qv;
		base = basev;
		interm = intermv;
		index = indexv;
		g_cont = g_contv;
		group_size = interm.length;

        
		// generating the session secret (nounce)
		nounce = BigInteger.ZERO;
		while( nounce.compareTo(BigInteger.ZERO) * 
			nounce.compareTo(BigInteger.ONE) == 0 )
		{			
			nounce = new BigInteger(q.bitLength(),RANSTATE).mod(q);
		}	
	}
    


	/**
	 * Upflow stage of this member. It computes the intermediate values for 
	 * the next member.
	 * This method measures the essential time elapsed to compute intermediate 
	 * values during upflow process of this member.
	 * 
	 *
	 * @return time required to compute intermediate values 
	 */
	public long upflow()
	{
		long start, end, timeElapsed=0;
	
		for(int i=0 ; i<index ; i ++ )
		{
			start = System.currentTimeMillis();
			interm[i] = interm[i].modPow(nounce, p);
			end = System.currentTimeMillis();
			timeElapsed += end - start;
		}
		if(!g_cont)
		{
			start = System.currentTimeMillis();
			interm[index+1] = interm[index].modPow(nounce, p);
			end = System.currentTimeMillis();
			timeElapsed += end - start;
		}
        
		return timeElapsed;
	}
    
	
	/**
	 * Now every member has got the intermediate value for itself. This method
	 * will calculate the group key in the view of this member and return the 
	 * time elapsed to calculate the group key from the final intermediate 
	 * value.
	 *
	 * @return the time elapsed to calculate the group key from the final
	 *         intermediate value
	 */
	public long finish()
	{
		long start, end;
        
		start = System.currentTimeMillis();
		interm[index] = interm[index].modPow(nounce, p);
		end = System.currentTimeMillis();
        
		return end-start;
	}
    
	
	/**
	 * Returns the private key of this member. This is only for debugging.
	 *
	 * @return private key of this member
	 */
	public BigInteger getSecret()
	{
		return nounce;
	}
    
    
	/**
	 * Returns a string showing how this member is set up
	 *
	 * @return a string showing how this member is set up
	 */
	public String toString()
	{
		return "member[" + index + "]: " + base + "^" + nounce
			+ " <" + q + "> (mod " + p + ") => group controller? "
			+ (g_cont?"yes":"no");
	}
    
}
