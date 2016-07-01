package com.transinfo.tplus.util.jcb;

public class RecordARID {

	public static String getF120Data(){

		String res = "";

		try {

			// get card number
			String cardNo = CardNumberGet.getCardNumber();

			// get transaction type
			String tt = TransactionTypeGet.getTT();

			res = res + tt + cardNo;

			if("0".equals(tt) || "5".equals(tt)){

				return res;

			}else{

				// get auth action code
				String actCode = AuthActionCodeGet.getAuthActionCode();

				// get purge date
				String purgeDate = PurgeDateGet.getPurgeDate();

				// get stop region list
				String srs = StopRegionSignGet.getSRS();

				// compose data
				res = res + actCode + purgeDate + srs;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;

	}

}
