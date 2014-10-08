package Server;

 import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class Server extends Thread
{

    private Hashtable<String, String> usridPwdMap = new Hashtable<String,String>();
    private String Username;
    private ServerUI sui;
    private TPA tpa;

    private Socket serverSocket;
    
	
	/**
	 * The data data block size in bytes that would be size of chunks of data in the file that would be 
	 * considered for Parity bytes generation and Error correction
	 */
	public static final int DATABLOCK_SIZE = 100;
	/**
	 * The size of parity bytes. This size decide how much errors bytes in a data block can be corrected. 
	 */
	public static final int PARITYBYTES_SIZE = 50;
	
	
	
    public static class OnlyExt implements FilenameFilter
    {
        String ext;
        public OnlyExt(String ext)
        {
            this.ext = "." + ext;
        }
        public boolean accept(File dir, String name)
        {
            return name.endsWith(ext);
        }
    }

    
    Server()
    {
        usridPwdMap.put("admin", "admin");
        usridPwdMap.put("student", "student");
    }
    
    Server(Socket Connection, ServerUI psui, TPA ptpa)
    {
        usridPwdMap.put("admin", "admin");
        usridPwdMap.put("student", "student");
        serverSocket = Connection;
        this.sui = psui;
        this.tpa = ptpa;
    }
    
    /**
     * Called on a new client connection to the server. Handles the communication with a client on a given connection.
     */
    
    public void run()
    {
        String clientRequestCMD;
        String Response = "";
        String RequestType;
        String delimiter = "\\|";
        String[] clientRequestCMDTokens;
        RSA rsa = new RSA();
        
        try
        {
        	//InputStream to read data from the client in binary format
        	InputStream inputFromClientStream= serverSocket.getInputStream();
        	//Reader to read data from the client in character format
        	Reader inputFromClientReader = new InputStreamReader(inputFromClientStream);
        	//OutputStream to write data to the client in binary format
            DataOutputStream outToClientStream = new DataOutputStream(serverSocket.getOutputStream());

            while(true)
            {
            	//try to read initial data in character format. Line at a time. Using the Reader
            	String commandTxt=CommProtocolHelper.readLineFromStream(inputFromClientStream);
           	
                clientRequestCMD = rsa.decrypt(commandTxt);
                
                System.out.println("\nRequest : " + clientRequestCMD);

                clientRequestCMDTokens = clientRequestCMD.split(delimiter);
                RequestType = clientRequestCMDTokens[0];

                

                if(RequestType.equalsIgnoreCase("LOGIN"))
                {
                    /* Login command. 
                     * Protocol  
                     * LOGIN|<username>|<password>
                     */
                	System.out.println(" CMD:LOGIN"+clientRequestCMDTokens[1]);
                    if(loginValidation(clientRequestCMDTokens[1], clientRequestCMDTokens[2]))
                    {
                    	//if login successful, create a directory on server for the user (if not already there)
                        File file=new File(clientRequestCMDTokens[1]);
                        if(!file.exists())
                            file.mkdir();
                        file=new File(clientRequestCMDTokens[1]+"/.rs");
                        if(!file.exists())
                            file.mkdir();
                        //LoadFiles();
                        Response = new String("SUCCESSFUL");
                    }
                    else
                        Response = new String("FAILED");
                    
                }
                else if(RequestType.equalsIgnoreCase("UPLOAD"))
                {
                    /* Upload file command. 
                     * Protocol
                     * UPLOAD|<filename>|<filesize>
                     * <file data continues in next line. The byte count should match the file size indicate in the command statement above>
                     */
                	FileOutputStream fileErasureOutstream=null;
                	FileOutputStream fileDataOutstream=null;
                	try{
	                	//Create a file output stream to write to a file on server side using data coming from client (save file on server)
	                	fileDataOutstream = new FileOutputStream(getUserDirectoryPath(clientRequestCMDTokens[1]));
	                    
	                	//Create a file output stream to write to a file erasure data (erasure data has to parity bytes as per ReedSolomon)
	                	fileErasureOutstream = new FileOutputStream(getUserDirectoryErasurePath(clientRequestCMDTokens[1]+".rs"));
	                    
	                	ReedSolomon rs=new ReedSolomon(Server.PARITYBYTES_SIZE);
	                	
	                	// Read data from the Client and write to the local file on the server
	                	// The amount of data read from the client is based on the data size indicated by the 
	                	// client in its UPLOAD command. The file name and file byte size is given in the command
	                	
	                	int dataSize=Integer.parseInt(clientRequestCMDTokens[2]);
	                	int dataRead=0;
	                	byte[] buff=new byte[Server.DATABLOCK_SIZE]; //buffer for the data chunk read off the client input socket stream 
	                	byte[] erasureBuff = new byte[rs.NPAR];//buffer for erasure/parity data calculated for the fle data chunk
	                	
	                	System.out.println(" CMD: UPLOAD file:"+ clientRequestCMDTokens[1] + " Size:" + dataSize + " available " + inputFromClientStream.available() + "\n Uploading...");
	                	while(dataRead<dataSize){
	                		int bytesRead;
	                		bytesRead=CommProtocolHelper.readBinary(inputFromClientStream, buff, Math.min(buff.length, (dataSize-dataRead)));
	                		
	                		fileDataOutstream.write(buff,0,bytesRead);
	                		//get the erasure data for the file data part
	                		erasureBuff=rs.getErasure(buff, Server.DATABLOCK_SIZE);
	                		
	                		fileErasureOutstream.write(erasureBuff);
	                		
	                		dataRead=dataRead+bytesRead;
	                		System.out.print(dataRead + " .. ");
	                	}
	                	System.out.println("Done");

	                	
	                    Response = new String("SUCCESSFUL");
                	}
                    catch(Exception ex)
                    {
                        Response = "FAILED|" + ex.getMessage()+ ex.toString();
                        System.out.println("CMD: UPLOAD. Error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    finally{
	                	if(fileDataOutstream!=null)
	                		fileDataOutstream.close();
	                	if(fileErasureOutstream!=null)
	                		fileErasureOutstream.close();
                    }
                }
                else if(RequestType.equalsIgnoreCase("DOWNLOAD"))
                {
                    /* Download file command. 
                     * Request Protocol
                     * 		DOWNLOAD|<filename>
                     * Response Protocol
                     * 		CONTENT-SIZE|<filesize>
                     * 		<file data content ...>
                     * 		SUCCESSFUL
                     */
                   	int bytesRead=0;
                	byte[] buff=new byte[1000]; //buffer for the data chunk read off the client input socket stream 
 
                    FileInputStream fileInputStream = null;
                    try
                    {
                    	File fileRequested=new File(getUserDirectoryPath(clientRequestCMDTokens[1]));
                    	//Send content size of the file. This required for the client to know the end of file data on the stream
                    	//Client is expected to assume end of file, after reading the indicated content size
                    	Response = new String("CONTENT-LENGTH|") + fileRequested.length();	
                    	CommProtocolHelper.writeCommand(outToClientStream, Response);
                        System.out.println(Response+" sending file data...");
                        //Start writing file content to the client
                        fileInputStream = new FileInputStream(getUserDirectoryPath(clientRequestCMDTokens[1]));
                        int totalBytesSent=0;
	                  	while((bytesRead=fileInputStream.read(buff, 0, buff.length))>0){
	                  		outToClientStream.write(buff,0,bytesRead);
	                  		totalBytesSent+=bytesRead;
	                  		System.out.print(totalBytesSent + " .. ");
	                  	}
	                  	System.out.println("Done");
	                	System.out.println("File " +clientRequestCMDTokens[1] + " downloaded to Client");
	                	
	                    Response = new String("SUCCESSFUL");

                    }
                    catch(Exception ex)
                    {
                        Response = "FAILED|" + ex.getMessage() + ex.toString();
                        System.out.println("CMD: DOWNLOAD. Error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    finally{
                    	if(fileInputStream!=null)
                    		fileInputStream.close();
                    }
                }
                else if(RequestType.equalsIgnoreCase("DELETE"))
                {
                	try{
	                    File f1 = new File(getUserDirectoryPath(clientRequestCMDTokens[1]));
	                    boolean success = f1.delete();
	
	                    if (!success)
	                        Response = "FAILED";
	                    else
	                        Response = "SUCCESSFUL";
	                    
	                    //delete the erasure file
	                    f1= new File(getUserDirectoryErasurePath(clientRequestCMDTokens[1]+".rs"));
	                    success = f1.delete();
                	}
                    catch(Exception ex){
                    	ex.printStackTrace();
                    	Response ="FAILED|"+ex.getMessage() + ex.toString();
                    }

                }
                else if(RequestType.equalsIgnoreCase("GETFILENAMES"))
                {
                	try{
	                    File file = new File(getUserDirectoryPath(""));
	                    //FilenameFilter textFiles = new Server.OnlyExt("txt");
	                    
	                    File Files[] = file.listFiles(); //textFiles);
	
	                    Response = "FILES|";
	                    for(int i = 0; i < Files.length; i++)
	                    {
	                        if(Files[i].isDirectory())
	                        	continue;
	                        Response = Response + Files[i].getName();
	                        if(i == Files.length - 1)
	                            break;
		                    Response = Response + "|";
	                    }
	            	}
	                catch(Exception ex){
	                	ex.printStackTrace();
	                	Response ="FAILED|"+ex.getMessage() + ex.toString();
	                }
	
	                }
                else if(RequestType.equalsIgnoreCase("VERIFY"))
                {
                	try{
	                    tpa.setTextArea("Received request to verify file " + clientRequestCMDTokens[1] + " for user " + getUsername() + "\n");
	                    boolean hasErrors=verifyAndCorrectFile(clientRequestCMDTokens[1]);
	                    if (hasErrors)
	                    {
	                        Response = "FILE CORRECTED";
	                        tpa.setTextArea("File " + clientRequestCMDTokens[1] + " was corrected and recovered successfully" + "\n");
	                    }
	                    else
	                    {
	                        tpa.setTextArea("No errors were found in the file " + clientRequestCMDTokens[1] + "\n");
	                        Response = "FILE OK";
	                    }
                	}
	                catch(Exception ex){
	                	ex.printStackTrace();
	                	Response ="FAILED|"+ex.getMessage() + ex.toString();
	                }
                }
                else if(RequestType.equalsIgnoreCase("LOGOFF"))
                {
                    inputFromClientReader.close();
                    outToClientStream.close();
                    serverSocket.close();
                    this.stop();
                }

                System.out.println("Response : " + Response);
                
            	//flush any more data on the input communication stream
            	CommProtocolHelper.flushInputStream(inputFromClientStream);
                //Write Command response
            	CommProtocolHelper.writeCommand(outToClientStream, Response);
            }        
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
    }
        
    String getUsername()
    {
        return Username;
    }
      
    String getUserDirectoryPath(String Filename)
    {
        return Username + "\\" + Filename;
    }
    String getUserDirectoryErasurePath(String Filename)
    {
        return Username + "\\.rs\\" + Filename;
    }
    
    boolean loginValidation(String Username, String Password)
    {
        String HashtablePassword;

        if (usridPwdMap.get(Username.toLowerCase()) != null)
        {
            HashtablePassword = usridPwdMap.get(Username.toLowerCase()).toString();
            if(Password.equalsIgnoreCase(HashtablePassword))
            {
                    this.Username = Username;
                    return Boolean.TRUE;
            }            
        }
            return Boolean.FALSE;             
    }
    
    /**
     * Load files in the user directory. Here user directory is same name as the username on the 
     * server file system. Only files with extension txt is listed.
     */
   /* public void LoadFiles()
    {
        File userDirectory = new File(getUsername());
        FileInputStream fileInputStream = null;
        FilenameFilter textFilesFilter = new Server.OnlyExt("txt");
        File Files[] = userDirectory.listFiles(textFilesFilter);

        //Iterate through each file in the user directory
        for(File file: Files)
        {
            int ch;
            StringBuffer strContent = new StringBuffer("");
            
            try
            {
                    fileInputStream = new FileInputStream(file);
                    while ((ch = fileInputStream.read()) != -1)
                            strContent.append((char) ch);
                    fileInputStream.close();
            }
            catch(Exception ex)
            {
            	ex.printStackTrace();
            }
        }
    }*/
    
    /**
     * Verify contents of file using it erasure data (.rs file). If errors are found,
     * a recovery is attempted and wherever possible the recovered/corrected data is
     * written to the file. This is done by using a temp file, which is later overwritten
     * on the original file on recovery.
     * 
     */
    public boolean verifyAndCorrectFile(String Filename) throws IOException
    {
    	FileOutputStream verifiedDataFileInputStream =null;
    	FileInputStream fileDataInputStream = null;
    	FileInputStream fileErasureInputStream  =null;
        boolean errorsFound=false;     
       	try{
        	//Create a file output stream to write corrected data, temperory file
        	verifiedDataFileInputStream = new FileOutputStream(getUserDirectoryPath(Filename)+".tmp");
            
        	//Create a file Input stream to read current file data
        	fileDataInputStream = new FileInputStream(getUserDirectoryPath(Filename));

        	//Create a file Input stream to read erasure data (erasure data is the parity bytes as per ReedSolomon)
        	fileErasureInputStream = new FileInputStream(getUserDirectoryErasurePath(Filename+".rs"));
            
        	ReedSolomon rs=new ReedSolomon(Server.PARITYBYTES_SIZE);
        	
        	// Read data from the Current data file, and the corresponding erasure file and output verified / corrected data to a tmp file
        	int dataRead=0;
        	byte[] buff=new byte[Server.DATABLOCK_SIZE]; //buffer for the data chunk read off the Original file
        	byte[] verifiedCorrectedDataBuff;//buffer for the verified & corrected data
        	byte[] erasureBuff = new byte[rs.NPAR];//buffer for erasure/parity data calculated for the fle data chunk
        	
        	System.out.println(" Verifying and Correcting:  file:"+ Filename +"\n Verifying ...");
        	while(true){
        		int bytesRead=fileDataInputStream.read(buff, 0, buff.length);
        		fileErasureInputStream.read(erasureBuff,0,Server.PARITYBYTES_SIZE);
        		if(bytesRead<0) break;
        		if(rs.checkIfErrorInData(buff, Server.DATABLOCK_SIZE, erasureBuff)){
        			errorsFound=true;
        			//when error in data, call correction mechanism
        			System.out.println("Found Error in...\n\t" + new String(buff));
        			verifiedCorrectedDataBuff=rs.getErrorCorrected(buff, Server.DATABLOCK_SIZE, erasureBuff);
        			System.out.println("corrected as...\n\t" + new String(verifiedCorrectedDataBuff));
        		}else{
        			verifiedCorrectedDataBuff=buff;//user original data if found without errors
        		}
        		verifiedDataFileInputStream.write(verifiedCorrectedDataBuff,0,bytesRead);
        		
        		dataRead=dataRead+bytesRead;
        		System.out.print(dataRead + " .. ");
        	}
        	System.out.println("Done");
        	
        	//Close all files
        	fileDataInputStream.close();fileDataInputStream=null;
        	fileErasureInputStream.close();fileErasureInputStream=null;
        	verifiedDataFileInputStream.close();verifiedDataFileInputStream=null;
        	
        	if(errorsFound){
            	//If errors found, overwrite the current file with the error corrected file
	        	//Delete the original file
           		boolean success=new File(getUserDirectoryPath(Filename)).delete();
           		System.out.println("File "+getUserDirectoryPath(Filename)+ " deleted?" + success);
	        	//Make the corrected file as original file name
           		success=new File(getUserDirectoryPath(Filename)+".tmp").renameTo(new File(getUserDirectoryPath(Filename)));
        		System.out.println(" Original file " + getUserDirectoryPath(Filename) + " overwritten with corrected file?" +success);
        	}else{
        		new File(getUserDirectoryPath(Filename)+".tmp").delete();
        	}
        		
        	return errorsFound;
        }
        catch(Exception ex)
        {      
	        if(fileDataInputStream!=null)
	    		fileDataInputStream.close();
	    	if(fileErasureInputStream!=null)
	    		fileErasureInputStream.close();
	    	if(verifiedDataFileInputStream!=null)
	    		verifiedDataFileInputStream.close();
            System.out.println("Verify Error: " + ex.getMessage()+ ex.toString());
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static void main(String argv[]) throws Exception
    {
        //Server server = new Server();
        ServerSocket serverSocket = new ServerSocket(8888);

		try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }


        ServerUI serverWindow = new ServerUI();
        serverWindow.setVisible(true);
        
        TPA tpaWindow = new TPA();
        tpaWindow.setVisible(true);
        tpaWindow.setLocation(637,0);
        
        while(true)
        {
            Socket connectionSocket = serverSocket.accept();
            //Create a new server client Socket communication handler
            Server server = new Server(connectionSocket, serverWindow, tpaWindow);
            server.start();
        }
    }
}