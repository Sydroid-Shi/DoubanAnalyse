package com.zhweizhu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.zhweizhu.util.AnalyseUtil;

public class GroupReader {

	private List<HouseRequirement> groupFinal = new ArrayList<HouseRequirement>();
	private List<HouseRequirement> groupRubbish = new ArrayList<HouseRequirement>();
	

	// Example: https://www.douban.com/group/zhuhai_zufang/discussion?start=0
	// Usage: LINK_COMMON_PREP + groupID + LINK_COMMON_END + pageIndex (pageIndex
	// starts from 0, increase is 25)
	public static String LINK_COMMON_PREP = "https://www.douban.com/group/";
	public static String LINK_COMMON_END = "/discussion?start=";
	
	public static int PAGE_INDEX_INCERASEMENT = 25;

	private List<String> mGroupIDs = new ArrayList<String>();
	private List<String> mHouseGroupLinkPres = new ArrayList<String>();
	private List<List<HouseRequirement>> mGroupHouseRequires = new ArrayList<List<HouseRequirement>>();

	private boolean mIsContinue = true;
	private String mEndDate = null;
	private String mPreStartDate = null;
	
	
	public GroupReader(String city) {
		if(city.equals(AnalyseUtil.GUANGZHOU)) {
			initGroupLinksPre_Guangzhou();
		}else if(city.equals(AnalyseUtil.ZHUHAI)) {
			initGroupLinksPre_Zhuhai();
		}else if(city.equals(AnalyseUtil.FOSHAN)) {
			initGroupLinksPre_Foshan();
		}else {
			initGroupLinksPre_Zhuhai();//default
		}
		
		
		setPreStartDate("10-23");//included
		setEndDate("10-25");//not included
	}
	
	public void run() {
		parseEachGroup();
		articleFilter();
		putAllInOneGroup();
		notPeriodDayFilter();
		printHouseRabbishs();
		printHouseResults();
	}
	
	public void setEndDate(String endDate) {
		mEndDate = endDate;
	}
	
	public void setPreStartDate(String preStartDate) {
		mPreStartDate = preStartDate;
	}
	
	//初始化每个小组的链接列表
	private void initGroupLinksPre_Zhuhai() {
		AnalyseUtil.print("[INFO] Start to initial the group pre links for zhuhai.");
		mGroupIDs.clear();
		mGroupIDs = AnalyseUtil.getZhuhaiGroupIDs();
		mHouseGroupLinkPres.clear();
		for(String groupID : mGroupIDs) {
			String groupLinkPre = LINK_COMMON_PREP + groupID + LINK_COMMON_END;
			mHouseGroupLinkPres.add(groupLinkPre);
			List<HouseRequirement> houseRequiresInOneGroup = new ArrayList<HouseRequirement>();
			mGroupHouseRequires.add(houseRequiresInOneGroup);
		}
		AnalyseUtil.print("[INFO] End to initial the group pre links for zhuhai.");
	}
	
	//Initial the previous of group links without pageIndex, result is stored in mHouseGroupLinkPres
	//Initial the List for house requirements for each group, result is stored in mGroupHouseRequires
	//The mapping for mHouseGroupLinkPres and mGroupHouseRequires is 1:1
	private void initGroupLinksPre_Guangzhou() {
		AnalyseUtil.print("[INFO] Start to initial the group pre links for guangzhou.");
		mGroupIDs.clear();
		mGroupIDs = AnalyseUtil.getGuangzhouGroupIDs();
		mHouseGroupLinkPres.clear();
		for(String groupID : mGroupIDs) {
			String groupLinkPre = LINK_COMMON_PREP + groupID + LINK_COMMON_END;
			mHouseGroupLinkPres.add(groupLinkPre);
			List<HouseRequirement> houseRequiresInOneGroup = new ArrayList<HouseRequirement>();
			mGroupHouseRequires.add(houseRequiresInOneGroup);
		}
		AnalyseUtil.print("[INFO] End to initial the group pre links for guangzhou.");
	}

	//初始化每个小组的链接列表
	private void initGroupLinksPre_Foshan() {
		AnalyseUtil.print("[INFO] Start to initial the group pre links for foshan.");
		mGroupIDs.clear();
		mGroupIDs = AnalyseUtil.getFoshanGroupIDs();
		mHouseGroupLinkPres.clear();
		for(String groupID : mGroupIDs) {
			String groupLinkPre = LINK_COMMON_PREP + groupID + LINK_COMMON_END;
			mHouseGroupLinkPres.add(groupLinkPre);
			List<HouseRequirement> houseRequiresInOneGroup = new ArrayList<HouseRequirement>();
			mGroupHouseRequires.add(houseRequiresInOneGroup);
		}
		AnalyseUtil.print("[INFO] End to initial the group pre links for foshan.");
	}
	
