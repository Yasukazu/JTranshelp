package jp.yasukazu.transhelp;
// 2018/8/31 YtM @yasukazu.jp
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

import java.text.Normalizer;

@SuppressWarnings("serial")
public class Transhelp extends ArrayList<HasStopString> {
  enum punct {
	SPC('\u0020'),
	WSPC('\u3000'),
    IDGCOMMA('\u3001'), //KUTEN \
	IDGFSTOP('\u3002'), //TOUTEN o
    EXCL('!'),
    QSTN('?'),
    COMMA(','),
    FLSTOP('.'),
    COLON(':'),
    SEMI(';'),
    WEXCL('\uFF01'),
    WQSTN('\uFF1F'),
    WCOMMA('\uFF0C'),
    WFLSTOP('\uFF0E'),
    WCOLON('\uFF1A'),
    WSEMI('\uFF1B'),
    ;
    private char ch;
    punct(char ch){
      this.ch = ch;
    }
    public char getChar(){
      return this.ch;
    }

  }
  enum wpunct {
		WSPC,
		IDGFSTOP,
	    IDGCOMMA,	  
	    WEXCL,
	    WQSTN,
	    WCOMMA,
	    WFLSTOP,
	    WCOLON,
	    WSEMI,
	    ;
  }
	static Set<Character> punctCharSet;
	static String punctCharStr;
	static {
		punctCharSet = new HashSet<Character>();
		EnumSet.allOf(punct.class).forEach(it -> punctCharSet.add(it.getChar()));
		StringBuilder sb = new StringBuilder();
		for (Character ch : punctCharSet)
			sb.append(ch);
		punctCharStr = sb.toString();		
	}
	List<String> org_lines;
  List<String> nrm_lines;
  //List<HasStopString> sentences;
  /**
   * Constructor
   * @param lines
   */
  public Transhelp(List<String> lines){
	super();
    org_lines = lines;
    nrm_lines = lines.stream()
    .map(str -> {
    	String nstr = Normalizer.normalize(str.trim(), Normalizer.Form.NFC);
    	return nstr;
    })
	.collect(Collectors.toList());
    addAll(getYsentence(nrm_lines));    
  }
  
  public List<HasStopString> getSentences() {
	  return this;
  }
  
  List<HasStopString> getYsentence(List<String> lines){
    List<HasStopString> nLines = new LinkedList<>();
    lines.forEach(line -> nLines.addAll(stop_split(line)));
    return nLines;
  }

  List<HasStopString> stop_split(String line){
    String rgx_dlms = "(?<=[" + punct.IDGFSTOP.ch + "])";
    List<Object> split_list = Arrays.asList((Object[])line.split(rgx_dlms));
    return split_list.stream().map(ln -> HasStopString.toHasStopString((String)ln)).collect(Collectors.toList()); 
  }

  public List<String> normalizedTexts(){
    return nrm_lines;
  }

  /**
   * @exception 
   * @return 
   */
  public List<Editor> editAll() throws TranshelpException {
	  List<Editor> editorList = new ArrayList<Editor> ();
	try {
	  for(HasStopString snt : this) {
				Enblock block = new Enblock(snt.str);
			  Editor edt = new Editor(block, snt.stop);
			  edt.recurEdit(Editor.cmdEnum.REVERSE); //do_reverse();
			  editorList.add(edt);
	  }
		}
		catch (TranshelpException e) {
			throw new TranshelpException("Improper grammer: " + e.getMessage());
		}
	  return editorList;
  }

}
