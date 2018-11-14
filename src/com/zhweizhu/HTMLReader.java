package com.zhweizhu;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zhweizhu.util.AnalyseUtil;

public class HTMLReader {
	// The requirement list for the group "zhuhai_zufang" (My group)
	private List<HouseRequirement> groupMine = new ArrayList<HouseRequirement>();
	// The requirement list for the group "611595" (推荐度五颗星)
	private List<HouseRequirement> groupFiveStar = new ArrayList<HouseRequirement>();
	// The requirement list for the group "576424" (无中介)
	private List<HouseRequirement> groupNoAgent = new ArrayList<HouseRequirement>();
	// The requirement list for the group "583425" (渔女头像)
	private List<HouseRequirement> groupFishGirl = new ArrayList<HouseRequirement>();
	// The requirement list for the group "zhfangzi" (珠海房子)
	private List<HouseRequirement> groupZhFangzi = new ArrayList<HouseRequirement>();
	// The requirement list for the group "555279" (被屏蔽)
	private List<HouseRequirement> groupBlocked = new ArrayList<HouseRequirement>();

	private List<HouseRequirement> groupFinal = new ArrayList<HouseRequirement>();
	private List<HouseRequirement> groupRubbish = new ArrayList<HouseRequirement>();
	

	// Example: https://www.douban.com/group/zhuhai_zufang/discussion?start=0
	// Usage: LINK_COMMON_PREP + groupID + LINK_COMMON_END + pageIndex (pageIndex
	// starts from 0, increase is 25)
	public static String LINK_COMMON_PREP = "https://www.douban.com/group/";
	public static String LINK_COMMON_END = "/discussion?start=";
	
	public static int PAGE_INDEX_INCERASEMENT = 25;
	
	//If you need get yesterday, should use the day after yesterday for judgement
	public static String THE_DAY_AFTER_YESTERDAY = "08-10";//you can modify the date here to get all the new added houses this day.
	public static String YESTERDAY = "08-11";
	
	public static String STOP_DAY = "08-10";
	
	public static String DAY_FROM_PRE = "8-10";
	public static String DAY_TO = "8-18";
	
	private String currentGroup;
	boolean isContinue = true;

	private List<String> mHouseGroupLinks;

	public HTMLReader() {
	}

	public void init() {
		mHouseGroupLinks = initGroupLinks();
		fillGroups();
		//printHouseRequirements();
		articleFilter();
		groupFilter();
		notTodayFilter();
		printHouseResults();
		printAllRemovedRequirements();
	}

	//To initial the group discussion link without page index.
	private List<String> initGroupLinks() {
		List<String> houseGroupLinks = new ArrayList<String>();
		houseGroupLinks.add(LINK_COMMON_PREP + "zhuhai_zufang" + LINK_COMMON_END);// My group
		houseGroupLinks.add(LINK_COMMON_PREP + "611595" + LINK_COMMON_END);// 推荐度五颗星
		houseGroupLinks.add(LINK_COMMON_PREP + "576424" + LINK_COMMON_END);// 无中介
		houseGroupLinks.add(LINK_COMMON_PREP + "583425" + LINK_COMMON_END);// 渔女头像
		houseGroupLinks.add(LINK_COMMON_PREP + "zhfangzi" + LINK_COMMON_END);// 珠海房子
		houseGroupLinks.add(LINK_COMMON_PREP + "555279" + LINK_COMMON_END);// 被屏蔽

		return houseGroupLinks;
	}
	
	private List<String> excludeUsers(){
		List<String> excludeUsers = new ArrayList<String>();
		excludeUsers.add("sd8d78s");
		excludeUsers.add("阿利狼");
		excludeUsers.add("陈陈陈啊");
		excludeUsers.add("A     ");
		return excludeUsers;
	}

