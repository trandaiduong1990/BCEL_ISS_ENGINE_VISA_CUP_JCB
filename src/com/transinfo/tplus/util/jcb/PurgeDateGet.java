package com.transinfo.tplus.util.jcb;

import java.util.Scanner;

public class PurgeDateGet {

	public static String getPurgeDate(){

		String res = "";

		try {

			System.out.println();
			System.out.println("Plz Purge date (YYYYMM) :: ");
			System.out.println();
			
			Scanner readUserInput = new Scanner(System.in);
			
			String userResult = readUserInput.nextLine();
			
			res = userResult;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;

	}

}
