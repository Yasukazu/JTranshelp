package jp.yasukazu.transhelp;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.text.Normalizer;
//import com.ibm.icu.text.Normalizer;

public class Transhelp {
  enum punct {
    KUTEN('\u3002'),
    ;
    private char ch;
    punct(char ch){
      this.ch = ch;
    }
    public char getChar(){
      return this.ch;
    }

  }
  List<String> org_lines;
  List<String> nrm_lines;
  List<HasStopString> sentences;
  public Transhelp(List<String> lines){
    org_lines = lines;
    nrm_lines = lines.stream()
    .map(str -> Normalizer.normalize(str, Normalizer.Form.NFKC)).collect(Collectors.toList());
    sentences = getYsentence(nrm_lines);
    
  }
  
  public List<HasStopString> getSentences() {
	  return this.sentences;
  }
  
  List<HasStopString> getYsentence(List<String> lines){
    List<HasStopString> nLines = new LinkedList<>();
    lines.forEach(line -> nLines.addAll(stop_split(line)));
    return nLines;
  }

  List<HasStopString> stop_split(String line){
    String rgx_dlms = "(?<=[" + punct.KUTEN.ch + "])";
    List<String> split_list = Arrays.asList(line.split(rgx_dlms));
    List<HasStopString> return_list = new ArrayList<>();
    split_list.forEach(ln -> return_list.add(new HasStopString(ln, ln.charAt(ln.length()-1) == punct.KUTEN.ch))); 
    return return_list;
  }

  public List<String> normalizedTexts(){
    return nrm_lines;
  };

}