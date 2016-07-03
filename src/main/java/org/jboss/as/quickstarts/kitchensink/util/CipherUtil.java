package org.jboss.as.quickstarts.kitchensink.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.SaltGenerator;
import org.jasypt.salt.StringFixedSaltGenerator;
import org.jboss.logging.Logger.Level;

public class CipherUtil {
	
	private static final String myEncryptionPassword = "e83bbc86-98b6-4ae5-89df-e7b30cbe62ac";
	private static final String mySalt = "3753af4e-8160-45ba-a21f-85760cf1aece";
	
    private static final Logger LOGGER = Logger.getLogger(CipherUtil.class.getName());
	
	public static String encrypt(String myText){
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndDES");
		encryptor.setPassword(myEncryptionPassword);
		SaltGenerator saltGenerator = new StringFixedSaltGenerator(mySalt);
		encryptor.setSaltGenerator(saltGenerator);

		try{
			String myEncryptedText = encryptor.encrypt(myText);
			return myEncryptedText;
		} catch(Exception ex){
			LOGGER.severe(String.format("COULD NOT EN-CRYPT TEXT: %s", myText));
			return null;
		}
	}
	
	public static String decrypt(String myEncryptedText){
		StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
        decryptor.setAlgorithm("PBEWithMD5AndDES");
		decryptor.setPassword(myEncryptionPassword);
		SaltGenerator saltGenerator = new StringFixedSaltGenerator(mySalt);
		decryptor.setSaltGenerator(saltGenerator);

		try{
			String myDecryptedText = decryptor.decrypt(myEncryptedText);
			return myDecryptedText;
		} catch(Exception ex){
			LOGGER.severe(String.format("COULD NOT DE-CRYPT TEXT: %s", myEncryptedText));
			return null;
		}
	}
	
    /**Generiert ein zufaelliges salt
     * @return ein zufaelliges salt
     * @throws NoSuchAlgorithmException
     */
    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
    	byte[] salt = null;
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        salt = new byte[16];
        sr.nextBytes(salt);
    	return salt;
    }
    
    /**Liefert fuer ein Passwort und ein Salt einen SHA-512 Hash
     * @param passwordToHash das zu hashende Passwort
     * @param salt der Salt
     * @return das gehashte Passwort
     * @throws NoSuchAlgorithmException 
     */
    public static String getHashedPassword(String passwordToHash, byte[] salt) throws NoSuchAlgorithmException
    {
        String generatedPassword = null;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] bytes = md.digest(passwordToHash.getBytes());
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        generatedPassword = sb.toString();
        return generatedPassword;
    }
    
    public static String createHashedPasswordWithSaltAppended(String passwordToHash) throws NoSuchAlgorithmException{
    	String hashedPasswordWithSaltAppended = null;
    	byte[] salt = getSalt();
    	String hashedPassword  = getHashedPassword(passwordToHash, salt);
    	hashedPasswordWithSaltAppended = hashedPassword + ":" + DatatypeConverter.printBase64Binary(salt);
    	return hashedPasswordWithSaltAppended;
    }
    
    public static boolean checkPasswords(String toCheckPassword, String existingPassword) throws NoSuchAlgorithmException{
    	if(existingPassword.contains(":") && existingPassword.split(":").length == 2 
    			&& existingPassword.split(":")[0].length() == 128 && existingPassword.split(":")[1].length() == 24){
    		//neue PW Method mit hash
    		String[] splits = existingPassword.split(":");
    		String existingHash = splits[0];
    		String existingSalt = splits[1];
    		byte[] existingSaltBytes = DatatypeConverter.parseBase64Binary(existingSalt);
    		String toCheckHash = getHashedPassword(toCheckPassword, existingSaltBytes);
    		if(existingHash.equals(toCheckHash)){
    			return true;
    		}
    	} else{
    		//alte Methode
    		if(toCheckPassword.equals(existingPassword)){
    			return true;
    		}
    	}
    	return false;
    }
}
