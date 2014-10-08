package Server;

import java.util.Arrays;

public class ReedSolomonTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        ReedSolomon rs = new ReedSolomon(20);
        byte [] indata=new byte[100];
        copyInto("1234567890".getBytes(),indata,0,10);
        byte [] erasure,correctedData;
        erasure=new byte[indata.length+rs.NPAR];

        rs.encode_data(indata, indata.length, erasure);
        
        erasure[0]='X';
        
        rs.decode_data(erasure, erasure.length);
        System.out.println(" error ?" + rs.check_syndrome());
        
        rs.correct_errors_erasures(erasure, erasure.length, 0, null);
        
         System.out.println("\tData/error data " + new String(indata) + "\n\t corrected data " + new String(erasure) + " len="+erasure.length);

       
        
        
        erasure=rs.getErasure(indata, indata.length);
        rs.checkIfErrorInData(indata, indata.length, erasure);
        correctedData=rs.getErrorCorrected(indata, indata.length, erasure);
        
        System.out.println("\tData/error data " + new String(indata) + "\n\t corrected data " + new String(correctedData) + " len="+correctedData.length);

        //create errors in data
        copyInto("123456AA7890".getBytes(),indata,0,11);
        rs.checkIfErrorInData(indata, indata.length, erasure);
        correctedData=rs.getErrorCorrected(indata, indata.length, erasure);
        
        System.out.println("\tData/error data " + new String(indata) + "\n\t corrected data " + new String(correctedData) + " len="+correctedData.length);        
        //create errors in data
        indata=new byte[100];
        copyInto("A2345678".getBytes(),indata,0,8);
        rs.checkIfErrorInData(indata, indata.length, erasure);
        correctedData=rs.getErrorCorrected(indata, indata.length, erasure);
        
        System.out.println("\tData/error data " + new String(indata) + "\n\t corrected data " + new String(correctedData) + " len="+correctedData.length);        
        
        
        
        
        
        
        
        
	}
	
	private static void copyInto(byte[] src, byte[] dest, int destOff, int srcBytes ){
		int i=0;
		while(i<srcBytes && i<src.length){
			dest[i+destOff]=src[i];
			i++;
		}
	}

}
