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
		preStartDay = "2019-" + preStartDay;
		endDay = "2019-" + endDay;
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

	public static List<String> excludeUserLinks_zhuhai(){
		List<String> excludeUserLinks = new ArrayList<String>();
		excludeUserLinks.add("https://www.douban.com/people/181519487/");//sd8d78s
		excludeUserLinks.add("https://www.douban.com/people/179692762/");//阿利狼
		excludeUserLinks.add("https://www.douban.com/people/182512261/");//陈陈陈啊
		excludeUserLinks.add("https://www.douban.com/people/160200975/");//山今
		excludeUserLinks.add("https://www.douban.com/people/180149790/");//微笑面对每一天
		excludeUserLinks.add("https://www.douban.com/people/182815921/");//豆油17777
		excludeUserLinks.add("https://www.douban.com/people/90747482/");//逐藏的钟
		excludeUserLinks.add("https://www.douban.com/people/185719145/");//豆友185719145
		excludeUserLinks.add("https://www.douban.com/people/186008364/");//豆友186008364
		excludeUserLinks.add("https://www.douban.com/people/63125840/");//Gary榆
		excludeUserLinks.add("https://www.douban.com/people/165087322/");//Apple
		excludeUserLinks.add("https://www.douban.com/people/180254026/");//中山青年
		excludeUserLinks.add("https://www.douban.com/people/187059234/");//豆瓣
		excludeUserLinks.add("https://www.douban.com/people/182660432/");//13392976160
		excludeUserLinks.add("https://www.douban.com/people/180856707/");//桀
		excludeUserLinks.add("https://www.douban.com/people/158624404/");//你不知道
		excludeUserLinks.add("https://www.douban.com/people/158430595/");//阿东andy
		excludeUserLinks.add("https://www.douban.com/people/188786662/");//18319656618
		excludeUserLinks.add("https://www.douban.com/people/180176140/");//嘉俊仔……
		excludeUserLinks.add("https://www.douban.com/people/188325010/");//万一
		excludeUserLinks.add("https://www.douban.com/people/189488401/");//老夏天32928
		excludeUserLinks.add("https://www.douban.com/people/189559315/");//早餐吃面包
		excludeUserLinks.add("https://www.douban.com/people/128141198/");//白树树
		excludeUserLinks.add("https://www.douban.com/people/119302132/");//若是一棵树
		excludeUserLinks.add("https://www.douban.com/people/yjcc-1020/");//雾霭
		excludeUserLinks.add("https://www.douban.com/people/163706051/");//哦！
		excludeUserLinks.add("https://www.douban.com/people/187739754/");//JAson__LEung
		excludeUserLinks.add("https://www.douban.com/people/192359543/");//小线条（珠海不求人）
		excludeUserLinks.add("https://www.douban.com/people/s7enn/");//s7enn
		excludeUserLinks.add("https://www.douban.com/people/194000963/");//Jassy
		excludeUserLinks.add("https://www.douban.com/people/193238048/");//奋斗的80后2676
		excludeUserLinks.add("https://www.douban.com/people/zhaodp/");//逼平
		excludeUserLinks.add("https://www.douban.com/people/192401645/");//小白
		excludeUserLinks.add("https://www.douban.com/people/192646945/");//刘伟东1
		excludeUserLinks.add("https://www.douban.com/people/195495422/");//陈欣予
		excludeUserLinks.add("https://www.douban.com/people/175266351/");//冲浪少女（珠海租房群）
		excludeUserLinks.add("https://www.douban.com/people/195867839/");//刘伟东1
		excludeUserLinks.add("https://www.douban.com/people/196158916/");//颠覆
		excludeUserLinks.add("https://www.douban.com/people/195611042/");//阿锋呀
		excludeUserLinks.add("https://www.douban.com/people/196950290/");//豆友196950290
		excludeUserLinks.add("https://www.douban.com/people/164799891/");//ggyy
		excludeUserLinks.add("https://www.douban.com/people/91654130/");//贝奇芽菜
		excludeUserLinks.add("https://www.douban.com/people/196905099/");//小莘
		excludeUserLinks.add("https://www.douban.com/people/su-dong/");//桑德先生
		excludeUserLinks.add("https://www.douban.com/people/130375078/");//乐观向上的大喵
		excludeUserLinks.add("https://www.douban.com/people/192951553/");//落 叶 
		excludeUserLinks.add("https://www.douban.com/people/197796330/");//爱豆
		excludeUserLinks.add("https://www.douban.com/people/198152671/");//点点
		excludeUserLinks.add("https://www.douban.com/people/197367513/");//雪儿
		excludeUserLinks.add("https://www.douban.com/people/198134827/");//晓彤
		excludeUserLinks.add("https://www.douban.com/people/196049292/");//珠海Q房网
		excludeUserLinks.add("https://www.douban.com/people/197537007/");//K
		excludeUserLinks.add("https://www.douban.com/people/197831844/");//开心
		excludeUserLinks.add("https://www.douban.com/people/196634605/");//~婷婷
		excludeUserLinks.add("https://www.douban.com/people/200321845/");//鲁罗霍谢
		excludeUserLinks.add("https://www.douban.com/people/202492934/");//A💋大白
		excludeUserLinks.add("https://www.douban.com/people/177829458/");//叁
		excludeUserLinks.add("https://www.douban.com/people/203611584/");//賈
		excludeUserLinks.add("https://www.douban.com/people/199075100/");//🍀
		excludeUserLinks.add("https://www.douban.com/people/204630105/");//珠海横琴房产
		excludeUserLinks.add("https://www.douban.com/people/202965289/");//吕蔡唐余
		excludeUserLinks.add("https://www.douban.com/people/201914476/");//A 小招
		excludeUserLinks.add("https://www.douban.com/people/106171983/");//发飙的散光少女
		excludeUserLinks.add("https://www.douban.com/people/200410628/");//茉莉蜜茶
		excludeUserLinks.add("https://www.douban.com/people/194639267/");//哭哭鸭
		return excludeUserLinks;
	}
	
	public static List<String> excludeUserLinks_guangzhou(){
		List<String> excludeUserLinks = new ArrayList<String>();
		excludeUserLinks.add("https://www.douban.com/people/183469060/");
		excludeUserLinks.add("https://www.douban.com/people/182820901/");
		excludeUserLinks.add("https://www.douban.com/people/181328964/");
		excludeUserLinks.add("https://www.douban.com/people/179320742/");
		excludeUserLinks.add("https://www.douban.com/people/153003345/");
		excludeUserLinks.add("https://www.douban.com/people/168863639/");
		excludeUserLinks.add("https://www.douban.com/people/177997871/");
		excludeUserLinks.add("https://www.douban.com/people/176981314/");
		excludeUserLinks.add("https://www.douban.com/people/72798565/");
		excludeUserLinks.add("https://www.douban.com/people/158563024/");
		excludeUserLinks.add("https://www.douban.com/people/162993850/");
		excludeUserLinks.add("https://www.douban.com/people/158565886/");
		excludeUserLinks.add("https://www.douban.com/people/183206281/");
		return excludeUserLinks;
	}
	
	public static List<String> excludeUserLinks_foshan(){
		List<String> excludeUserLinks = new ArrayList<String>();
		excludeUserLinks.add("https://www.douban.com/people/171042688/");//范
		excludeUserLinks.add("https://www.douban.com/people/186805216/");//账号停用
		excludeUserLinks.add("https://www.douban.com/people/186806038/");//账号停用
		excludeUserLinks.add("https://www.douban.com/people/186039398/");//豆友186039398
		excludeUserLinks.add("https://www.douban.com/people/169101315/");//账号停用
		excludeUserLinks.add("https://www.douban.com/people/186246727/");//老陈先生
		excludeUserLinks.add("https://www.douban.com/people/186397424/");//随寓青年社区
		excludeUserLinks.add("https://www.douban.com/people/159688293/");//岁月如歌
		excludeUserLinks.add("https://www.douban.com/people/158147887/");//mark
		excludeUserLinks.add("https://www.douban.com/people/164404648/");//一鉝米
		excludeUserLinks.add("https://www.douban.com/people/185002615/");//毅生
		excludeUserLinks.add("https://www.douban.com/people/185010693/");//YOU+佛山U妹
		excludeUserLinks.add("https://www.douban.com/people/arttoone/");//北极喵
		excludeUserLinks.add("https://www.douban.com/people/184690070/");//提拉米苏
		excludeUserLinks.add("https://www.douban.com/people/184698049/");//埃拉亚
		excludeUserLinks.add("https://www.douban.com/people/183503241/");//梦想家
		excludeUserLinks.add("https://www.douban.com/people/186165682/");//小乐乐
		excludeUserLinks.add("https://www.douban.com/people/185088667/");//诺
		excludeUserLinks.add("https://www.douban.com/people/169385966/");//匯和公寓
		excludeUserLinks.add("https://www.douban.com/people/185566517/");//河畔雅苑
		excludeUserLinks.add("https://www.douban.com/people/179020511/");//账号停用
		excludeUserLinks.add("https://www.douban.com/people/176662326/");//周厚壮
		excludeUserLinks.add("https://www.douban.com/people/185647475/");//爱加公寓
		excludeUserLinks.add("https://www.douban.com/people/179886484/");//戏子
		excludeUserLinks.add("https://www.douban.com/people/179275030/");//liya
		excludeUserLinks.add("https://www.douban.com/people/146683592/");//L。S。H。
		excludeUserLinks.add("https://www.douban.com/people/159211636/");//嘉州电梯公馆
		excludeUserLinks.add("https://www.douban.com/people/185305002/");//如此安好
		excludeUserLinks.add("https://www.douban.com/people/43592487/");//Sharonx
		excludeUserLinks.add("https://www.douban.com/people/185517348/");//豆
		return excludeUserLinks;
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
            File logFile = new File("D:\\result\\output-zhuhai-1221-1231.txt"); // 相对路径，如果没有则要建立一个新的output。txt文件 
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
