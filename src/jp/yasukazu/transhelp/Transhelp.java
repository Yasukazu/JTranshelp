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
	WSPC('\u3000'),
	IDGFSTP('\u3001'), //TOUTEN o
    IDGCOMMA('\u3002'), //KUTEN \
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
    punct(char ch){
      this.ch = ch;
    }
    public char getChar(){
      return this.ch;
    }

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
    String rgx_dlms = "(?<=[" + punct.IDGCOMMA.ch + "])";
    List<String> split_list = Arrays.asList(line.split(rgx_dlms));
    List<HasStopString> return_list = new ArrayList<>();
    split_list.forEach(ln -> { 
    	HasStopString hss = HasStopString.toHasStopString(ln); 
    	return_list.add(hss);
    });
    return return_list;
  }

  public List<String> normalizedTexts(){
    return nrm_lines;
  }
  TranshelpException exception;
  public TranshelpException getException() {
	  return exception;
  }
  /**
   * 
   * @return null if fail, remaining TranshelpException available by getException().
   */
  public List<Editor> editAll() {
	  List<Editor> editorList = new ArrayList<Editor> ();
	  exception = null;
	  forEach(snt -> {
		  try {
			  List<Object> nlist = Enblock.load(snt.str);
			  Editor edt = new Editor(nlist, snt.stop);
			  edt.recurEdit(Editor.cmdKey.REVERSE); //do_reverse();
			  editorList.add(edt);
		  }
		  catch (TranshelpException e) {
			  exception = e;
		  }
	  });
	  return exception == null ? editorList : null;
  }

}
