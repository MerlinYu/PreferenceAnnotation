package lucky.com.preferenceannotion;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import lucky.com.preferenceannotion.AutoPreferenceManager;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

//    AutoPerferenceManager autoPerferenceManager;
    int id  = AutoPreferenceManager.LoginPreference().getAd(this);
    AutoPreferenceManager.LoginPreference().setId(this,"helloworld!");
    String name = AutoPreferenceManager.LoginPreference().getId(this);
    Log.v("===tag===","  AutoPreferenceManager ====" +name);





  }



}
