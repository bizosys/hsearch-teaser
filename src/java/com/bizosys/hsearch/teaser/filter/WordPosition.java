package com.bizosys.hsearch.teaser.filter;

/**
 * Carries sighting information of a word inside the content
 * @author karan
 *
 */
public class WordPosition {
	
	/**
	 * Query keyword position E.g. (abinash karan hbase = 0,1,2)
	 */
	public int index;
	
	/**
	 * Start position of the word in the given corpus
	 */
	public int start;
	
	/**
	 * End position start position + word length
	 */
	public int end;
	
	/**
	 * Default constrctor
	 * @param index	Query keyword position
	 * @param start	Start position of the word
	 * @param end	End position
	 */
	public WordPosition(int index, int start, int end) {
		this.index = index;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String toString() {
		return "index:" + index + ", start:" + start + ", end:" + end;
	}
}
