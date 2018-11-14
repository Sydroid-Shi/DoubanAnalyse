package com.zhweizhu.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.zhweizhu.DoubanGroup;

public class AnalyseUtil {
	public static String GUANGZHOU = "guangzhou";//广州
	public static String ZHUHAI = "zhuhai";//珠海
	public static String FOSHAN = "foshan";//佛山
	
	public static void print(String str) {
		System.out.println(str);
		writeToFile(str);
	}
	
	//To get the HTML content from an URL
	public static String getURLContent(String urlStr) {
		String charset = "UTF-8";// charset is default as "utf-8"
		String urlContent = null;
		AnalyseUtil.print("Getting content from: " + urlStr);
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			InputStream is = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			is.close();
			br.close();
			urlContent = sb.toString();

		} catch (Exception e) {
			AnalyseUtil.print("Exception happens in getURLContent: \n" + e);
		} finally {
			AnalyseUtil.print("\n The page content is: \n" + urlContent);
		}

		return urlContent;
	}
	
	public static String[] splitHTMLtoArticles(String htmlStr) {

		String start = "<table class";//<tbody> cannot be used for split
		String end = "</table>";//</tbody cannot be used.
		String splitStr = "</tr>";
		String[] articles;
		try {
			htmlStr = htmlStr.substring(htmlStr.indexOf(start)+7, htmlStr.lastIndexOf(end));
			//AnalyseUtil.print("content after substring: \n" + content);
			articles = htmlStr.trim().split(splitStr);
		}catch(Exception e) {
			AnalyseUtil.print("Exception happens in splitHTMLtoArticles: \n" + e);
			return null;
		}
		return articles;
	}
	
	/**
	 * To parse and get the page count for the Group Search URL.
	 * @param htmlStr The HTML result from: "https://www.douban.com/group/search?start=0&cat=1019&sort=relevance&q=广州租房"
	 * @return The page count for the Group Search URL
	 */
	public static int parseGroupSearchPageCount(String htmlStr) {
		String cutPrevious = "data-total-page";//demo: <span class="thispage" data-total-page="12">1</span>
		String cutEnd = ">1</span>";
		int pageCount = 0;
		if(htmlStr != null) {
			try {
				String pageCountStr = htmlStr.substring(htmlStr.indexOf(cutPrevious)+17, htmlStr.indexOf(cutEnd)-1);
				AnalyseUtil.print("The pageCountStr is : " + pageCountStr);
				pageCount = Integer.parseInt(pageCountStr);
			}catch(Exception e) {
				AnalyseUtil.print("Exception happens in parseGroupSearchPageCount: \n" + e);
			}
		}
		return pageCount;
	}
	
	/**
	 * 
	 * @param htmlStr The HTML result from: "https://www.douban.com/group/search?start=" + pageIndex + "&cat=1019&sort=relevance&q=" + keyWord
	 * @return The item list from every page
	 */
	public static List<DoubanGroup> parseGroups(String htmlStr){
		String cutPagePre = "<div class=\"groups\">";
		String cutPageEnd = "<span class=\"thispage\" data-total-page=";
		String splitGroup = "<div class=\"result\">";
		
		String cutGroupLinkPre = "<a class=\"nbg\" href=";//<a class="nbg" href="https://www.douban.com/group/537239/" onclick="moreurl(this,{i: '19', query: '%E5%B9%BF%E5%B7%9E%E7%A7%9F%E6%88%BF', from: 'group_search', sid: 537239})" title="广州免费租房大合集"><img class
		String cutGroupLinkEnd = "\" onclick=";
		String cutGroupNamePre = "title=\"";
		String cutGroupNameEnd = "\"><img";
		String cutGroupCountPre = "<div class=\"info\">";//<div class="info">4739 个成员 在此聚集 </div>
		String cutGroupCountEnd = " 个";
		
		List<DoubanGroup> doubanGroups = new ArrayList<DoubanGroup>();
		
		if(htmlStr != null) {
			try {
				String groupsPage = htmlStr.substring(htmlStr.indexOf(cutPagePre), htmlStr.indexOf(cutPageEnd));
				String[] groups = groupsPage.split(splitGroup);
				if(groups == null || groups.length == 0) return null;
				for(int i = 1; i < groups.length; i++) {//ignore item 0
					AnalyseUtil.print("The item content is: " + groups[i]);
					String groupLink = groups[i].substring(groups[i].indexOf(cutGroupLinkPre)+21, groups[i].indexOf(cutGroupLinkEnd));
					String groupName = groups[i].substring(groups[i].indexOf(cutGroupNamePre)+7, groups[i].indexOf(cutGroupNameEnd));
					String countTemp = groups[i].substring(groups[i].indexOf(cutGroupCountPre));
					String groupCount = countTemp.substring(countTemp.indexOf(cutGroupCountPre)+18, countTemp.indexOf(cutGroupCountEnd));
					AnalyseUtil.print("The group result is {groupName: " + groupName + ", groupLink: " + groupLink + ", groupCount: " + groupCount);
					DoubanGroup dbGroup = new DoubanGroup();
					dbGroup.setGroupName(groupName);
					dbGroup.setGroupURL(groupLink);
					dbGroup.setGroupMemberCount(Integer.parseInt(groupCount));
					doubanGroups.add(dbGroup);
				}
			}catch(Exception e) {
				AnalyseUtil.print("Exception happens in parseGroups: \n" + e);
			}
		}
		return doubanGroups;
	}
	

	public static boolean isBetweenPeriod(String currentDay, String preStartDay, String endDay) {
		preStartDay = "2018-" + preStartDay;
		endDay = "2018-" + endDay;
		if(preStartDay == null || endDay == null) {
			return false;
		}
		if(currentDay.compareTo(preStartDay) > 0 && currentDay.compareTo(endDay) <= 0) {
			return true;
		}
		return false;
	}
	

	public static List<String> getZhuhaiGroupIDs() {
		List<String> zhGroupIDs = new ArrayList<String>();

		zhGroupIDs.add("zhuhai_zufang");//My group
		zhGroupIDs.add("611595");//推荐度五颗星
		zhGroupIDs.add("576424");//无中介
		zhGroupIDs.add("583425");//渔女头像
		zhGroupIDs.add("zhfangzi");//珠海房子
		zhGroupIDs.add("555279");//被屏蔽
		return zhGroupIDs;
	}

	public static List<String> excludeUsers_zhuhai(){
		List<String> excludeUsers = new ArrayList<String>();
		excludeUsers.add("sd8d78s");
		excludeUsers.add("阿利狼");
		excludeUsers.add("陈陈陈啊");
		excludeUsers.add("A     ");
		return excludeUsers;
	}
	
	//Add group IDs to the list.
	public static List<String> getGuangzhouGroupIDs() {
		List<String> gzGroupIDs = new ArrayList<String>();
		//gzGroupIDs.add("gz020");//广州租房★（个人房源免费推广）
		//gzGroupIDs.add("tianhezufang");//广州天河租房（个人房源免费推广）
		gzGroupIDs.add("gz_rent");//广州租房
		/*gzGroupIDs.add("532699");//广州租房团
		//gzGroupIDs.add("haizhuzufang");//广州海珠租房（个人房源免费推广）
		//gzGroupIDs.add("yuexiuzufang");//广州越秀租房（个人房源免费推广）
		gzGroupIDs.add("IloveGZ");//广州租房（好评度★★★★★）
		gzGroupIDs.add("zunar_gz");//广州租房族（爱分享，易租房）
		gzGroupIDs.add("zu.gz.soufun");//广州租房交友（原毕业生租房）
		gzGroupIDs.add("549582");//广州租房
		//gzGroupIDs.add("panyuzufang");//广州番禺租房（个人房源免费推广）
		//gzGroupIDs.add("baiyunzufang");//广州白云租房（个人房源免费推广）
		gzGroupIDs.add("gzzf");//广州合租-广州租个人房源
		//gzGroupIDs.add("liwanzufang");//广州荔湾租房（个人房源免费推广）
		gzGroupIDs.add("huangpuzufang");//广州3号线+5号线+APM地铁沿线租房
		gzGroupIDs.add("366393");//广州真房实客网租房组
		gzGroupIDs.add("banjia");//广州合伙租房那些事
		gzGroupIDs.add("558241");//广州公寓租房信息
		gzGroupIDs.add("592739");//广州租房信息-推荐度★★★★★
		gzGroupIDs.add("576562");//广州天河租房【无中介费】
		gzGroupIDs.add("583602");//广州租房（房东直租，中介勿进）
		gzGroupIDs.add("606682");//广州租房-男女不限(推荐★★★★★)
		gzGroupIDs.add("575188");//广州租房大全【好评★★★★★】
		gzGroupIDs.add("maquezufang");//【广州租房】无中介服务站-找朋友
		gzGroupIDs.add("637254");//广州租房
*/		return gzGroupIDs;
	}

	public static List<String> getFoshanGroupIDs() {
		List<String> fsGroupIDs = new ArrayList<String>();

		fsGroupIDs.add("503571");//佛山租房（推荐度★★★★★）
		fsGroupIDs.add("514329");//佛山人小组：【租房版】
		fsGroupIDs.add("fszf");//佛山合租 - 佛山租个人房源
		fsGroupIDs.add("536016");//佛山租房族
		fsGroupIDs.add("551824");//佛山租房
		return fsGroupIDs;
	}

	public static void writeToFile(String str) {
		try {
            File logFile = new File("D:\\result\\output-foshan-1001-1026.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件 
            if(!logFile.exists()) {
            	logFile.createNewFile(); // 创建新文件  
            }
            //BufferedWriter out = new BufferedWriter(new FileWriter(logFile));  
            FileWriter out = new FileWriter(logFile, true);
            out.write(str + "\r\n"); // \r\n即为换行  
            //out.flush(); // 把缓存区内容压入文件
            out.close(); // 最后记得关闭文件  
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
