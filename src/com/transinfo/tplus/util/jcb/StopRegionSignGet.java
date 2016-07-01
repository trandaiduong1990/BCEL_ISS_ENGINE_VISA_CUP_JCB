package com.transinfo.tplus.util.jcb;

import java.util.Scanner;

public class StopRegionSignGet {

	public static String getSRS(){

		String res = "";

		try {

			System.out.println();
			System.out.println("Do you want to include stop region (Y/N) ? ");
			System.out.println();
			
			Scanner readUserInput = new Scanner(System.in);
			
			String userResult = readUserInput.nextLine();
			
			if("N".equals(userResult)){
				return res;
			}else{
				
				System.out.println("----------------------");
				System.out.println("0: NOT Include");
				System.out.println("1: Include");
				System.out.println("99. Exit");
				System.out.println();
				
				// get all the regions' values
				
				//JAPAN
				System.out.println();
				System.out.println("Do you want to include JAPAN into stop region ? ");
				System.out.println();
				
				readUserInput = new Scanner(System.in);
				
				userResult = readUserInput.nextLine();
				
				// append values
				res = res + userResult;
				
				//ASIA
				System.out.println();
				System.out.println("Do you want to include ASIA into stop region ? ");
				System.out.println();
				
				readUserInput = new Scanner(System.in);
				
				userResult = readUserInput.nextLine();
				
				// append values
				res = res + userResult;
				
				//USA
				System.out.println();
				System.out.println("Do you want to include USA into stop region ? ");
				System.out.println();
				
				readUserInput = new Scanner(System.in);
				
				userResult = readUserInput.nextLine();
				
				// append values
				res = res + userResult;
				
				//EUROPE
				System.out.println();
				System.out.println("Do you want to include EUROPE into stop region ? ");
				System.out.println();
				
				readUserInput = new Scanner(System.in);
				
				userResult = readUserInput.nextLine();
				
				// append values
				res = res + userResult;
				
				//LOCAL
				System.out.println();
				System.out.println("Do you want to include LOCAL into stop region ? ");
				System.out.println();
				
				readUserInput = new Scanner(System.in);
				
				userResult = readUserInput.nextLine();
				
				// append values
				res = res + userResult;
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;

	}

}
