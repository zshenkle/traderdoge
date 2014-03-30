package com.traderdoge;

import java.net.UnknownHostException;

public class TDWebApp
{
	public final static String SERVER_NAME = "traderdoge.com";
	public final static String FTP_USERNAME = "traderdo";
	public final static String CONTENT_DIR = "contentdir";
	public final static String STATUS_FILE_NAME = "status";
	
	public static void main(String[] args)
	{
		if (args == null || args.length == 0)
        {
            printUsage();
            return;
        }
		
		String command = args[0];
		
		if (command.equals("build"))
		{
			ContentDir td = new ContentDir(CONTENT_DIR, false);
			System.out.println(td.getReport());
		}
		else if (command.equals("buildfresh"))
		{
			ContentDir td = new ContentDir(CONTENT_DIR, true);
			System.out.println(td.getReport());
		}
		else if (command.equals("release"))
		{
			try
			{
				String password = args[1];
				ContentDir.ftp(password, CONTENT_DIR);
			}
			catch (UnknownHostException uhe)
			{
				uhe.printStackTrace();
			}
			catch (ArrayIndexOutOfBoundsException aioobe)
			{
				System.err.println("An FTP password must be provided.");
			}
		}
		else
		{
			System.err.println("Unknown command " + command);
			printUsage();
		}
	}

	private static void printUsage()
    {
        System.out.println("usage: [build|buildfresh|release <ftp-password>]");
        System.out.println("   build                  builds web pages from mysql data, html templates, images, into " + CONTENT_DIR + ", updates \"" + STATUS_FILE_NAME + "\" file");
        System.out.println("   buildfresh             deletes " + CONTENT_DIR + " and " + STATUS_FILE_NAME + " and then does fresh build");
        System.out.println("   release <ftp-password> ftps contents of " + CONTENT_DIR + " to " + SERVER_NAME + " using ftp user: " + FTP_USERNAME);
    }
}
