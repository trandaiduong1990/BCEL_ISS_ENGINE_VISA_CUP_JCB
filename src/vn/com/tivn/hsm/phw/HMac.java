package vn.com.tivn.hsm.phw;

import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
* This class defines common routines for generating
* authentication signatures for AWS requests.
*/
public class HMac {
private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";


/**
* Computes RFC 2104-compliant HMAC signature.
* * @param data
* The data to be signed.
* @param key
* The signing key.
* @return
* The Base64-encoded RFC 2104-compliant HMAC signature.
* @throws
* java.security.SignatureException when signature generation fails
*/
public static String calculateRFC2104HMAC(String data, String key)
throws java.security.SignatureException
{
String result;
try {

// get an hmac_sha1 key from the raw key bytes
SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

// get an hmac_sha1 Mac instance and initialize with the signing key
Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
mac.init(signingKey);

// compute the hmac on input data bytes
byte[] rawHmac = mac.doFinal(data.getBytes());
System.out.println(NumberUtil.hexString(rawHmac));
// base64-encode the hmac
//result = Encoding.EncodeBase64(rawHmac);
byte[] encoded = null;//Base64.encodeBase64(rawHmac);
//String encodedString = new String(encoded);
result = new String(encoded);
} catch (Exception e) {
throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
}
return result;
}

public static void main(String s[])throws Exception
{
	HMac hmac = new HMac();
	System.out.println(hmac.calculateRFC2104HMAC("Hello1","11111111111111111111111111111111"));
	//byte[] mac =NumberUtil.hex2byte("CBFE563B0887F41405BB9B0FB8290779BF612505");
	//System.out.println(new String(Base64.encodeBase64(mac)));
	//INFO: Received response from host EE07010014CBFE563B0887F41405BB9B0FB8290779BF612505
}
}

