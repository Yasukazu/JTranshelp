package jp.yasukazu.transhelp;
// 2018/8/30 YtM @ yasukazu.jp
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@SuppressWarnings("serial")
public class Editor extends ArrayList<Object> {
	  enum cmd {
			REVERSE('/'),
		    ;
		    private char ch;
		    cmd(char ch){
		      this.ch = ch;
		    }
		    public char getChar(){
		      return this.ch;
		    }
		}
	  static Set<Character> cmdCharSet;
	  static String cmdCharStr;
	static Set<Character> cmdchset;
	static Map<Character, Cmd> cmdKeyMap;
	static {
		cmdchset = new HashSet<Character>();
		EnumSet.allOf(cmd.class).forEach(it -> cmdchset.add(it.getChar()));
	}
	public interface Cmd {
		public void exec(List<Object> l);
	}
	class Void implements Cmd {
		@Override
		public void exec(List<Object> lst) {
		}
	}
	class Reverse implements Cmd {
		@Override
		public void exec(List<Object> lst) {
			boolean includes = false;
			for (Object it : lst) 
				if (it instanceof Character) { // Character is command
					includes = true;
					break;
				}
			if (!includes)
				return;
			RunCutter rc = new RunCutter(lst, cmd.REVERSE.getChar());
			int[] pos_len;
			try {
				while ((pos_len = rc.get())!= null) {
					Collections.reverse(lst.subList(pos_len[0], pos_len[0] + pos_len[1]));
				}
			}
			catch (TranshelpException e) {
				
			}
		}
		
	}
	Cmd keyToCmd(char cmd) {
		switch(cmd) {
		case '/':
			return new Reverse();
		}
		return new Void();
	}
	public enum cmdKey {
		REVERSE('/'),
		THAT(':'),
		;
		public char key;
		cmdKey(char key) {
			this.key = key;
		}		
	}
	Cmd cmdKeyToCmd(cmdKey ck) {
		switch(ck) {
		case REVERSE:
			return new Reverse();
		case THAT:
			return new Void();
		}
		return new Void();
	}
	
	/**
	 * get run [B op C] from [A B op C D]
	 * @author Yasukazu
	 *
	 */
	class RunCutter {
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
		&& cmdchset.contains(((String)it).charAt(0))) {
				char ch = ((String)it).charAt(0);
				list.set(i, ch);
			}
			
		}
		/*
		list.stream().map(it -> 
		it instanceof String && ((String)it).length() == 1
		&& cmdchset.contains(((String)it).charAt(0)) ? 
				((String)it).charAt(0) : it 
		)
		.collect(Collectors.toList()); */
	}
	public void recurEdit(cmdKey ck) {
		class Recur {
			//cmdKey ck;
			Cmd cmd;
			Recur(cmdKey ck) {
				//this.ck = ck;
				cmd = cmdKeyToCmd(ck);//keyToCmd(ck.key);
			}
			void recurExec(List<Object> list, int nest) {
				enchar_cmd(list);
				cmd.exec(list);
				list.removeAll(Arrays.asList(ck.key));
				for (Object item : list) {
					if (item instanceof EnclosedArray) {
						recurExec((EnclosedArray)item, nest + 1);
					}			
				}									
			}
			void enchar_cmd(List<Object> list) {
				for (int i = 0; i < list.size(); ++i) {
					Object it = list.get(i);
					if (it instanceof String && ((String)it).length() == 1
							&& ((String)it).charAt(0) == ck.key ) { //&& cmdchset.contains(((String)it).charAt(0))) {
						char ch = ((String)it).charAt(0);
						list.set(i, ch);
					}					
				}
			}
		}
		Recur rc = new Recur(ck);
		rc.recurExec(this, 0);
	}
	/**
	 * 
	 * @param cmd
	 * @throws TranshelpException
	 */
	public void recur_edit(Cmd cmd) throws TranshelpException {
		_recur_edit(this, cmd, 0);
	}
	static final int MAXNEST = 9;
	void _recur_edit(List<Object> lst, Cmd cmd, int nst) throws TranshelpException {
		if (nst > MAXNEST)
			throw new TranshelpException(String.format("recur %d edit", nst));
		enchar_cmd(lst);
		cmd.exec(lst);
		for (Object item : lst) {
			if (item instanceof EnclosedArray) {
				_recur_edit((EnclosedArray)item, cmd, nst + 1);
			}			
		}					
	}
	public void do_reverse() throws TranshelpException {
		recur_edit(new Reverse());
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
