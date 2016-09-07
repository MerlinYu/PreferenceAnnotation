package com.lucky;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by yuchao on 8/27/16.
 * this class is mainly to build PreferenceItem class.
 * <p>
 *   if the @PreferenceItem annotates AccountAccountPreference,
 *   it will automatically build a class AccountPreferenceProcessor.
 * </p>
 * <p>Note:
 * <strong>
 *   if @PreferenceItem's tableName is AccountPreferenceProcessor's table.
 *   The default value = "preference_annotation"
 *   @see {@link @PreferenceItem}
 * </strong>
 * </p>
 * <div>
 *   AccountPreferenceProcessor include three method : set**(),get**(),clear();
 *   <strong>
 *     Normal Preference need a store key but this class will generate automacly a key.
 *     if you annotate like this {
 *     @PrefenceItem
 *     public AccountPreference {
 *       @PreferenceField
 *        String name;
 *       }
 *     }
 *     the auto key is AccountPreference_name;
 *   </strong>
 * </div>
 *
 *
 */
public class BuildPreferenceClass {

  private static final String CLASS_PROCESSOR = "Processor";
  private static final String SET = "set";
  private static final String GET = "get";
  private static final String CONTEXT = "Context";
  private static final String CTX = "ctx";
  private static final String BASIC_TYPE_STRING = "String";
  private static final String VALUE = "value";
  private static final String BASIC_TYPE_INT = "int";
  private static final String BASIC_TYPE_BOOLEAN = "boolean";
  private static final String BASIC_TYPE_LONG = "long";
  private static final String BASIC_TYPE_DOUBLE = "double";
  private static final String BASIC_TYPE_FLOAT = "float";
  private static final String BASIC_TYPE_VOID = "void";
  private static final String DEFAULT_STRING = "null";
  private static final String DEFAULT_INT = "0";
  private static final String DEFAULT_BOOLEAN = "false";


  String pkgName;
  String destClassName;
  String sourceClassName;
  Filer filer;
  Types typeUtils;
  Elements elementUtils;
  Messager messager;
  JavaWriter javaWriter;

  private BuildPreferenceClass() {

  }


  public BuildPreferenceClass(final Filer filer,
                              final Types typeUtils,
                              final Elements elementUtils,
                              final Messager messager) {
    this.filer = filer;
    this.elementUtils = elementUtils;
    this.typeUtils = typeUtils;
    this.messager = messager;
  }



