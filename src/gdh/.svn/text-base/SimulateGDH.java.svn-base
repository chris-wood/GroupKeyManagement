/*
 * SimulateGDH.java
 *
 */

import java.io.*;
import java.math.*;

/**
 *  This is a main class to simulate the protocols in this project.<br>
 * <br>Usage: <code>java SimulateAGDH file_base file_q group_size class_name [-d]</code>
 * <pre>
 *        <I>file_base</I> :  name of file containing the group generator
 *        <I>file_q</I> : name of file containing the order of the unique subgroup of 
 *                        Zp* where p = 2*q + 1
 *        <I>group size</I> : number of members to simulate
 *        <I>class_name</I> : name of class name to simulate 
 *           (MemberAGDH, MemberSAGDH, OptimizedAGDH, OptimizedSAGDH, MACedAGDH:MD5, 
 *            MACedAGDH:SHA1, MACedSAGDH:MD5, and MACedSAGDH:SHA1)
 *        <I>-d</I> : optional argument for debug
 * </pre>
 * <br>This simulation inludes only the measurement of time elapsed for exponential 
 * computation and doesn't consider the time for communication (network) cost.
 * <br>In the broadcasting stage, only the maximum amount of time in finish() of each
 * member is accumulated.
 * 
 *
 * @author   Jisoo Kim(jsk4445@cs.rit.edu)
 * @version  18-May-2005
 *
 */
public class SimulateGDH 
{

