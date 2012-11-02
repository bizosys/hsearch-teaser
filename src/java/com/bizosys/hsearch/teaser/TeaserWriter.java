package com.bizosys.hsearch.teaser;

import java.io.IOException;
import java.util.List;

import com.bizosys.hsearch.byteutils.Storable;
import com.bizosys.hsearch.hbase.HWriter;
import com.bizosys.hsearch.teaser.filter.DocumentVO;

public class TeaserWriter {
	
	public void write( Storable partitionKey, List<DocumentVO> teasers) throws IOException {
		TeaserRecordAddBatch record = new TeaserRecordAddBatch(partitionKey.toBytes());
		record.addTeasers(teasers);
		HWriter.getInstance(true).merge(SchemaManager.TABLE_TEASER, record);
	}

	public void write( Storable partitionKey, DocumentVO teasers) throws IOException {
		TeaserRecordAdd record = new TeaserRecordAdd(partitionKey.toBytes());
		record.addTeaser(teasers);
		HWriter.getInstance(true).merge(SchemaManager.TABLE_TEASER, record);
	}

	public void deletes( Storable partitionKey, List<Integer> teaserIds) throws IOException {
		TeaserRecordDelete record = new TeaserRecordDelete(partitionKey.toBytes());
		record.addTeasers(teaserIds);
		HWriter.getInstance(true).merge(SchemaManager.TABLE_TEASER, record);
	}
}

