package jp.yasukazu.transhelp;
import java.util.EnumSet;

import jp.yasukazu.transhelp.Transhelp.punct;
// 2018/8/31 : wide stop characters
import java.util.HashSet;
import java.util.Set;

/**
 * String with stip character like period(aka: full stop), question mark, exclamation mark
 * @author yasukazu
 *
 */
public class HasStopString {
	static EnumSet<punct> fullStopPunctSet = EnumSet.of(punct.WEXCL, punct.WFLSTOP, punct.IDGFSTOP, punct.WQSTN);
	static Set<Character> fullStopCharSet;
	static String fullStopCharStr;
	static {
		StringBuilder sb = new StringBuilder();
		fullStopPunctSet.forEach(sc -> sb.append(sc.ch));
		fullStopCharStr = sb.toString();		
		fullStopCharSet = new HashSet<>();
		fullStopPunctSet.forEach(sc -> fullStopCharSet.add(sc.ch));
		fullStopCharSet.remove(punct.WFLSTOP.ch);
	}

	public static HasStopString toHasStopString(String str) {
		str = str.trim();
		if (str.length() == 0)
			return new HasStopString("", '\0');
		char lastc = str.charAt(str.length() - 1);
		if (!fullStopCharSet.contains(lastc))
			return new HasStopString(str, '\0');			
		return new HasStopString(str.substring(0, str.length() - 1), lastc);
	}
	String str;
	char stop;
	HasStopString(String str, char stop) {
		this.str = str;
		this.stop = stop;
	}
	
	HasStopString(String str, boolean has_stop){
		this.str = has_stop ? str.substring(0, str.length()-1) : str;
		this.stop = has_stop ? str.charAt(str.length()-1) : 0;
	}
	
	public String getStr() {
		return this.str;
	}
	public char getStop() {
		return this.stop;
	}
	public void nilStop() {
		this.stop = 0;
	}
	public void appendStop() {
		if (this.stop == 0)
			return;
		this.str = this.str + this.stop;
		nilStop();
	}

}

