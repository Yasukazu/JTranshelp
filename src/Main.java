import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import jp.yasukazu.transhelp.*;

class Main {
  public static void main(String[] args) {
	  List<Object> run_iter_sample = new ArrayList<>();
	  run_iter_sample.addAll(Arrays.asList("a b".split(" ")));
	  run_iter_sample.add(Editor.cmdEnum.REVERSE);
	  run_iter_sample.addAll(Arrays.asList("c d".split(" ")));
	  run_iter_sample.add(Editor.cmdEnum.REVERSE);
	  run_iter_sample.addAll(Arrays.asList("e f".split(" ")));
	  Editor.RunIter run_iter = new Editor.RunIter(run_iter_sample, Editor.cmdEnum.REVERSE);
	  List<List<Object>> output_list = new ArrayList<>();
	  for (List<Object> part_list : run_iter) {
		  output_list.add(part_list);
	  }
    String usage_filename = "README.md";
    String filename = "input.txt";
    try (Stream<String> stream = Files.lines(Paths.get(filename), StandardCharsets.UTF_8);
	  Stream<String> usage_stream = Files.lines(Paths.get(usage_filename), StandardCharsets.UTF_8))
    {
	  usage_stream.forEach(System.out::println);
	  List<String> lines = stream.map(String::trim).collect(Collectors.toList());
	  Transhelp thelp = new Transhelp(lines);

		List<Editor> editorList = thelp.editAll();	
		  for (int i = 0; i < thelp.size(); ++i) {
			  HasStopString hstr = thelp.get(i);
			  System.out.println("Orig: " + hstr.getStr() + hstr.getStop());
			  Editor edt = editorList.get(i);
			  System.out.println("Edit: " + edt.toString() + edt.getStop());			  
		  }
	}
    catch (IOException e) {
	  System.out.print("IOException occured: ");
	  e.printStackTrace();
	}
    catch (TranshelpException e) {
		System.out.println("Sentence is not proper format.");
		e.printStackTrace();
	}
  }
}