package jp.yasukazu.transhelp;

public class HasStopString {
	public String str;
	public char stop;
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
