package jp.yasukazu.transhelp;

import jp.yasukazu.transhelp.Enblock.bracketPair;
import java.util.ArrayList;
import java.util.List;

public class EnclosedArray extends ArrayList<Object> {
	private static final long serialVersionUID = 1002001L;
	bracketPair pair;
	public EnclosedArray(List<Object> list, bracketPair pair) {
		super(list);
		this.pair = pair;
	}
	public EnclosedArray(List<Object> ary) {
		super(ary);
		this.pair = bracketPair.NUL;
	}
	public EnclosedArray() {
		super();
		this.pair = bracketPair.NUL;
	}
	public bracketPair getPair() {
		return pair;
	}
	public char getBegin() {
		return getPair().getBegin();
	}
	public char getEnd() {
		return getPair().getEnd();
	}
	public void insert() {
		if (pair != bracketPair.NUL) {
			this.add(0, "" + pair.getBegin());
			this.add("" + pair.getEnd());
			pair = bracketPair.NUL;
		}
	}
	public void setPair(bracketPair pair) {
		this.pair = pair;
	}
}