	//For loop the group link list to get messages from each group and fill to the group house list
	//TODO Use the multiple threads to reduce the time.
	private void fillGroups() {
		if (mHouseGroupLinks != null) {
			int index = 0;
			for (String groupLink : mHouseGroupLinks) {
				if(index == 0) {
					currentGroup = "groupMine";
				}else if(index == 1) {
					currentGroup = "groupFiveStar";
				}else if(index == 2) {
					currentGroup = "groupNoAgent";
				}else if(index == 3) {
					currentGroup = "groupFishGirl";
				}else if(index == 4) {
					currentGroup = "groupZhFangzi";
				}else if(index == 5) {
					currentGroup = "groupBlocked";
				}else {
					AnalyseUtil.print("No such group! \n");
					return;
				}
				fillGroup(groupLink);
				index++;
			}
		}
	}

	//to get every page content for one group and fill to the group house list
	private void fillGroup(String groupLink) {
		AnalyseUtil.print("Filling the group: " + currentGroup);
		int index = 0;
		String groupLink_all;
		isContinue = true;
		while (isContinue) {
			groupLink_all = groupLink + index;
			AnalyseUtil.print("Now is parsing the link: " + groupLink_all);
			String content = AnalyseUtil.getURLContent(groupLink_all);
			parseToList(content);

			index += PAGE_INDEX_INCERASEMENT;//Next page.
			//if(index > 50) isContinue = false;//only three pages: 0,25,50
			try {
				Thread.sleep(60000);//when the douban is requested 20 times in 1 minute, the IP will be blocked by douban.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void parseToList(String content) {

		String[] articles;
		try {
			articles = AnalyseUtil.splitHTMLtoArticles(content);
			
			for(String article : articles) {
				AnalyseUtil.print("Split article: " + article);
				if(!article.contains("<tr class=\"th\">")) {
					HouseRequirement house = new HouseRequirement();
					parseArticle(house, article);
					switch(currentGroup) {
					case "groupMine": groupMine.add(house);break;
					case "groupFiveStar": groupFiveStar.add(house);break;
					case "groupNoAgent": groupNoAgent.add(house);break;
					case "groupFishGirl": groupFishGirl.add(house);break;
					case "groupZhFangzi": groupZhFangzi.add(house);break;
					case "groupBlocked": groupBlocked.add(house);break;
						default:AnalyseUtil.print("No such group name.");
					}
				}
			}
		}catch(Exception e) {
			AnalyseUtil.print("Exception happens in parseToList: \n" + e);
		}
	}
	
	private void parseArticle(HouseRequirement house, String article) {
		String authorName;
		String requirementTitle;
		String requirementLink;
		String lastUpdateTime;
		String replyNum;
		try {
			String[] articleSplit = article.trim().split("</td>");
			if(articleSplit.length != 4) {
				AnalyseUtil.print("The split number for an article is wrong: " + articleSplit.length);
				return;
			}
			
			requirementLink = articleSplit[0].substring(articleSplit[0].indexOf("href=")+6,articleSplit[0].indexOf("/\"")+1);
			requirementTitle = articleSplit[0].substring(articleSplit[0].indexOf(" title=")+7,articleSplit[0].lastIndexOf(" class="));
			authorName = articleSplit[1].substring(articleSplit[1].lastIndexOf("\">")+2, articleSplit[1].indexOf("</a>"));
			replyNum = articleSplit[2].substring(articleSplit[2].lastIndexOf("\">")+2);
			lastUpdateTime = articleSplit[3].substring(articleSplit[3].lastIndexOf("\">"));
			
			if(isContinue) {//if it is true, should be verified. If false, means it needs to be turned out, not need to verified again.
				isContinue(lastUpdateTime);//TODO return the value in order to stop the process if the date meets.
			}
				
			
			house.setAuthorName(authorName);
			house.setRequirementLink(requirementLink);
			house.setLastUpdateTime(lastUpdateTime);
			house.setReplyNum(replyNum);
			house.setRequirementTitle(requirementTitle);
		}catch(Exception e) {
			AnalyseUtil.print("Exception happens in parseArticle: \n" + e);
		}
	}
	
	private void isContinue(String str) {
		isContinue = compareToTheDate2(str, DAY_FROM_PRE);//if meets the date, stop. To get the results before the date.
	}
	
	/**
	 * 
	 * @param str
	 * @return true means it is the date, false means after the date, there is no before the date.
	 */
	private boolean compareToTheDate(String str, String judgeDate) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
		if(judgeDate == null) judgeDate = sdf.format(date);//use today as judge day
		if(str.contains(judgeDate)) {
			return true;
		}
		return false;
	}

	private boolean compareToTheDate2(String str, String judgeDate) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
		if(judgeDate == null) judgeDate = sdf.format(date);//use today as judge day
		if(str.compareTo(judgeDate) > 0) {
			return true;
		}
		return false;
	}

