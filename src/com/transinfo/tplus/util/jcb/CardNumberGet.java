package com.transinfo.tplus.util.jcb;

import java.util.Scanner;

public class CardNumberGet {

	public static String getCardNumber(){

		String res = "";

		try {

			System.out.println();
			System.out.println("Plz enter 16 digits card number :: ");
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
