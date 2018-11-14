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
	public static String GUANGZHOU = "guangzhou";//����
	public static String ZHUHAI = "zhuhai";//�麣
	public static String FOSHAN = "foshan";//��ɽ
	
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
	 * @param htmlStr The HTML result from: "https://www.douban.com/group/search?start=0&cat=1019&sort=relevance&q=�����ⷿ"
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
		
		String cutGroupLinkPre = "<a class=\"nbg\" href=";//<a class="nbg" href="https://www.douban.com/group/537239/" onclick="moreurl(this,{i: '19', query: '%E5%B9%BF%E5%B7%9E%E7%A7%9F%E6%88%BF', from: 'group_search', sid: 537239})" title="��������ⷿ��ϼ�"><img class
		String cutGroupLinkEnd = "\" onclick=";
		String cutGroupNamePre = "title=\"";
		String cutGroupNameEnd = "\"><img";
		String cutGroupCountPre = "<div class=\"info\">";//<div class="info">4739 ����Ա �ڴ˾ۼ� </div>
		String cutGroupCountEnd = " ��";
		
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
		zhGroupIDs.add("611595");//�Ƽ��������
		zhGroupIDs.add("576424");//���н�
		zhGroupIDs.add("583425");//��Ůͷ��
		zhGroupIDs.add("zhfangzi");//�麣����
		zhGroupIDs.add("555279");//������
		return zhGroupIDs;
	}

	public static List<String> excludeUsers_zhuhai(){
		List<String> excludeUsers = new ArrayList<String>();
		excludeUsers.add("sd8d78s");
		excludeUsers.add("������");
		excludeUsers.add("�³³°�");
		excludeUsers.add("A     ��");
		return excludeUsers;
	}
	
	//Add group IDs to the list.
	public static List<String> getGuangzhouGroupIDs() {
		List<String> gzGroupIDs = new ArrayList<String>();
		//gzGroupIDs.add("gz020");//�����ⷿ����˷�Դ����ƹ㣩
		//gzGroupIDs.add("tianhezufang");//��������ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("gz_rent");//�����ⷿ
		/*gzGroupIDs.add("532699");//�����ⷿ��
		//gzGroupIDs.add("haizhuzufang");//���ݺ����ⷿ�����˷�Դ����ƹ㣩
		//gzGroupIDs.add("yuexiuzufang");//����Խ���ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("IloveGZ");//�����ⷿ�������ȡ����
		gzGroupIDs.add("zunar_gz");//�����ⷿ�壨���������ⷿ��
		gzGroupIDs.add("zu.gz.soufun");//�����ⷿ���ѣ�ԭ��ҵ���ⷿ��
		gzGroupIDs.add("549582");//�����ⷿ
		//gzGroupIDs.add("panyuzufang");//���ݷ�خ�ⷿ�����˷�Դ����ƹ㣩
		//gzGroupIDs.add("baiyunzufang");//���ݰ����ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("gzzf");//���ݺ���-��������˷�Դ
		//gzGroupIDs.add("liwanzufang");//���������ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("huangpuzufang");//����3����+5����+APM���������ⷿ
		gzGroupIDs.add("366393");//�����淿ʵ�����ⷿ��
		gzGroupIDs.add("banjia");//���ݺϻ��ⷿ��Щ��
		gzGroupIDs.add("558241");//���ݹ�Ԣ�ⷿ��Ϣ
		gzGroupIDs.add("592739");//�����ⷿ��Ϣ-�Ƽ��ȡ�����
		gzGroupIDs.add("576562");//��������ⷿ�����н�ѡ�
		gzGroupIDs.add("583602");//�����ⷿ������ֱ�⣬�н������
		gzGroupIDs.add("606682");//�����ⷿ-��Ů����(�Ƽ�������)
		gzGroupIDs.add("575188");//�����ⷿ��ȫ�����������
		gzGroupIDs.add("maquezufang");//�������ⷿ�����н����վ-������
		gzGroupIDs.add("637254");//�����ⷿ
*/		return gzGroupIDs;
	}

	public static List<String> getFoshanGroupIDs() {
		List<String> fsGroupIDs = new ArrayList<String>();

		fsGroupIDs.add("503571");//��ɽ�ⷿ���Ƽ��ȡ����
		fsGroupIDs.add("514329");//��ɽ��С�飺���ⷿ�桿
		fsGroupIDs.add("fszf");//��ɽ���� - ��ɽ����˷�Դ
		fsGroupIDs.add("536016");//��ɽ�ⷿ��
		fsGroupIDs.add("551824");//��ɽ�ⷿ
		return fsGroupIDs;
	}

	public static void writeToFile(String str) {
		try {
            File logFile = new File("D:\\result\\output-foshan-1001-1026.txt"); // ���·�������û����Ҫ����һ���µ�output��txt�ļ� 
            if(!logFile.exists()) {
            	logFile.createNewFile(); // �������ļ�  
            }
            //BufferedWriter out = new BufferedWriter(new FileWriter(logFile));  
            FileWriter out = new FileWriter(logFile, true);
            out.write(str + "\r\n"); // \r\n��Ϊ����  
            //out.flush(); // �ѻ���������ѹ���ļ�
            out.close(); // ���ǵùر��ļ�  
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
