package org.jboss.as.quickstarts.kitchensink.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.SaltGenerator;
import org.jasypt.salt.StringFixedSaltGenerator;

public class CipherUtil {
	
	private static final String myEncryptionPassword = "e83bbc86-98b6-4ae5-89df-e7b30cbe62ac";
	private static final String mySalt = "3753af4e-8160-45ba-a21f-85760cf1aece";
	
	public static String encrypt(String myText){
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndDES");
		encryptor.setPassword(myEncryptionPassword);
		SaltGenerator saltGenerator = new StringFixedSaltGenerator(mySalt);
		encryptor.setSaltGenerator(saltGenerator);

		String myEncryptedText = encryptor.encrypt(myText);
		return myEncryptedText;
	}
	
	public static String decrypt(String myEncryptedText){
		StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
        decryptor.setAlgorithm("PBEWithMD5AndDES");
		decryptor.setPassword(myEncryptionPassword);
		SaltGenerator saltGenerator = new StringFixedSaltGenerator(mySalt);
		decryptor.setSaltGenerator(saltGenerator);

		String myDecryptedText = decryptor.decrypt(myEncryptedText);
		return myDecryptedText;
	}
}
