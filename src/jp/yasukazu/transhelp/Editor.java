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
	static Set<Character> cmdchset;
	static Map<Character, Cmd> cmdKeyMap;
	static {
		cmdchset = new HashSet<Character>();
		EnumSet.allOf(cmdEnum.class).forEach(it -> cmdchset.add(it.ch));
	}
	public interface Cmd {
		public void exec(List<Object> l) throws TranshelpException;
	}
	static class Void implements Cmd {
		@Override
		public void exec(List<Object> lst) {
		}
	}
	static class Reverse implements Cmd {
		@Override
		public void exec(List<Object> lst) throws TranshelpException {
			boolean includes  = false;
			for (Object it : lst) 
				if (it instanceof Character) { // Character is command
					includes = true;
					break;
				}
			if (!includes)
				return;
			RunCutter rc = new RunCutter(lst, cmdEnum.REVERSE.getChar());
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
	public void recurEdit(cmdEnum ck) throws TranshelpException {
		class Recur {
			cmdEnum ck;
			Recur(cmdEnum ck) {
				this.ck = ck;
			}
			void recurExec(List<Object> list, int nest) throws TranshelpException {
				enchar_cmd(list);
				try {
				  ck.getCmd().exec(list);
				}
				catch (TranshelpException e) {
					throw new TranshelpException(e.getMessage());
				}				
				list.removeAll(Arrays.asList(ck.getChar()));
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
							&& ( ((String)it).charAt(0) == ck.ch  || ((String)it).charAt(0) == ck.wch ) ){
						list.set(i, ck.ch); // accept full-width command
					}					
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
