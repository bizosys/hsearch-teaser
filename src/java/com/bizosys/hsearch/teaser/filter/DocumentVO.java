package com.bizosys.hsearch.teaser.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.bizosys.hsearch.byteutils.SortedBytesArray;
import com.bizosys.hsearch.byteutils.SortedBytesInteger;
import com.google.protobuf.ByteString;

public class DocumentVO implements Comparator<DocumentVO>{
	public static final String EMPTY = "";
	public static final byte[] EMPTY_BYTE = new byte[0];

	public long rowId = -1;
	private int hid = -1;
	private String sid = EMPTY;
	private String url = EMPTY;
	private byte[] title = EMPTY_BYTE;
	private byte[] teaser = EMPTY_BYTE;
	private byte[] preview = EMPTY_BYTE;
	
	public DocumentVO(int hid, String sid, String url, String title , String teaser, String preview )
	throws UnsupportedEncodingException{
		
		this.hid = hid;
		this.sid = sid;
		this.url = url;
		this.title = title.getBytes("UTF-8");
		this.teaser = teaser.getBytes("UTF-8");
		this.preview = preview.getBytes("UTF-8");
	}
	
	public void modify(DocumentVO another) {
		this.hid = another.hid;
		this.sid = another.sid;
		this.url = another.url;
		this.title = another.title;
		this.teaser = another.teaser;
		this.preview = another.preview;
	}
	
	public int getHid() {
		return this.hid;
	}
	
	public String getSid() {
		return new String(this.sid);
	}
	
	public String getUrl() {
		return new String(this.url);
	}
	
	public byte[] getTitleBytes() {
		return this.title;
	}
	
	public String getTitle() {
		return new String(this.title);
	}
	
	public byte[] getTeaserBytes() {
		return this.teaser;
	}
	
	public void setTeaserBytes(byte[] teaserB) {
		this.teaser = teaserB;
	}	
	
	public String getTeaser() {
		if ( null == this.teaser) return null;
		String teaserStr = new String(this.teaser);
		return teaserStr.trim();
	}
	
	public byte[] getPreviewBytes() {
		return this.preview;
	}
	
	public String getPreview() {
		return new String(this.preview);
	}
	
	public DocumentVO() {
	}
	
	public DocumentVO(int hid, TeaserByteUtils.TeaserVO teaserB) {
		this.hid = hid;
		this.sid = teaserB.getSid();
		this.url = teaserB.getUrl();
		this.title = teaserB.getTitle().toByteArray();
		this.teaser = teaserB.getTeaser().toByteArray();
		this.preview = teaserB.getPreview().toByteArray();
	}
	
	public static byte[] toBytes(List<DocumentVO> teasersL) throws IOException {
		
		List<byte[]> _keyB_valueB_tmp = new ArrayList<byte[]>();
		List<Integer> keys = new ArrayList<Integer>(teasersL.size());
		List<byte[]> values = new ArrayList<byte[]>(teasersL.size());
		
		for (DocumentVO docVO : teasersL) {
			keys.add(docVO.hid);
			values.add(toBytes(docVO).toByteArray() );
		}
		
		_keyB_valueB_tmp.add(SortedBytesInteger.getInstance().toBytes(keys, false));
		_keyB_valueB_tmp.add(SortedBytesArray.getInstance().toBytes(values, false));
		
		return SortedBytesArray.getInstance().toBytes(_keyB_valueB_tmp, true);
	}
	
	public static ByteString toBytes(DocumentVO teaserVO) {
		TeaserByteUtils.TeaserVO.Builder aTeaserBytes = TeaserByteUtils.TeaserVO.newBuilder();
		aTeaserBytes.setSid(teaserVO.sid);
		aTeaserBytes.setUrl(teaserVO.url);
		aTeaserBytes.setTitle(ByteString.copyFrom(teaserVO.title));
		aTeaserBytes.setTeaser( ByteString.copyFrom(teaserVO.teaser));
		aTeaserBytes.setPreview(ByteString.copyFrom(teaserVO.preview));
		return aTeaserBytes.build().toByteString();
	}

	public static List<DocumentVO> fromBytes(byte[] inputData) throws IOException {
		
		byte[] keyB = SortedBytesArray.getInstance().getValueAt(inputData, 0);
		byte[] valuesB = SortedBytesArray.getInstance().getValueAt(inputData, 1);
		
		List<DocumentVO> allDocs = new ArrayList<DocumentVO>();
		int total = ( null == keyB ) ? 0 : keyB.length / 4;
		for (int index=0; index<total; index++) {
			int aKey = SortedBytesInteger.getInstance().getValueAt(keyB, index);
			byte[] aTeaserBytes = SortedBytesArray.getInstance().getValueAt(valuesB, index);
			TeaserByteUtils.TeaserVO parsedTeasers = TeaserByteUtils.TeaserVO.parseFrom(aTeaserBytes);
			if ( null == parsedTeasers) continue;
			allDocs.add(new DocumentVO(aKey, parsedTeasers) );
		}
		return allDocs;
	}	
		
	public static List<DocumentVO> fromBytesFind(byte[] inputData, List<Integer> findHids) throws Exception {
		
		byte[] keyB = SortedBytesArray.getInstance().getValueAt(inputData, 0);
		byte[] valuesB = SortedBytesArray.getInstance().getValueAt(inputData, 1);
		
		List<DocumentVO> foundTeasers = new ArrayList<DocumentVO>(findHids.size());
		
		for (Integer aHid : findHids) {
			int foundAt = SortedBytesInteger.getInstance().getEqualToIndex(keyB, aHid.intValue());
			if ( -1 == foundAt ) continue;
			byte[] aTeaserBytes = SortedBytesArray.getInstance().getValueAt(valuesB, foundAt);
			TeaserByteUtils.TeaserVO parsedTeasers = TeaserByteUtils.TeaserVO.parseFrom(aTeaserBytes);
			if ( null == parsedTeasers) continue;
			foundTeasers.add(new DocumentVO(aHid, parsedTeasers) );
		}
		
		return foundTeasers;
	}

	@Override
	public int compare(DocumentVO o1, DocumentVO o2) {
		if ( o1.hid == o2.hid) return 0;
		if ( o1.hid < o2.hid ) return -1;
		else return 1;
	}
}