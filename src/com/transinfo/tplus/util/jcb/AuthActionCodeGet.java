package com.transinfo.tplus.util.jcb;

import java.util.Scanner;

public class AuthActionCodeGet {

	public static String getAuthActionCode(){

		String res = "";

		try {

			System.out.println();
			System.out.println("Plz select Transaction Type :: ");
			System.out.println();
			
			System.out.println();
			System.out.println("----------------------");
			System.out.println("01: Refer to Issuer");
			System.out.println("04: Pick Up");
			System.out.println("05: Decline");
			System.out.println("07: Pick up Fraud");
			System.out.println("41: Pick up Lost");
			System.out.println("43: Pick Up Stolen");
			System.out.println("99. Exit");
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
