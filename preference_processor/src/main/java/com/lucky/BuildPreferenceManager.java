package com.lucky;

import com.squareup.javawriter.JavaWriter;
import com.squareup.javawriter.StringLiteral;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by yuchao on 8/27/16.
 * this class is mainly to build  a class of AutoPreferenceManager to manage @PreferenceItem preference.
 * <p>Note:</p>
 * <strong>you must make sure your Preference Class's name is unique</strong>
 * <strong>
 *   AutoPreferenceManager's package  depends on the @PreferenceItem class's package.
 *   If your @PreferenceItem has different package, it will use @PreferenceItem appears the mostly.
 *   @see {@link @PreferenceProcessor}
 * </strong>
 *
 * <div>
 *   AccountPreferenceManager.****.set**();
 * </div>
 */

public class BuildPreferenceManager {
  private static final String CLASS_PROCESSOR = "Processor";
  private static final String CONTEXT = "Context";
  private static final String CTX = "ctx";

  private static final String DEFAULT_MANAGER_NAME = "AutoPreferenceManager";

  String pkgName;
  String destClassName;
  String sourceClassname;
  Filer filer;
  Types typeUtils;
  Elements elementUtils;
  Messager messager;
  JavaWriter javaWriter;
  Map<String,String> staticClassList;

  private BuildPreferenceManager() {

  }


  public BuildPreferenceManager(final Filer filer,
                                final Types typeUtils,
                                final Elements elementUtils,
                                final Messager messager) {
    this.filer = filer;
    this.elementUtils = elementUtils;
    this.typeUtils = typeUtils;
    this.messager = messager;
  }

  public void generateFile() throws IllegalStateException,IOException{
    this.generateFile(DEFAULT_MANAGER_NAME);
  }


  public void generateFile(String sourceClassName) throws IllegalStateException,IOException{
    this.sourceClassname= sourceClassName;
    destClassName = sourceClassName;
    JavaFileObject managerFile = filer.createSourceFile(destClassName);

    javaWriter = new JavaWriter(managerFile.openWriter());
    ArrayList<String> importsList = getImports(staticClassList);
    javaWriter.emitPackage(pkgName)
        .emitImports("android.content.Context")
        .emitImports(importsList)
        .emitEmptyLine()
        .emitEmptyLine()
        .beginType(destClassName, "class", EnumSet.of(Modifier.PUBLIC))
        .emitEmptyLine();

    for (Map.Entry<String,String> entry : staticClassList.entrySet()) {
      String className = entry.getKey();
      String pkgName = entry.getValue();
      generatePropertyClass(className, pkgName);
    }
    generateClear();
    javaWriter.endType();
    javaWriter.close();
  }


  public boolean isNeedPackageName() {
    return true;
  }

  public void setPackageName(String pkgName) {
    this.pkgName = pkgName;
  }

  public String getPackageName() {
    return pkgName;
  }

  public void addStaticClass(String pkgName, String className) {
    if (PreferenceUtils.isNull(pkgName) || PreferenceUtils.isNull(className)) {
      return;
    }
    if (staticClassList == null) {
      staticClassList = new HashMap<>(20);
    }
    String key =  pkgName + "." + className;
    staticClassList.put(className, pkgName);
  }

  /**
   * generate class property {class a = null}
   * @param pkgName  the class belong package com.*.*;
   * @param className class name like:AccountPreferenceProcessor
   * * */
  public void generatePropertyClass(String className,String pkgName) throws IOException{
    if (pkgName == null || className == null) {
      return;
    }
    String valueName = getValueName(className);
    generateConst(EnumSet.of(Modifier.PRIVATE,Modifier.STATIC),className,
        valueName,"null");
    generateStaticMethod(className);
  }


//generate static get Preference method
  public void generateStaticMethod(String className) throws IOException{
    javaWriter.beginMethod(pkgName+"."+className,getStaticMethodName(className),
        EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));
    String valueName = getValueName(className);
    javaWriter.beginControlFlow("if( %s == null )",valueName);
    javaWriter.beginControlFlow("synchronized (%s.class)",className);
    javaWriter.emitStatement("%s = new %s()",valueName,className);
    javaWriter.endControlFlow();
    javaWriter.endControlFlow();
    javaWriter.emitStatement("return %s",valueName);
    javaWriter.endMethod()
        .emitEmptyLine();
  }

  public void generateClear() throws IOException{
    if (staticClassList == null) {
      return;
    }
    javaWriter.beginMethod("void", "clearAll",
        EnumSet.of(Modifier.PUBLIC,Modifier.STATIC),CONTEXT,CTX);
    for (Map.Entry<String,String> entry: staticClassList.entrySet()) {
      String classNameKey = entry.getKey();
      String propertyName = getValueName(classNameKey);
      javaWriter.beginControlFlow("if (%s == null)",propertyName);
      javaWriter.emitStatement("%s = new %s() ",propertyName,classNameKey);
      javaWriter.endControlFlow();
      javaWriter.emitStatement("%s.clear(%s)",propertyName,CTX).emitEmptyLine();
    }
    javaWriter.endMethod();
  }

  public void generateConst(EnumSet modifier, String type, String name,String value)
      throws IOException{
    javaWriter.emitField(type, name, modifier, value);
  }


  public String getStaticMethodName(String className) {
    if (className.endsWith(CLASS_PROCESSOR)) {
      return className.replace(CLASS_PROCESSOR,"");
    }
    return className;
  }

  private String getValueName(String className) {
    if (className.endsWith(CLASS_PROCESSOR)) {
      return "_" + className.replace(CLASS_PROCESSOR,"");
    }
    return "_" + toLowerCase(className);
  }

  // 首字母小写转化为大写
  private String toUpperCase(String type) {
    char[] ch = type.toCharArray();
    if (ch[0] >= 'a' && ch[0] <= 'z' ) {
      ch[0] -=32;
    }
    return String.valueOf(ch);
  }

  // 首字母大写转化为小写
  private String toLowerCase(String type) {
    char[] ch = type.toCharArray();
    if (ch[0] >= 'a' && ch[0] <= 'z' ) {
      ch[0] +=32;
    }
    return String.valueOf(ch);
  }


  private ArrayList<String> getImports(Map<String,String> classList) {
    if (classList == null) {
      return null;
    }
    ArrayList<String> importsList = new ArrayList<>(20);
    for (Map.Entry<String,String> entry : classList.entrySet()) {
      String key = entry.getKey();
      String pkg = entry.getValue();
      importsList.add(pkg + "."+ key);
    }
    importsList.trimToSize();
    return importsList;
  }

/*  private String removeSameSymbol(String string) {

    Pattern p = Pattern.compile("%%\\d+");
    Matcher m = p.matcher(string);
    if (m.find()) {
      string = string.substring(0,m.start());
      messager.printMessage(Diagnostic.Kind.OTHER," ==== " + string);
    }
    return string;
  }*/
}
