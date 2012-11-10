package com.bizosys.hsearch.teaser;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestFerrari;

import com.bizosys.hsearch.byteutils.Storable;
import com.bizosys.hsearch.teaser.filter.DocumentVO;
import com.oneline.ferrari.TestAll;

public class TeaserWriterTest extends TestCase {
	public static String[] modes = new String[] { "all", "random", "method"};
	public static String mode = modes[1];  
	
	public static void main(String[] args) throws Exception {
		TeaserWriterTest t = new TeaserWriterTest();
		
		if ( modes[0].equals(mode) ) {
			TestAll.run(new TestCase[]{t});
		} else if  ( modes[1].equals(mode) ) {
	        TestFerrari.testRandom(t);
	        
		} else if  ( modes[2].equals(mode) ) {
			t.setUp();
			t.testRandomHindiwords();
			t.tearDown();
		}
	}

	@Override
	protected void setUp() throws Exception {
	}
	
	@Override
	protected void tearDown() throws Exception {
	}
	

	private void testAddDocument(long partition, int hid, String id, String url, String title, String teaser, String preview) throws Exception {
		TeaserWriter tw = new TeaserWriter();
		DocumentVO tvo = new DocumentVO(hid, id,url, title, teaser, preview);
		tw.write(new Storable(partition), tvo);
	}
	
	/**
	public void testCheckOneRecord() throws Exception {
		testA(1L, 1, "id1", "http://www.one.com", "This is One Title", "One starts from one and extends to infinite", "No Preview");
		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(1);
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("starts");		
		TeaserReader.read(new Storable(1L), hids, matchingWords, 40);
	}
	*/

	public void testCheckTwoRecord() throws Exception {
		
		Long bucketId = 6L;
		testAddDocument(bucketId, 1, "x1", "http://www.one.com", 
				"This is One Title", ".One starts from one and extends to infinite. However donkies are cows and cows are dogs.Large kala Lala Mala Ramchik pksdfkjd dsfdkjs dsjfhdkjs sdjfhdskj sdfdshf dsfdshfdshfh  shfdshjf j sdfdshf dsjhf sd sdhfdsjhf dsjfgdshfgdsj f END", "No Preview...");
		testAddDocument(bucketId, 2, "x2", "http://www.one.com", "This is two Title", "One starts from two and extends to infinite are yy ttt.....", "No Preview");
		testAddDocument(bucketId, 3, "x3", "http://www.one.com", "This is third Title :- what's your name ???" ,"  how are you ", "No Answer YEt...." );

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("are");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(1);hids.add(2);hids.add(3);
		List<DocumentVO> founds = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30); 
		
