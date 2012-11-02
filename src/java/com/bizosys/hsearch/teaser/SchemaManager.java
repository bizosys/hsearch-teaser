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
package com.bizosys.hsearch.teaser;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.apache.hadoop.hbase.regionserver.StoreFile;
import org.apache.log4j.Logger;

import com.bizosys.hsearch.hbase.HDML;
import com.bizosys.hsearch.teaser.filter.TeaserFilter;

/**
 * It is a facade for schema related opeations.
 * This schema creates in pristine mode 154 directories and 378 files.
 * @author karan
 *
 */
public class SchemaManager {
	
	private static SchemaManager instance = null;
	public static Logger l = Logger.getLogger(SchemaManager.class.getName());
	
	public static final SchemaManager getInstance() {
		if ( null != instance) return instance;
		synchronized (SchemaManager.class) {
			if ( null != instance) return instance;
			instance = new SchemaManager();
		}
		return instance;
	}
	
	/**
	 * Default constructor
	 *
	 */
	public SchemaManager(){
	}
	
	private static final String NO_COMPRESSION = Compression.Algorithm.NONE.getName();

	private String teaserCompression = NO_COMPRESSION;	
	private boolean teaserBlockCache = true;	
	private int teaserBlockSize = HColumnDescriptor.DEFAULT_BLOCKSIZE;	
	private String teaserBloomFilter = StoreFile.BloomType.NONE.toString();;	
	private int teaserRepMode = HConstants.REPLICATION_SCOPE_GLOBAL;;	

	public static final String TABLE_TEASER = "teaser";
	
	/**
	 * Checks and Creates all necessary tables required for HSearch index.
	 */
	public boolean init() {

		this.teaserCompression = resolveCompression("gz");

		try {
			
			//int rev = conf.getInt("record.revision",1);
			List<HColumnDescriptor> colFamilies = new ArrayList<HColumnDescriptor>();
			
			HColumnDescriptor teaser = 
				new HColumnDescriptor( TeaserFilter.TEASER_COLUMN_BYTES,
					1, teaserCompression, 
					false, teaserBlockCache,
					teaserBlockSize,					
					HConstants.FOREVER, 
					teaserBloomFilter,
					teaserRepMode);

			colFamilies.add(teaser);
			HDML.create(TABLE_TEASER, colFamilies);
			
			
			return true;
			
		} catch (Exception sf) {
			sf.printStackTrace(System.err);
			l.fatal(sf);
			return false;
		} 
	}

	/**
	 * Compression method to HBase compression code.
	 * @param methodName
	 * @return
	 */
	private static String resolveCompression(String methodName) {
		String compClazz =  Compression.Algorithm.GZ.getName();
		if ("gz".equals(methodName)) {
			compClazz = Compression.Algorithm.GZ.getName();
		} else if ("lzo".equals(methodName)) {
			compClazz = Compression.Algorithm.LZO.getName();
		} else if ("none".equals(methodName)) {
			compClazz = Compression.Algorithm.NONE.getName();
		}
		return compClazz;
	}
	
	public static void main(String[] args) throws Exception {
		new SchemaManager().init();
	}
	
}