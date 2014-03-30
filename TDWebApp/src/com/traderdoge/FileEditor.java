package com.traderdoge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class FileEditor
{
	public static String cpFileToDir(File src, File destDir)
	{
		if (!src.exists())
        {
            return "Source file " + src + " does not exist.";
        }
		
        if (destDir.exists())
        {
            destDir.delete();
        }
        destDir.mkdirs();
        
        try
		{
			FileUtils.copyFileToDirectory(src, destDir, true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
        
        return "";
	}
	
    public static String cpDirContents(String srcDirPath, String destDirPath)
    {
        File srcDir = new File(srcDirPath);
        if (!srcDir.exists())
        {
            return "Source directory " + srcDirPath + " does not exist.";
        }
        
        File destDir = new File(destDirPath);
        if (destDir.exists())
        {
            destDir.delete();
        }
        destDir.mkdirs();
        
        File[] srcDirFiles = srcDir.listFiles();
        
        for (File f : srcDirFiles)
        {
            try
            {
                if (f.isDirectory())
                {
                    FileUtils.copyDirectoryToDirectory(f, destDir);
                }
                else
                {
                    FileUtils.copyFileToDirectory(f, destDir);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        return "";
    }
    
    public static String replaceInDir(String path, final String filePattern, String find, String replace, String stopAt)
    {
        File dir = new File(path);
        if (!dir.exists())
        {
            return "Directory " + path + " does not exist.";
        }
        
        if (!dir.isDirectory())
        {
            return "Directory " + path + " is not a directory.";
        }
        
        ArrayList<File> fileList = getFiles(dir, filePattern);
        
        for (File f : fileList)
        {   
            try
			{
				replaceInFile(f, find, replace);
			}
			catch (FileNotFoundException e)
			{
				continue;
			}
        }
        
        return "";
    }

    private static ArrayList<File> getFiles(File dir, final String filePattern)
    {
        ArrayList<File> result = new ArrayList<File>();
        
        File[] fileList = dir.listFiles();
        
        for (File f : fileList)
        {
            if (f.getName().contains(filePattern))
            {
                result.add(f);
            }
            else if (f.isDirectory())
            {
                result.addAll(getFiles(f, filePattern));
            }
        }
        
        return result;
    }
    
    public static void templateReplace(String templatePath, String destFilePath, HashMap<String, String> replaceMap) throws FileNotFoundException
    {
        File f = new File(templatePath);
        
        StringBuilder sb = new StringBuilder();
        appendFileToBuffer(sb, f);
        
        String sbStr = sb.toString();
        Set<String> keySet = replaceMap.keySet();
        for (String key : keySet)
        {
            sbStr = sbStr.replace(key, replaceMap.get(key));
        }
        
        writeNewFile(f, sbStr.getBytes());
    }
    
    public static void replaceInFileName(String filePath, final String find, final String replace) throws FileNotFoundException
    {
        File f = new File(filePath);
        replaceInFile(f, find, replace);
    }
    
    public static void replaceInFile(File f, final String find, final String replace) throws FileNotFoundException
    {
        StringBuilder sb = new StringBuilder();
        appendFileToBuffer(sb, f);
        
        String str = sb.toString();
        StringBuilder newSB = new StringBuilder();
        newSB.append(str.replace(find, replace));
        
        writeNewFile(f, newSB.toString().getBytes());
    }
    
    private static void appendFileToBuffer(StringBuilder sb, File f)
    {
    	try
    	{
	    	FileInputStream fis;
	        fis = new FileInputStream(f);               
	        final int READ_SIZE = 128;
	        int amtRead = 0;
	        byte[] buf = new byte[READ_SIZE];
	        do
	        {
	            amtRead = fis.read(buf, 0, READ_SIZE);
	            if (amtRead > 0)
	            {
	                sb.append(new String(buf, 0, amtRead));
	            }
	        }
	        while (amtRead > 0);
	        fis.close();
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public static void deleteDir(File dir)
    {
    	if (dir.exists())
    	{
    		if (dir.isDirectory())
    		{
	    		try
				{
					FileUtils.deleteDirectory(dir);
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		else
    		{
    			dir.delete();
    		}
    	}
    }
    
    public static ArrayList<String> getDirectoryNames(File path)
    {
    	ArrayList<String> result = new ArrayList<String>();
        
        File[] fileList = path.listFiles();
        
        for (File f : fileList)
        {
            if (f.isDirectory())
            {
                result.add(f.getName());
            }
        }
        
        return result;
    }
    
    public static String getFileChunk(File f, String beginningMatch, String endMatch)
    {
    	String result = "";
    	
    	StringBuilder sb = new StringBuilder();
        appendFileToBuffer(sb, f);
        if (beginningMatch == null)
        {
        	return sb.toString();
        }
        
    	String s = sb.toString();
    	int loc0 = s.indexOf(beginningMatch);
    	if (loc0 != -1)
    	{
    		int loc1 = s.indexOf(endMatch, loc0);
    		if (loc1 != -1)
    		{
    			result = s.substring(loc0, loc1);
    		}
    	}
        
    	return result;
    }
    
    public static void writeNewFile(File f, byte[] content)
    {
    	try
        {
    		if (f.exists())
    		{
    			f.delete();
    		}
            f.createNewFile();
            
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(content);
            fos.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
