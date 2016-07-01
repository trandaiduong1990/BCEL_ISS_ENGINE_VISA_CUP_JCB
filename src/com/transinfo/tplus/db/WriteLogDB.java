/*
 * Copyright (c) 2001-2002 OneEmpower Pte Ltd. Singapore. All Rights Reserved.
 * This work contains trade secrets and confidential
 * material of  OneEmpower Pte Ltd. Singapore and its use of
 * disclosure in whole or in part without express written permission of
 * OneEmpower Pte Ltd. Singapore. is prohibited.
 * File Name          : WriteLogDB.java
 * Author             : I.T. Solutions (Inida) Pvt Ltd.
 * Date of Creation   : October 10, 2001
 * Description        : WriteLogDB with the update methods
 * Version Number     : 1.0
 * Modification History:
 * Date 		 Version No.		Modified By  	 	  Modification Details.
 * 29-12-2001	   1.01				Rohini R			  Updated all methods so as to
 *														  form prepared stmts if needed
 *														  only.
 *
 * 26/03/2002 	   1.02				RajeeV				  Added SPA functionality
 * 06-July-2002	   1.03				Kabilan A.N			  For UAT bug fix - Removed SOPs.
 * 28-May-2003	   1.04				Rajeev				  SecureCode Changes.
 */

package com.transinfo.tplus.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.transinfo.tplus.TPlusCodes;
import com.transinfo.tplus.TPlusException;
import com.transinfo.tplus.debug.DebugWriter;
import com.transinfo.tplus.log.ErrorDataBean;
import com.transinfo.tplus.log.ErrorLog;
import com.transinfo.tplus.log.SystemDataBean;
import com.transinfo.tplus.log.SystemLog;
import com.transinfo.tplus.util.DBManager;
import com.transinfo.tplus.util.DBManagerBCEL;
import com.transinfo.tplus.util.TPlusResultSet;


/**
 * This class has methods to update the logs to the database.
 * The methods of this class are called by the WriteLogSB.
 */

@SuppressWarnings({ "unused", "unchecked", "serial" })
public class WriteLogDB implements java.io.Serializable
{

	/**
	 * This method inserts all the elements in the ArrayList
	 * to the TRANSACTION_LOG table using  prepared statement.
	 * @param   objTransLog that is to be added to Database
	 * @throws TPlusException
	 */

