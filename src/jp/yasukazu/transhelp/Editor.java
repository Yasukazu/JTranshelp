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

			RunCutter rc = new RunCutter(lst, ce);//cmdEnum.REVERSE.ch);
			int[] pos_len;
			try {
				while ((pos_len = rc.get())!= null) {
					List<Object> sub_list = lst.subList(pos_len[0], pos_len[0] + pos_len[1]);
					sub_list.remove(ce);
					Collections.reverse(sub_list);
				}
			}
			catch (TranshelpException e) {
				throw new TranshelpException(e.getMessage() + " + improper grammer in Run.");
			}
		}
		
	}
	
	/**
	 * get run [B op C] from [A B op C D]
	 * @author Yasukazu
	 *
	 */
	public static class RunIter implements Iterator<List<Object>>, Iterable<List<Object>> {
		cmdEnum sym;
		LinkedList<Object> queue;
		public RunIter(List<Object> aa, cmdEnum sym) {
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
			return queue != null;
		}

		@Override
		public List<Object> next() {
			List<Object> rList;
			if (queue.size() == 0) // sliced() == null)
				return null;
			int ps;
			if (queue.size() < 3 || (ps = queue.indexOf(sym)) < 0)  {
				rList = queue;
				queue = null;
				return rList;
			}			
			if (ps == 0)
				throw new TranshelpError("No item before " + sym);
			if (ps == queue.size()-1)
				throw new TranshelpError("Ends with " + sym);
			if (ps > 1) {
				rList = new ArrayList<Object>(queue.subList(0, ps - 1));
				while (ps-- > 1) {
					queue.poll(); // drop
				}
				return rList;
			}
			rList = new ArrayList<Object>();
			rList.add(queue.poll());
			while (ispat(queue)) {
				rList.add(queue.poll());
				rList.add(queue.poll());			
			}
			return rList;
		}
	}

	static class RunCutter  {
		cmdEnum sym;
		List<Object> aa;
		int pos;
		RunCutter(List<Object> aa, cmdEnum sym) {
			this.sym = sym;
			this.aa = new ArrayList<Object>(aa);
			this.pos = 0;
		}
		
		/**
		 * is pattern
		 * @param aa
		 * @return
		 */
		boolean ispat(List<Object> aa) throws TranshelpException {
			if (aa.size() < 2)
				return false;
			if (!(aa.get(0) instanceof cmdEnum) || ((cmdEnum)aa.get(0)) != sym)
				return false;			
			if ((aa.get(1) instanceof cmdEnum) && ((cmdEnum)aa.get(1)) == sym)
				throw new TranshelpException("Duplicating:" + sym);
			return true;
		}
		
		/**
		 * 
		 * @return int[] (but null if error) of Position and Run-length
		 * @throws TranshelpException 
		 */
		int[] get() throws TranshelpException {
			if (sliced() == null || sliced().size() < 3)
				return null;
			int ps = sliced().indexOf(sym);
			if (ps < 0)
				return null;
			if (ps == 0 && pos == 0)
				throw new TranshelpException("No item before " + sym);
			if (ps == sliced().size()-1)
				throw new TranshelpException("Ends with " + sym);
			List<Object> subslice = sliced().subList(ps, sliced().size());					
			int prcd = 0; // proceed			
			for (Object lastObj = subslice.get(prcd++); prcd < subslice.size(); ++prcd) {
				Object obj = subslice.get(prcd);
				if (lastObj instanceof cmdEnum) {
					if (obj instanceof cmdEnum && (cmdEnum)obj == sym)
						throw new TranshelpException("Duplicating same Operator " + sym);
				}
				else
					if (!(obj instanceof cmdEnum) || (cmdEnum)obj != sym) 
						break;
				lastObj = obj;
			}
			int[] rvs = {pos + ps - 1, prcd + 1};
			pos += ps + prcd;
			return rvs;
		}
						
		/**
		 * 
		 * @return : view of subList starting pos but null if unavailable
		 */
		List<Object> sliced() {		
			return pos < aa.size() ? aa.subList(pos, aa.size()) : null; 
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
	
	public static Iterable<List<Object>> idgcommaSplitIter(List<Object> list) {
		class EnumIdgComma implements Iterable<List<Object>>, Iterator<List<Object>> {
			List<Object> list;
			int cur;
			int size;
			EnumIdgComma(List<Object> list) {
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
					return null;
				List<Object> rList;
				int idx = cur;
				for (; idx < size; ++idx) {
					Object obj = list.get(idx);
					if (obj instanceof punct && ((punct)obj) == punct.IDGCOMMA) {
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
		return new EnumIdgComma(list);
	}
	
	public static List<List<Object>> idgcommaSplit(List<Object> list) {
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
				for (List<Object> list : idgcommaSplitAlloc(lst)) {
				//for (int i = 0; i < allocList.size(); ++i) {
					//List<Object> list = allocList.get(i);
					if (list.contains(ck))
						try {
							List<Object> aList = new ArrayList<>(list);
						    ck.cmd.exec(aList, ck);
							for (Object it : aList) {
								if (it instanceof EnclosedArray) {
									recurExec((EnclosedArray)it, nest + 1);
								}			
							}
							//list.remove(ck);
							tmpList.add(aList);
						}
						catch (TranshelpException e) {
							throw new TranshelpException(e.getMessage());
						}
				}
				lst.clear();
				int tmpList_size = tmpList.size();
				for (int i = 0; i < tmpList_size; ++i) {
					List<Object> iList = tmpList.get(i);
					//iList.remove(ck);
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
						boolean print = ((EnclosedArray)obj).getPair() != Enblock.bracketPair.BRACKET;
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
