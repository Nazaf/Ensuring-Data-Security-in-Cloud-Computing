package Server;


public class ReedSolomon
{
    public int NPAR = 128;
    public  int MAXDEG = NPAR * 2;

    public final int TRUE = 1;
    public final int FALSE = 0;

    public final int PPOLY = 0x1D;

    public String fileName;
    public byte[] msg;
    public int ML;
    public byte[] codeword = new byte[256];
    public int datalength = 0;
    /* Encoder parity bytes */
    public int[] pBytes = new int[MAXDEG];

    /* Decoder syndrome bytes */
    public int[] synBytes = new int[MAXDEG];

    /* generator polynomial */
    public int[] genPoly = new int[MAXDEG * 2];

    public int[] gexp = new int[512];
    public int[] glog = new int[256];

    /* The Error Locator Polynomial, also known as Lambda or Sigma. Lambda[0] == 1 */
    public int[] Lambda = new int[MAXDEG];

    /* The Error Evaluator Polynomial */
    public int[] Omega = new int[MAXDEG];

    /* error locations found using Chien's search*/
    public int[] ErrorLocs = new int[256];
    public int NErrors;

    /* erasure flags */
    public int[] ErasureLocs = new int[256];
    public int NErasures;

    /* Some debugging routines to introduce errors or erasures
    into a codeword.
    */

    ReedSolomon(String fileName)
    {
            this.fileName = fileName;
    }

    /**
     * Construct RS with a size for NPAR (parity bytes size)
     */
    ReedSolomon(int pNPAR)
    {
    	this.NPAR=pNPAR;
    	this.MAXDEG=2 * pNPAR;
    	
    	//re-initialize array objects sized based on above two parameters
        /* Encoder parity bytes */
         pBytes = new int[MAXDEG];
         
         /* Decoder syndrome bytes */
         synBytes = new int[MAXDEG];

         /* generator polynomial */
         genPoly = new int[MAXDEG * 2];

         
         /* The Error Locator Polynomial, also known as Lambda or Sigma. Lambda[0] == 1 */
         Lambda = new int[MAXDEG];

         /* The Error Evaluator Polynomial */
         Omega = new int[MAXDEG];

         
         initialize_ecc ();
    }

    void set_data(String Data)
    {
        msg = new byte[Data.length()];
        Data.getBytes();//bytes(0, Data.length(), msg, 0);
        ML = msg.length + NPAR;
    }

    void set_filename(String fileName)
    {
        this.fileName = fileName;
    }

    String get_filename()
    {
        return this.fileName;
    }

    /* Introduce a byte error at LOC */
    void byte_err (int err, int loc, byte[] dst)
    {
        //printf("Adding Error at loc %d, data %#x\n", loc, dst[loc-1]);
        dst[loc-1] ^= err;
    }

    /* Pass in location of error (first byte position is
    labeled starting at 1, not 0), and the codeword.
    */
    void byte_erasure (int loc, byte dst[], int cwsize, int erasures[])
    {
        //printf("Erasure at loc %d, data %#x\n", loc, dst[loc-1]);
        dst[loc-1] = 0;
    }
    
    void Modified_Berlekamp_Massey ()
    {
        int n, L, L2, k, d, i;
        int [] psi = new int[MAXDEG];
        int [] psi2 = new int [MAXDEG];
        int [] D = new int[MAXDEG];
        int [] gamma = new int[MAXDEG];

        /* initialize Gamma, the erasure locator polynomial */
        init_gamma(gamma);

        /* initialize to z */
        copy_poly(D, gamma);
        mul_z_poly(D);

        copy_poly(psi, gamma);
        k = -1; L = NErasures;

        for (n = NErasures; n < NPAR; n++) {

            d = compute_discrepancy(psi, synBytes, L, n);

            if (d != 0) {

                /* psi2 = psi - d*D */
                for (i = 0; i < MAXDEG; i++) psi2[i] = psi[i] ^ gmult(d, D[i]);


                if (L < (n-k)) {
            L2 = n-k;
            k = n-L;
            /* D = scale_poly(ginv(d), psi); */
            for (i = 0; i < MAXDEG; i++) D[i] = gmult(psi[i], ginv(d));
            L = L2;
                }

                /* psi = psi2 */
                for (i = 0; i < MAXDEG; i++) psi[i] = psi2[i];
            }

            mul_z_poly(D);
        }

        for(i = 0; i < MAXDEG; i++) Lambda[i] = psi[i];
        compute_modified_omega();
    }

