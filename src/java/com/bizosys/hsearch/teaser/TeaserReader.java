package com.bizosys.hsearch.teaser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bizosys.hsearch.byteutils.Storable;
import com.bizosys.hsearch.hbase.ColumnFamName;
import com.bizosys.hsearch.hbase.HReader;
import com.bizosys.hsearch.teaser.filter.DocumentVO;
import com.bizosys.hsearch.teaser.filter.TeaserFilter;

public class TeaserReader {
	
	public static List<DocumentVO> read( Storable partition,  List<Integer> hids, List<String> matchingWords, int teaserLength ) throws IOException {
		
		List<byte[]> matchingWordsB = new ArrayList<byte[]>(matchingWords.size());
		for (String aWord : matchingWords) {
			matchingWordsB.add(aWord.getBytes());
		}
		
		TeaserFilter filter = new TeaserFilter(hids,matchingWordsB, teaserLength);
		/**
		HReader.getAllValues(SchemaManager.TABLE_TEASER, TeaserFilter.TEASER_FAMILY_BYTES,  
			TeaserFilter.TEASER_COLUMN_BYTES, filter, new ProcessRow());
		*/

		ProcessRow processRow = new ProcessRow();
		byte[] pkB = partition.toBytes();
		byte[] out = HReader.getScalar(SchemaManager.TABLE_TEASER, TeaserFilter.TEASER_FAMILY_BYTES,  
				TeaserFilter.TEASER_COLUMN_BYTES, pkB, filter);
		ColumnFamName cfn = new ColumnFamName(TeaserFilter.TEASER_FAMILY_BYTES,  TeaserFilter.TEASER_COLUMN_BYTES);
		processRow.process(pkB, cfn, out);
		return processRow.teasers;
		
	}

}

