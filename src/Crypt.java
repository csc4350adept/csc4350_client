import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypt {

		public static String getPwordHash(String pword) throws CryptException {
		String salted = pword + "bbqwtfomg"; //This should really be more random
		byte[] key;
		
		MessageDigest sha = null;
		
		try {
			key = salted.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-256"); //SHA-256 is part of the SHA-2 family. SHA-1 is bad due to collision attacks
			key = sha.digest(key);
			key = Arrays.copyOf(key, 32); //Getting 32 length key here because we can afford it
			return Base64.getEncoder().encodeToString(key);
		} catch (NoSuchAlgorithmException e) {
			throw new CryptException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new CryptException(e.getMessage());
		}
	}
	
	private static SecretKeySpec getCryptKey(String pword) throws CryptException {
		String salted = pword + "omgwtfbbq"; //This should really be more random
		byte[] key;
		
		MessageDigest sha = null;
		
		try {
			key = salted.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-256"); //SHA-256 is part of the SHA-2 family. SHA-1 is bad due to collision attacks
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			return new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException e) {
			throw new CryptException(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			throw new CryptException(e.getMessage());
		}
	}
	
	public static String encrypt(String strToEncrypt, String pword) throws CryptException {
        try {
        	SecretKeySpec secretKey = getCryptKey(pword);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
        	throw new CryptException(e.getMessage());
        }
    }
	
	public static String decrypt(String strToDecrypt, String pword) throws CryptException {
        try {
        	SecretKeySpec secretKey = getCryptKey(pword);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
        	throw new CryptException(e.getMessage());
        }
    }
	
}
