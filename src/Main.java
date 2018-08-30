import java.nio.charset.StandardCharsets;
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
    try (Stream<String> stream = Files.lines(Paths.get(filename), StandardCharsets.UTF_8);
	  Stream<String> usage_stream = Files.lines(Paths.get("..", usage_filename), StandardCharsets.UTF_8))
    {
	  usage_stream.forEach(System.out::println);
	  List<String> lines = stream.map(String::trim).collect(Collectors.toList());
	  Transhelp thelp = new Transhelp(lines);
	  List<Editor> editorList = thelp.editAll();
	  if (editorList != null)
		  for (int i = 0; i < thelp.size(); ++i) {
			  HasStopString hstr = thelp.get(i);
			  System.out.println("Orig: " + hstr.str + hstr.stop);
			  Editor edt = editorList.get(i);
			  System.out.println("Edit: " + edt.toString() + edt.getStop());			  
		  }
	}
    catch (IOException e) {
	  System.out.print("IOException occured: ");
	  e.printStackTrace();
	}
  }
}