import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import jp.yasukazu.transhelp.*;

class Main {
  public static void main(String[] args) {
    String usage_filename = "README.md";
    String filename = "input.txt";
    try (Stream<String> stream = Files.lines(Paths.get(filename));
	  Stream<String> usage_stream = Files.lines(Paths.get(usage_filename))) {
	  usage_stream.forEach(System.out::println);
	  List<String> lines = stream.map(String::trim).collect(Collectors.toList());
	  Transhelp thelp = new Transhelp(lines);
		  thelp.getSentences().forEach(snt -> {
			  System.out.println("Original text:\n" + snt.str);
			  try {
				  List<Object> nlist = Enblock.load(snt.str);
				  Editor edt = new Editor(nlist);
				  edt.recurEdit(Editor.cmdKey.REVERSE); //do_reverse();
				  String edtStr = edt.toString();
				  System.out.println("Edited text:\n" + edtStr + snt.stop);
			  }
			  catch (TranshelpException e) {
				  System.err.println(snt.str + " <- Inproper sentence.");
				  e.printStackTrace();
			  }
		  });

	}
    catch (IOException e) {
	  System.out.print("IOException occured: ");
	  e.printStackTrace();
	}
  }
}