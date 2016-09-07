package lucky.com.preferenceannotion;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.lucky.PreferenceField;
import com.lucky.PreferenceItem;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Button btn = (Button) findViewById(R.id.set_btn);
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(MainActivity.this,"account set value!",Toast.LENGTH_LONG).show();
        AutoPreferenceManager.LoginPreference().setId(MainActivity.this,"helloworld!");
        AutoPreferenceManager.AccountPreference().setKitty(MainActivity.this,20);

      }
    });

    Button getBtn = (Button) findViewById(R.id.get_btn);
    getBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AutoPreferenceManager.AccountPreference().setKitty(MainActivity.this,20);
        AutoPreferenceManager.clearAll(MainActivity.this);
      }
    });

  }

}
