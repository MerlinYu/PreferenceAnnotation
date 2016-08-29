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

  @PreferenceField
  public boolean ok;
}
