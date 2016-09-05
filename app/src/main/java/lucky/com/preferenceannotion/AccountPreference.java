package lucky.com.preferenceannotion;

import com.lucky.PreferenceField;
import com.lucky.PreferenceItem;

/**
 * Created by yuchao on 8/26/16.
 */
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

  @PreferenceField (defaultValue = "123")
  public double merlin;
}
