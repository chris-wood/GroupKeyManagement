/*
 * MACedAGDH.java
 *
 */

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.math.*;

/**
 * This class represents a member in the authenticated version Group Diffie-
 * Hellman (A-GDH) using HMAC which protects messages between group members from 
 * forgery or modification by a man-in-the-middle attacker.<br>
 * The hashing algorithm behind HMAC used here can be one of SHA-1 or MD5.
 *
 *
 * @author	Jisoo Kim(jsk4445@cs.rit.edu)
 * @version     3-May-2005
 *
 */

public class MACedAGDH extends OptimizedAGDH implements OptimizedGDHInterface
{
    
	//
	// protected variables
	//

	/**
	 *  Array of Mac tags. A Mac tag is <code>byte[]</code> type.
	 */
	protected byte[][] macTag;

	/**
	 *  Array of the private keys shared with other members.<br>
	 *  This will be used in generating and verifying the Mac tag.
	 */
	protected SecretKeySpec[] macKeys;

	/**
	 *  Name of Mac protocol (HMACSHA1 or HMACMD5).<br> 
	 *  This should be specified in constructor's argument.
	 */
	protected String macMode;  

	/**
	 *  Array of pre-computed variables to be computed in <code>precomputeK()</code>.
	 */
	protected BigInteger[] precomputed;

	/**
	 *  Array of Mac tags for out-bound 
	 */
	protected Mac macOutbound[];
    

	/**
	 * Creates a group member with specific group protocol parameters.
	 *
	 * @param pv a large prime where pv = 2*qv + 1
	 * @param qv a large prime where pv = 2*qv + 1
	 * @param basev a generator of Zq* where q = qv
	 * @param intermv array of intermediate values
	 * @param longtermv array of long-term public keys
	 * @param macTagv array of Mac tags
	 * @param macModev name of Mac protocol (HMACSHA1 or HMACMD5)
	 * @param indexv index of this member in the group protocol [0,groupsize-1]
	 * @param g_contv indicating if this member is a group controller
	 */
	public MACedAGDH(BigInteger pv,
		BigInteger qv,
		BigInteger basev,
		BigInteger[] intermv,
		BigInteger[] longtermv,
		byte[][] macTagv,      
		String macModev,       
		int indexv,
		boolean g_contv) throws IllegalGroupParameterException
	{	
		super(pv,qv,basev,intermv,longtermv,indexv,g_contv);
		this.macTag = macTagv;
		this.macMode = macModev;        
	}


	/**
	 * Pre-computes the long-term private key and its inverse of group operation and
	 * initialize the Mac tags.
	 * This should be always invoked before upflow().
	 *
	 */
	public void precomputeK() 
	{
		sharedSecretK = new BigInteger[group_size];
        
		if( g_cont )
		{   // group controller
			inverseOfK = new BigInteger[group_size];
			precomputed = new BigInteger[group_size];
            
			for(int i=0 ; i <group_size-1 ; i++ )
			{
				sharedSecretK[i]
					= functionF( longtermKeys[i].modPow(longtermSecret,p) );
				try
				{
					inverseOfK[i] = sharedSecretK[i].modInverse(q);
				}
				catch(ArithmeticException ae){ throw ae; }
				precomputed[i] 
					= sharedSecretK[i].multiply(nounce).mod(q); 
			}
		}
		else
		{    // not group controller
			inverseOfK  = new BigInteger[1];
			precomputed = new BigInteger[1];
            
			for(int i=0 ; i <group_size ; i++ )
				if( i != index )
					sharedSecretK[i] = functionF
						( longtermKeys[i].modPow(longtermSecret,p) );
			try
			{
				inverseOfK[0] = sharedSecretK[group_size-1].modInverse(q);
			}
			catch(ArithmeticException ae){ throw ae; }  
			precomputed[0] =  inverseOfK[0].multiply(nounce).mod(q); 
		}        
        
		// initialize the secret keys for MAC
		this.macKeys = new SecretKeySpec[sharedSecretK.length];
		for(int i = 0 ; i < macKeys.length ; i++ )
		{
			if( i != index )
				macKeys[i] = new SecretKeySpec(sharedSecretK[i].toByteArray(),
					macMode);
		}
	}
   