	/**
	 * See the class description for the usage of this class
	 *
	 * @param args arguments from the command line
	 */
	public static void main( String[] args ) 
	{
		int group_size;
		double timeElapsed = 0;
		boolean debug = false;
		BigInteger p=null, q=null, base=null;
		BigInteger[] intermValues;
		BigInteger[] longtermPublicKeys;		
		GroupDiffieHellmanInterface[] members=null;		
		BufferedReader inputBase = null;
		BufferedReader inputQ = null;
		byte[][] macTagArray = null; // will be used only for Mac tag
		String macMode = "MACSHA1";  // MACSHA1 or HMACMD5
		int macSize = 20;
	
		if( args.length < 4 || args.length > 5 )
		{
			System.err.println("Usage: java SimulateAGDH <file_base> " +
				"<file_q> <group size> <class_name> [-d]");
			System.exit(1);
		}
	
		try
		{
			inputBase = new BufferedReader( new FileReader( args[0] ) );
			inputQ = new BufferedReader( new FileReader( args[1] ) );
			base = new BigInteger( inputBase.readLine() );
			q = new BigInteger( inputQ.readLine() );
		}
		catch(Exception e)
		{
			System.err.println("error in reading file(s).");
			System.err.println(e);
			System.exit(1);
		}
		finally
		{
			try
			{
				if( inputBase != null ) inputBase.close();
				if( inputQ != null ) inputQ.close();
			}
			catch( Exception ex )
			{ // do nothing and exit
			}
		}
        
		if(!q.isProbablePrime(10))
		{
			System.err.println("the input number q is not a prime!");
			System.exit(1);
		}
		p = q.multiply( new BigInteger("2") ).add( BigInteger.ONE ); // p=2*q+1
		if(!p.isProbablePrime(10))
		{
			System.err.println("p=2*q+1 is not a prime!");
			System.exit(1);
		}
	
		group_size = Integer.parseInt( args[2] );
		intermValues = new BigInteger[group_size];       
		longtermPublicKeys = new BigInteger[group_size];

		int protocolID = 0;
		if( args[3].equals("MemberGDH") )
		{
			members = new MemberGDH[group_size];
			protocolID = -1;
		}
		else if( args[3].equals("MemberAGDH") )
		{
			members = new MemberAGDH[group_size];
			protocolID = 0;
		}
		else if( args[3].equals("MemberSAGDH") )
		{
			members = new MemberSAGDH[group_size]; 
			protocolID = 1;
		}
		else if( args[3].equals("OptimizedAGDH") )
		{
			members = new OptimizedAGDH[group_size]; 
			protocolID = 2;
		}
		else if( args[3].equals("OptimizedSAGDH") )
		{
			members = new OptimizedSAGDH[group_size]; 
			protocolID = 3;
		}
		else if( args[3].equals("MACedAGDH:SHA1") )
		{
			members = new MACedAGDH[group_size]; 
			protocolID = 4;
			macSize = 20;       // 20 bytes
			macTagArray = new byte[group_size][macSize]; 
			macMode = "HMACSHA1";
		}
		else if( args[3].equals("MACedAGDH:MD5") )
		{
			members = new MACedAGDH[group_size]; 
			protocolID = 4;
			macSize = 16;      // 16 bytes
			macTagArray = new byte[group_size][macSize];  
			macMode = "HMACMD5";
		}
		else if( args[3].equals("MACedSAGDH:SHA1") )
		{
			members = new MACedSAGDH[group_size]; 
			protocolID = 5;
			macSize = 20;      // 20 bytes
			macTagArray = new byte[group_size][macSize];  
			macMode = "HMACSHA1";
		}
		else if( args[3].equals("MACedSAGDH:MD5") )
		{
			members = new MACedSAGDH[group_size]; 
			protocolID = 5;
			macSize = 16;      // 16 bytes
			macTagArray = new byte[group_size][macSize];  
			macMode = "HMACMD5";
		}
		else
		{
			System.err.println(args[3] + " is not a proper class name");
			System.err.println("Choose one among MemberAGDH, MemberSAGDH, " +
				"OptimizedAGDH, OptimizedSAGDH, MACedAGDH:MD5 "+
				"MACedAGDH:SHA1, MACedSAGDH:MD5, and " + 
				"MACedSAGDH:SHA1");
			System.exit(1);
		}

		System.out.println("creating members........");
		boolean gc = false;
		for(int i=0 ; i<group_size ; i++ )
		{
			intermValues[i] = base;
			if( i == group_size - 1 ) gc = true;

			try
			{
				switch(protocolID)
				{
					case -1:
						members[i] = new MemberGDH(p,q,base,intermValues,i,gc);
						break;

					case 0:
						members[i] = new MemberAGDH(p,q,base,intermValues,
							longtermPublicKeys, i, gc);
						break;
                        
					case 1:
						members[i] = new MemberSAGDH(p,q,base,intermValues,
							longtermPublicKeys, i, gc);
						break;
                        
					case 2:
						members[i] = new OptimizedAGDH(p,q,base,intermValues,
							longtermPublicKeys, i, gc);
						break;
                        
					case 3:
						members[i] = new OptimizedSAGDH(p,q,base,intermValues,
							longtermPublicKeys, i, gc);
						break;
                        
					case 4:
						members[i] = new  MACedAGDH(p, q, base, intermValues,
							longtermPublicKeys, macTagArray, macMode, i, gc);
						break;
			
					case 5:
						members[i] = new MACedSAGDH(p, q, base, intermValues,
							longtermPublicKeys,macTagArray,	macMode, i, gc);
						break;

					default:
						// shouldn't reach here
						System.err.println("Wrong protocol");
						System.exit(1);			
				}
			}
			catch(IllegalGroupParameterException igpe)
			{
				System.err.println("Illegal parameter(s) during creating member");
				System.err.println(igpe.toString());
				System.exit(1);
			}
			if(args.length == 5 && args[4].equals("-d") )
				System.out.println(members[i]);
		}
		if(args.length == 5 && args[4].equals("-d") )
		{
			System.out.println("generated the long-term public keys.....");
			for(int j=0 ; j<group_size ; j++)
				System.out.println(j+": "+longtermPublicKeys[j]);
		}
		System.out.println();

		if( protocolID > 1 )
		{
			System.out.println("pre-computing the shared secrets.........\n");
			for(int i=0 ; i<group_size ; i++)
			{
				((OptimizedGDHInterface)members[i]).precomputeK();
			}
		}
        
		System.out.println("upflow stage started.......");
		for(int i=0 ; i<group_size ; i++ )
		{
			timeElapsed += members[i].upflow();
			System.out.println("member["+i+"] is done.");
                
			if(args.length == 5 && args[4].equals("-d") )
			{
				for(int j=0;j<group_size;j++)
					System.out.print(j+": "+intermValues[j]+"\t");
				System.out.println();	   
			}
		}
		System.out.println();
        
		System.out.println("broadcasting........");
		boolean success=true;
		long max = 0, tempT = 0;		
		for( int i=0 ; i<group_size ; i++ )
		{
			tempT = members[i].finish();
			if( tempT > max ) max = tempT;
			if(args.length == 5 && args[4].equals("-d") )
				System.out.println("member["+i+"]'s key : "+intermValues[i]);
		}
		timeElapsed += max;  // only max is accumulated
		for( int i=0 ; i<group_size-1 ; i++ )
		{
			if(success)
				success = intermValues[i].equals(intermValues[i+1]);
			else
				break;
		}
		System.out.println("end of the protocol : " + 
			(success?"success":"failure"));
		System.out.println("total elapsed time is "
			+ timeElapsed/1000.0 + " seconds.");        
	} 

} // SimulateGDH


