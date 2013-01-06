/*
 * MemberSAGDH.java
 *
 */

import java.math.*;
import java.util.*;

/**
 * This class represents a member in strong authenticated version of Group Diffie-
 * Hellman(SA-GDH).<br>
 * Constructor is initialized with several parameters such as group size, 
 * index of this member, safe prime p = 2*q + 1 where q is a large prime, etc.
 * In SA-GDH protocol, every member is assumed to have shared the long-term
 * public key with each other. However, this is not optimized so that the
 * long-term secret and its modulous inverse should be computed in upflow()
 * and finish().<br>
 * Once each member is created, upflow() method will be invoked in order to 
 * proceed the upflow stage in SA-GDH protocol using the shared long-term secret.
 * In SA-GDH, every member implicitly authenticates every other member.<br>
 * At last, finish() method will compute the group key from intermediate value
 * using the long-term private keys shared with other members.
 *
 *
 * @author      Jisoo Kim(jsk4445@cs.rit.edu)
 * @version     3-May-2005
 *
 */

public class MemberSAGDH extends MemberAGDH implements GroupDiffieHellmanInterface
{

    //
    // protected variable
    //

    protected BigInteger[] sharedSecretK;


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
    public MemberSAGDH(BigInteger pv,
		BigInteger qv,
		BigInteger basev,
		BigInteger[] intermv,
		BigInteger[] longtermv,
		int indexv,
		boolean g_contv) throws IllegalGroupParameterException
    {
        super(pv,qv,basev,intermv,longtermv,indexv,g_contv);
        sharedSecretK = new BigInteger[group_size];
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
        BigInteger sharedSecret;
        
        for(int i=0 ; i<group_size ; i++)
            {
                if( i != index )
                    {
                        start = System.currentTimeMillis();
                        sharedSecret 
                            = functionF(longtermKeys[i].modPow(longtermSecret,p));
                        interm[i] = interm[i].
                            modPow(nounce.multiply(sharedSecret).mod(q),p);
                        end = System.currentTimeMillis();
                        timeElapsed += end - start;
                        sharedSecretK[i] = sharedSecret;   // record the value of K
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
        long start, end, timeElapsed=0;
        BigInteger inverseK,exponent=BigInteger.ONE;
        
        for(int i=0 ; i<group_size ; i++)
            {
                start = System.currentTimeMillis();
                if(i != index)
                    {
                        inverseK = sharedSecretK[i].modInverse(q);
                        exponent = exponent.multiply(inverseK).mod(q);
                    }		
                end = System.currentTimeMillis();
                timeElapsed += end - start;
            }
        
        start = System.currentTimeMillis();
        interm[index] = interm[index].
            modPow(nounce.multiply(exponent).mod(q),p);
        end = System.currentTimeMillis();
        timeElapsed += end - start;
        
        return timeElapsed;
    }
    
}  // MemberSAGDH
