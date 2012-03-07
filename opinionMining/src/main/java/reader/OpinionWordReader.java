package reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import eu.ubipol.opinionmining.owl_engine.Utils;

public class OpinionWordReader {
  private Scanner reader;
  private double curScore = 0;
  private int wordType;

  public OpinionWordReader(String filePath) throws FileNotFoundException {
    reader = new Scanner(new FileInputStream(filePath));
  }

  public boolean HasNextLine() {
    return reader.hasNextLine();
  }

  public int GetCurrentType() {
    return wordType;
  }

  public double GetCurrentScore() {
    return curScore;
  }

  public String GetNext() {
    String[] lineArray = reader.nextLine().split("\t");
    curScore = Double.parseDouble(lineArray[2]);

    if (lineArray[1].equals("n"))
      wordType = Utils.NOUN_ID;
    else if (lineArray[1].equals("r"))
      wordType = Utils.ADVERB_ID;
    else if (lineArray[1].equals("a"))
      wordType = Utils.ADJECTIVE_ID;
    else
      wordType = Utils.VERB_ID;

    return lineArray[0].trim().toLowerCase();
  }
}
