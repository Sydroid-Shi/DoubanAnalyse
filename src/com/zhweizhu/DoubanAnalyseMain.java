package com.zhweizhu;

import com.zhweizhu.util.AnalyseUtil;

public class DoubanAnalyseMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//HTMLReader reader = new HTMLReader();
		//reader.init();
		
		//GroupSearchReader gsReader = new GroupSearchReader();
		//gsReader.run();
		
//		GroupReader groupReader = new GroupReader(AnalyseUtil.GUANGZHOU);//AnalyseUtil.ZHUHAI
		GroupReader groupReader = new GroupReader(AnalyseUtil.ZHUHAI);
//		GroupReader groupReader = new GroupReader(AnalyseUtil.FOSHAN);
		groupReader.setPreStartDate("12-21");//included,MM-dd
		groupReader.setEndDate("12-31");//no included,MM-dd
		groupReader.run();//remember to set the file location in AnalyseUtil.writeToFile
		
		//test
//		boolean isB = AnalyseUtil.isBetweenPeriod("2018-07-31", "08-27", "09-11");
//		AnalyseUtil.print("" + isB);
		
		
		/*String testStart = "09-29";
		String testEnd = "10-03";
		String test3 = "09-02";
		String test4 = "09-02 01:00";
		String test5 = "09-03";
		
		String currentDate = "2018-08-06 13:41:07";
		boolean isTrue = AnalyseUtil.isBetweenPeriod(currentDate, testStart, testEnd);
		AnalyseUtil.print("" + isTrue);
		try {
//		int compare1 = test1.compareTo(test2);
//		int compare2 = test3.compareTo(test2);
//		int compare3 = test4.compareTo(test3);
//		AnalyseUtil.print("" + compare1);
//		AnalyseUtil.print("" + compare2);
//		AnalyseUtil.print("" + compare3);
//		AnalyseUtil.print("" + test4.compareTo(test5));
		}catch(Exception e) {
			e.printStackTrace();
		}*/
	}

}
