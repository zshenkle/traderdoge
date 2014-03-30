package com.traderdoge;

import java.io.File;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class ContentDir
{
	private final static String HEADER_START = "<!--^HEADER_START^";
	private final static String HEADER_END = "^HEADER_END^-->";
	private final static String TEMPLATE_HEADER_REPLACE = "<!--HEADER-->";
	private static final String REPORT_ALL = "ALL";
	
	private enum HeaderItem
	{
		IMG_NAME, BG_IMG_NAME, TITLE, SENTENCE, PRICE, DATE, PHRASES
	}
	
	private String report = "";
	
	public ContentDir(String path, boolean fresh)
	{
		File statusFile = new File(TDWebApp.STATUS_FILE_NAME);
		File contentDir = new File(path);

		if (fresh)
		{
			FileEditor.deleteDir(contentDir);
			statusFile.delete();
		}
		
		if (statusFile.exists())
		{
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			
			if (contentDir.exists() && contentDir.isDirectory())
			{
				System.out.println("Build already done on " + sdf.format(statusFile.lastModified()));
				return;
			}
			
			System.out.println("Build already done on " + sdf.format(statusFile.lastModified()) + " but " + path + " does not seem to exist, doing new build.");
			FileEditor.deleteDir(contentDir);
			statusFile.delete();
		}
		
		ArrayList<String> contentDDirNames = new ArrayList<String>();
		if (contentDir.exists())
		{
			contentDDirNames = FileEditor.getDirectoryNames(contentDir);
		}
		
		DB db = new DB();
		HashMap<Integer, DogeItem> dbItems = db.getAll();
		
		generateReport(contentDir, contentDDirNames, dbItems);
		
		if (!contentDir.exists())
		{
			// make all contentDDirs for first time
			
			contentDir.mkdir();
			Collection<DogeItem> dbItemsValues = dbItems.values();
			for (DogeItem dbItem : dbItemsValues)
			{
				copyFilesTocontentDDir(contentDir, dbItem);
			}
			
			return;
		}
		
		// delete any dirs in the test dir that aren't in dbItems
		for (String contentDDirName : contentDDirNames)
		{
			int contentDDirNameInt = -1;
			try
			{
				contentDDirNameInt = Integer.parseInt(contentDDirName);
			}
			catch (NumberFormatException nfe)
			{
			}
			
			if (!dbItems.containsKey(contentDDirNameInt))
			{
				File contentDDir = new File(contentDir, contentDDirName);
				FileEditor.deleteDir(contentDDir);
			}
		}
		
		// loop through the commands (in between {}) in the status file and apply the changes to the folders/files
		
		int loc1 = 0;
		int loc0 = 0;
		while (loc0 != -1)
		{
			loc0 = report.indexOf('{', loc1);
			if (loc0 != -1)
			{
				loc1 = report.indexOf('}', loc0);
				if (loc1 != -1)
				{
					String idStr = report.substring(loc0 + 1, loc1);
					int id = -1;
					try
					{
						id = Integer.parseInt(idStr);
					}
					catch (NumberFormatException nfe)
					{
						nfe.printStackTrace();
						continue;
					}
					
					DogeItem dbItem = dbItems.get(id);
					copyFilesTocontentDDir(contentDir, dbItem);
				}
				else
				{
					System.err.println("Missing } while building test directory from status report");
					break;
				}
			}
		}
	}

	public String getReport()
	{
		if (report == null || report.length() == 0)
		{
			return "Nothing needs to be updated.";
		}
		
		return "Nodes to be sent via FTP: " + report;
	}
	
	private void generateReport(File f, ArrayList<String> contentDDirNames, HashMap<Integer, DogeItem> dbItems)
	{
		if (f.exists())
		{
			if (!f.isDirectory())
			{
				f.delete();
				f.mkdir();
			}
			
			report = "";
			Collection<DogeItem> dbItemsValues = dbItems.values();
			for (DogeItem dbItem : dbItemsValues)
			{
				String updateReport = updateReport(f, contentDDirNames, dbItem);
				report += updateReport;
			}
		}
		else
		{
			report = REPORT_ALL;
		}
		
		File statusFile = new File(TDWebApp.STATUS_FILE_NAME);
		FileEditor.writeNewFile(statusFile, report.getBytes());
	}
	
	/**
	 * @param testDir root directory holding each dir for each doge item page
	 * @param contentDDirs list of these directory names
	 * @param dbItem the item in the database representing what its corresponding td dir should have
	 * @return a command, example {4} update td dir id=4
	 */
	private String updateReport(File testDir, ArrayList<String> contentDDirs, DogeItem dbItem)
	{
		String idFromDB = new String("" + dbItem.id);
		String result = "{" + idFromDB;
		
		if (!contentDDirs.contains(idFromDB))
		{
			return result + "}";
		}
		
		File contentDDir = new File(testDir, idFromDB);
		if (!contentDDir.isDirectory())
		{
			return result + "}";
		}
		
		File indexHtml = new File(contentDDir, "index.html");
		if (!indexHtml.exists())
		{
			return result + "}";
		}
		
		String header = FileEditor.getFileChunk(indexHtml, HEADER_START, HEADER_END);
		
		String imgName = getHeaderItemValue(header, HeaderItem.IMG_NAME);
		File testDirImgFile = new File(contentDDir, imgName);
		String bgImgName = getHeaderItemValue(header, HeaderItem.BG_IMG_NAME);
		File testDirBGImgFile = new File(contentDDir, bgImgName);
		
		// if either the img or bg img doesn't exist, or either of their names differ from their db counterpart, then write the contentDDir
		if (!testDirImgFile.exists() || !dbItem.img.hasSameName(testDirImgFile) || 
				!testDirBGImgFile.exists() || !dbItem.bgImg.hasSameName(testDirBGImgFile))
		{
			return result + "}";
		}
		
		if (!dbItem.img.isSameModDate(testDirImgFile))
		{
			return result + "}";
		}
		
		if (!dbItem.bgImg.isSameModDate(testDirBGImgFile))
		{
			return result + "}";
		}
		
		String title = getHeaderItemValue(header, HeaderItem.TITLE);
		String sentence = getHeaderItemValue(header, HeaderItem.SENTENCE);
		String price = getHeaderItemValue(header, HeaderItem.PRICE);
		String date = getHeaderItemValue(header, HeaderItem.DATE);
		String phrases = getHeaderItemValue(header, HeaderItem.PHRASES);
		
		if (!(title.equals(dbItem.title) && sentence.equals(dbItem.sentence) &&
				price.equals(dbItem.price) && date.equals(dbItem.date) &&
				phrases.equals(dbItem.getPhrasesString())))
		{
			return result + "}";
		}
		
		return "";
	}

	private String getHeaderItemValue(String header, HeaderItem hi)
	{
		String result = "";
		
		int loc0 = header.indexOf(hi.name());
		if (loc0 != -1)
		{
			int loc1 = header.indexOf('[', loc0);
			if (loc1 != -1)
			{
				int loc2 = header.indexOf(']', loc1);
				if (loc2 != -1)
				{
					result = header.substring(loc1 + 1, loc2);
				}
			}
		}
		
		return result;
	}
	
	private void copyFilesTocontentDDir(File testDir, DogeItem dbItem)
	{
		File contentDDir = new File(testDir, "" + dbItem.id);
		FileEditor.deleteDir(contentDDir);
		contentDDir.mkdir();
		FileEditor.cpFileToDir(dbItem.img.getFile(), contentDDir);
		FileEditor.cpFileToDir(dbItem.bgImg.getFile(), contentDDir);
		generateNewIndexHtml(dbItem, contentDDir);
	}
	
	private void generateNewIndexHtml(DogeItem dbItem, File contentDDir)
	{
		String template = "<html>" + TEMPLATE_HEADER_REPLACE + "<body>page from test template</body></html>";
		
		String header = HEADER_START + HeaderItem.IMG_NAME.name() + "=[" + dbItem.img.getFile().getName() + "]" +
				HeaderItem.BG_IMG_NAME.name() + "=[" + dbItem.bgImg.getFile().getName() + "]" +
				HeaderItem.TITLE.name() + "=[" + dbItem.title + "]" +
				HeaderItem.SENTENCE.name() + "=[" + dbItem.sentence + "]" +
				HeaderItem.PRICE.name() + "=[" + dbItem.price + "]" +
				HeaderItem.DATE.name() + "=[" + dbItem.date + "]" +
				HeaderItem.PHRASES.name() + "=[" + dbItem.getPhrasesString() + "]" + HEADER_END;
		
		String html = template.replace(TEMPLATE_HEADER_REPLACE, header);
		FileEditor.writeNewFile(new File(contentDDir, "index.html"), html.getBytes());
	}
	
	/**
	 * loop through the commands (in between {}) in the status file and ftp the appropriate files
	 * 
	 * @param password ftp password
	 * @param path
	 * @throws UnknownHostException
	 */
	public static void ftp(String password, String path) throws UnknownHostException
	{
		HashMap<String, String> localToRemoteMap = new HashMap<String, String>();
		File statusFile = new File(TDWebApp.STATUS_FILE_NAME);
		
		if (!statusFile.exists())
		{
			System.err.println("Status file \"" + TDWebApp.STATUS_FILE_NAME + "\" needed to indicate what needs sending via FTP.");
			return;
		}
		
		String report = FileEditor.getFileChunk(statusFile, null, null);
		
		if (report.equals(REPORT_ALL))
		{
			localToRemoteMap.put(path + "/*", TDWebApp.SERVER_NAME + "/");
		}
		else
		{
			int loc1 = 0;
			int loc0 = 0;
			while (loc0 != -1)
			{
				// loop through commands, example "{2}"
				loc0 = report.indexOf('{', loc1);
				if (loc0 != -1)
				{
					loc1 = report.indexOf('}', loc0);
					if (loc1 != -1)
					{
						String idStr = report.substring(loc0 + 1, loc1);
						localToRemoteMap.put(path + "/" + idStr, TDWebApp.SERVER_NAME + "/" + idStr);
					}
					else
					{
						System.err.println("Missing } while building test directory from status report");
						break;
					}
				}
			}
		}
		
		if (!localToRemoteMap.isEmpty())
		{
			if (DoFTP.storeFiles(TDWebApp.SERVER_NAME, TDWebApp.FTP_USERNAME, password, localToRemoteMap, true))
			{
				statusFile.delete();
			}
		}
		else
		{
			statusFile.delete();
		}
	}
}