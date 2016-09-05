package com.lucky;

/**
 * Created by yuchao on 9/2/16.
 */
public class PreferenceUtils {

  public static boolean isNull(String string) {
    if (string == null || string.equals("")) {
      return true;
    }
    return false;
  }

  public static boolean isString(String string) {
    if (isNull(string)) {
      return false;
    }
    return true;
  }

  public static boolean isInt(String string){
    if (isNull(string)) {
      return false;
    }
    try {
      Integer.valueOf(string);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return false;
  }


  public static boolean isBoolean(String string) {
    if (isNull(string)) {
      return false;
    }
    if (string.equals("false") ||string.equals("true")) {
      return true;
    }
    return false;
  }

  public static boolean isFloat(String string) {
    if (isNull(string)) {
      return false;
    }
    try {
      Float f = Float.valueOf(string);
      System.out.println(" float " + f);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static boolean isDouble(String string){
    if (isNull(string)) {
      return false;
    }
    try {
      Double.valueOf(string);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static boolean isLong(String string) {
    if (isNull(string)) {
      return false;
    }
    try {
      Long.valueOf(string);
      return true;
    }catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return false;
  }



  public static void main(String... args) {
    System.out.println("============== is int ===========");

    boolean result = isInt("");
    System.out.println(result);
//    result = isInt("123");
//    System.out.println(result);
   /* result = isInt("123.0");
    System.out.println(result);
    result = isInt("gfdgfd");
    System.out.println(result);
*/

    System.out.println("============== is float ===========");
    result = isFloat("123");
    System.out.println(result);
    result = isFloat("123.0");
    System.out.println(result);
    result = isFloat("123.0f");
    System.out.println(result);
    result = isFloat("123.0fffff");
    System.out.println(result);


  }
}
