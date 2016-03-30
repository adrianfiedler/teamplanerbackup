package org.jboss.as.quickstarts.kitchensink.util;

import org.jasypt.util.text.BasicTextEncryptor;

public class CipherUtil {
	
	private static final String myEncryptionPassword = "e83bbc86-98b6-4ae5-89df-e7b30cbe62ac";
	
	public static String encrypt(String myText){
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(myEncryptionPassword);
		String myEncryptedText = textEncryptor.encrypt(myText);
		return myEncryptedText;
	}
	
	public static String decrypt(String myEncryptedText){
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(myEncryptionPassword);
		String plainText = textEncryptor.decrypt(myEncryptedText);
		return plainText;
	}
}