	/**
	 * Perfoms upflow stage in the view of this member.
	 * Before calculating the intermediate values, it verifies the Mac tag so that the 
	 * message received can be protected from man-in-the-middle attack.<br>
	 * If there is something wrong with the Mac tag, <code>MACErrorException</code> is 
	 * thrown in runtime.
	 * <br>This method returns the time elapsed to compute intermediate values and verify 
	 * the Mac tags during upflow process of this member.
	 *
	 * @return time required to compute intermediate values and verify the Mac tag
	 */  
	public long upflow()
	{
		long start, end, timeElapsed=0;
		Mac macInbound = null, macOutbound = null;
		byte[] checkMac;
	
		// initialize Mac objects
		try
		{
			macInbound = Mac.getInstance(macMode);
			macOutbound = Mac.getInstance(macMode);
			if( index != 0 )
				macInbound.init(macKeys[index-1]);
			if( !g_cont )
				macOutbound.init(macKeys[index+1]);
		}
		catch(NoSuchAlgorithmException nsae)
		{
			throw new MACErrorException("MAC error in upflow()! : " +
				nsae.toString());
		}
		catch(InvalidKeyException ike)
		{
			throw new MACErrorException("MAC error in upflow()! : " + 
				ike.toString());
		}       

		// first, verify the mac tag if it's not the first member
		if(index != 0 )
		{
			for(int i=0 ; i <= index ; i ++ )
			{
				start = System.currentTimeMillis();
				macInbound.update(interm[i].toByteArray());
				end = System.currentTimeMillis();
				timeElapsed += end-start;
			}
			start = System.currentTimeMillis();
			checkMac = macInbound.doFinal();
			if( checkMac.length != macTag[0].length )
				throw new MACErrorException("member[" + index +
					"] detected corrupted MAC tag");
			else
				for(int j=0 ; j < checkMac.length ; j++)
				{		    
					if( checkMac[j] != macTag[0][j] )
						throw new MACErrorException
							("member[" + index + "] detected corrupted MAC tag");
				}
			end = System.currentTimeMillis();
			timeElapsed += end-start;            
		}
        
		// the tag is ok. Now starting the upflow stage
		if(g_cont)
		{
			for(int i=0 ; i<index ; i ++ )
			{
				start = System.currentTimeMillis();		
				interm[i] = interm[i].modPow( precomputed[i], p );
				end = System.currentTimeMillis();
				timeElapsed += end - start;
			}

			// the outbound Mac tag for broadcasting
			for(int i=0 ; i < index ; i ++ )
			{
				try
				{
					// this can be done in precomputeK()
					macOutbound.init(macKeys[i]);
				}
				catch(InvalidKeyException ike)
				{
					throw new MACErrorException("MAC error in upflow()! " 
						+ ike.toString());
				}  
				start = System.currentTimeMillis();
				macOutbound.update(interm[i].toByteArray());
				macTag[i] = macOutbound.doFinal();  // tag for each member
				end = System.currentTimeMillis();
				timeElapsed += end-start;
			}
		}
		else
		{
			for(int i=0 ; i < index ; i ++ )
			{
				start = System.currentTimeMillis();
				interm[i] = interm[i].modPow(nounce, p);   
				end = System.currentTimeMillis();
				timeElapsed += end-start;         
			}    
			start = System.currentTimeMillis();
			interm[index+1] = interm[index].modPow(nounce, p);
			end = System.currentTimeMillis();
			timeElapsed += end-start;

			// the outbound Mac tag for the next member
			for(int i=0 ; i <= index+1 ; i ++ )
			{
				start = System.currentTimeMillis();
				macOutbound.update(interm[i].toByteArray()); 
				end = System.currentTimeMillis();
				timeElapsed += end-start;
			}
			start = System.currentTimeMillis();
			macTag[0]= macOutbound.doFinal(); // tag for the next member
			end = System.currentTimeMillis();
			timeElapsed += end-start;            
		}   

		return timeElapsed;
	}
    
	/**
	 * Now every member has got the intermediate value for itself in A-GDH protocol.<br>
	 * Every member (except the group controller) verifies the Mac tag from the group
	 * controller before group key computation. If there is something wrong with the tag,
	 * <code>MACErrorException</code> is thrown in runtime.<br>
	 * This method will calculate the group key in the view of this member and return 
	 * the time elapsed to calculate the group key from the final intermediate value.
	 *
	 * @return the time elapsed to calculate the group key from the final
	 *         intermediate value
	 */
	public long finish()
	{
		long start, end, timeElapsed=0;
        
		// tag check is not necessary for group controller
		if(g_cont)
		{
			start = System.currentTimeMillis();
			interm[index] = interm[index].modPow(nounce, p);
			end = System.currentTimeMillis();
			timeElapsed = end - start;
		}
		else
		{
			Mac mac;
			byte[] macCheck;
            
			// initialize Mac object to verify the tag from group controller
			try
			{  
				mac = Mac.getInstance(macMode);
				mac.init(macKeys[group_size-1]);
			}
			catch(NoSuchAlgorithmException nsae)
			{
				throw new MACErrorException("MAC error in finish()! " 
					+ nsae.toString());
			}
			catch(InvalidKeyException ike)
			{
				throw new MACErrorException("MAC error in finish()! " 
					+ ike.toString());
			} 
            
			// verifying the MAC tag
			start = System.currentTimeMillis();
			mac.update(interm[index].toByteArray());
			macCheck = mac.doFinal();
			if( macCheck.length != macTag[index].length )
				throw new MACErrorException();
			else
				for(int j=0 ; j < macCheck.length ; j++)
				{
					if( macCheck[j] != macTag[index][j] )
						throw new MACErrorException();
				}
            
			// now tag is ok
			interm[index] = interm[index].modPow(precomputed[0], p);
			end = System.currentTimeMillis();
			timeElapsed = end - start;
		}

		return timeElapsed;	
	}    
    
} // MACedAGDH