    /* given Psi (called Lambda in Modified_Berlekamp_Massey) and synBytes,
        compute the combined erasure/error evaluator polynomial as
        Psi*S mod z^4
        */
    void compute_modified_omega ()
    {
        int i;
        int [] product = new int[MAXDEG*2];

        mult_polys(product, Lambda, synBytes);
        zero_poly(Omega);
        for(i = 0; i < NPAR; i++) Omega[i] = product[i];

    }

    /* polynomial multiplication */
    void mult_polys (int[] dst, int[] p1, int[] p2)
    {
        int i, j;
        int [] tmp1 = new int[MAXDEG*2];

        for (i=0; i < (MAXDEG*2); i++) dst[i] = 0;

        for (i = 0; i < MAXDEG; i++) {
            for(j=MAXDEG; j<(MAXDEG*2); j++) tmp1[j]=0;

            /* scale tmp1 by p1[i] */
            for(j=0; j<MAXDEG; j++) tmp1[j]=gmult(p2[j], p1[i]);
            /* and mult (shift) tmp1 right by i */
            for (j = (MAXDEG*2)-1; j >= i; j--) tmp1[j] = tmp1[j-i];
            for (j = 0; j < i; j++) tmp1[j] = 0;

            /* add into partial product */
            for(j=0; j < (MAXDEG*2); j++) dst[j] ^= tmp1[j];
        }
    }

    /* gamma = product (1-z*a^Ij) for erasure locs Ij */
    void init_gamma (int[] gamma)
    {
        int e;
        int [] tmp = new int[MAXDEG];

        zero_poly(gamma);
        zero_poly(tmp);
        gamma[0] = 1;

        for (e = 0; e < NErasures; e++) {
            copy_poly(tmp, gamma);
            scale_poly(gexp[ErasureLocs[e]], tmp);
            mul_z_poly(tmp);
            add_polys(gamma, tmp);
        }
    }

    void compute_next_omega (int d, int[] A, int[] dst, int[] src)
    {
        int i;
        for ( i = 0; i < MAXDEG;  i++) {
            dst[i] = src[i] ^ gmult(d, A[i]);
        }
    }

    int compute_discrepancy (int[] lambda, int[] S, int L, int n)
    {
        int i, sum=0;

        for (i = 0; i <= L; i++)
            sum ^= gmult(lambda[i], S[n-i]);
        return (sum);
    }

    /********** polynomial arithmetic *******************/

    void add_polys (int[] dst, int[] src)
    {
        int i;
        for (i = 0; i < MAXDEG; i++) dst[i] ^= src[i];
    }

    void copy_poly (int[] dst, int[] src)
    {
        int i;
        for (i = 0; i < MAXDEG; i++) dst[i] = src[i];
    }

    void scale_poly (int k, int[] poly)
    {
        int i;
        for (i = 0; i < MAXDEG; i++) poly[i] = gmult(k, poly[i]);
    }


    void zero_poly (int[] poly)
    {
        int i;
        for (i = 0; i < MAXDEG; i++) poly[i] = 0;
    }

    /* multiply by z, i.e., shift right by 1 */
    void mul_z_poly (int[] src)
    {
        int i;
        for (i = MAXDEG-1; i > 0; i--) src[i] = src[i-1];
        src[0] = 0;
    }

    /* Finds all the roots of an error-locator polynomial with coefficients
       Lambda[j] by evaluating Lambda at successive values of alpha.*/

    void Find_Roots ()
    {
        int sum, r, k;
        NErrors = 0;

        for (r = 1; r < 256; r++) {
            sum = 0;
            /* evaluate lambda at r */
            for (k = 0; k < NPAR+1; k++) {
                sum ^= gmult(gexp[(k*r)%255], Lambda[k]);
            }
            if (sum == 0)
                {
            ErrorLocs[NErrors] = (255-r); NErrors++;
                }
        }
    }


    int correct_errors_erasures (byte[] codeword, int csize, int nerasures, int[] erasures)
    {
        int r, i, j, err;

        NErasures = nerasures;
        for (i = 0; i < NErasures; i++) ErasureLocs[i] = erasures[i];

        Modified_Berlekamp_Massey();
        Find_Roots();


        if ((NErrors <= NPAR) && NErrors > 0) {

            /* check for illegal error locs */
            for (r = 0; r < NErrors; r++) {
                if (ErrorLocs[r] >= csize) {
                            return(0);
                }
            }

            for (r = 0; r < NErrors; r++) {
                int num, denom;
                i = ErrorLocs[r];
                /* evaluate Omega at alpha^(-i) */

                num = 0;
                for (j = 0; j < MAXDEG; j++)
            num ^= gmult(Omega[j], gexp[((255-i)*j)%255]);

                /* evaluate Lambda' (derivative) at alpha^(-i); */
                denom = 0;
                for (j = 1; j < MAXDEG; j += 2) {
            denom ^= gmult(Lambda[j], gexp[((255-i)*(j-1)) % 255]);
                }

                err = gmult(num, ginv(denom));
                codeword[csize-i-1] ^= err;
            }
            return(1);
        }
        else {
            return(0);
        }
    }

