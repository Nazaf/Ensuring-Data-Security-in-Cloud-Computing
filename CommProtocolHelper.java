package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Helper class with utility method for the Handling Communication between client and sever
 *
 */
public class CommProtocolHelper {
	
	private static RSA rsa=new RSA();
	/**
	 * Reads a line from the input stream. The stream is assumed to be a binary stream.
	 * Character reading till newline 
	 * @param inStream
	 * @return the data string read
	 * @throws IOException 
	 */
	public static String readLineFromStream(InputStream inStream) throws IOException{
    	StringBuffer dataLine=new StringBuffer();
    	Reader inputReader= new InputStreamReader(inStream);//Convert to character stream
    	char c[]=new char[1];
    	while(inputReader.read(c)>0){
    		if((c[0]=='\n' || c[0]=='\r')) 
    			break;
    		dataLine.append(c[0]);
    		//System.out.print(c[0]);
    	}
    	return dataLine.toString();
	}
	/**
	 * Remove all data in the Stream. Used when resetting communication streams for next
	 * communication. Used at both Client and Server on the socket input streams before
	 * executing the next command (Client) or waiting for next command (Server) 
	 * @throws IOException 
	 */
	public static void flushInputStream(InputStream inputStreamToFlush) throws IOException{
//		int bytesToFlushed=0;
//		while(inputStreamToFlush.available()>0){
//			System.out.println((byte)inputStreamToFlush.read());
//			bytesToFlushed++;
//		}
//		System.out.println(" Flushed " + bytesToFlushed);
	}
	/**
	 * Writes a command on the communication stream. Encrypts the command, adds a new line0
	 * at the end command, as the delimiter. The delimiter is not encrypted.
	 * @param outCommStream the output communication stream to write to (of type DataOutputStream)
	 * @return
	 * @throws IOException 
	 */
	public static void writeCommand(OutputStream outCommStream, String command) throws IOException{
		outCommStream.write((rsa.encrypt(command) + "\n").getBytes()); // only command is encrypted not the new line
	}
	/**
	 * Read into a binary array. A helper method provided to enable future expansion of on the stream encryption
	 * @param inStream
	 * @param readDataBytes
	 * @param bytesToRead
	 * @return
	 * @throws IOException
	 */
	public static int readBinary(InputStream inStream, byte[] readDataBytes, int bytesToRead) throws IOException{
		return inStream.read(readDataBytes, 0, bytesToRead);
	}
}
