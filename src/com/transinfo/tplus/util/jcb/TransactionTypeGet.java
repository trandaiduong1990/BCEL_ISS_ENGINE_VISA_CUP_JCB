package com.transinfo.tplus.util.jcb;

import java.util.Scanner;

public class TransactionTypeGet {

	public static String getTT(){

		String res = "";

		try {

			System.out.println();
			System.out.println("Plz select Transaction Type :: ");
			System.out.println();
			
			System.out.println();
			System.out.println("----------------------");
			System.out.println("0. Delete");
			System.out.println("1. Add");
			System.out.println("2. Revise");
			System.out.println("5. Inquiry");
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
