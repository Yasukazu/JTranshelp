package jp.yasukazu.transhelp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Map;

import jp.yasukazu.transhelp.Transhelp.punct;

@SuppressWarnings("serial")
public class Enblock extends ArrayList<Object> {
	static Set<Character> dlmSet;
	static Map<Character,Character> dlmMap;
	static {
		dlmSet = new HashSet<Character>();
		for(char ch : Editor.cmdCharSet)
			dlmSet.add(ch);
		dlmSet.add(punct.IDGCOMMA.ch);
		dlmMap = new HashMap<Character,Character>(Editor.cmdCharMap);
		dlmMap.put(punct.IDGCOMMA.ch, punct.IDGCOMMA.ch);
	}

	public Enblock(String txt) throws TranshelpException {
		super();
		List<Object> list;
		try {
			list = _load(txt, 0);
		}
		catch (TranshelpException e) {
			throw new TranshelpException(e.getMessage());
		}
		addAll(list);
	}

	public enum bracketPair {
		PAREN("()"), // kakko
		BRACKET("[]"), // kaku-kakko
		BRACE("{}"), // nami-kakko
		CBRKT("\u300c\u300d"), // kagi-kakko
		WCBRKT("\u300e\u300f"), // niju-kagi-kakko
		NUL("  "),
		;
		public char[] set;
		bracketPair(String str){
			set = new char[2];
			set[0] = str.charAt(0);
			set[1] = str.charAt(1);			
		}
		public final char getBegin() {
			return set[0];
		}
		public char getEnd() {
			return set[1];
		}
	};

	public static bracketPair getPair(char ch) {
		switch (ch) {
		case '(':
			return bracketPair.PAREN;
		case '[':
			return bracketPair.BRACKET;
		case '{':
			return bracketPair.BRACE;
		case '\u300c':
			return bracketPair.CBRKT;
		case '\u300e':
			return bracketPair.WCBRKT;
		}
		return bracketPair.NUL;
	}

	static int MAX_NEST = 9;
	public static String bracket_content(String str,  bracketPair pair) throws TranshelpException {
		int nest = 1;
		int ind = 0; 
		while (ind < str.length()) {
			char ch = str.charAt(ind); 
			if(ch == pair.getBegin()) {
				nest += 1;
				if (nest > MAX_NEST)
					throw new TranshelpException(String.format("Over maximum nest %d", MAX_NEST));
			}
			else if (ch == pair.getEnd()) {
				nest -= 1;
				if (nest == 0)
					return str.substring(0, ind);
			}
			++ind;
		}
		if (nest != 0) {
			System.err.println(str);
			throw new TranshelpException(String.format("%d nest remains.", nest));
		}
		return "";
	}
	public static String en_paren(String s) {
		return '(' + s + ')';
	}
	public static String rgx_remaining(char ch) {
		return String.format("(?<=%1s)|(?=%1s)", ch, ch);
	}
	public static String rgx_command(char ch) {
		return String.format("(?<=%1s)|(?=%1s)", ch, ch);
	}
	public static String rgx_multi_remaining(char... elel) {
		StringBuilder sb = new StringBuilder();
		for (char elm : elel)
			sb.append(rgx_remaining(elm));
		return sb.toString();
	}
	public static String rgx_set_remaining(Set<Character> set) {
		StringBuilder sb = new StringBuilder();
		for (char elm : set)
			sb.append(rgx_remaining(elm));
		return sb.toString();
	}
	
	public static List<Object> load(String st) throws TranshelpException {
		return _load(st, 0);
	}

	
	static String spc_dlmrx = "[\\s" + Transhelp.spaceCharEnum.WSPC.ch + "]+";
	static String idgcomma_dlmrx = '(' + rgx_remaining(punct.IDGCOMMA.ch) + ')'; 
	static String dlmrx = String.join("|", en_paren(spc_dlmrx), en_paren(idgcomma_dlmrx));//, en_paren(rgx_set_remaining(Editor.cmdCharSet)));
	static List<Object> dlmrx_convert(StringBuilder buff) {
		List<Object> rList = new ArrayList<>();
		for (String tk : Arrays.asList(buff.toString().split(dlmrx))) {
			if (tk.length() == 1) {
				char ch = tk.charAt(0);
				if (ch == punct.IDGCOMMA.ch)
					rList.add(ch);
				else if (Editor.cmdCharSet.contains(ch))
					rList.add(Editor.charCmdEnumMap.get(ch));
				else
					rList.add(tk);
			}
			else
				rList.add(tk);
		}
		return rList;
	}
	static List<Object> _load(String st, int level) throws TranshelpException {
		if (level > MAX_NEST) {
			throw new TranshelpException("Too deep nest!");
		}
		List<Object> stack = new ArrayList<>();
		StringBuilder buff = new StringBuilder();
		//String dlmrx = "[\\s" + Transhelp.spaceCharEnum.WSPC.ch + "]+";		
		for (int pos = 0; pos < st.length(); ++pos) {
			char ch = st.charAt(pos);
			Enblock.bracketPair pair = Enblock.getPair(ch); 
			if (pair != Enblock.bracketPair.NUL) {
				if (buff.length() > 0 && buff.toString().trim().length() > 0) {
					stack.addAll(dlmrx_convert(buff));
					buff.setLength(0);
				}
				if (pos + 1 >= st.length())
					return stack;
				String nst = st.substring(pos + 1);
				try {
					String n2st = Enblock.bracket_content(nst, pair);
					if (n2st.length() > 0) {
						stack.add(new EnclosedArray(_load(n2st, level+1), pair));
						pos += n2st.length() + 1;
					}
					else {
						stack.add(new EnclosedArray(_load(nst, level+1), pair));
						return stack;
					}
				}
				catch (TranshelpException e) {
					throw new TranshelpException("bracket_content:" + nst);
				}
			}
			else {
				buff.append(ch);
			}			
		}
		if (buff.length() > 0 && buff.toString().trim().length() > 0) 
			stack.addAll(dlmrx_convert(buff));
		return stack;
	}
		
}
