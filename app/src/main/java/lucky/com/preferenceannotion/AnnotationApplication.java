package lucky.com.preferenceannotion;

import android.app.Application;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lucky.PreferenceConstant;

/**
 * Created by yuchao on 8/26/16.
 */
public class AnnotationApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
//    PreferenceManager.
//    AutoPreferenceManager
    Log.v("===tag=== ", this.getPackageName());
    PreferenceConstant.setPkgName(this.getPackageName());

  }
}
