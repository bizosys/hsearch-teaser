package com.bizosys.hsearch.teaser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestFerrari;

import com.bizosys.hsearch.byteutils.ByteUtil;
import com.bizosys.hsearch.teaser.filter.TeaserByteUtils;
import com.google.protobuf.ByteString;
import com.oneline.ferrari.TestAll;

public class TestSerialization extends TestCase {
	public static String[] modes = new String[] { "all", "random", "method"};
	public static String mode = modes[2];  
	
	public static void main(String[] args) throws Exception {
		TestSerialization t = new TestSerialization();
		
		if ( modes[0].equals(mode) ) {
			TestAll.run(new TestCase[]{t});
		} else if  ( modes[1].equals(mode) ) {
	        TestFerrari.testRandom(t);
	        
		} else if  ( modes[2].equals(mode) ) {
			t.setUp();
			t.testSerialization();
			t.tearDown();
		}
	}

	@Override
	protected void setUp() throws Exception {
	}
	
	@Override
	protected void tearDown() throws Exception {
	}
	
	public void testSerialization() throws Exception {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean isWrite = true;
		if (isWrite) {
			TeaserByteUtils.FindTeaser.Builder builder = TeaserByteUtils.FindTeaser.newBuilder();
			builder.setTeaserLength(100);
			for (Integer aHid : new int[]{11,12,13,14,15}) {
				builder.addHids(aHid);
			}
			
			for (String aWord : new String[]{"abinash", "karan"} ) {
				builder.addMatchWords(ByteString.copyFrom(aWord.getBytes()));
			}

			byte[] ser = builder.build().toByteArray();
			int len = ser.length;
			out.write(ByteUtil.toBytes(len));
			out.write(ser, 0, len);
			out.flush();
			System.out.println("Written bytes :" + out.size());

			out.close();
		}

		boolean isRead = true;
		if ( isRead ) {
			byte[] outB = out.toByteArray();
			System.out.println(outB.length);
			ByteArrayInputStream is = new ByteArrayInputStream(outB);
			BufferedInputStream bis=new BufferedInputStream(is);
			System.out.println("Output Length : " + is.available());
			
			DataInput in = new DataInputStream(bis);
			
			int length = in.readInt();
			System.out.println("Input Length : " + length);
			
			byte[] ser = new byte[length];
			
			System.out.println(ser.length + "== " + length);
			in.readFully(ser, 0, length);
			TeaserByteUtils.FindTeaser findTeaser = TeaserByteUtils.FindTeaser.parseFrom(ser);
			List<Integer> hids = findTeaser.getHidsList();
			List<ByteString> matchWords = findTeaser.getMatchWordsList();
			int teaserLength = findTeaser.getTeaserLength();
			
			System.out.println("hids:" + hids.toString());
			System.out.println("matchWords:" + matchWords.toString());
			System.out.println("teaserLength:" + teaserLength);
		}
		
		
	}
}