	//filter the articles in one group
	private void articleFilter() {

		AnalyseUtil.print("Running the articleFilter...");
		List<String> tempTitles = new ArrayList<String>();

		List<HouseRequirement> groupTemp = new ArrayList<HouseRequirement>();
		for(HouseRequirement house : groupMine) {
			if(tempTitles.contains(house.getRequirementTitle()) || house.getRequirementTitle().contains("求") || house.getRequirementTitle().contains("已")) {

				//groupMine.remove(house);
				groupRubbish.add(house);
			}else {
				groupTemp.add(house);
				tempTitles.add(house.getRequirementTitle());
			}
		}
		groupMine.clear();
		groupMine.addAll(groupTemp);
		groupTemp.clear();
		for(HouseRequirement house : groupFiveStar) {
			if(tempTitles.contains(house.getRequirementTitle()) || house.getRequirementTitle().contains("求") || house.getRequirementTitle().contains("已")) {

				//groupFiveStar.remove(house);
				groupRubbish.add(house);
			}else {
				groupTemp.add(house);
				tempTitles.add(house.getRequirementTitle());
			}
		}
		groupFiveStar.clear();
		groupFiveStar.addAll(groupTemp);
		groupTemp.clear();
		for(HouseRequirement house : groupNoAgent) {
			if(tempTitles.contains(house.getRequirementTitle()) || house.getRequirementTitle().contains("求") || house.getRequirementTitle().contains("已")) {

				//groupNoAgent.remove(house);
				groupRubbish.add(house);
			}else {
				groupTemp.add(house);
				tempTitles.add(house.getRequirementTitle());
			}
		}
		groupNoAgent.clear();
		groupNoAgent.addAll(groupTemp);
		groupTemp.clear();
		for(HouseRequirement house : groupFishGirl) {
			if(tempTitles.contains(house.getRequirementTitle()) || house.getRequirementTitle().contains("求") || house.getRequirementTitle().contains("已")) {

				//groupFishGirl.remove(house);
				groupRubbish.add(house);
			}else {
				groupTemp.add(house);
				tempTitles.add(house.getRequirementTitle());
			}
		}
		groupFishGirl.clear();
		groupFishGirl.addAll(groupTemp);
		groupTemp.clear();
		for(HouseRequirement house : groupZhFangzi) {
			if(tempTitles.contains(house.getRequirementTitle()) || house.getRequirementTitle().contains("求") || house.getRequirementTitle().contains("已")) {

				//groupZhFangzi.remove(house);
				groupRubbish.add(house);
			}else {
				groupTemp.add(house);
				tempTitles.add(house.getRequirementTitle());
			}
		}
		groupZhFangzi.clear();
		groupZhFangzi.addAll(groupTemp);
		groupTemp.clear();
		for(HouseRequirement house : groupBlocked) {
			if(tempTitles.contains(house.getRequirementTitle()) || house.getRequirementTitle().contains("求") || house.getRequirementTitle().contains("已")) {

				//groupBlocked.remove(house);
				groupRubbish.add(house);
			}else {
				groupTemp.add(house);
				tempTitles.add(house.getRequirementTitle());
			}
		}
		groupBlocked.clear();
		groupBlocked.addAll(groupTemp);
		groupTemp.clear();
	}
	//cross filter for same articles in all groups
	private void groupFilter() {
		AnalyseUtil.print("Running the groupFilter...");
		groupFinal.addAll(groupMine);
		groupFinal.addAll(groupFiveStar);
		groupFinal.addAll(groupNoAgent);
		groupFinal.addAll(groupFishGirl);
		groupFinal.addAll(groupZhFangzi);
		groupFinal.addAll(groupBlocked);
	}
	//filter the article that is not created by today
	private void notTodayFilter() {
		AnalyseUtil.print("Running the notTodayFilter...");
		try {

			List<HouseRequirement> groupTempRemove = new ArrayList<HouseRequirement>();
			List<String> excludeUsers = excludeUsers();
			for(HouseRequirement house : groupFinal) {
				String content = AnalyseUtil.getURLContent(house.getRequirementLink());
				content = content.substring(content.indexOf("display:inline-block"), content.indexOf("link-report"));
				String date = content.substring(content.indexOf(">")+1, content.indexOf("</span>"));
				AnalyseUtil.print("The publish date for this article is: " + content);
				if(!compareToTheDate2(date, DAY_FROM_PRE) || compareToTheDate2(date, DAY_TO)) {//>= x > 
					groupRubbish.add(house);
					groupTempRemove.add(house);
					AnalyseUtil.print("The house has been removed from list: " + house.getRequirementTitle());
				}
				
				else if(excludeUsers.contains(house.getAuthorName())) {
					groupRubbish.add(house);
					groupTempRemove.add(house);
					AnalyseUtil.print("The house has been removed from list: " + house.getRequirementTitle() + " user: " + house.getAuthorName());
					
				}
			}
			groupFinal.removeAll(groupTempRemove);
		}catch(Exception e) {
			AnalyseUtil.print("Exception happens in notTodayFilter: \n" + e);
			
		}
	}
	
