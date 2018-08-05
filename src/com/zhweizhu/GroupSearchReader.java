package com.zhweizhu;

import java.util.ArrayList;
import java.util.List;

import com.zhweizhu.util.AnalyseUtil;

public class GroupSearchReader {
	
	private static String GROUP_SEARCH_PRE = "https://www.douban.com/group/search?start=";
	private static String GROUP_SEARCH_END = "&cat=1019&sort=relevance&q=";
	private static String GROUP_GUANGZHOU = "¹ãÖÝ×â·¿";
	
	private String mKeyWord;
	private List<DoubanGroup> mAllGroups = new ArrayList<DoubanGroup>();
	
	public GroupSearchReader() {
		mKeyWord = GROUP_GUANGZHOU;
	}
	
	public GroupSearchReader(String keyWord) {
		mKeyWord = keyWord;
	}
	
	public void run() {
		int pageCount = getPageCount(GROUP_SEARCH_PRE + "0" + GROUP_SEARCH_END + mKeyWord);
		int index = 0;
		for(int i=0; i<=pageCount; i++) {
			String pageURL = GROUP_SEARCH_PRE + index + GROUP_SEARCH_END + mKeyWord;
			List<DoubanGroup> pageGroups = getGroupsForCertainPage(pageURL);
			mAllGroups.addAll(pageGroups);
			index += 20;
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		printAllGroups();
	}
	
	private int getPageCount(String pageZoreURL) {
		AnalyseUtil.print("Starting to get the page count...");
		int pageCount = 0;
		try {
			String htmlStr = AnalyseUtil.getURLContent(pageZoreURL);
			pageCount = AnalyseUtil.parseGroupSearchPageCount(htmlStr);
		}catch(Exception e) {
			AnalyseUtil.print("Exception happens in getPageCount: \n" + e);
			
		}
		AnalyseUtil.print("Finished to get the page count...");
		return pageCount;
	}
	
	private List<DoubanGroup> getGroupsForCertainPage(String pageURL){
		AnalyseUtil.print("Starting to get the page groups...");
		List<DoubanGroup> groups = new ArrayList<DoubanGroup>();
		try {
			String htmlStr = AnalyseUtil.getURLContent(pageURL);
			groups = AnalyseUtil.parseGroups(htmlStr);
		}catch(Exception e) {
			AnalyseUtil.print("Exception happens in getGroupsForCertainPage: \n" + e);
			
		}
		AnalyseUtil.print("Finished to get the page gorups...");
		return groups;
	}
	
	private void printAllGroups() {
		AnalyseUtil.print("All the groups: ");
		for(DoubanGroup group : mAllGroups) {
			AnalyseUtil.print("Group name: " + group.getGroupName() + " , group link: " + group.getGroupURL() + " , member count: " + group.getGroupMemberCount());
		}
	}

}
