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
	
	
	public GroupReader() {
		initGroupLinksPre_Guangzhou();
		setEndDate("08.05");//used for test here
		setPreStartDate("08.01");
	}
	
	public void run() {
		parseEachGroup();
		articleFilter();
		notPeriodDayFilter();
		printHouseResults();
	}
	
	public void setEndDate(String endDate) {
		mEndDate = endDate;
	}
	
	public void setPreStartDate(String preStartDate) {
		mPreStartDate = preStartDate;
	}
	
	//Initial the previous of group links without pageIndex, result is stored in mHouseGroupLinkPres
	//Initial the List for house requirements for each group, result is stored in mGroupHouseRequires
	//The mapping for mHouseGroupLinkPres and mGroupHouseRequires is 1:1
	private void initGroupLinksPre_Guangzhou() {
		mGroupIDs.clear();
		mGroupIDs = getGuangzhouGroupIDs();
		mHouseGroupLinkPres.clear();
		for(String groupID : mGroupIDs) {
			String groupLinkPre = LINK_COMMON_PREP + groupID + LINK_COMMON_END;
			mHouseGroupLinkPres.add(groupLinkPre);
			List<HouseRequirement> houseRequiresInOneGroup = new ArrayList<HouseRequirement>();
			mGroupHouseRequires.add(houseRequiresInOneGroup);
		}
	}
	
	//Add group IDs to the list.
	private List<String> getGuangzhouGroupIDs() {
		List<String> gzGroupIDs = new ArrayList<String>();
		gzGroupIDs.add("gz020");//广州租房★（个人房源免费推广）
		gzGroupIDs.add("tianhezufang");//广州天河租房（个人房源免费推广）
		gzGroupIDs.add("gz_rent");//广州租房
		gzGroupIDs.add("532699");//广州租房团
		gzGroupIDs.add("haizhuzufang");//广州海珠租房（个人房源免费推广）
		gzGroupIDs.add("yuexiuzufang");//广州越秀租房（个人房源免费推广）
		gzGroupIDs.add("IloveGZ");//广州租房（好评度★★★★★）
		gzGroupIDs.add("zunar_gz");//广州租房族（爱分享，易租房）
		gzGroupIDs.add("zu.gz.soufun");//广州租房交友（原毕业生租房）
		gzGroupIDs.add("549582");//广州租房
		gzGroupIDs.add("panyuzufang");//广州番禺租房（个人房源免费推广）
		gzGroupIDs.add("baiyunzufang");//广州白云租房（个人房源免费推广）
		gzGroupIDs.add("gzzf");//广州合租-广州租个人房源
		gzGroupIDs.add("liwanzufang");//广州荔湾租房（个人房源免费推广）
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
		return gzGroupIDs;
	}

	private void parseEachGroup() {
		int groupCount = mHouseGroupLinkPres.size();
		for(int i=0; i<groupCount; i++) {
			AnalyseUtil.print("Filling the group: " + mGroupIDs.get(i));

			int index = 0;
			String groupLink_all;
			mIsContinue = true;
			
			while (mIsContinue) {
				groupLink_all = mHouseGroupLinkPres.get(i) + index;
				AnalyseUtil.print("Now is parsing the link: " + groupLink_all);
				String content = AnalyseUtil.getURLContent(groupLink_all);
				parseToList(content, i);

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
	}
	
	private void parseToList(String content, int groupIndex) {

		String[] articles;
		try {
			articles = AnalyseUtil.splitHTMLtoArticles(content);
			
			for(String article : articles) {
				AnalyseUtil.print("Split article: " + article);
				if(!article.contains("<tr class=\"th\">")) {
					HouseRequirement house = new HouseRequirement();
					parseArticle(house, article);
					mGroupHouseRequires.get(groupIndex).add(house);
					
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
			
			isContinue(lastUpdateTime);
			
			house.setAuthorName(authorName);
			house.setRequirementLink(requirementLink);
			house.setLastUpdateTime(lastUpdateTime);
			house.setReplyNum(replyNum);
			house.setRequirementTitle(requirementTitle);
		}catch(Exception e) {
			AnalyseUtil.print("Exception happens in parseArticle: \n" + e);
		}
	}
	
	//To check when to finish the page reader.
	private void isContinue(String str) {
		mIsContinue = compareToEndday(str);
	}
	
	//compare to the previous day of the start day
	private boolean compareToEndday(String str) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
		String currentDate = sdf.format(date);
		if(mPreStartDate != null) {
			currentDate = mPreStartDate;
		}
		
		if(str.contains(currentDate)) {//once you meet the day, it will finish to loop.
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

		AnalyseUtil.print("Running the articleFilter...");
		List<String> tempTitles = new ArrayList<String>();

		List<HouseRequirement> groupTemp = new ArrayList<HouseRequirement>();
		for(int i=0; i<mGroupIDs.size(); i++) {
			for(HouseRequirement house : mGroupHouseRequires.get(i)) {
				if(tempTitles.contains(house.getRequirementTitle()) || house.getRequirementTitle().contains("求") || house.getRequirementTitle().contains("已")) {
	
					//groupMine.remove(house);
					groupRubbish.add(house);//remove the items which title repeated, title contains "求", title contains "已"。
				}else {
					groupTemp.add(house);
					tempTitles.add(house.getRequirementTitle());
				}
			}
			mGroupHouseRequires.get(i).clear();
			mGroupHouseRequires.get(i).addAll(groupTemp);
			groupTemp.clear();
		}
		putAllInOneGroup();
	}
	
	private void putAllInOneGroup() {
		for(int i=0; i<mGroupIDs.size(); i++) {
			groupFinal.addAll(mGroupHouseRequires.get(i));//TODO filter same user's article
		}
	}
	

	//filter the article that is not created by today
	private void notPeriodDayFilter() {
		AnalyseUtil.print("Running the notTodayFilter...");
		try {

			List<HouseRequirement> groupTempRemove = new ArrayList<HouseRequirement>();
			for(HouseRequirement house : groupFinal) {
				String content = AnalyseUtil.getURLContent(house.getRequirementLink());
				content = content.substring(content.indexOf("display:inline-block"), content.indexOf("link-report"));
				String date = content.substring(content.indexOf(">")+1, content.indexOf("</span>"));
				AnalyseUtil.print("The publish date for this article is: " + content);
				if(AnalyseUtil.isBetweenPeriod(date, mPreStartDate, mEndDate)) {
					groupRubbish.add(house);
					groupTempRemove.add(house);
					AnalyseUtil.print("The house has been removed from list: " + house.getRequirementTitle());
				}
			}
			groupFinal.removeAll(groupTempRemove);
		}catch(Exception e) {
			AnalyseUtil.print("Exception happens in notTodayFilter: \n" + e);
			
		}
	}
	
	//标题[<a href="https://www.douban.com/group/topic/120707892/">点这里</a>]
	private List<String> buildResults(List<HouseRequirement> group) {
		AnalyseUtil.print("Running the buildResults...: from " + mPreStartDate + "(not include) to " + mEndDate + ", count is: " + group.size());
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
	
	
}
