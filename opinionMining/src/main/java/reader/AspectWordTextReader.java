package reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class AspectWordTextReader {
  private int curIndex = 1;
  private Scanner reader;
  private String[] words = null;

  public AspectWordTextReader(String filePath) throws FileNotFoundException {
    reader = new Scanner(new FileInputStream(filePath));
  }

  public boolean HasNext() {
    if ((words == null || words.length == curIndex || words[curIndex].trim().equals(""))
        && !reader.hasNextLine())
      return false;
    if (words == null || words.length == curIndex || words[curIndex].trim().equals("")) {
      curIndex = 1;
      words = reader.nextLine().split("\t");
      words[0] = words[0].substring(1);
      words[0] = words[0].substring(0, words[0].length() - 1);
    }
    return true;
  }

  public String GetNext() {
    return words[curIndex++];
  }

  public String GetCurrentAspect() {
    return words[0];
  }
}
