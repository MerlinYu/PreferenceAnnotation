# PreferenceAnnotation

#background
When we develop android apps , there will be many many preferences,and we have to write many same code to set and get the value.
This plugin will use apt to solve that problem.Use this plugin will help you manage prefrence class.

#configuration
in base module build.gradle:
```bash
	dependencies {
 		classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
	}
```
in app module build.gradle:
```bash
	apply plugin: 'com.neenbedankt.android-apt'
	dependencies {
    	compile project(':preference_annotation')
    	apt project(':preference_processor')
	}
```
#use 
```Java
@PreferenceItem( tableName = "account")
public class AccountPreference {
  @PreferenceField
  public String name;
  
  @PreferenceField
  public String account;

  @PreferenceField( defaultValue = "true")
  public boolean ok;

  @PreferenceField( defaultValue = "10")
  public int kitty;
}
```
Android studio : Build->Rebuild Project 

the dest file will be:app->build->generated->source->apt->debug

file:AccountPrefreenceProcessor and AutoPreferenceManager

we can use AutoPreferenceManager to set and get preference value like this:
```Java
        AutoPreferenceManager.AccountPreference().setKitty(MainActivity.this,20);
        AutoPreferenceManager.AccountPreference().getKitty(MainActivity.this);
        AutoPreferenceManager.clearAll(MainActivity.this);
```
   
#principle
The principle is to use Java Annotation Processor to generate code automatically.
There define two annotation type @PreferenceItem and @PreferenceField.The first type is use for preference class and 
the second is use for property field.


Copyright MerlinYu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