		for (DocumentVO documentVO : founds) {
			System.out.println(documentVO.getTeaser());
		}
	}
	

	
	/*public void testCheckTwoRecordSameId() throws Exception {
		testA(9L, 1, "x1", "http://www.one.com", "This is One Title", "One starts from one and extends to infinite", "No Preview");
		testA(9L, 1, "x2", "http://www.one.com", "This is One Title", "One starts from one and extends to infinite", "No Preview");
	}

	public void testCheckTwoRecordSameIdDifferentPart() throws Exception {
		testA(9L, 1, "x1", "http://www.one.com", "This is One Title", "One starts from one and extends to infinite", "No Preview");
		testA(10L, 1, "x2", "http://www.one.com", "This is One Title", "One starts from one and extends to infinite", "No Preview");
	}*/

	
	public void testSpecialChar() throws Exception {
		Long bucketId = 16L;
		testAddDocument(bucketId, 1, "s1", "http://www.one.com", 
				"This is Special character Title", ".Las Vegas is a sin!#^& City. asd asd sad asd asdsaasd asda ssdsa asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("sin!#^&");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(1);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals("Las Vegas is a sin!#^& City. asd asd sad asd asdsaasd asda ssdsa", aTeaser);
	}
	
	public void testKeyword() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City asd asd asd sad asd asdsaaasd asda ssdsa";
		testAddDocument(bucketId, 2, "s2", "http://www.one.com", 
				"This is  keyword.", "." + inputString + " asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("sad");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(2);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		System.out.println( " Result :" + aTeaser );
		assertEquals(inputString, aTeaser);
	}
	
	//two words//
	public void testTwoWords() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City . asd asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 3, "s3", "http://www.one.com", 
				"This is  two words", "." + inputString + " asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("is a");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(3);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	
	// three words key search //
	public void testThreeWords() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City. asd asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 4, "s4", "http://www.one.com", 
				"This is  three words key search", "." + inputString + " asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("asd asd asd");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(4);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	//random words//
	public void testRandom() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City. asd asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 5, "s5", "http://www.one.com", 
				"This is  Random words", "." + inputString + " asd ", "No Preview...");
		
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("asd asdsaasd");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(5);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}

	
	//double words//
	public void testDoubleWords() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City. asd asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 6, "s6", "http://www.one.com", 
				"This is  Double word", "." + inputString + " asd ", "No Preview...");
		

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("asd asd");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(6);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	//three same word contd//
	public void testThreeWordsCount() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City. asd asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 7, "s7", "http://www.one.com", 
				"This is  three words contd..", "." + inputString + " asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("asd asd asd");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(7);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	//multiple words//
	public void testMultiple() throws Exception {
		Long bucketId = 16L;
		testAddDocument(bucketId, 8, "s8", "http://www.one.com", 
				"This is  multiple word been used..", ".Las Vegas is a sin!#^& City. asd asd asd sad asd asdsaasd asda ssdsa asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("is");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(8);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals("Las Vegas is a sin!#^& City. asd asd asd sad asd asdsaasd asda ssdsa", aTeaser);
	}
	
	// out of the box word search//
	public void testNoWord() throws Exception {
		Long bucketId = 16L;
		testAddDocument(bucketId, 9, "s9", "http://www.one.com", 
				"This is  no word ..", ".Las Vegas is a sin!#^& City. asd asd asd sad asd asdsaasd asda ssdsa asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("GoD");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(9);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals("", aTeaser);
	}
	
	public void testBeginOfWords() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City. asd asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 10, "s10", "http://www.one.com", 
				"This is  begin of word", "." + inputString + " asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("Las");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(10);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	public void testEndOfWords() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City asd asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 11, "s11", "http://www.one.com", 
				"This is test end of word..", "." + inputString + " asd ", "No Preview...");
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("asd");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(11);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	public void testCapsWords() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City asd asd HELLO asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 12, "s12", "http://www.one.com", 
				"This is test end of word..", "." + inputString + " asd ", "No Preview...");
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("HELLO");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(12);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	public void testSmallWords() throws Exception {
		Long bucketId = 16L;
		String inputString = "Las Vegas is a sin!#^& City asd hello asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 13, "s13", "http://www.one.com", 
				"This is test end of word..", "." + inputString + " asd ", "No Preview...");
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("hello");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(13);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	public void testsmallFWords() throws Exception {
		Long bucketId = 16L;
		String inputString = "lAS Vegas is a sin!#^& City asd hello asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 14, "s14", "http://www.one.com", 
				"This is test end of word..", "." + inputString + " asd ", "No Preview...");
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("lAS");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(14);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
		public void testFirstWordsLastCaps() throws Exception {
		Long bucketId = 16L;
		String inputString = "laS Vegas is a sin!#^& City asd hello asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 16, "s16", "http://www.one.com", 
				"This is test end of word..", "." + inputString + " asd ", "No Preview...");
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("laS");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(16);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		assertEquals(inputString, aTeaser);
	}
	
	public void testLastLWordsCaps() throws Exception {
		Long bucketId = 16L;
		String inputString = "तुम्हारा नाम क्या है ? Las Vegas is a sin!#^& City.  asd hello  asd asd sad asd asdsaasd asda ssdsa";
		testAddDocument(bucketId, 50, "s10", "http://www.one.com", 
				"This is  begin of word", "." + inputString + " asd ", "No Preview...");

		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("ssdsa");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(50);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 50);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
			for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}

		System.out.println("aTeaser : [" + aTeaser + "]");
		System.out.println("inputString : [" + inputString + "]");
		assertEquals("asd hello  asd asd sad asd asdsaasd asda ssdsa", aTeaser);
		}

	
	public void testFirstHindiwords() throws Exception {
		Long bucketId = 16L;
		String inputString = "तुम्हारा नाम क्या है ? Las Vegas is a sin!#^& City.  asd hello  asd asd sad asd asdsaasd asda ssdsA";
		
		testAddDocument(bucketId, 30, "s19", "http://www.one.com", 
				"This is test end of word..", "the " + inputString + "ASD", "No Preview...");
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("तुम्हारा");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(30);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		System.out.println(teasers.size());
		assertEquals(1, teasers.size());
		
	    String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		System.out.println("Found Teaser : " + aTeaser);
		System.out.println("aTeaser : [" + aTeaser + "]");
		System.out.println("inputString : [" + inputString + "]");
		assertEquals("तुम्हारा नाम क्या है ? Las Vegas is a sin!#^& City.  asd hello", aTeaser);
		}
	
	
	public void testLastHindiwords() throws Exception {
		Long bucketId = 16L;
		String inputString = "तुम्हारा नाम क्या है ? Las Vegas is a sin!#^& City.  asd hello  asd asd sad asd asdsaasd asda ssdsA";
		testAddDocument(bucketId, 25 , "s20", "http://www.one.com", 
				"This is test end of word..", "The" + inputString + " asd ", "No Preview...");
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("क्या");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(25);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		System.out.println("aTeaser : [" + aTeaser + "]");
		System.out.println("inputString : [" + inputString + "]");
		assertEquals("नाम क्या है ? Las Vegas is a sin!#^& City.  asd hello  asd asd sad asd asdsaasd", aTeaser);
	}
	
	public void testRandomHindiwords() throws Exception {
		Long bucketId = 16L;
		String inputString = "तुम्हारा नाम क्या है ? Las Vegas is a sin!#^& City.  asd hello  asd asd sad asd asdsaasd asda ssdsA";
		testAddDocument(bucketId, 40 , "s21", "http://www.one.com", 
				"This is test end of word..", "." + inputString + " asd ", "No Preview...");
		List<String> matchingWords = new ArrayList<String>();
		matchingWords.add("नाम");		
		List<Integer> hids = new ArrayList<Integer>();
		hids.add(40);
		List<DocumentVO> teasers = TeaserReader.read(new Storable(bucketId), hids, matchingWords, 30);
		assertNotNull("Expecting one result", teasers);
		assertEquals(1, teasers.size());
		
		String aTeaser = null;
		for (DocumentVO vo : teasers) {
			aTeaser = vo.getTeaser();
		}
		System.out.println("aTeaser : [" + aTeaser + "]");
		System.out.println("inputString : [" + inputString + "]");
		assertEquals("तुम्हारा नाम क्या है ? Las Vegas is a sin!#^& City.  asd hello", aTeaser);
	}
	
}
