package com.zhweizhu;

import com.zhweizhu.util.AnalyseUtil;

public class DoubanAnalyseMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//HTMLReader reader = new HTMLReader();
		//reader.init();
		
		//GroupSearchReader gsReader = new GroupSearchReader();
		//gsReader.run();
		
		GroupReader groupReader = new GroupReader();
		groupReader.run();
		
		/*String test1 = "07-31";
		String test2 = "08-01";
		String test3 = "09-02";
		try {
		int compare1 = test1.compareTo(test2);
		int compare2 = test3.compareTo(test2);
		AnalyseUtil.print("" + compare1);
		AnalyseUtil.print("" + compare2);
		}catch(Exception e) {
			e.printStackTrace();
		}*/
	}

}
