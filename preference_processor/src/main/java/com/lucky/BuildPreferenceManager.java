package com.lucky;

import com.squareup.javawriter.JavaWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
 * </strong>
 *
 * <p>
 *   AccountPreferenceManager.****.set**();
 * </p>
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
  Set<String> successSet;

  private BuildPreferenceManager() {}


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
    JavaFileObject managerFile = filer.createSourceFile(pkgName + "." + destClassName);

    javaWriter = new JavaWriter(managerFile.openWriter());
    ArrayList<String> importsList = getImports(staticClassList);
    messager.printMessage(Diagnostic.Kind.NOTE, " imports package:" + importsList.toString());
    javaWriter.emitPackage(pkgName)
        .emitImports("android.content.Context")
        .emitImports(importsList)
        .emitEmptyLine()
        .beginType(destClassName, "class", EnumSet.of(Modifier.PUBLIC))
        .emitEmptyLine();

    for (Map.Entry<String,String> entry : staticClassList.entrySet()) {
      String className = entry.getKey();
      String pkgName = entry.getValue();
      if(generatePropertyClass(className, pkgName)) {
        if (successSet == null) {
          successSet = new HashSet<>(20);
        }
        successSet.add(className);
      }
    }
    generateClear(successSet);
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
    staticClassList.put(className, pkgName);
  }

  /**
   * generate class property {class a = null}
   * @param pkgName  the class belong package com.*.*;
   * @param className class name like:AccountPreferenceProcessor
   * * */
  public boolean generatePropertyClass(String className,String pkgName) {
    if (pkgName == null || className == null) {
      return false;
    }
    String valueName = getValueName(className);
    try {
      generateConst(EnumSet.of(Modifier.PRIVATE,Modifier.STATIC),className,
          valueName,"null");
      generateStaticMethod(className);
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, " Generate Property Filed IOException!" + e.getMessage());
      return false;
    }
    return true;
  }


  //generate static get Preference method
  public void generateStaticMethod(String className) throws IOException{
    javaWriter.beginMethod(className, getStaticMethodName(className),
        EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));
    String valueName = getValueName(className);
    javaWriter.beginControlFlow("if( %s == null )",valueName)
        .beginControlFlow("synchronized (%s.class)",className)
        .emitStatement("%s = new %s()",valueName,className)
        .endControlFlow()
        .endControlFlow()
        .emitStatement("return %s",valueName)
        .endMethod()
        .emitEmptyLine();
  }

  public void generateClear(Set<String> clearClassList) throws IOException{
    if (clearClassList == null) {
      return;
    }
    javaWriter.beginMethod("void", "clearAll",
        EnumSet.of(Modifier.PUBLIC,Modifier.STATIC),CONTEXT,CTX);
    Iterator iterator = clearClassList.iterator();
    while (iterator.hasNext()) {
      String classClearName = (String)iterator.next();
      String propertyName = getValueName(classClearName);
      javaWriter.beginControlFlow("if (%s == null)",propertyName)
          .emitStatement("%s = new %s() ",propertyName,classClearName)
          .endControlFlow()
          .emitStatement("%s.clear(%s)",propertyName,CTX).emitEmptyLine();
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
    return "_" + PreferenceUtils.toLowerCase(className);
  }



  private ArrayList<String> getImports(Map<String,String> classList) {
    if (classList == null) {
      return null;
    }
    ArrayList<String> importsList = new ArrayList<>(20);
    for (Map.Entry<String,String> entry : classList.entrySet()) {
      String key = entry.getKey();
      String pkg = entry.getValue();
      /** class's package  is the same as AutoPreferenceManager's package name don't need to imports its package*/
      if (!pkg.equals(pkgName)) {
        importsList.add(pkg + "."+ key);
      }
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
