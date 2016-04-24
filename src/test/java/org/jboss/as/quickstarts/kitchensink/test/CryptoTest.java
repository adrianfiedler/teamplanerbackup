/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.kitchensink.test;

import static org.junit.Assert.assertEquals;

import org.jboss.as.quickstarts.kitchensink.util.CipherUtil;
import org.junit.Test;

public class CryptoTest {

    @Test
    public void testRegister() throws Exception {
    	String text = "OmVRTrVqzZ?8Rh4CTN3Pqq!xuldZx6G8Q22rKrgBYAA=";
    	String cipher1 = CipherUtil.encrypt(text);
    	String cipher2 = CipherUtil.encrypt(text);
    	assertEquals(cipher1, cipher2);
    	
    	String decrypt1 = CipherUtil.decrypt(cipher1);
    	String decrypt2 = CipherUtil.decrypt(cipher2);
    	assertEquals(decrypt1, decrypt2);
    	
    	String jule = CipherUtil.encrypt("Jule");
    	String adrian = CipherUtil.encrypt("Adrian");
    	String fiedler = CipherUtil.encrypt("Fiedler");
    	
    	assertEquals(text, decrypt1);
    }

}
