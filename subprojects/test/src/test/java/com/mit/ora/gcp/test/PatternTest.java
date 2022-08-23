/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */


package com.mit.ora.gcp.test;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.Test;


public class PatternTest {
	@Test
	public void testEmailPattern() {
//	    ArrayList<String> email = new ArrayList<>(Arrays.asList("ABC@XYZ.com","nash@jazz.com","gade@gmail.com","bolton@yahoo.com","ramsay@gmail.com","ssmsay@gmail.com"));
//	    String pattern = Pattern.getPattern(email);
//	    assertEquals("[A-Za-z]{3,6}@[A-Za-z]{3,5}.com",pattern);
	}
	
	@Test
	public void testCharsPattern() {
//	    ArrayList<String> flood = new ArrayList<>(Arrays.asList("","FL10038084A","FL99999999A","FL10038084A","FL10038084A","FL10038084A","FL10038084A","FL10038084A"));
//	    String pattern = Pattern.getPattern(flood);
//	    assertEquals("FL[0-9]{8}A",pattern);
	}

	
	@Test
	public void testNumericPattern() {
//	    ArrayList<String> label = new ArrayList<>(Arrays.asList("<=50","","=50",">=50","",">50"));
//	    String pattern = Pattern.getPattern(label);
//	    assertEquals("[<=>]{1,2}50",pattern);
	}

	
	@Test
	public void testSpecialCharsPattern() {
//	    ArrayList<String> stars = new ArrayList<>(Arrays.asList("a*b","f*&b","908*hju(~`"));
//	    String pattern = Pattern.getPattern(stars);
//	    assertEquals("[0-9a-z]{1,3}*[a-z`&(~]{1,6}",pattern);
	}

}
