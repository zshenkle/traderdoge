package com.traderdoge;

public class DogeItem
{
	public int id;
	public Image img;
	public Image bgImg;
	public String title;
	public String sentence;
	public String price;
	public String date;
	private String[] phrases;
	
	 public DogeItem(int id, 
			 Image img,
			 Image bgImg,
			 String title,
			 String sentence,
			 String price,
			 String date,
			 String[] phrases)
	 {
		 this.id = id;
		 this.img = img;
		 this.bgImg = bgImg;
		 this.title = title;
		 this.sentence = sentence;
		 this.price = price;
		 this.date = date;
		 this.phrases = phrases;
	 }
	 
	 public String[] getPhrases()
	 {
		 return phrases;
	 }
	 
	 public String getPhrasesString()
	 {
		 String result = "";
		 
		 for (String phrase : phrases)
		 {
			 result += phrase + ",";
		 }
		 
		 return result.substring(0, Math.max(0, result.length() - 1));
	 }
}