package com.transinfo.tplus.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jpos.iso.ISOUtil;

import com.transinfo.tplus.TPlusPrintOutput;

public class CardEncryption {

	private static byte[] encryptKey = "1234567890ABCDEFGHIJKLMN".getBytes();
	private static byte[] ivSpec = "tivn!@#$".getBytes();

	private static String algoritham = "DESede";
	private static String algoMethod = "DESede/CBC/PKCS5Padding";

	private static Cipher encCipher;
	private static Cipher decCipher;

	public CardEncryption() throws InvalidKeyException,
			NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, InvalidAlgorithmParameterException {

		// 1
		// Create a DESede key spec from the key
		//DESedeKeySpec spec = new DESedeKeySpec(encryptKey);

		// Get the secret key factor for generating DESede keys
		//SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algoritham);

		// Generate a DESede SecretKey object
		//SecretKey theKey = keyFactory.generateSecret(spec);
		
		// 2
		SecretKey theKey = new SecretKeySpec(encryptKey, algoritham);

		// Create a DESede Cipher
		encCipher = Cipher.getInstance(algoMethod);
		decCipher = Cipher.getInstance(algoMethod);

		// Create an initialization vector (necessary for CBC mode)
		IvParameterSpec IvParameters = new IvParameterSpec(ivSpec);

		encCipher.init(Cipher.ENCRYPT_MODE, theKey, IvParameters);
		decCipher.init(Cipher.DECRYPT_MODE, theKey, IvParameters);

	}

	public static synchronized String encrypt(String plainText) throws IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		
		SecretKey theKey = new SecretKeySpec(encryptKey, algoritham);
		// Create a DESede Cipher
		encCipher = Cipher.getInstance(algoMethod);
		// Create an initialization vector (necessary for CBC mode)
		IvParameterSpec IvParameters = new IvParameterSpec(ivSpec);

		encCipher.init(Cipher.ENCRYPT_MODE, theKey, IvParameters);

		String encryptText = "";

		byte[] plaintextBytes = plainText.getBytes();
		// Encrypt the data
		byte[] encrypted = encCipher.doFinal(plaintextBytes);
		encryptText = ISOUtil.hexString(encrypted);

		return encryptText;

	}

	public static synchronized String decrypt(String encryptText) throws IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		
		SecretKey theKey = new SecretKeySpec(encryptKey, algoritham);

		// Create a DESede Cipher
		decCipher = Cipher.getInstance(algoMethod);

		// Create an initialization vector (necessary for CBC mode)
		IvParameterSpec IvParameters = new IvParameterSpec(ivSpec);

		decCipher.init(Cipher.DECRYPT_MODE, theKey, IvParameters);

		String plainText = "";

		byte[] encryptTextBytes = ISOUtil.hex2byte(encryptText);
		// Encrypt the data
		byte[] plained = decCipher.doFinal(encryptTextBytes);
		plainText = new String(plained);

		return plainText;

	}

	public static void main(String[] args) throws IllegalBlockSizeException {
		
		try {
			
			//CardEncryption objNishanEncryption = new CardEncryption();
			
			String plainText = "giftpac";
			TPlusPrintOutput.printMessage(plainText);
			
			String encStr = CardEncryption.encrypt(plainText);
			TPlusPrintOutput.printMessage(encStr);
			
			/*String decStr = objNishanEncryption.decrypt("9B2BC72BE879EC38");
			TPlusPrintOutput.printMessage(decStr);*/
			
		}  catch (Exception e) {
			
		}
		
	}

}