	public static void updateLog(com.transinfo.tplus.log.TransactionDataBean objTransDataBean)throws TPlusException
	{
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB: Updating Transaction Log");

		String strTranxType = "";
		String strOrgMTI    = "";
		double dblAmt 		= 0;

		DBManager objDbManager = new DBManager();
		PreparedStatement objPrepStatement=null;
		Connection con=null;

		try
		{

			if(con==null)
			{
				con=objDbManager.getConnection();
			}

			StringBuffer strQuery=new StringBuffer("INSERT INTO TRANXLOG("+
					"TRANXLOGID,ISSUER_ID,DATETIME,MERCHANTID,MERCHANTNAME,TERMINALID,"+
					"TRANXCODE,CARDNUMBER,EXPIREDDATE, AMOUNT, CURRCODE, TRACENO, MCC,"+
					"TCC, TRACK2DATA,REFNO,APPROVALCODE, RESPONSECODE,PINDATA,DELETED,POSENTRYMODE,POSCONDITIONCODE,ACQID,"+
					"TRACENO2,ORIGINALAMOUNT,ORIGINALCURRCODE,TRANSMISSION_DATETIME,TRANX_CARDHOLDER_AMT,TRANX_SETTLEMENT_AMT,"+
					"TRANX_SETTLEMENT_CURR,TRANX_FEE,RECON,MTI,F90,REMARKS,F55_EXIST,F55,ISAUTHCOMVOID,ACQCOUNTRYCODE,"	+
					"CONV_RATE,DEBIT_ACCTNO,F55_RESPONSE,LIMIT_USED,TRANX_IDENTIFIER,PARTIAL_AMT,TRANX_CURRCONV_AMT,TRANXCODE_SUBTYPE," +
					"ORDER_NO,DATA_VERIFICATION_CODE,ECOM_KEY)" +
			" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			System.out.println("SQL="+strQuery.toString());

			if(objPrepStatement==null)
			{
				objPrepStatement=con.prepareStatement(strQuery.toString());
			}

			StringBuffer sbfDTDVal = new StringBuffer();
			sbfDTDVal.append("SELECT SEQ_TRANXLOG.NEXTVAL AS TRANXID FROM DUAL");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("TransactionDB:"+sbfDTDVal.toString());
			System.out.println(" SQL="+ sbfDTDVal.toString());
			boolean bolExecute = objDbManager.executeSQL(sbfDTDVal.toString());
			TPlusResultSet objRs = objDbManager.getResultSet();
			String strTransId ="";

			if (objRs.next())
			{
				strTransId = objRs.getString("TRANXID");

			}

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB: TRAXCODE:"+objTransDataBean.getMTI()+"  "+objTransDataBean.getProcessingCode());

			//String strTranxCode = TPlusTransType.getTranxType(objTransDataBean.getMTI(),objTransDataBean.getProcessCode());
			//objPrepStatement.setString(1,objTransLog.getTransactionID());
			String strMerchantName = objTransDataBean.getMerchantName();
			if(strMerchantName != null)
			{
				strMerchantName.replace("'","''");
			}


			System.out.println("1 : TransactionId  "+strTransId);
			objTransDataBean.setTransactionId(strTransId);
			objPrepStatement.setString(1,strTransId);
			System.out.println("2 -Issuer Id : "+objTransDataBean.getIssuerId());
			objPrepStatement.setString(2,objTransDataBean.getIssuerId());
			Long time=objTransDataBean.getTimeStamp();
			objPrepStatement.setTimestamp(3,new Timestamp(time.longValue()));
			System.out.println("3 - time :  "+time.longValue());
			objPrepStatement.setString(4,isNull(objTransDataBean.getMerchantId()));
			System.out.println("4 - MerchantId "+objTransDataBean.getMerchantId());
			objPrepStatement.setString(5,isNull(strMerchantName));
			System.out.println("5 - TerminalId  "+objTransDataBean.getTerminalId());
			objPrepStatement.setString(6,isNull(objTransDataBean.getTerminalId()));

			System.out.println("6 - TerminalId  "+objTransDataBean.getTerminalId());
			objPrepStatement.setString(7,objTransDataBean.getTranxCode());
			System.out.println("7 - TranxCode "+objTransDataBean.getTranxCode());
			objPrepStatement.setString(8,isNull(objTransDataBean.getCardNo()));
			System.out.println("8 - CardNo "+objTransDataBean.getCardNo());
			objPrepStatement.setString(9,isNull(objTransDataBean.getExpDate()));
			System.out.println("9 - ExpDate "+objTransDataBean.getExpDate());
			//objPrepStatement.setDouble(10,objTransDataBean.getAmount()/100);
			/*if(objTransDataBean.getTranxCode() == null || objTransDataBean.getTranxCode().equalsIgnoreCase("ENQUIRY")){
				objTransDataBean.setTranxAmt("0");
			}*/
			if(objTransDataBean.getTranxAmt() ==null || objTransDataBean.getTranxAmt().trim().equals(""))
			{
				objTransDataBean.setTranxAmt("0");
			}

			objPrepStatement.setDouble(10,(Double.valueOf(objTransDataBean.getTranxAmt()).doubleValue())/Integer.valueOf(objTransDataBean.getCurrDecimalPoint()));
			System.out.println("10 - Amount  "+Double.valueOf(objTransDataBean.getTranxAmt()).doubleValue()/Integer.valueOf(objTransDataBean.getCurrDecimalPoint()));
			objPrepStatement.setString(11,isNull(objTransDataBean.getTranxCurrCode()));
			System.out.println("11 - TranxCurrCode "+objTransDataBean.getTranxCurrCode());
			objPrepStatement.setString(12,isNull(objTransDataBean.getTraceNo()));
			System.out.println("12 - TraceNo "+objTransDataBean.getTraceNo());
			objPrepStatement.setString(13,isNull(objTransDataBean.getMCC()));
			System.out.println("13 - MCC "+objTransDataBean.getMCC());
			objPrepStatement.setString(14,isNull(objTransDataBean.getTCC()));
			System.out.println("14 - TCC  "+objTransDataBean.getTCC());
			objPrepStatement.setString(15,isNull(objTransDataBean.getTrack2Data()));
			System.out.println("15 - Track2Data "+objTransDataBean.getTrack2Data());

			objPrepStatement.setString(16,isNull(objTransDataBean.getRefNo()));
			System.out.println("16 - RefNo "+objTransDataBean.getRefNo());
			objPrepStatement.setString(17,isNull(objTransDataBean.getApprovalCode()));
			System.out.println("17 - ApprovalCode "+objTransDataBean.getResponseCode());
			objPrepStatement.setString(18,isNull(objTransDataBean.getResponseCode()));
			System.out.println("18 - ResponseCode "+objTransDataBean.getResponseCode());
			objPrepStatement.setString(19,isNull(objTransDataBean.getPINData()));
			System.out.println("19 - PINData "+objTransDataBean.getPINData());
			objPrepStatement.setString(20,isNull(objTransDataBean.getDeleted()));
			System.out.println("20 - Deleted "+objTransDataBean.getDeleted());


			objPrepStatement.setString(21,isNull(objTransDataBean.getPOSEntryMode()));
			System.out.println("21 -POSEntryMode:  "+objTransDataBean.getPOSEntryMode());
			objPrepStatement.setString(22,isNull(objTransDataBean.getPOSCondCode()));
			System.out.println("22 -POSCondCode:  "+objTransDataBean.getPOSCondCode());
			objPrepStatement.setString(23,isNull(objTransDataBean.getAcqInstId()));
			System.out.println("23 -Acq Id:  "+objTransDataBean.getPOSEntryMode());
			objPrepStatement.setString(24,isNull(objTransDataBean.getTraceNo2()));
			System.out.println("24 -TraceNo2 :  "+objTransDataBean.getTraceNo2());
			objPrepStatement.setDouble(25,objTransDataBean.getOrgAmt());
			System.out.println("25 -OrgAmt:  "+objTransDataBean.getOrgAmt());
			objPrepStatement.setString(26,isNull(objTransDataBean.getOrgCurrCode()));
			System.out.println("26 - OrgCurrCode :  "+objTransDataBean.getOrgCurrCode());
			objPrepStatement.setString(27,isNull(objTransDataBean.getTransmissionDateTime()));
			System.out.println("27 - TransmissionDateTime :  "+objTransDataBean.getTransmissionDateTime());
			objPrepStatement.setString(28,isNull(objTransDataBean.getTranxCHAmt()));
			System.out.println("28 - TranxCHAmt :  "+objTransDataBean.getTranxCHAmt());
			objPrepStatement.setString(29,isNull(objTransDataBean.getTranxSettledAmt()));
			System.out.println("29 - TranxSettledAmt :  "+objTransDataBean.getTranxSettledAmt());
			objPrepStatement.setString(30,isNull(objTransDataBean.getTranxSettledCurr()));
			System.out.println("30 - TranxSettledCurr :  "+objTransDataBean.getTranxSettledCurr());
			objPrepStatement.setString(31,isNull(objTransDataBean.getTranxFee()));
			System.out.println("31 - TranxFee :  "+objTransDataBean.getTranxFee());
			objPrepStatement.setString(32,isNull(objTransDataBean.getRecon()));
			System.out.println("32 - Recon :  "+objTransDataBean.getRecon());
			objPrepStatement.setString(33,isNull(objTransDataBean.getMTI()));
			System.out.println("33 - MTI:  "+objTransDataBean.getMTI());
			objPrepStatement.setString(34,isNull(objTransDataBean.getF90()));
			System.out.println("34 - F90 :  "+objTransDataBean.getF90());
			objPrepStatement.setString(35,isNull(objTransDataBean.getRemarks()));
			System.out.println("35 - Remarks :  "+objTransDataBean.getRemarks());
			System.out.println("36 - F55 Exists :  "+objTransDataBean.getF55Exist());
			objPrepStatement.setString(36,isNull(objTransDataBean.getF55Exist()));
			System.out.println("37 - F55 :  "+objTransDataBean.getF55());
			objPrepStatement.setString(37,isNull(objTransDataBean.getF55()));
			System.out.println("38 - isAuthComVoid :  "+objTransDataBean.getIsAuthComVoid());
			objPrepStatement.setString(38,isNull(objTransDataBean.getIsAuthComVoid()));
			System.out.println("39 - acq country code :  "+objTransDataBean.getAcqCountryCode());
			objPrepStatement.setString(39,isNull(objTransDataBean.getAcqCountryCode()));
			System.out.println("40 - currency conversion code :  "+objTransDataBean.getCurrConRate());
			objPrepStatement.setString(40,isNull(objTransDataBean.getCurrConRate()));
			System.out.println("41 - debit account no :  "+objTransDataBean.getDebitAccNo());
			objPrepStatement.setString(41,isNull(objTransDataBean.getDebitAccNo()));
			System.out.println("42 - f55 response :  "+objTransDataBean.getF55Res());
			objPrepStatement.setString(42,isNull(objTransDataBean.getF55Res()));
			System.out.println("43 - limit used :  "+objTransDataBean.getLimitUsed());
			objPrepStatement.setString(43,isNull(objTransDataBean.getLimitUsed()));
			System.out.println("44 - Tranx Identifier :  "+objTransDataBean.getTranxIdentifier());
			objPrepStatement.setString(44,isNull(objTransDataBean.getTranxIdentifier()));
			System.out.println("45 - Partial Amt :  "+objTransDataBean.getPartialAmt());
			objPrepStatement.setDouble(45,Double.valueOf(objTransDataBean.getPartialAmt()));
			System.out.println("46 - Partial Amt :  "+objTransDataBean.getTranxCurrConvAmt());
			objPrepStatement.setDouble(46,Double.valueOf(objTransDataBean.getTranxCurrConvAmt()));
			System.out.println("47 - Tranx Code Sub Type :  "+objTransDataBean.getTranxCodeSubType());
			objPrepStatement.setString(47,objTransDataBean.getTranxCodeSubType());
			System.out.println("48 - Order NO :  "+objTransDataBean.getOrderNo());
			objPrepStatement.setString(48,objTransDataBean.getOrderNo());
			System.out.println("49 - DVC :  "+objTransDataBean.getDataVerificationCode());
			objPrepStatement.setString(49,objTransDataBean.getDataVerificationCode());
			System.out.println("50 - KEY :  "+objTransDataBean.getKey());
			objPrepStatement.setString(50,objTransDataBean.getKey());

			objPrepStatement.executeUpdate();

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Sucessfully updated TransLog");
		}
		catch(TPlusException objTPlusExcep)
		{
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in updating TransactionLog."+
					objTPlusExcep.toString());
			throw objTPlusExcep;
		}
		catch(Exception objExcep)
		{
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in updating TransactionLog."+
					objExcep.toString());
			throw new TPlusException
			(TPlusCodes.SQL_QUERY_ERR,objExcep.getMessage());
		}
		finally
		{
			try
			{
				if(objPrepStatement!=null)
				{
					objPrepStatement.close();
					objPrepStatement=null;
				}
			}
			catch(Exception objExcep)
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in closing prepared statement.");
				throw new TPlusException(TPlusCodes.SQL_QUERY_ERR,
						objExcep.getMessage());
			}
			finally
			{
				try
				{
					if(con!=null)
					{
						objDbManager.closeConnection(con);
						con=null;
					}
				}
				catch(Exception Exp)
				{
					throw new TPlusException(TPlusCodes.DATABASE_CONN_ERR,Exp.getMessage());
				}
			}
		}

	}


	public static void insertPreAuthTranx(com.transinfo.tplus.log.TransactionDataBean objTransDataBean)throws TPlusException
	{
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB: Updating Transaction Log");

		String strTranxType = "";
		String strOrgMTI    = "";
		double dblAmt 		= 0;

		DBManager objDbManager = new DBManager();
		PreparedStatement objPrepStatement=null;
		Connection con=null;

		try
		{

			if(con==null)
			{
				con=objDbManager.getConnection();
			}

			StringBuffer strQuery=new StringBuffer("INSERT INTO PREAUTH_TRANX("+
					"AUTHID,TRANXLOGID,COMPLETED,EXPIRY_DATE,CREATE_DATE )"	+
			" VALUES(?,?,?,?,?)");
			System.out.println("SQL="+strQuery.toString());

			if(objPrepStatement==null)
			{
				objPrepStatement=con.prepareStatement(strQuery.toString());
			}




			StringBuffer sbfDTDVal = new StringBuffer();
			sbfDTDVal.append("SELECT SEQ_PREAUTH_ID.NEXTVAL AS PREAUTHID FROM DUAL");
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("TransactionDB:"+sbfDTDVal.toString());
			System.out.println(" SQL="+ sbfDTDVal.toString());
			boolean bolExecute = objDbManager.executeSQL(sbfDTDVal.toString());
			TPlusResultSet objRs = objDbManager.getResultSet();
			String preAuthId ="";

			if (objRs.next())
			{
				preAuthId = objRs.getString("PREAUTHID");

			}

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB: TRAXCODE:"+objTransDataBean.getMTI()+"  "+objTransDataBean.getProcessingCode());

			//String strTranxCode = TPlusTransType.getTranxType(objTransDataBean.getMTI(),objTransDataBean.getProcessCode());
			//objPrepStatement.setString(1,objTransLog.getTransactionID());

			System.out.println("1 : PREAUTH Id  "+preAuthId);
			objPrepStatement.setString(1,preAuthId);

			System.out.println("2 -TranxLog Id : "+objTransDataBean.getTransactionId());
			objPrepStatement.setString(2,objTransDataBean.getTransactionId());

			System.out.println("3 - Completed  :"+"N");
			objPrepStatement.setString(3,"N");

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, 7);
			Date expDate = calendar.getTime();

			System.out.println("4 - Expiry Date :"+expDate);
			objPrepStatement.setTimestamp(4,new Timestamp(expDate.getTime()));

			Long time=objTransDataBean.getTimeStamp();
			System.out.println("5 - time :  "+time.longValue());
			objPrepStatement.setTimestamp(5,new Timestamp(time.longValue()));

			objPrepStatement.executeUpdate();

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Sucessfully updated TransLog");
		}
		catch(TPlusException objTPlusExcep)
		{
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in updating TransactionLog."+
					objTPlusExcep.toString());
			throw objTPlusExcep;
		}
		catch(Exception objExcep)
		{
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in updating TransactionLog."+
					objExcep.toString());
			throw new TPlusException
			(TPlusCodes.SQL_QUERY_ERR,objExcep.getMessage());
		}
		finally
		{
			try
			{
				if(objPrepStatement!=null)
				{
					objPrepStatement.close();
					objPrepStatement=null;
				}
			}
			catch(Exception objExcep)
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in closing prepared statement.");
				throw new TPlusException(TPlusCodes.SQL_QUERY_ERR,
						objExcep.getMessage());
			}
			finally
			{
				try
				{
					if(con!=null)
					{
						objDbManager.closeConnection(con);
						con=null;
					}
				}
				catch(Exception Exp)
				{
					throw new TPlusException(TPlusCodes.DATABASE_CONN_ERR,Exp.getMessage());
				}
			}
		}

	}


	//EOA by Rajeev on 26/03/2002.

	/**
	 * This method inserts all the elements in the ArrayList
	 * to the ERROR_LOG table using  prepared statement.
	 * @param   objErrorLog that is to be added to Database
	 */

	public static void updateLog(ErrorLog objErrorLog)throws TPlusException
	{

		if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB: Updating Error Log");
		//Added By Rohini
		ArrayList objArlLog=objErrorLog.getLog();
		if (objArlLog.size() > 0)
		{
			//Added By Rohini

			ErrorDataBean objErrorDataBean;
			DBManager objDbManager = new DBManager();
			Connection con=null;
			PreparedStatement objPrepStatement=null;
			try
			{
				if(con==null)
				{
					con=objDbManager.getConnection();
				}

				StringBuffer strQuery=new StringBuffer("INSERT INTO TRANX_ERRORLOG(ERROR_ID,ISSUER_ID,REQUEST_TYPE,"+
						"ERROR_CODE,ERROR_TYPE,ERROR_SRC,MERCHANT_ID,TERMINAL_ID,DESCRIPTION,DATETIME) "+
				"VALUES(?,?,?,?,?,?,?,?,?,?)");

				System.out.println("Error Log="+strQuery.toString());

				if(objPrepStatement==null)
				{
					objPrepStatement=con.prepareStatement(strQuery.toString());
				}

				for(int i=0;i<objArlLog.size();i++)
				{

					StringBuffer sbfDTDVal = new StringBuffer();
					sbfDTDVal.append("SELECT SEQ_ERRORLOG.NEXTVAL AS ERRORID FROM DUAL");
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("ERRORDB:"+sbfDTDVal.toString());
					System.out.println(" SQL="+ sbfDTDVal.toString());
					boolean bolExecute = objDbManager.executeSQL(sbfDTDVal.toString());
					TPlusResultSet objRs = objDbManager.getResultSet();
					String strErrorId ="";

					if (objRs.next())
					{
						strErrorId = objRs.getString("ERRORID");

					}

					System.out.println("ERROR ID="+strErrorId);

					objErrorDataBean=(ErrorDataBean)objArlLog.get(i);
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+strErrorId);
					objPrepStatement.setString(1,isNull(strErrorId));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+objErrorDataBean.getIssuerId());
					objPrepStatement.setString(2,isNull(objErrorDataBean.getIssuerId()));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+objErrorDataBean.getRequestType());
					objPrepStatement.setString(3,isNull(objErrorDataBean.getRequestType()));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+objErrorDataBean.getErrorCode());
					objPrepStatement.setString(4,isNull(objErrorDataBean.getErrorCode()));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+objErrorDataBean.getErrorType());
					objPrepStatement.setString(5,isNull(objErrorDataBean.getErrorType()));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+objErrorDataBean.getErrorSrc());
					objPrepStatement.setString(6,isNull(objErrorDataBean.getErrorSrc()));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+objErrorDataBean.getMerchantId());
					objPrepStatement.setString(7,isNull(objErrorDataBean.getMerchantId()));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+objErrorDataBean.getTerminalId());
					objPrepStatement.setString(8,isNull(objErrorDataBean.getTerminalId()));
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:"+objErrorDataBean.getErrorDesc());
					objPrepStatement.setString(9,isNull(objErrorDataBean.getErrorDesc()));
					Long time=objErrorDataBean.getTimeStamp();
					objPrepStatement.setTimestamp(10,new Timestamp(time.longValue()));

					objPrepStatement.executeUpdate();

				}
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Successfully updated ErrorLog.");

			}
			catch(TPlusException objTPlusExcep)
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in updating ErrorLog"+
						objTPlusExcep.toString());
				throw objTPlusExcep;
			}
			catch(Exception objExcep)
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in updating ErrorLog"+
						objExcep.toString());
				throw new TPlusException(TPlusCodes.SQL_QUERY_ERR,objExcep.getMessage());
			}
			finally
			{
				try
				{
					if(objPrepStatement!=null)
					{
						objPrepStatement.close();
						objPrepStatement=null;
					}
				}
				catch(Exception objExcep)
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in closing prepared statement.");
					//throw new TPlusException(TPlusCodes.SQL_QUERY_ERR,objExcep.getMessage());
				}
				finally
				{
					try
					{
						if(con!=null)
						{
							objDbManager.closeConnection(con);
							con=null;
						}
					}
					catch(Exception Exp)
					{
						if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in closing DB Connection...");
					}
				}
			}
		}
	}

	/**
	 * This method inserts all the elements in the ArrayList
	 * to the PA_LOG table using  prepared statement.
	 * @param   objCacheLog that is to be added to Database
	 */




	/**
	 * This method inserts all the elements in the ArrayList
	 * to the SYSTEMLOG table using  prepared statement.
	 * @param   objSysLog that is to be added to Database
	 */
	public static void updateLog(SystemLog objSysLog)throws TPlusException
	{

		ArrayList objArlLog=objSysLog.getLog();
		if (objArlLog.size() > 0)
		{

			SystemDataBean objSysDataBean;
			DBManager objDbManager = new DBManager();
			Connection con=null;
			PreparedStatement objPrepStatement=null;
			try
			{
				if(con==null)
				{
					con=objDbManager.getConnection();
				}

				StringBuffer strQuery=new StringBuffer("INSERT INTO SYSTEMLOG("+
				"MODULE_ID,ERROR_CODE,ERROR_TYPE,DESCRIPTION,TIME_STAMP) VALUES(?,?,?,?,?)");

				if(objPrepStatement==null)
				{
					objPrepStatement=con.prepareStatement(strQuery.toString());
				}

				for(int i=0;i<objArlLog.size();i++)
				{
					objSysDataBean=(SystemDataBean)objArlLog.get(i);
					objPrepStatement.setString(1,objSysDataBean.getModuleCode());
					objPrepStatement.setString(2,objSysDataBean.getErrorCode());
					objPrepStatement.setString(3,objSysDataBean.getErrorType());
					objPrepStatement.setString(4,objSysDataBean.getErrorDescription());
					Long time=objSysDataBean.getTimeStamp();
					objPrepStatement.setTimestamp(5,new Timestamp(time.longValue()));
					objPrepStatement.executeUpdate();
				}
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Successfully updated SYSTEMLOG.");

			}
			catch(TPlusException objTPlusExcep)
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB: Error in updating SYSTEMLOG "+
						objTPlusExcep.toString());
				throw objTPlusExcep;
			}
			catch(Exception objExcep)
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB: Error in updating SYSTEMLOG "+
						objExcep.toString());
				throw new TPlusException
				(TPlusCodes.SQL_QUERY_ERR,"Error in updating SYSTEMLOG"+objExcep.getMessage());
			}
			finally
			{
				try
				{
					if(objPrepStatement!=null)
					{
						objPrepStatement.close();
						objPrepStatement=null;
					}
				}
				catch(Exception objExcep)
				{
					if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in closing prepared statement.");
					throw new TPlusException(TPlusCodes.SQL_QUERY_ERR,
							objExcep.getMessage());
				}
				finally
				{
					try
					{
						if(con!=null)
						{
							objDbManager.closeConnection(con);
							con=null;
						}
					}
					catch(Exception Exp)
					{
						System.out.println("100 ");
						throw new TPlusException(TPlusCodes.DATABASE_CONN_ERR,Exp.getMessage());
					}
				}
			}
		}
	}

	// TO BE REMOVED
	/**
	 * Method check for value is null
	 * @param String
	 * @return String
	 */

	public static String isNull(String strValue)
	{
		return strValue == null ? " " : strValue;
	}
	
	public static void insertSMSDVC(com.transinfo.tplus.log.TransactionDataBean objTransDataBean)throws TPlusException
	{
		if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB: insertSMSDVC");

		DBManager objManager = new DBManager();
		DBManagerBCEL objDbManagerBCEL = new DBManagerBCEL();
		PreparedStatement objPrepStatement=null;
		Connection con=null;

		try
		{

			if(con==null)
			{
				con=objDbManagerBCEL.getConnection();
			}

			StringBuffer strQuery=new StringBuffer("INSERT INTO CUP_ECOM_SMS (" 
													+ "CUP_ECOM_SMS_ID,CARD_NUMBER,PHONE_NUMBER,DYNAMIC_VERIFY_CODE,INSERTED_DATE,INSERTED_BY,STATUS )" 
													+ " VALUES(?,?,?,?,?,?,?)");
			
			System.out.println("SQL="+strQuery.toString());

			if(objPrepStatement==null)
			{
				objPrepStatement=con.prepareStatement(strQuery.toString());
			}

			StringBuffer sbfDTDVal = new StringBuffer();
			sbfDTDVal.append("SELECT CUP_ECOM_SMS_ID_SEQ.NEXTVAL AS CUP_ECOM_SMS_ID FROM DUAL");
			
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("TransactionDB:"+sbfDTDVal.toString());
			
			System.out.println(" SQL="+ sbfDTDVal.toString());
			boolean bolExecute = objManager.executeSQL(sbfDTDVal.toString());
			TPlusResultSet objRs = objManager.getResultSet();
			String cupSMSId ="";

			if (objRs.next())
			{
				cupSMSId = objRs.getString("CUP_ECOM_SMS_ID");

			}

			System.out.println("1 : cupSMSId  "+cupSMSId);
			objPrepStatement.setString(1,cupSMSId);

			System.out.println("2 Card No : "+objTransDataBean.getCardNo());
			objPrepStatement.setString(2,objTransDataBean.getCardNo());

			System.out.println("3 - Mobile NO  :"+objTransDataBean.getMobileNo());
			objPrepStatement.setString(3,objTransDataBean.getMobileNo());

			System.out.println("4 - DVC  :"+objTransDataBean.getDataVerificationCode());
			objPrepStatement.setString(4,objTransDataBean.getDataVerificationCode());

			Calendar calendar = Calendar.getInstance();
			Date insertDate = calendar.getTime();

			System.out.println("5 - Inserted Date :"+insertDate);
			objPrepStatement.setTimestamp(5,new Timestamp(insertDate.getTime()));

			System.out.println("6 - Inserted By  : ISS Engine");
			objPrepStatement.setString(6,"ISS Engine");

			System.out.println("7 - Status  : N");
			objPrepStatement.setString(7,"N");

			objPrepStatement.executeUpdate();

			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Sucessfully inserted insertSMSDVC");
		}
		catch(TPlusException objTPlusExcep)
		{
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in inserting insertSMSDVC."+ objTPlusExcep.toString());
			throw objTPlusExcep;
		}
		catch(Exception objExcep)
		{
			if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in updating TransactionLog. "+ objExcep.toString());
			throw new TPlusException
			(TPlusCodes.SQL_QUERY_ERR,objExcep.getMessage());
		}
		finally
		{
			try
			{
				if(objPrepStatement!=null)
				{
					objPrepStatement.close();
					objPrepStatement=null;
				}
			}
			catch(Exception objExcep)
			{
				if (DebugWriter.boolDebugEnabled) DebugWriter.write("WriteLogDB:Error in closing prepared statement.");
				throw new TPlusException(TPlusCodes.SQL_QUERY_ERR, objExcep.getMessage());
			}
			finally
			{
				try
				{
					if(con!=null)
					{
						objDbManagerBCEL.closeConnection(con);
						con=null;
					}
				}
				catch(Exception Exp)
				{
					throw new TPlusException(TPlusCodes.DATABASE_CONN_ERR,Exp.getMessage());
				}
			}
		}

	}


}