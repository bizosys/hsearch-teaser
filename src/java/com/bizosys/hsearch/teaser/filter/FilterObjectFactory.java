package com.bizosys.hsearch.teaser.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FilterObjectFactory {
	private static FilterObjectFactory thisInstance = new FilterObjectFactory();
	public static FilterObjectFactory getInstance() {
		return thisInstance;
	}
	
	private static int MINIMUM_CACHE = 10;
	private static int MAXIMUM_CACHE = 4096;

	Stack<List<WordPosition>> wordPositions =  new Stack<List<WordPosition>>();
	
	public  List<WordPosition> getWordPosition() {
		List<WordPosition> entry = null;
		if (wordPositions.size() > MINIMUM_CACHE ) entry = wordPositions.pop();
		if ( null != entry ) return entry;
		return new ArrayList<WordPosition>();
	}
	
	public  void putWordPosition(List<WordPosition> entry ) {
		if ( null == entry) return;
		entry.clear();
		if (wordPositions.size() > MAXIMUM_CACHE ) return;
		if (wordPositions.contains(entry) ) return;
		wordPositions.add(entry);
	}			
}
