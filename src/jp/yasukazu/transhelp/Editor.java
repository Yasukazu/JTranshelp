package jp.yasukazu.transhelp;
// 2018/8/30 YtM @ yasukazu.jp
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.yasukazu.transhelp.Transhelp.punct;

import java.util.HashSet;

@SuppressWarnings("serial")
public class Editor extends ArrayList<Object> {
	  enum cmdEnum {
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
		public void exec(List<Object> l, char ch) throws TranshelpException;
	}
	static class Void implements Cmd {
		@Override
		public void exec(List<Object> lst, char ch) {
		}
	}
	static class Reverse implements Cmd {
		@Override
		public void exec(List<Object> lst, char ch) throws TranshelpException {

			RunCutter rc = new RunCutter(lst, ch);//cmdEnum.REVERSE.ch);
			int[] pos_len;
			try {
				while ((pos_len = rc.get())!= null) {
					Collections.reverse(lst.subList(pos_len[0], pos_len[0] + pos_len[1]));
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
	static class RunCutter {
		char sym;
		List<Object> aa;
		int pos;
		RunCutter(List<Object> aa, char sym) {
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
			if (!(aa.get(0) instanceof Character) || ((Character)aa.get(0)) != sym)
				return false;			
			if ((aa.get(1) instanceof Character) && ((Character)aa.get(1)) == sym)
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
				if (lastObj instanceof Character) {
					if (obj instanceof Character && (Character)obj == sym)
						throw new TranshelpException("Duplicating same Operator " + sym);
				}
				else
					if (!(obj instanceof Character) || (Character)obj != sym) 
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
	
	public static Enumeration<List<Object>> idgcommaYsplit(List<Object> list) {
		class EnumIdgComma implements Enumeration<List<Object>> {
			List<Object> list;
			int pos;
			int cur;
			int size;
			EnumIdgComma(List<Object> list) {
				this.list = list;
				pos = cur = 0;
				this.size = list.size();
			}

			@Override
			public boolean hasMoreElements() {
				return cur < size;
			}

			@Override
			public List<Object> nextElement() {
				if (!hasMoreElements())
					return null;
				List<Object> rList;
				int idx = cur;
				for (; idx < size; ++idx) {
					Object obj = list.get(idx);
					if (obj instanceof Character && ((Character)obj) == punct.IDGCOMMA.ch) {
						rList = new ArrayList<>(list.subList(cur, idx));
						cur += idx + 2;
						return rList;
					}
				}
    			rList = list.subList(cur, size);
	  			cur = size;
				return rList;
			}			
		}
		return new EnumIdgComma(list);
	}

	public void recurEdit(cmdEnum ck) throws TranshelpException {
		class Recur {
			cmdEnum ck;
			Recur(cmdEnum ck) {
				this.ck = ck;
			}
			void recurExec(List<Object> lst, int nest) throws TranshelpException {
				Enumeration<List<Object>> idgcomma_split = idgcommaYsplit(lst);
				List<List<Object>> idgcomma_split_list = new ArrayList<>();
				while (idgcomma_split.hasMoreElements()) 
					idgcomma_split_list.add(idgcomma_split.nextElement());
				List<Object> removeList = new ArrayList<>();
				for (int i = 0; i < idgcomma_split_list.size(); ++i) {
					List<Object> list = idgcomma_split_list.get(i);
					try {
						//enchar_cmd(list);
					    ck.cmd.exec(list, ck.ch);
						for (int j = 0; j < list.size(); ++j) {
							Object it = list.get(j);
							if (it instanceof EnclosedArray) {
								recurExec((EnclosedArray)it, nest + 1);
							}			
							else if (it instanceof Character && ((Character)it) == ck.ch)
								removeList.add(it); //list.remove(it);
						}
					  
					}
					catch (TranshelpException e) {
						throw new TranshelpException(e.getMessage());
					}

					int rsize = removeList.size();
					for (int k = 0; k < rsize; ++k) {
						list.remove(removeList.get(k));
					}
					removeList.clear();
				}
				
			}
		}
		Recur rc = new Recur(ck);
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
