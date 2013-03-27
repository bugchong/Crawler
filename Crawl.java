package com.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawl 
{
	public int ThreadNum = 5;
	public static String Domain = "http://liba.com/";
	public int WebIndex =0;
	public String Zdomain = null;
	public ArrayList<String> Unvisit = new ArrayList<String>();//����URL����
	public HashSet<String> Visted = new HashSet();//����URL����
	Document Doc = null;
	Pattern p1 =Pattern.compile("^http\\:.+#\\w+$");
	//���������
	public static void main(String[] args) 
	{
		Crawl crawl = new Crawl();
		
	} 
	//������ҳ��ȡ
	public Crawl()
	{
		//��������
		int Starturl = Domain.indexOf("http://");
		if(Starturl>=0)
		{
			Starturl+=7;
			int Endurl = Domain.indexOf("/", Starturl);
			if(Endurl>0)
			{
				Zdomain = Domain.substring(Starturl, Endurl);
			}else 
			{
				Zdomain = Domain.substring(Starturl);
			}		
		}else 
		{
			  Zdomain = Domain;				
		}
		//����Jsoup��ȡ����
		long StartTime = System.currentTimeMillis();
		CrawlGetUrl(Domain);
		while(true)
		{
			if(Unvisit.isEmpty())
			{
				long FinishTime = System.currentTimeMillis();
				System.out.println("Complete!");
				long UsedTime = FinishTime-StartTime;
				System.out.println("����ʱ"+UsedTime+"����");
				break;
			}else
			{
				String tmpUrl = getCrawlURL();
				CrawlGetUrl(tmpUrl);
			}
		}
		System.out.println("visit�ڵ�URL:"+Visted);
		System.out.println("visit������:"+Visted.size());
	}	
	//URL�����ӷ������������鷽��
	public synchronized void dealURL(String sUrl)
	{
		String sTmp = null;
		if (sUrl.contains("?"))
		{
			sTmp = sUrl.substring(0, sUrl.indexOf("?"));
		}
		else
		{
			sTmp = sUrl;
		}
		boolean bAdd = true;
		int nCount = 0;
		Object o[] = Visted.toArray();
		for (int i = 0; i < o.length; i++)
		{			
			if (o[i].toString().contains(sTmp))
			{
				nCount++;
				if (nCount >= 2)
				{
					bAdd = false;
					break;
				}
			}
		}
		if (bAdd)
		{
			Visted.add(sUrl);
			if(!Unvisit.contains(sUrl))
			{
				Unvisit.add(sUrl);
			}
//			System.out.println("visit������:"+Visted.size());
		}
	}
	public synchronized String getCrawlURL()
	{
		   
			String sTmpUrl = null;
			if(Unvisit.size()>0)
			{
	     	sTmpUrl = Unvisit.get(0);
	     	Unvisit.remove(0);	 
	     	CrawlGetUrl(sTmpUrl);
	     	Visted.add(sTmpUrl);
	     	}
			return sTmpUrl;	
	}
	//Jsoup��ȡ����
	public synchronized void CrawlGetUrl(String sUrl)
	{
		try {
			int statusCode = Jsoup.connect(sUrl).execute().statusCode();			
			if(statusCode == 200)
			{						
				Doc = Jsoup.connect(sUrl).header("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)")
				.header("Accept-Language", "zh-CN")
				.header("Accept-Encoding", "gzip, deflate")
				.header("Proxy-Connection", "keep-alive")
				.timeout(3000)
				.get();
				
				Elements links = Doc.select("a");				
				for(Element link : links)
				{
					String AbsUrl = link.absUrl("href");
					if (AbsUrl.contains(".pdf") || AbsUrl.contains("@") || AbsUrl.contains(".rar") || AbsUrl.contains(":80")
						|| AbsUrl.contains(".zip") || AbsUrl.contains(".jpg")|| AbsUrl.contains(".pdf") || AbsUrl.contains(".png"))
					{						
						continue;
					}	
					//ȥ������BAIDU��ҳ�е�TAG������
					Matcher m = p1.matcher(AbsUrl);
					if(m.matches())
					{
						AbsUrl = AbsUrl.substring(0,AbsUrl.indexOf("#"));
					}
					if(AbsUrl.contains(Zdomain))
					{
						if(AbsUrl.endsWith("/")||AbsUrl.endsWith("#"))
			 			{ 				
							AbsUrl = AbsUrl.substring(0,AbsUrl.length()-1);								
			 			}
//						�ټ�һ���ж�,����Unvisit������ʼ�մ��ڼ����ظ�ѭ����URL
						if(Visted.contains(AbsUrl))
						{
							Unvisit.remove(AbsUrl);
							
						}else
						{
							dealURL(AbsUrl);
						}					
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();			
		}
	}
}