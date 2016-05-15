package org.jboss.as.quickstarts.kitchensink.util;

import java.util.logging.Logger;

import javax.inject.Inject;

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
}
