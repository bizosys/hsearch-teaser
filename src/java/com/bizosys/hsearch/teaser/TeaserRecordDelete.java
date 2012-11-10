package com.bizosys.hsearch.teaser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bizosys.hsearch.byteutils.ISortedByte;
import com.bizosys.hsearch.byteutils.SortedBytesArray;
import com.bizosys.hsearch.byteutils.SortedBytesInteger;
import com.bizosys.hsearch.hbase.NV;
import com.bizosys.hsearch.hbase.Record;
import com.bizosys.hsearch.teaser.filter.TeaserFilter;
import com.bizosys.hsearch.treetable.CellBase;

public class TeaserRecordDelete extends Record {
	
	public Set<Integer> deletes = new HashSet<Integer>();
	
	public TeaserRecordDelete(byte[] pk) {
		super(pk);
	}
	
	public void addTeasers(List<Integer> deleteList) {
		this.deletes.addAll(deleteList);
	}
	
	@Override
	public void merge(byte[] fam, byte[] name, byte[] data) throws IOException {
		
		if ( TeaserFilter.TEASER_COLUMN_CHAR1 != fam[0] ) return;
		if ( TeaserFilter.TEASER_COLUMN_CHAR1 != name[0] ) return;
		
		ISortedByte<byte[]> sba = SortedBytesArray.getInstance().parse(data);
		byte[] keyB = sba.getValueAt(0);
		byte[] valuesB = sba.getValueAt(1);
		
		if ( null == keyB) return;
		int totalKeys = keyB.length/4;
		if ( 0 == totalKeys) return;
		
		List<Integer> finalKeys = new ArrayList<Integer>();
		List<byte[]> finalValues = new ArrayList<byte[]>();
		
		ISortedByte<Integer> keysC = SortedBytesInteger.getInstance().parse(keyB);
		ISortedByte<byte[]> valuesC = SortedBytesArray.getInstance().parse(valuesB);
		for ( int index=0; index <totalKeys; index++) {
			int aKey = keysC.getValueAt(index);
			if ( deletes.contains(aKey)) continue;
			
			finalKeys.add(aKey);
			finalValues.add(valuesC.getValueAt(index));
		}
		
		byte[] changedBytes = CellBase.serializeKV(SortedBytesInteger.getInstance().toBytes(finalKeys),
			SortedBytesArray.getInstance().toBytes(finalValues));
		finalKeys.clear();
		finalValues.clear();
		
		super.getNVs().add( new NV(fam,name, changedBytes) );
	}
}
