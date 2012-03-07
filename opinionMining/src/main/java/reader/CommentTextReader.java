package reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;

public class CommentTextReader {
  private Scanner reader;
  private static int curId = 0;
  private String curLine = null;
  private Date curDate = null;
  private int[] ratings = new int[8];

  public CommentTextReader(String filePath) throws FileNotFoundException {
    reader = new Scanner(new FileInputStream(filePath));
  }

  public int GetCurrentId() {
    return curId;
  }

  public boolean HasNextLine() {
    String result = "";
    while (reader.hasNextLine() && !result.startsWith("<Content>"))
      result = reader.nextLine().trim();
    if (reader.hasNextLine()) {
      result = result.substring(9).trim();
      String temp = reader.nextLine();
      while (reader.hasNextLine() && !temp.startsWith("<Date>")) {
        result += " " + temp.trim();
      }
      // Jan 6, 2009
      int day = 1, month = 1, year = 2011;
      temp = temp.substring(6);
      if (temp.substring(0, 3).equals("Jan"))
        month = 1;
      else if (temp.substring(0, 3).equals("Feb"))
        month = 2;
      else if (temp.substring(0, 3).equals("Mar"))
        month = 3;
      else if (temp.substring(0, 3).equals("Apr"))
        month = 4;
      else if (temp.substring(0, 3).equals("May"))
        month = 5;
      else if (temp.substring(0, 3).equals("Jun"))
        month = 6;
      else if (temp.substring(0, 3).equals("Jul"))
        month = 7;
      else if (temp.substring(0, 3).equals("Aug"))
        month = 8;
      else if (temp.substring(0, 3).equals("Sep"))
        month = 9;
      else if (temp.substring(0, 3).equals("Oct"))
        month = 10;
      else if (temp.substring(0, 3).equals("Nov"))
        month = 11;
      else if (temp.substring(0, 3).equals("Dec"))
        month = 12;
      temp = temp.substring(1);
      day = Integer.parseInt(temp.substring(3, temp.indexOf(',')));
      temp = temp.substring(temp.indexOf(','));
      year = Integer.parseInt(temp.substring(temp.indexOf(' ') + 1));
      GregorianCalendar cal = new GregorianCalendar(year, month, day);
      curDate = cal.getTime();
      String[] tempRatings = reader.nextLine().substring(8).split("\t");
      for (int i = 0; i < 8; i++)
        ratings[i] = Integer.parseInt(tempRatings[i]);
      curId++;
      curLine = result;
      return true;
    }
    return false;
  }

  public String GetNext() {
    return curLine;
  }

  public int[] GetCurrentRatings() {
    return ratings;
  }

  public Date GetCurrentDate() {
    return curDate;
  }
}
