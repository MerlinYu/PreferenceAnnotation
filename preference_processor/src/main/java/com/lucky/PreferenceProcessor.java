package com.lucky;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class PreferenceProcessor extends AbstractProcessor {

  private static final String ERROR_MESSAGE = "only class can be annotated with @%s";

  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    types.add(PreferenceItem.class.getName());
    types.add(PreferenceField.class.getName());
    return types;
  }

  private static String NOTE = " preference annotation processor ";

  /**
   * @return true : this annotation is received by this processor,
   *                 other sub Processor will not receive this annotation.
   * */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      return true;
    }

    Set<? extends Element> preferenceFields = roundEnv.getElementsAnnotatedWith(PreferenceField.class);
    Map<Element, List<Element>> preferenceObjects = new HashMap<>(20);
    int i = 0;
    messager.printMessage(Diagnostic.Kind.NOTE, "<<<<<<<<<<< Preference Processor start to work！>>>>>>>>>>>>");
    // class element为key将对应的field形成list put 到 map中
    for (final Element field : preferenceFields) {
      messager.printMessage(Diagnostic.Kind.NOTE, "element " + i++ + " field " + field.getSimpleName());
      final Element classElement = field.getEnclosingElement();
      if (classElement.getKind() != ElementKind.CLASS) {
        sendErrorMessage(classElement, ERROR_MESSAGE, PreferenceItem.class.getSimpleName());
        return true;
      }
      if (classElement.getAnnotation(PreferenceItem.class) != null) {
        List<Element> list = preferenceObjects.get(classElement);
        if (list == null) {
          list = new ArrayList<>();
          preferenceObjects.put(classElement,list);
        }
        list.add(field);
      }
    }
    if (preferenceObjects.size() == 0) {
      return false;
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "preference objects "+preferenceObjects.toString());

    String uniquePkgName = getUniquePkgName(preferenceObjects);
    if (uniquePkgName == null) {
      messager.printMessage(Diagnostic.Kind.ERROR, "can't get unique pkg name ");
      return false;
    }
    messager.printMessage(Diagnostic.Kind.NOTE, "pkg name " + uniquePkgName);

   try {
    // auto generate PreferenceManager class
     if (preferenceObjects == null || preferenceObjects.size() == 0) {
       return false;
     }
     BuildPreferenceManager preferenceManager = new BuildPreferenceManager(filer,typeUtils,elementUtils,messager);
     if (preferenceManager.isNeedPackageName()) {
       preferenceManager.setPackageName(uniquePkgName);
     }
     for (final Map.Entry<Element, List<Element>> entry: preferenceObjects.entrySet()) {
       Element classObject = entry.getKey();
       List<Element> fieldProperty = entry.getValue();
       BuildPreferenceClass itemClass = new BuildPreferenceClass(filer, typeUtils, elementUtils, messager);
       boolean success = itemClass.generateFile(classObject, fieldProperty);
       if (success) {
         preferenceManager.addStaticClass(itemClass.getPackageName(),itemClass.getDestClassName());
       }
     }
     preferenceManager.generateFile();

     // preferenceManager.generateClear();
    // preferenceManager.endFile();
    } catch (IllegalStateException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "IllegalStateException" + e.getMessage());
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "IOException" + e.getMessage());
    } catch (NullPointerException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "NullPointerException " + e.getMessage());
   }
    messager.printMessage(Diagnostic.Kind.NOTE, "<<<<<<<<<<< Preference Processor finished！>>>>>>>>>>>>");

 /*   BuildPreferenceManagerPoet poet = new BuildPreferenceManagerPoet(filer,typeUtils,elementUtils,messager);
    poet.setPackageName(uniquePkgName);
    try {
      poet.generateFile();

    }catch (IOException e) {
       e.printStackTrace();
    }
*/

    return false;
  }

  private void sendErrorMessage(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  // 计算PreferenceManager 的包名
  private String getUniquePkgName(final Map<Element, List<Element>> elementMap) {

    Map<String,Integer> pkgMap = new HashMap<>();

    // account pkgName appear
    for (Element element : elementMap.keySet()) {
      String pkgName = elementUtils.getPackageOf(element).getQualifiedName().toString();
      Integer integer = pkgMap.get(pkgName);
      if (null == integer) {
        integer  = 1;
      } else {
        integer += 1;
      }
      pkgMap.put(pkgName, integer);
    }

    // PkgName次数最多的一个做为PreferenceManager的包名
    Integer tempCount = 0;
    String pkgName = null;
    for (Map.Entry<String,Integer> entry :pkgMap.entrySet()) {
      if (entry.getValue() > tempCount) {
        tempCount = entry.getValue();
        pkgName = entry.getKey();
      }
    }
    return pkgName;
  }
  
  public static void main(String ...args) {

  }
}