    void init_galois_tables ()
    {
        /* initialize the table of powers of alpha */
        int i, z;
        int pinit,p1,p2,p3,p4,p5,p6,p7,p8;

        pinit = p2 = p3 = p4 = p5 = p6 = p7 = p8 = 0;
        p1 = 1;

        gexp[0] = 1;
        gexp[255] = gexp[0];
        glog[0] = 0;

        for (i = 1; i < 256; i++) {
        pinit = p8;
        p8 = p7;
        p7 = p6;
        p6 = p5;
        p5 = p4 ^ pinit;
        p4 = p3 ^ pinit;
        p3 = p2 ^ pinit;
        p2 = p1;
        p1 = pinit;
        gexp[i] = p1 + p2*2 + p3*4 + p4*8 + p5*16 + p6*32 + p7*64 + p8*128;
        gexp[i+255] = gexp[i];
        }

        for (i = 1; i < 256; i++) {
        for (z = 0; z < 256; z++) {
            if (gexp[z] == i) {
        glog[i] = z;
        break;
            }
        }
        }
    }

    /* multiplication using logarithms */
    int gmult(int a, int b)
    {
        int i,j;
        if (a==0 || b == 0) return (0);
        i = glog[a];
        j = glog[b];
        return (gexp[i+j]);
    }

    int ginv (int elt)
    {
        return (gexp[255-glog[elt]]);
    }

    /* Initialize lookup tables, polynomials, etc. */
    void initialize_ecc ()
    {
        /* Initialize the galois field arithmetic tables */
        init_galois_tables();

        /* Compute the encoder generator polynomial */
        compute_genpoly(NPAR, genPoly);
    }

    void zero_fill_from (byte[] buf, int from, int to)
    {
        int i;
        for (i = from; i < to; i++) buf[i] = 0;
    }

    /* Append the parity bytes onto the end of the message */
    void build_codeword (byte[] msg, int nbytes, byte[] dst)
    {
        int i;

        for (i = 0; i < nbytes; i++) dst[i] = msg[i];

        for (i = 0; i < NPAR; i++) {
            dst[i+nbytes] = (byte)pBytes[NPAR-1-i];
        }
    }

    
       /* Reed Solomon Decoder
        *
        * Computes the syndrome of a codeword. Puts the results
        * into the synBytes[] array.
        */

    void decode_data(byte[] data, int nbytes)
    {
        int i, j, sum;
        for (j = 0; j < NPAR;  j++) {
            sum	= 0;
            for (i = 0; i < nbytes; i++) {
   	    	 	//System.out.println(" int " + ((int)data[i]&0xFF) + " byte " + (byte)(data[i]) + " char " + (char)(data[i]));
                sum = ((int)data[i]&0xFF) ^ gmult(gexp[j+1], sum);
            }
            synBytes[j]  = sum;
        }
    }

    /* Check if the syndrome is zero */
    int check_syndrome ()
    {
        int i, nz = 0;
        for (i =0 ; i < NPAR; i++) {
        if (synBytes[i] != 0) {
                nz = 1;
                break;
        }
        }
        return nz;
    }

    /* Create a generator polynomial for an n byte RS code.
        * The coefficients are returned in the genPoly arg.
        * Make sure that the genPoly array which is passed in is
        * at least n+1 bytes long.
        */

    void compute_genpoly (int nbytes, int[] genpoly)
    {
        int i;
        int [] tp = new int[256];
        int [] tp1 = new int[256];

        /* multiply (x + a^n) for n = 1 to nbytes */

        zero_poly(tp1);
        tp1[0] = 1;

        for (i = 1; i <= nbytes; i++) {
            zero_poly(tp);
            tp[0] = gexp[i];		/* set up x+a^n */
            tp[1] = 1;

            mult_polys(genpoly, tp, tp1);
            copy_poly(tp1, genpoly);
        }
    }

    /* Simulate a LFSR with generator polynomial for n byte RS code.
        * Pass in a pointer to the data array, and amount of data.
        *
        * The parity bytes are deposited into pBytes[], and the whole message
        * and parity are copied to dest to make a codeword.
        *
        */

