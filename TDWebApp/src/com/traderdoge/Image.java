package com.traderdoge;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;

public class Image
{
	private File f = null;
	
	public Image(String path) throws FileNotFoundException
	{
		f = new File(path);
		
		if (!f.exists())
		{
			f = null;
			throw new FileNotFoundException("Could not find image file: " + path);
		}
	}
	
	public File getFile()
	{
		return f;
	}
	
	public String getModDateString()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		return sdf.format(f.lastModified());
	}

	public boolean isSameModDate(File imgFile)
	{
		if (imgFile != null)
		{
			return f.lastModified() == imgFile.lastModified();
		}
		
		return false;
	}

	public boolean hasSameName(File imgFile)
	{
		if (imgFile != null)
		{
			return f.getName().equals(imgFile.getName());
		}
		
		return false;
	}
}
