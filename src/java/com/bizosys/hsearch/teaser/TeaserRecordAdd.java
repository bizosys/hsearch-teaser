package com.bizosys.hsearch.teaser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bizosys.hsearch.hbase.NV;
import com.bizosys.hsearch.hbase.Record;
import com.bizosys.hsearch.teaser.filter.DocumentVO;
import com.bizosys.hsearch.teaser.filter.TeaserFilter;

public class TeaserRecordAdd extends Record {
	
	public DocumentVO addThisDocument = null;
	
	public TeaserRecordAdd(byte[] pk) {
		super(pk);
	}
	
	public void addTeaser(DocumentVO teaser) {
		this.addThisDocument = teaser;
	}
	
	@Override
	public List<NV> getNVs() throws IOException {
		if ( this.nvs.size() == 0 ) {
			List<DocumentVO> finalDocs = new ArrayList<DocumentVO>(1);
			finalDocs.add(addThisDocument);
			NV nv = new NV(TeaserFilter.TEASER_FAMILY_BYTES,
				TeaserFilter.TEASER_COLUMN_BYTES, DocumentVO.toBytes(finalDocs));
			super.getNVs().add(nv);			
		}
		
		return this.nvs;
	}	

	@Override
	public void merge(byte[] fam, byte[] name, byte[] data) throws IOException {
		
		if ( TeaserFilter.TEASER_COLUMN_CHAR1 != fam[0] ) return;
		if ( TeaserFilter.TEASER_COLUMN_CHAR1 != name[0] ) return;
		
		List<DocumentVO> allDocs = ( null == data ) ? new ArrayList<DocumentVO>(1) : DocumentVO.fromBytes(data);
		DocumentVO deleteDoc = null;
		for (DocumentVO documentVO : allDocs) {
			if ( documentVO.getHid() == addThisDocument.getHid()) {
				deleteDoc =  documentVO;
				break;
			}
		}
		
		//Is it already existing, remove It?
		if ( null != deleteDoc ) allDocs.remove(deleteDoc);
		
		allDocs.add(this.addThisDocument);
		
		Collections.sort(allDocs, new DocumentVO());
		
		NV nv = new NV(fam,name );
		nv.data = DocumentVO.toBytes(allDocs);
		super.getNVs().add(nv);

		System.out.println("Final documents Packed : " + allDocs.size() + " , of size : " + nv.data.length);
		
	}
	
}
