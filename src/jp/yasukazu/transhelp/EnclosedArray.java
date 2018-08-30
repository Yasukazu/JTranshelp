package jp.yasukazu.transhelp;

import java.util.ArrayList;
import java.util.List;

public class EnclosedArray extends ArrayList<Object> {
	private static final long serialVersionUID = 1002001L;
	Enblock.bracketPair pair;
	public EnclosedArray(List<Object> list, Enblock.bracketPair pair) {
		super(list);
		this.pair = pair;
	}
	public EnclosedArray(List<Object> ary) {
		super(ary);
		this.pair = Enblock.bracketPair.NUL;
	}
	public EnclosedArray() {
		super();
		this.pair = Enblock.bracketPair.NUL;
	}
	public Enblock.bracketPair getPair() {
		return pair;
	}
	public char getBegin() {
		return getPair().getBegin();
	}
	public char getEnd() {
		return getPair().getEnd();
	}
	public void insert() {
		if (pair != Enblock.bracketPair.NUL) {
			this.add(0, "" + pair.getBegin());
			this.add("" + pair.getEnd());
			pair = Enblock.bracketPair.NUL;
		}
	}
	public void setPair(Enblock.bracketPair pair) {
		this.pair = pair;
	}
}