  public boolean generateFile(Element element, List<Element> fieldProperty) {
    PreferenceItem item = element.getAnnotation(PreferenceItem.class);

    String table = item.tableName();
    String sourceClassName = typeUtils.asElement(element.asType()).getSimpleName().toString();
    // auto generate PreferenceItem class
    this.sourceClassName = sourceClassName;
    destClassName = sourceClassName + CLASS_PROCESSOR;
    messager.printMessage(Diagnostic.Kind.NOTE,  destClassName + " .class...");
    pkgName = elementUtils.getPackageOf(element).getQualifiedName().toString();

    try {
      JavaFileObject classFile = filer.createSourceFile(pkgName+"."+destClassName, element);

      javaWriter = new JavaWriter(classFile.openWriter());
      javaWriter.emitPackage(pkgName)
          .emitImports("android.content.Context")
          .emitImports("android.content.SharedPreferences")
          .emitEmptyLine()
          .beginType(destClassName, "class", EnumSet.of(Modifier.PUBLIC))
          .emitEmptyLine();

      generateConst(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC),
          "String","table", table );
      generateMethod(fieldProperty);
      javaWriter.endType();
      messager.printMessage(Diagnostic.Kind.NOTE, destClassName + " success");
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.NOTE, destClassName + " failed");
      messager.printMessage(Diagnostic.Kind.ERROR, "IOException " + e.getMessage());
      return false;
    } finally {
      try {
        if (javaWriter != null) {
          javaWriter.close();
        }
      } catch (IOException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IOException " + e.getMessage());
      }
    }
    return true;
  }


  public void generateConst(EnumSet modifier, String type,String name, String value)
      throws IOException{
    javaWriter.emitField(type, name, modifier, "\""+value+ "\"")
        .emitEmptyLine();
  }


  private void generateMethod(List<Element> fieldProperty) throws IOException{
    generatePropertyMethod(fieldProperty);
    generateClearMethod();
  }


  private void generatePropertyMethod(List<Element> fieldProperty)
      throws IOException,IllegalStateException{
    for (Element element : fieldProperty) {
//    field type eg: int,boolean
      PreferenceField item = element.getAnnotation(PreferenceField.class);
      String fieldDefaultValue = item.defaultValue();

      TypeMirror fieldType = element.asType();
      String type = getPropertyType(typeUtils,elementUtils,fieldType);
      String propertyName = element.getSimpleName().toString();

      // generate set method
      javaWriter.beginMethod(BASIC_TYPE_VOID, getSetMethodName(false,propertyName),
          EnumSet.of(Modifier.PUBLIC), CONTEXT, CTX, type, VALUE);
      String methodSuffix = PreferenceUtils.toUpperCase(type);
      javaWriter.emitStatement("SharedPreferences sharedPreferences " +
          "= %s.getSharedPreferences(%s.table,Context.MODE_PRIVATE)",
          CTX, destClassName);
      javaWriter.emitStatement("SharedPreferences.Editor  editor = sharedPreferences.edit()");
      String preferenceKey = getPreferenceKey(sourceClassName,propertyName);
      javaWriter.emitStatement("editor.put%s(" + "\""+preferenceKey+ "\"" + ",%s)",methodSuffix,VALUE);
      javaWriter.emitStatement("editor.apply()");
      javaWriter.endMethod();

      // get method
      javaWriter.beginMethod(type, getSetMethodName(true,propertyName),EnumSet.of(Modifier.PUBLIC),
          CONTEXT,CTX);
      javaWriter.emitStatement("SharedPreferences sharedPreferences " +
          "= %s.getSharedPreferences(%s.table,Context.MODE_PRIVATE)",
          CTX ,destClassName);
      String defaultValue = getDefaultValue(type, fieldDefaultValue);
      javaWriter.emitStatement("return sharedPreferences.get"+methodSuffix+"(%s,"+defaultValue+")",
           "\""+preferenceKey+"\"");
      javaWriter.endMethod();

    }
  }

  public boolean isNeedPackageName() {
    return false;
  }


  public void generateClearMethod() throws IOException{
    javaWriter.beginMethod("void", "clear",EnumSet.of(Modifier.PUBLIC), CONTEXT,CTX);
    javaWriter.emitStatement("SharedPreferences sharedPreferences " +
        "= %s.getSharedPreferences(%s.table,Context.MODE_PRIVATE)",
        CTX ,destClassName);
    javaWriter.emitStatement("SharedPreferences.Editor  editor = sharedPreferences.edit()");
    javaWriter.emitStatement("editor.clear()");
    javaWriter.emitStatement("editor.apply()");
    javaWriter.endMethod().emitEmptyLine();
  }



  public String getDestClassName() {
    return destClassName;
  }

  public String getPackageName() {
    return pkgName;
  }


  private String getPropertyType(Types typeUtils,Elements elementUtils,TypeMirror typeMirror) {
    // basic type
    TypeKind typeKind = typeMirror.getKind();
    if (typeKind.isPrimitive()) {
      String type = typeKind.toString().toLowerCase();
      if (type.equals("double")) {
        type = "float";
      }
      return type;
    }
    if (typeUtils.isAssignable(typeMirror, getTypeMirror(elementUtils,String.class))) {
      return BASIC_TYPE_STRING;
    }
    return null;
  }

  private TypeMirror getTypeMirror(Elements elementUtils,Class<?> type) {
    return elementUtils.getTypeElement(type.getCanonicalName()).asType();
  }


  /**
   * @param isGet
   * @param property property name like :String name,name is property
   * @return method name like:getName;
   * */
  private String getSetMethodName(boolean isGet,  String property) {
    if (PreferenceUtils.isNull(property)) {
      return null;
    }
    return isGet ?
        GET + PreferenceUtils.toUpperCase(property) : SET + PreferenceUtils.toUpperCase(property);
  }

  /**
   * @deprecated according type and filed default value  to get real default value
   * @param type
   * @param fieldDefaultValue
   * @return default value match type
   * */
  private String getDefaultValue(String type,String fieldDefaultValue) {
    if (type == null) {
      throw new IllegalStateException();
    }
    if (type.equals(BASIC_TYPE_INT)) {
      return PreferenceUtils.isInt(fieldDefaultValue) ? fieldDefaultValue : DEFAULT_INT;
    } else if (type.equals(BASIC_TYPE_STRING)){
      return PreferenceUtils.isString(fieldDefaultValue) ? fieldDefaultValue :DEFAULT_STRING;
    } else if (type.equals(BASIC_TYPE_BOOLEAN)) {
      return PreferenceUtils.isBoolean(fieldDefaultValue) ? fieldDefaultValue : DEFAULT_BOOLEAN;
    } else if (type.equals(BASIC_TYPE_LONG)) {
      return PreferenceUtils.isLong(fieldDefaultValue) ? fieldDefaultValue : DEFAULT_INT;
    } else if(type.equals(BASIC_TYPE_DOUBLE)
        || type.equals(BASIC_TYPE_FLOAT)) {
      return PreferenceUtils.isFloat(fieldDefaultValue) ? fieldDefaultValue : DEFAULT_INT;
    }
    return null;
  }

  /**
   * @deprecated according classname and propertyName to get Preference key
   * @return store Preference key like AccountPreference_name
   * */
  private String getPreferenceKey(String className,String propertyName) {
    return className +"_"+ propertyName;
  }



}
