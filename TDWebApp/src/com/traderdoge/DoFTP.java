package com.traderdoge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

public final class DoFTP
{
	public static boolean DEBUG = false;
	
	public static void test(String password)
	{
		HashMap<String, String> localToRemoteMap = new HashMap<String, String>();
		
		localToRemoteMap.put("imgs/", TDWebApp.SERVER_NAME + "/imgs/");
		
		try
		{
			storeFiles(TDWebApp.SERVER_NAME, TDWebApp.FTP_USERNAME, password, localToRemoteMap, true);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
	
    public static boolean storeFiles(String server, String username, String password, HashMap<String, String> localToRemoteMap, boolean binaryTransfer) throws UnknownHostException
    {
    	if (DEBUG)
    	{
    		return true;
    	}
    	
        final FTPClient ftp = new FTPClient();
        ftp.setListHiddenFiles(true);

        // suppress login details
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

        try
        {
            int reply;
            ftp.connect(server);
            System.out.println("Connected to " + server + " on " + ftp.getDefaultPort());

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                return false;
            }
        }
        catch (IOException e)
        {
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server. server: " + server);
            e.printStackTrace();
            return false;
        }

        try
        {
            if (!ftp.login(username, password))
            {
                ftp.logout();
                System.err.println("Failed to ftp login, username: " + username);
                return false;
            }

            System.out.println("Remote system is " + ftp.getSystemType());

            if (binaryTransfer) 
            {
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
            } 
            else 
            {
                // in theory this should not be necessary as servers should default to ASCII
                // but they don't all do so - see NET-500
                ftp.setFileType(FTP.ASCII_FILE_TYPE);
            }

            // Use passive mode as default because most of us are
            // behind firewalls these days.
            ftp.enterLocalPassiveMode();

            ftp.setUseEPSVwithIPv4(false);

            Set<String> localFiles = localToRemoteMap.keySet();
        	for (String local : localFiles)
        	{
        		if (local.endsWith("/*"))
        		{
        			String remote = localToRemoteMap.get(local);
        			if (!remote.endsWith("/")) remote = remote + "/";
        			File localF = new File(local.substring(0, local.indexOf("/*")));
        			if (localF.exists() && localF.isDirectory())
        			{
        				File[] fList = localF.listFiles();
        				for (File innerF : fList)
        				{
        					uploadFileOrDir(ftp, innerF, remote + innerF.getName(), false);
        				}
        				
        				continue;
        			}
        		}
        		
        		File f = new File(local);
        		uploadFileOrDir(ftp, f, localToRemoteMap.get(local), true);
        	}

            ftp.noop(); // check that control connection is working OK

            ftp.logout();
        }
        catch (FTPConnectionClosedException e)
        {
            System.err.println("Server closed connection.");
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
        }
        
        return true;
    }

	private static void uploadFileOrDir(FTPClient ftp, File f, String remote, boolean makeRemoteDir) throws IOException
	{
		if (f.isDirectory())
		{
			File[] fList;
			if (!remote.endsWith("/")) remote = remote + "/";
			if (true) ftp.makeDirectory(remote);
			fList = f.listFiles();
			
			for (File innerF : fList)
			{
				uploadFileOrDir(ftp, innerF, remote + innerF.getName(), true);
			}
			
			return;
		}
		
		InputStream input;
        input = new FileInputStream(f.getPath());
        ftp.storeFile(remote, input);
        input.close();
	}
}
