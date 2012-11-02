package com.bizosys.hsearch.teaser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bizosys.hsearch.hbase.NV;
import com.bizosys.hsearch.hbase.Record;
import com.bizosys.hsearch.teaser.filter.DocumentVO;
import com.bizosys.hsearch.teaser.filter.TeaserFilter;

public class TeaserRecordAddBatch extends Record {
	
	Map<Integer, DocumentVO> addDocuments = null;

	public TeaserRecordAddBatch(byte[] pk) {
		super(pk);
	}
	
	public void addTeasers(List<DocumentVO> newDocs) {
		this.addDocuments = new HashMap<Integer, DocumentVO>();
		for (DocumentVO documentVO : newDocs) {
			this.addDocuments.put(documentVO.getHid(), documentVO);
		}
	}
	
	@Override
	public List<NV> getNVs() throws IOException {
		
		if ( this.nvs.size() == 0 ) {
			List<DocumentVO> finalDocs = new ArrayList<DocumentVO>(1);
			finalDocs.addAll(addDocuments.values());
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
		
		for (DocumentVO existingDoc : allDocs) {
			int existingHid = existingDoc.getHid();
			
			//Modify exisitng docs
			if (addDocuments.containsKey(existingHid)) {
				DocumentVO newDoc = addDocuments.get(existingHid);
				existingDoc.modify(newDoc);
				addDocuments.remove(existingHid);
			}
			
			allDocs.add(existingDoc);
		}
		
		//Add remaining new docs
		allDocs.addAll(addDocuments.values());
		
		Collections.sort(allDocs, new DocumentVO());
		
		NV nv = new NV(fam,name );
		nv.data = DocumentVO.toBytes(allDocs);
		super.getNVs().add(nv);

		System.out.println("Final documents Packed : " + allDocs.size() + " , of size : " + nv.data.length);
	}
	
}
