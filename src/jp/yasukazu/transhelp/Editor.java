package jp.yasukazu.transhelp;
// 2018/8/30 YtM @ yasukazu.jp
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.yasukazu.transhelp.Transhelp.punct;
import jp.yasukazu.transhelp.Enblock.bracketPair;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class Editor extends ArrayList<Object> {
	  public enum cmdEnum {
			REVERSE('/', new Reverse(), '\uff0f'),
		    ;
		  Cmd cmd;	  
		  char ch;
		  char wch;
		    cmdEnum(char ch, Cmd cmd, char wch){
		      this.ch = ch;
		      this.cmd = cmd;
		      this.wch = wch;
		    }
		    public char getChar(){
		      return this.ch;
		    }
		    public Cmd getCmd() {
		    	return cmd;
		    }
	  }
	  static Set<Character> cmdCharSet;
	  static String cmdCharStr;
	static Map<Character, Cmd> cmdKeyMap;
	static Map<Character, Character> cmdCharMap;
	static Map<Character, cmdEnum> charCmdEnumMap;
	static {
		cmdCharSet = new HashSet<Character>();
		EnumSet.allOf(cmdEnum.class).forEach(it -> {
			cmdCharSet.add(it.ch);
			cmdCharSet.add(it.wch);
		});
		cmdKeyMap = new HashMap<Character, Cmd>();
		EnumSet.allOf(cmdEnum.class).forEach(it -> {
			cmdKeyMap.put(it.ch, it.cmd);
		});
		cmdCharMap = new HashMap<Character, Character>();
		EnumSet.allOf(cmdEnum.class).forEach(it -> {
			cmdCharMap.put(it.wch, it.ch);
			cmdCharMap.put(it.ch, it.ch);
		});
		charCmdEnumMap = new HashMap<Character, cmdEnum>();
		EnumSet.allOf(cmdEnum.class).forEach(it -> {
			charCmdEnumMap.put(it.wch, it);
			charCmdEnumMap.put(it.ch, it);
		});
	}
	public interface Cmd {
		public void exec(List<Object> l, cmdEnum ch) throws TranshelpException;
	}
	static class Void implements Cmd {
		@Override
		public void exec(List<Object> lst, cmdEnum ch) {
		}
	}
	static class Reverse implements Cmd {
		@Override
		public void exec(List<Object> lst, cmdEnum ce) throws TranshelpException {
			List<Object> nList = new ArrayList<>();
			try {
				for (List<Object> list : new RunIter(lst, ce)) {
					if (list.contains(ce)) {
						List<Object> aList = new ArrayList<>(list);
						Collections.reverse(aList);
						aList.remove(ce);
						nList.addAll(aList);
					}
					else
						nList.addAll(list);
				}
			}
			catch(TranshelpError e) {
				throw new TranshelpException("Error in Reverse: " + e.getMessage());
			}
			lst.clear();
			lst.addAll(nList);
		}
		
	}
	
	/**
	 * get run [B op C] from [A B op C D]
	 * @author Yasukazu
	 * @next() 
	 */
	static class RunIter implements Iterator<List<Object>>, Iterable<List<Object>> {
		cmdEnum sym;
		LinkedList<Object> queue;
		RunIter(List<Object> aa, cmdEnum sym) {
			this.sym = sym;
			this.queue = new LinkedList<Object>(aa);
		}
		
		boolean ispat(List<Object> aa) {
			if (aa.size() < 2)
				return false;
			if (aa.get(0) instanceof cmdEnum && (cmdEnum)aa.get(0) == sym && !(aa.get(1) instanceof cmdEnum))
				return true;
			return false;
		}
		

		@Override
		public Iterator<List<Object>> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return queue.size() > 0;
		}

		@Override
		public List<Object> next() {
			List<Object> rList;
			int ps;
			if (queue.size() < 3 || (ps = queue.indexOf(sym)) < 0)  {
				rList = new ArrayList<Object>(queue);
				queue.clear();
				return rList;
			}	
			if (ps == queue.size()-1)
				throw new TranshelpError("Ends with " + sym);
			switch (ps) {
			case 0: //if (ps == 0)
				throw new TranshelpError("No item before " + sym);
			case 1:
				rList = new ArrayList<Object>();
				rList.add(queue.poll());
				while (ispat(queue)) {
					rList.add(queue.poll());
					rList.add(queue.poll());			
				}
				return rList;
			default: //			if (ps > 1) {
				rList = new ArrayList<Object>(queue.subList(0, ps - 1));
				while (ps-- > 1) {
					queue.poll(); // drop
				}
				return rList;
			}
		}
	}

	
	char stopChar;
	/**
	 * Constructor
	 * @param list
	 * @param stop
	 */
	public Editor(List<Object> list, char stop) {
		super(list);
		stopChar = stop;
	}
	public char getStop() {
		return stopChar;
	}
		
	/**
	 * make all String of command character into Character
	 * @param lst
	 */
	void enchar_cmd(List<Object> list) {
		for (int i = 0; i < list.size(); ++i) {
			Object it = list.get(i);
			if (it instanceof String && ((String)it).length() == 1
		&& cmdCharSet.contains(((String)it).charAt(0))) {
				char ch = ((String)it).charAt(0);
				list.set(i, ch);
			}
			
		}
	}
	
	class IterIdgComma implements Iterable<List<Object>>, Iterator<List<Object>> {
		List<Object> list;
		int cur;
		int size;
		IterIdgComma(List<Object> list) {
			this.list = list;
			cur = 0;
			this.size = list.size();
		}

		@Override
		public boolean hasNext() {
			return cur < size;
		}

		@Override
		public List<Object> next() {
			if (!hasNext())
				return list.subList(cur, size); // empty sublist
			List<Object> rList;
			int idx = cur;
			for (; idx < size; ++idx) {
				Object obj = list.get(idx);
				if (obj == punct.IDGCOMMA) {
					rList = list.subList(cur, idx);
					cur = idx + 2;
					return rList;
				}
			}
			rList = list.subList(cur, size);
			cur = size;
			return rList;
		}

		@Override
		public Iterator<List<Object>> iterator() {
			return this;
		}

	}
	Iterable<List<Object>> idgcommaSplitIter(List<Object> list) {
		return new IterIdgComma(list);
	}
	
	static List<List<Object>> idgcommaSplit(List<Object> list) {
		List<List<Object>> rList = new ArrayList<>();
		int idx = 0;
		do {
			idx = list.indexOf(punct.IDGCOMMA);
			if (idx >= 0) {
				if (idx > 0)
					rList.add(list.subList(0, idx));
				if (idx >= list.size() - 1)
					return rList;
				list = list.subList(idx + 1, list.size());
			}
		} while (idx >= 0 && list.size() > 0);
		if (list.size() > 0)
			rList.add(list);
		return rList;			
	}
	public static List<List<Object>> idgcommaSplitAlloc(List<Object> list) {
		List<List<Object>> rList = new ArrayList<>();
		int idx = 0;
		do {
			idx = list.indexOf(punct.IDGCOMMA);
			if (idx >= 0) {
				if (idx > 0)
					rList.add(new ArrayList<Object>(list.subList(0, idx)));
				if (idx >= list.size() - 1)
					return rList;
				list = list.subList(idx + 1, list.size());
			}
		} while (idx >= 0 && list.size() > 0);
		if (list.size() > 0)
			rList.add(new ArrayList<Object>(list));
		return rList;			
	}
	public void  recurEdit(cmdEnum ck) throws TranshelpException {
		class RecurEdit {
			cmdEnum ck;
			RecurEdit(cmdEnum ck) {
				this.ck = ck;
			}
			void recurExec(List<Object> lst, int nest) throws TranshelpException {
				List<List<Object>> tmpList = new ArrayList<>();
				for (List<Object> split : idgcommaSplit(lst)) {
					List<Object> asplit = new ArrayList<>(split);
					if (split.contains(ck)) {
						try {
						    ck.cmd.exec(asplit, ck);
							for (Object it : asplit) {
								if (it instanceof EnclosedArray) {
									recurExec((EnclosedArray)it, nest + 1);
								}			
							}
						}
						catch (TranshelpException e) {
							throw new TranshelpException(e.getMessage());
						}
					}
					tmpList.add(asplit);
				}
				lst.clear();
				int tmpList_size = tmpList.size();
				for (int i = 0; i < tmpList_size; ++i) {
					List<Object> iList = tmpList.get(i);
					lst.addAll(iList);
					if (i < tmpList_size - 1)
						lst.add(punct.IDGCOMMA);
				}
				
			}
		}

		RecurEdit rc = new RecurEdit(ck);
		try {
			rc.recurExec(this, 0);
		}
		catch(TranshelpException e) {
			throw new TranshelpException(e.getMessage());
		}
	}

	public String toString() {
		class ToString {
			StringBuilder bldr;
			ToString() {
				bldr = new StringBuilder(); 
			}
			void recur(List<Object> stack) {
				for (Object obj : stack) {
					if (obj instanceof EnclosedArray) {
						boolean print = ((EnclosedArray)obj).getPair() != bracketPair.BRACKET;
						if (print)
							bldr.append(((EnclosedArray)obj).getBegin());
						recur((EnclosedArray)obj);
						if (print)
							bldr.append(((EnclosedArray)obj).getEnd());
					}
					else if (obj instanceof punct)
						bldr.append(((punct)obj).ch);
					else {
						bldr.append(obj);
					}
				}
			}
			String toStr() {
				return bldr.toString();
			}
		}
		ToString ts = new ToString();
		ts.recur(this);
		return ts.toStr();
	}

}
