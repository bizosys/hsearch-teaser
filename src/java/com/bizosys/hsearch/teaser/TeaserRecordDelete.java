package com.bizosys.hsearch.teaser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bizosys.hsearch.byteutils.SortedBytesArray;
import com.bizosys.hsearch.byteutils.SortedBytesInteger;
import com.bizosys.hsearch.hbase.NV;
import com.bizosys.hsearch.hbase.Record;
import com.bizosys.hsearch.teaser.filter.TeaserFilter;

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
		
		byte[] keyB = SortedBytesArray.getInstance().getValueAt(data, 0);
		byte[] valuesB = SortedBytesArray.getInstance().getValueAt(data, 1);
		
		if ( null == keyB) return;
		int totalKeys = keyB.length/4;
		if ( 0 == totalKeys) return;
		
		List<Integer> finalKeys = new ArrayList<Integer>();
		List<byte[]> finalValues = new ArrayList<byte[]>();
		for ( int index=0; index <totalKeys; index++) {
			int aKey = SortedBytesInteger.getInstance().getValueAt(keyB, index);
			if ( deletes.contains(aKey)) continue;
			finalKeys.add(aKey);
			finalValues.add(SortedBytesArray.getInstance().getValueAt(valuesB, index));
		}
		
		List<byte[]> _keyB_valueB_tmp = new ArrayList<byte[]>();
		_keyB_valueB_tmp.add(SortedBytesInteger.getInstance().toBytes(finalKeys, true));
		_keyB_valueB_tmp.add(SortedBytesArray.getInstance().toBytes(finalValues, true));
		byte[] changedBytes = SortedBytesArray.getInstance().toBytes(_keyB_valueB_tmp, true);

		NV nv = new NV(fam,name );
		nv.data = changedBytes;
		super.getNVs().add(nv);
	}
}