	//标题[<a href="https://www.douban.com/group/topic/120707892/">点这里</a>]
	private List<String> buildResults(List<HouseRequirement> group) {
		AnalyseUtil.print("Running the buildResults...: " + YESTERDAY);
		List<String> results = new ArrayList<String>();
		for(HouseRequirement house : group) {
			String result = "";
			result = "<p>" + house.getRequirementTitle() + "[<a href=\"" + house.getRequirementLink() + "\">点这里</a>]</p><br/>";
			results.add(result);
		}
		return results;
	}
	
	private void printHouseResults() {
		List<String> results = buildResults(groupFinal);
		for(String result : results) {

			AnalyseUtil.print(result);
		}
	}
	
	private void printAllRemovedRequirements() {
		List<String> results = buildResults(groupRubbish);
		for(String result : results) {

			AnalyseUtil.print(result);
		}
		
	}
	
	private void printHouseRequirements() {
		AnalyseUtil.print("Houses from groupMine. \n");
		for(HouseRequirement house : groupMine) {
			AnalyseUtil.print("House[ author: " + house.getAuthorName() + " title: " + house.getRequirementTitle() + " link: " + house.getRequirementLink() + "]");
		}
		AnalyseUtil.print("Houses from groupFiveStar. \n");
		for(HouseRequirement house : groupFiveStar) {
			AnalyseUtil.print("House[ author: " + house.getAuthorName() + " title: " + house.getRequirementTitle() + " link: " + house.getRequirementLink() + "]");
		}
		AnalyseUtil.print("Houses from groupNoAgent. \n");
		for(HouseRequirement house : groupNoAgent) {
			AnalyseUtil.print("House[ author: " + house.getAuthorName() + " title: " + house.getRequirementTitle() + " link: " + house.getRequirementLink() + "]");
		}
		AnalyseUtil.print("Houses from groupFishGirl. \n");
		for(HouseRequirement house : groupFishGirl) {
			AnalyseUtil.print("House[ author: " + house.getAuthorName() + " title: " + house.getRequirementTitle() + " link: " + house.getRequirementLink() + "]");
		}
		AnalyseUtil.print("Houses from groupZhFangzi. \n");
		for(HouseRequirement house : groupZhFangzi) {
			AnalyseUtil.print("House[ author: " + house.getAuthorName() + " title: " + house.getRequirementTitle() + " link: " + house.getRequirementLink() + "]");
		}
		AnalyseUtil.print("Houses from groupBlocked. \n");
		for(HouseRequirement house : groupBlocked) {
			AnalyseUtil.print("House[ author: " + house.getAuthorName() + " title: " + house.getRequirementTitle() + " link: " + house.getRequirementLink() + "]");
		}
	}
}
