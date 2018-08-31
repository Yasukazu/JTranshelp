package jp.yasukazu.transhelp;
import java.util.Arrays;
// 2018/8/31 : wide stop characters
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * String with stip character like period(aka: full stop), question mark, exclamation mark
 * @author yasukazu
 *
 */
public class HasStopString {
	enum stopChars {
	    CJKSPC('\u3000'),
	    TOUTEN('\u3001'),
	    KUTEN('\u3002'),
	    EXCL('!'),
	    QSTN('?'),
	    COMMA(','),
	    FLSTP('.'),
	    COLON(':'),
	    SEMI(';'),
	    WEXCL('\uFF01'),
	    WQSTN('\uFF1F'),
	    WCOMMA('\uFF0C'),
	    WFLSTP('\uFF0E'),
	    WCOLON('\uFF1A'),
	    WSEMI('\uFF1B'),
	    ;
	    private char ch;
	    stopChars(char ch){
	      this.ch = ch;
	    }
	    public char getChar(){
	      return this.ch;
	    }		
	}
	static Set<Character> stopCharSet;
	static String stopCharStr;
	static stopChars[] wStopChars = {stopChars.KUTEN, stopChars.WEXCL, stopChars.WFLSTP, stopChars.WQSTN};
	static Set<Character> wStopCharSet;
	static String wStopCharStr;
	static Set<Character> fullstopCharSet;
	static String fullstopCharStr;
	static stopChars[] fullstopChars = {stopChars.KUTEN, stopChars.WEXCL, stopChars.WFLSTP, stopChars.WQSTN};
	static {
		stopCharSet = new HashSet<Character>();
		EnumSet.allOf(stopChars.class).forEach(it -> stopCharSet.add(it.ch));
		StringBuilder sb = new StringBuilder();
		for (Character ch : stopCharSet)
			sb.append(ch);
		stopCharStr = sb.toString();
		wStopCharSet = new HashSet<Character>();
		Arrays.asList(wStopChars).forEach(sc -> wStopCharSet.add(sc.ch));
		sb.setLength(0);
		for (Character ch : wStopCharSet)
			sb.append(ch);
		wStopCharStr = sb.toString();		
	}

	public static HasStopString toHasStopString(String str) {
		str = str.trim();
		if (str.length() == 0)
			return new HasStopString("", '\0');
		char lastc = str.charAt(str.length() - 1);
		if (!stopCharSet.contains(lastc))
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

