package com.traderdoge;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class DB
{
	private final static boolean _TESTING = true;
	
	public HashMap<Integer, DogeItem> getAll()
	{
		HashMap<Integer, DogeItem> result = new HashMap<Integer, DogeItem>();
		
		if (_TESTING)
		{
			try
			{
				result.put(0, new DogeItem(0, new Image("imgs/img0.png"), new Image("bgs/bg0.jpg"), "doge item 0", "such 0", "9.90", "21111210", new String[]{"so 0-1", "very 0-2"}));
				result.put(1, new DogeItem(1, new Image("imgs/img1.png"), new Image("bgs/bg1.jpg"), "doge item 1", "such 1", "9.91", "21111211", new String[]{"so 1-1", "very 1-2"}));
				result.put(2, new DogeItem(2, new Image("imgs/img2.png"), new Image("bgs/bg2.jpg"), "doge item 2", "such 2", "9.92", "21111212", new String[]{"so 2-1", "very 2-2"}));
				result.put(3, new DogeItem(3, new Image("imgs/img3.png"), new Image("bgs/bg3.jpg"), "doge item 3", "such 3", "9.93", "21111213", new String[]{"so 3-1", "very 3-2"}));
				result.put(4, new DogeItem(4, new Image("imgs/img4.png"), new Image("bgs/bg4.jpg"), "doge item 4", "such 4", "9.94", "21111214", new String[]{"so 4-1", "very 4-2"}));
				result.put(5, new DogeItem(5, new Image("imgs/img5.png"), new Image("bgs/bg5.jpg"), "doge item 5", "such 5", "9.95", "21111215", new String[]{"so 5-1", "very 5-2"}));
			}
			catch (FileNotFoundException fnfe)
			{
				fnfe.printStackTrace();
			}
		}
		
		return result;
	}
}