    void encode_data (byte[] msg, int nbytes, byte[] dst)
    {
        int i, dbyte, j;
        int [] LFSR = new int[NPAR+1];

        for(i=0; i < NPAR+1; i++) LFSR[i]=0;

        for (i = 0; i < nbytes; i++) {
            dbyte = msg[i] ^ LFSR[NPAR-1];
            for (j = NPAR-1; j > 0; j--) {
                LFSR[j] = LFSR[j-1] ^ gmult(genPoly[j], dbyte);
            }
            LFSR[0] = gmult(genPoly[0], dbyte);
        }

        for (i = 0; i < NPAR; i++)
            pBytes[i] = LFSR[i];

        build_codeword(msg, nbytes, dst);
    }
    
    
    /* 
     * Returns the Parity bits as per RS for the passed in data
     * Note the parity bits are orders such that they can be directly
     * appended to the Data bits to form the Codeword (encoded data)
     */

	 public byte[] getErasure (byte[] data, int nbytes)
	 {
	     int i, dbyte, j;
	     int [] LFSR = new int[NPAR+1];
	     byte [] erasureByte = new byte[NPAR];
	
	     for(i=0; i < NPAR+1; i++) LFSR[i]=0;
	
	     for (i = 0; i < nbytes; i++) {
	         dbyte = ((int)data[i]&0xFF) ^ LFSR[NPAR-1];
	         for (j = NPAR-1; j > 0; j--) {
	             LFSR[j] = LFSR[j-1] ^ gmult(genPoly[j], dbyte);
	         }
	         LFSR[0] = gmult(genPoly[0], dbyte);
	     }
	
	     for (i = 0; i < NPAR; i++)
	         pBytes[i] = LFSR[i];
	     
	     for (i = 0; i < NPAR; i++){
	    	 erasureByte[i] = (byte)(pBytes[i]);
	    	 //System.out.println(" int " + pBytes[i] + " byte " + (byte)(pBytes[i]) + " char " + (char)(pBytes[i]));
	     }
	     
	     
	     return erasureByte;
	
	 }
   
	 /* This method assumes that the data + erasure = codeword.
	  * The errors are assumed in the data and not in the erasure
	  * 
	  */
	
	public byte[] getErrorCorrected(byte[] data, int nbytes, byte[] erasure)
	{
	    byte[] codeword= getCodeWord(data, nbytes, erasure);
	    //Attempt to correct errors. Errors assumed only in data not the erasure/parity bit (note last two parameters 0 and null
        this.correct_errors_erasures(codeword, codeword.length, 0, null);
        
        //return just the corrected data by extracting it from the codeword
        byte[] correctData=new byte[nbytes];
        for (int i = 0; i < nbytes; i++) {
            correctData[i] = codeword[i];
        }
        return correctData;
        
	}
	 /* Check if any error errors
	  * Passed in data and the parity bytes erasure
	  * 
	  * The method, computes the syndrome for ( data + erasure) i.e. codeword,
	  * then checked if there are any error, and if error returns true.
	  * 
	  */
	
	public boolean checkIfErrorInData(byte[] data, int nbytes, byte[] erasure)
	{
       byte[] codeword= getCodeWord(data, nbytes, erasure);
       
       //this.decode_data(codeword, codeword.length);
       
      // System.out.println("If Data Error " + check_syndrome());

	  // calculate syndrome bytes
	  int[] syndromeBytes= new int[MAXDEG];
	  int i, j, sum;
	  for (j = 0; j < NPAR;  j++) {
	      sum	= 0;
	      for (i = 0; i < codeword.length; i++) {
	    	  
	    	  
	          sum = ((int)(codeword[i])&0xFF) ^ gmult(gexp[j+1], sum);
	          
		    //	 System.out.println(i + "." + sum + " int " + (((int)(codeword[i]))&0xFF) + " byte " + (byte)(codeword[i]) + " char " + (char)(codeword[i]));


	      }
	      syndromeBytes[j]  = sum;
	  }
	  
      this.synBytes=syndromeBytes;
	  
	  //check if any errors by validating the syndromeBytes
	    /* Check if the syndrome is zero */
       boolean hasDataError = false;
       for (i =0 ; i < NPAR; i++) {
	        if (syndromeBytes[i] != 0) {
	                hasDataError = true;
	                break;
	        }
		  
       }
       
       if(!hasDataError){
       	System.out.println(" NO ERRORS");
       	return false;
       }else{
          	System.out.println(" HAS ERRORS");
    	   return true;
       }

	}
	
    /** Returns codeword (encoded data) by appending the parity bytes (erasure) 
     * to the end of the message
    **/
    public byte[] getCodeWord (byte[] data, int nbytes, byte[] erasure)
    {
        int i;
        byte[] codeword=new byte[nbytes+NPAR];
        for (i = 0; i < nbytes; i++) codeword[i] = (byte)data[i];

        for (i = 0; i < NPAR; i++) {
        	codeword[i+nbytes] = (byte)erasure[NPAR-1-i];
        }
        
        return codeword; 
    }
    
    
}
