package nlp_engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class Aspects {
  private static Map<String, List<String>> aspectList = null;

  public static void FillAspectList(File file) {
    try {
      aspectList = new TreeMap<String, List<String>>();
      Scanner reader = new Scanner(file);
      while (reader.hasNextLine()) {
        String[] lineArray = reader.nextLine().split("\t");
        lineArray[0] = lineArray[0].substring(1, lineArray[0].length() - 1);
        for (int i = 1; i < lineArray.length; i++)
          AddKeyword(lineArray[0], lineArray[i]);
        lineArray = null;
      }
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void FillAspectList() {
    try {
      aspectList = new TreeMap<String, List<String>>();
      Scanner reader = new Scanner(new File("FeatureWords.txt"));
      while (reader.hasNextLine()) {
        String[] lineArray = reader.nextLine().split("\t");
        lineArray[0] = lineArray[0].substring(1, lineArray[0].length() - 1);
        for (int i = 1; i < lineArray.length; i++)
          AddKeyword(lineArray[0], lineArray[i]);
        lineArray = null;
      }
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void AddKeyword(String aspectName, String keyword) {
    aspectName = aspectName.toLowerCase().trim();
    keyword = keyword.toLowerCase().trim();
    if (!aspectList.containsKey(aspectName))
      aspectList.put(aspectName, new ArrayList<String>());
    aspectList.get(aspectName).add(keyword);
  }

  public static Long GetAspectOfKeyword(String keyword) {
    if (aspectList == null)
      FillAspectList();
    keyword = keyword.toLowerCase().trim();
    Long counter = new Long(0);
    for (Entry<String, List<String>> e : aspectList.entrySet()) {
      if (e.getValue().contains(keyword))
        return counter;
      counter++;
    }
    return new Long(-1);
  }

  public static String GetAspectName(Long aspectId) {
    if (aspectList == null)
      FillAspectList();
    Long counter = new Long(0);
    for (Entry<String, List<String>> e : aspectList.entrySet()) {
      if (counter.compareTo(aspectId) != 0)
        counter++;
      else
        return e.getKey();
    }
    return null;
  }
}