	private void parseEachGroup() {
		AnalyseUtil.print("[INFO] Start to parse each group.");
		int groupCount = mHouseGroupLinkPres.size();
		for(int i=0; i<groupCount; i++) {
			AnalyseUtil.print("[INFO] Filling the group: " + mGroupIDs.get(i));

			int index = 0;//Page Index for each group
			String groupLink_all;
			mIsContinue = true;
			
			while (mIsContinue) {
				groupLink_all = mHouseGroupLinkPres.get(i) + index;
				AnalyseUtil.print("[INFO] Now is parsing the link: " + groupLink_all);
				String content = AnalyseUtil.getURLContent(groupLink_all);
				parseToList(content, i);

				index += PAGE_INDEX_INCERASEMENT;//Next page.
				//if(index > 50) isContinue = false;//only three pages: 0,25,50
				try {
					Thread.sleep(40000);//when the douban is requested 20 times in 1 minute, the IP will be blocked by douban.
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		AnalyseUtil.print("[INFO] End to parse each group.");
	}
	
	private void parseToList(String content, int groupIndex) {
		AnalyseUtil.print("[INFO] Start to parse articles of the page.");

		String[] articles;
		try {
			articles = AnalyseUtil.splitHTMLtoArticles(content);//String
			
			for(String article : articles) {
//				AnalyseUtil.print("[DEBUG] Split article: " + article);
				if(!article.contains("<tr class=\"th\">")) {
					HouseRequirement house = new HouseRequirement();//String:artical parse to HouseRequirement:house
					parseArticle(house, article);
					if(mIsContinue) {
						mGroupHouseRequires.get(groupIndex).add(house);
					}else {
						break;
					}
					
				}
			}
		}catch(Exception e) {
			AnalyseUtil.print("[ERROR] Exception happens in parseToList: \n" + e);
		}
		AnalyseUtil.print("[INFO] End to parse articles of the page.");
	}
	
	private void parseArticle(HouseRequirement house, String article) {
		AnalyseUtil.print("[INFO] Start to parse articles to the object HouseRequirement.");
		String authorName;
		String requirementTitle;
		String requirementLink;
		String lastUpdateTime;
		String replyNum;
		try {
			String[] articleSplit = article.trim().split("</td>");
			if(articleSplit.length != 4) {
				AnalyseUtil.print("[ERROR] The split number for an article is wrong: " + articleSplit.length);
				return;
			}
			
			requirementLink = articleSplit[0].substring(articleSplit[0].indexOf("href=")+6,articleSplit[0].indexOf("/\"")+1);
			requirementTitle = articleSplit[0].substring(articleSplit[0].indexOf(" title=")+7,articleSplit[0].lastIndexOf(" class="));
			authorName = articleSplit[1].substring(articleSplit[1].lastIndexOf("\">")+2, articleSplit[1].indexOf("</a>"));
			replyNum = articleSplit[2].substring(articleSplit[2].lastIndexOf("\">")+2);
			lastUpdateTime = articleSplit[3].substring(articleSplit[3].lastIndexOf("\">")+2);
			
			if(mIsContinue) {
				isContinue(lastUpdateTime);
				AnalyseUtil.print("[INFO] *Is continue is changed to be: " + mIsContinue + ", lastUpdateTime: " + lastUpdateTime + ", startTime: " + mPreStartDate);
			}
			
			house.setAuthorName(authorName);
			house.setRequirementLink(requirementLink);
			house.setLastUpdateTime(lastUpdateTime);
			house.setReplyNum(replyNum);
			house.setRequirementTitle(requirementTitle);
		}catch(Exception e) {
			AnalyseUtil.print("[ERROR] Exception happens in parseArticle for the article: \n" + article + "\n Error Message: \n" + e);
		}
		AnalyseUtil.print("[INFO] End to parse articles to the object HouseRequirement.");
	}
	
	//To check when to finish the page reader.
	private void isContinue(String str) {
		mIsContinue = compareToTheday(str, mPreStartDate);
	}
	
	//compare to the previous day of the start day
	private boolean compareToTheday(String str, String judgeDate) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
		String currentDate = sdf.format(date);
		if(judgeDate == null || judgeDate.trim().isEmpty()) {
			judgeDate = currentDate;
		}
		
		/*if(str.contains(currentDate)) {//once you meet the day, it will finish to loop.
			return true;
		}*/
		if(str.compareTo(judgeDate) > 0) {
			return true;
		}
		return false;
	}


	/**
	 * Filter for every groups and put all in one group.
	 * 1. same title in same group or cross group
	 * 2. the title with the word "求"
	 * 3. the title with the word "已"
	 */
	private void articleFilter() {
		AnalyseUtil.print("[INFO] Start to filter the houses for each group.");

		//AnalyseUtil.print("Running the articleFilter...");
		List<String> tempTitles = new ArrayList<String>();
		List<String> tempUsers = new ArrayList<String>();

		List<HouseRequirement> groupTemp = new ArrayList<HouseRequirement>();

		List<String> excludeUsers = AnalyseUtil.excludeUsers_zhuhai();
		
		for(int i=0; i<mGroupIDs.size(); i++) {//each group
			for(HouseRequirement house : mGroupHouseRequires.get(i)) {//each house of the group
				if(tempTitles.contains(house.getRequirementTitle()) || 
						house.getRequirementTitle().contains("求") || 
						house.getRequirementTitle().contains("已") ||
						 house.getRequirementTitle().contains("删")) {
	
					//groupMine.remove(house);
					groupRubbish.add(house);//remove the items which title repeated, title contains "求", title contains "已"。
					AnalyseUtil.print("[DEBUG] Title is repeated or contains 求，已，删: " + house.getRequirementTitle() + " | link: " + house.getRequirementLink());
				}else if(tempUsers.contains(house.getAuthorName())){
					AnalyseUtil.print("[DEBUG] User is repeated: " + house.getRequirementTitle() + " | user: " + house.getAuthorName() + " | link: " + house.getRequirementLink());//keep the latest update one
					groupRubbish.add(house);
				}else if(excludeUsers.contains(house.getAuthorName())){
					AnalyseUtil.print("[DEBUG] User is in black list: " + house.getRequirementTitle() + " | user: " + house.getAuthorName() + " | link: " + house.getRequirementLink());//keep the latest update one
					groupRubbish.add(house);
				}else {
					groupTemp.add(house);
					tempTitles.add(house.getRequirementTitle());
					tempUsers.add(house.getAuthorName());
				}
			}
			mGroupHouseRequires.get(i).clear();
			mGroupHouseRequires.get(i).addAll(groupTemp);
			groupTemp.clear();
		}
		AnalyseUtil.print("[INFO] End to filter the houses for each group.");
	}
	
	private void putAllInOneGroup() {
		AnalyseUtil.print("[INFO] Start to put all houses in one group.");
		for(int i=0; i<mGroupIDs.size(); i++) {
			groupFinal.addAll(mGroupHouseRequires.get(i));//TODO filter same user's article
		}
		AnalyseUtil.print("[INFO] End to put all houses in one group.");
	}
	

	//filter the article that is not created by today
	private void notPeriodDayFilter() {
		AnalyseUtil.print("[INFO] Start to remove the houses not in the sample period time.");
		String content=null;
		List<HouseRequirement> groupTempRemove = new ArrayList<HouseRequirement>();
		for(HouseRequirement house : groupFinal) {
			try {

				content = AnalyseUtil.getURLContent(house.getRequirementLink());
				
				if(!content.contains("你想要的东西不在这儿")) {//The link should be useful
					String houseContent = content.substring(content.indexOf("id=\"link-report\""), content.indexOf("id=\"link-report_group\""));
					if(!houseContent.contains(".jpg")) {
						AnalyseUtil.print("[DEBUG] No picture for this house: " + house.getRequirementTitle() + " | link: " + house.getRequirementLink());
						groupRubbish.add(house);
						groupTempRemove.add(house);
						continue;
					}
					content = content.substring(content.indexOf("display:inline-block"), content.indexOf("link-report"));
					String date = content.substring(content.indexOf(">")+1, content.indexOf("</span>"));//house created date
					//AnalyseUtil.print("The publish date for this article is: " + content);
					AnalyseUtil.print("[DEBUG] The publish time for the house: " + house.getRequirementTitle() + " | link: " + house.getRequirementLink() + "current/start/end: " + date + "/" + mPreStartDate + "/" + mEndDate);
					if(!AnalyseUtil.isBetweenPeriod(date, mPreStartDate, mEndDate)) {//TODO BUG: filter may remove the latest article?? maybe not a bug, because it is published before.
						groupRubbish.add(house);
						groupTempRemove.add(house);
						AnalyseUtil.print("[DEBUG] The house has been removed from list: " + house.getRequirementTitle() + " | link: " + house.getRequirementLink());
					}
				}
			}catch(Exception e) {
				AnalyseUtil.print("Exception happens in notTodayFilter for the content: \n" + content + "\n Error Message: \n" + e);
				
			}
		}
		groupFinal.removeAll(groupTempRemove);
		AnalyseUtil.print("[INFO] End to remove the houses not in the sample period time.");
	}
	
	//标题[<a href="https://www.douban.com/group/topic/120707892/">点这里</a>]
	private List<String> buildResults(List<HouseRequirement> group) {
		AnalyseUtil.print("[INFO] Start to build and format the results: from " + mPreStartDate + " to " + mEndDate + "(not include), count is: " + group.size());
		List<String> results = new ArrayList<String>();
		for(HouseRequirement house : group) {
			String result = "";
			result = "<p>" + house.getRequirementTitle() + "[<a href=\"" + house.getRequirementLink() + "\">点这里</a>]</p><br/>";
			results.add(result);
		}
		AnalyseUtil.print("[INFO] End to build and format the results.");
		return results;
	}
	

	private void printHouseResults() {
		AnalyseUtil.print("[INFO] Start to print the houses.");
		List<String> results = buildResults(groupFinal);
		for(String result : results) {
			AnalyseUtil.print(result);
		}
		AnalyseUtil.print("[INFO] End to print the houses.");
	}

	private void printHouseRabbishs() {
		AnalyseUtil.print("[INFO] Start to print the rubbishes.");
		List<String> results = buildResults(groupRubbish);
		for(String result : results) {
			AnalyseUtil.print(result);
		}
		AnalyseUtil.print("[INFO] End to print the rubbishes.");
	}
	
	
}
