package com.bizosys.hsearch.teaser;

import java.util.List;
import java.util.Vector;

import com.bizosys.hsearch.byteutils.Storable;
import com.bizosys.hsearch.hbase.ColumnFamName;
import com.bizosys.hsearch.hbase.IScanCallBack;
import com.bizosys.hsearch.teaser.filter.DocumentVO;

public class ProcessRow implements IScanCallBack {
	public List<DocumentVO> teasers = new Vector<DocumentVO>();
	
	@Override
	public void process(byte[] pk, ColumnFamName colName, byte[] storedBytes) {
		
		try {
			List<DocumentVO> results =  DocumentVO.fromBytes(storedBytes);
			if ( null != results) {
				for (DocumentVO documentVO : results) {
					documentVO.rowId = Storable.getLong(0, pk);
				}
			}
			teasers.addAll(results);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		

	}

}
