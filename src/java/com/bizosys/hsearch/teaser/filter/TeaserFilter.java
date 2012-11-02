/*
* Copyright 2010 Bizosys Technologies Limited
*
* Licensed to the Bizosys Technologies Limited (Bizosys) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The Bizosys licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.bizosys.hsearch.teaser.filter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;

import com.google.protobuf.ByteString;

/**
 * Sending the complete document over the wire may Jam the network on a 
 * heavy concurrent user base. This filter ensures sending the most
 * relevant section only. It also uses multiple Region servers to create
 * the teasers to serve a search request.
 * @author karan
 */
public class TeaserFilter implements Filter {
	public static final String TEASER_COLUMN = "T";
	public static final byte[] TEASER_COLUMN_BYTES = TEASER_COLUMN.getBytes();
	public static final byte[] TEASER_FAMILY_BYTES = TEASER_COLUMN.getBytes();
	public static final byte TEASER_COLUMN_CHAR1 = "T".getBytes()[0];
	
	/**
	 * Default teaser section length
	 */
	int teaserLength = 360;
	
	/**
	 * Searched words
	 */
	List<ByteString> matchWords = null;
	byte[][] matchWordsB = null;

	/**
	 * Searched words
	 */
	List<Integer> hids = null;

	/**
	 * Default constructor
	 *
	 */
	public TeaserFilter(){}
	
	/**
	 * Constructor
	 * @param bWords	Searched words
	 * @param cutLength	Teaser section length
	 */
	public TeaserFilter(List<Integer> hids, List<byte[]> matchingWords, int teaserLength){
		this.hids = hids;
		
		this.matchWords = new ArrayList<ByteString>();
		for (byte[] aWord : matchingWords) {
			this.matchWords.add(ByteString.copyFrom(aWord));
		}
		this.teaserLength = teaserLength;
	}
	
	public boolean filterAllRemaining() {
		return false;
	}

	public boolean filterRow() {
		return false;
	}

	/**
	 * last chance to drop entire row based on the sequence of filterValue() 
	 * calls. Eg: filter a row if it doesn't contain a specified column
	 */
	@Override
	public void filterRow(List<KeyValue> kvL) {

		if ( null == kvL) return;
		int kvT = kvL.size();
		if ( 0 == kvT) return;
		
		KeyValue kv = null;
		List<DocumentVO> teasers = null;
		
		Iterator<KeyValue> kvItr = kvL.iterator();
		
		for ( int i=0; i< kvT; i++ ) {
			kv = kvItr.next();
			if (TEASER_COLUMN_CHAR1 != kv.getFamily()[0]) continue;
			
			byte[] inputData = kv.getValue();
			if ( null == inputData) return;
			if (inputData.length == 0) return;
			
			try {
				teasers = DocumentVO.fromBytesFind(inputData, this.hids);
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				
			}
			if ( null == teasers) {
				System.out.println("No teasers found. Returning");
				return;
			}
			
			TeaserExtractor extractor = new TeaserExtractor(this.matchWordsB);
			for (DocumentVO teaserVO : teasers) {
				extractor.setContent(teaserVO.getTeaserBytes(), 0, -1);
				byte[] section = extractor.find(100);
				System.out.println("The Section Selected: " + new String(section));
				teaserVO.setTeaserBytes(section);
			}
			System.out.println("Total teasers processed : " + teasers.size());
			
			break;
		}
		
		if ( null == teasers) return;
		
		kvL.clear();
		
		try {
			kvL.add(new KeyValue(kv.getRow(), kv.getFamily(), kv.getQualifier(), DocumentVO.toBytes(teasers)) );
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	/**
	 * true to drop this row, if false, we will also call
	 */
	public boolean filterRowKey(byte[] rowKey, int offset, int length) {
		return false;
	}
	
	public KeyValue getNextKeyHint(KeyValue arg0) {
		return null;
	}
	
	public boolean hasFilterRow() {
		return true;
	}
	
	public void reset() {
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int length = in.readInt();
		System.out.println("TeaserFilter Length :" + length);
		byte[] ser = new byte[length];
		in.readFully(ser, 0, length);
		TeaserByteUtils.FindTeaser findTeaser = TeaserByteUtils.FindTeaser.parseFrom(ser);
		this.hids = findTeaser.getHidsList();
		this.matchWords = findTeaser.getMatchWordsList();
		this.teaserLength = findTeaser.getTeaserLength();
		
		this.matchWordsB = new byte [this.matchWords.size()][];
		int index = 0;
		for (ByteString aWord : this.matchWords) {
			this.matchWordsB[index] = aWord.toByteArray();
			index++;
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		TeaserByteUtils.FindTeaser.Builder builder = TeaserByteUtils.FindTeaser.newBuilder();
		builder.setTeaserLength(this.teaserLength);
		for (Integer aHid : this.hids) {
			builder.addHids(aHid);
		}
		
		for (ByteString aWord : this.matchWords) {
			builder.addMatchWords(aWord);
		}
		byte[] ser = builder.build().toByteArray();
		out.writeInt(ser.length);
		out.write(ser);
	}

	public ReturnCode filterKeyValue(KeyValue arg0) {
		return ReturnCode.INCLUDE;
	}
}
