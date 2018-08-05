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
		gzGroupIDs.add("gz020");//�����ⷿ����˷�Դ����ƹ㣩
		gzGroupIDs.add("tianhezufang");//��������ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("gz_rent");//�����ⷿ
		gzGroupIDs.add("532699");//�����ⷿ��
		gzGroupIDs.add("haizhuzufang");//���ݺ����ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("yuexiuzufang");//����Խ���ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("IloveGZ");//�����ⷿ�������ȡ����
		gzGroupIDs.add("zunar_gz");//�����ⷿ�壨���������ⷿ��
		gzGroupIDs.add("zu.gz.soufun");//�����ⷿ���ѣ�ԭ��ҵ���ⷿ��
		gzGroupIDs.add("549582");//�����ⷿ
		gzGroupIDs.add("panyuzufang");//���ݷ�خ�ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("baiyunzufang");//���ݰ����ⷿ�����˷�Դ����ƹ㣩
		gzGroupIDs.add("gzzf");//���ݺ���-��������˷�Դ
		gzGroupIDs.add("liwanzufang");//���������ⷿ�����˷�Դ����ƹ㣩
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
	 * 2. the title with the word "��"
	 * 3. the title with the word "��"
	 */
	private void articleFilter() {

		AnalyseUtil.print("Running the articleFilter...");
		List<String> tempTitles = new ArrayList<String>();

		List<HouseRequirement> groupTemp = new ArrayList<HouseRequirement>();
		for(int i=0; i<mGroupIDs.size(); i++) {
			for(HouseRequirement house : mGroupHouseRequires.get(i)) {
				if(tempTitles.contains(house.getRequirementTitle()) || house.getRequirementTitle().contains("��") || house.getRequirementTitle().contains("��")) {
	
					//groupMine.remove(house);
					groupRubbish.add(house);//remove the items which title repeated, title contains "��", title contains "��"��
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
	
	//����[<a href="https://www.douban.com/group/topic/120707892/">������</a>]
	private List<String> buildResults(List<HouseRequirement> group) {
		AnalyseUtil.print("Running the buildResults...: from " + mPreStartDate + "(not include) to " + mEndDate + ", count is: " + group.size());
		List<String> results = new ArrayList<String>();
		for(HouseRequirement house : group) {
			String result = "";
			result = "<p>" + house.getRequirementTitle() + "[<a href=\"" + house.getRequirementLink() + "\">������</a>]</p><br/>";
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
