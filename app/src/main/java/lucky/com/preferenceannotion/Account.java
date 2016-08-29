package lucky.com.preferenceannotion;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yuchao on 8/26/16.
 */
public class Account {


  private Context context;
  private static String table = "";

  private static Account account;

  public Account(Context context){
    this.context = context;
  }

  public  static Account context(Context context) {
    if (account == null) {
      account = new Account(context);
    }
    return account;
  }


  public String getPreferenceTable() {
    return Account.table;
  }


  public String getId(String id) {
//    boolean
    SharedPreferences sharedPreferences = context.getSharedPreferences(Account.table, Context.MODE_PRIVATE);
    sharedPreferences.getBoolean("",false);
    sharedPreferences.getLong("",0);
    sharedPreferences.getInt("",0);

    SharedPreferences.Editor  editor = sharedPreferences.edit();
    editor.putString("","");
    editor.apply();

    return sharedPreferences.getString(id,null);

  }


  public void clear() {
    SharedPreferences sharedPreferences = context.getSharedPreferences(Account.table, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
//    editor.
    editor.clear();
    editor.apply();
  }
}
