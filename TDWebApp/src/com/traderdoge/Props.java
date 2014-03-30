package com.traderdoge;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Props extends Properties
{
    private static final long serialVersionUID = 7126750187573136807L;
    private static final String FILE_NAME = "tdwebapp.properties";
    private static final String NOT_SET_VALUE = "not-set";
    
    public enum Field
    {
        NONE
    }
    
//    private final static String HTDOCS_APKS_DIR = "/opt/htdocs/apks/";
//    private final static String DB_NAME = "appParams.db";
//    private final static String IMAGES_SVN_BASE_URL = "http://cv1.ubmedia:97/repos/technology/images/platforms/android/";
//    private final static String APKS_SVN_BASE_URL = "http://cv1.ubmedia:97/viewvc/technology/devices/android/apks/";
//    private final static String SVN_USERNAME = "zshenkle";
//    private final static String SVN_PASSWORD = "peterpaniclow";

    private static Props props = null;
    
    private boolean wasJustCreated = false;
    
    public static Props getProps()
    {
        if (props == null)
        {
            props = new Props();
        }
        
        return props;
    }
    
    private Props()
    {
        String fileName = FILE_NAME;
        File f = new File(fileName);
        if (f.exists())
        {
            try
            {
                InputStream is = new DataInputStream(new FileInputStream(fileName));
                load(is);
                is.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            wasJustCreated = true;
            
            for (Field field : Field.values())
            {
            	setProperty(field.name(), NOT_SET_VALUE);
            }
            
            try
            {
                OutputStream os = new FileOutputStream(FILE_NAME);
                store(os, null);
                os.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public boolean getWasJustCreated()
    {
        return wasJustCreated;
    }
    
    public String getPropertyValue(Field f) throws IllegalArgumentException
    {
        String propVal = getProperty(f.name());
        
        if (propVal.equals(NOT_SET_VALUE))
        {
            throw new IllegalArgumentException("Property " + f.name() + " is not set.");
        }
        
        return propVal;
    }
}
