package lucky.com.preferenceannotion;

import com.lucky.PreferenceField;
import com.lucky.PreferenceItem;

/**
 * Created by yuchao on 8/26/16.
 */
@PreferenceItem
public class LoginPreference {

  @PreferenceField
  public int time;

  @PreferenceField
  public String id;

  @PreferenceField
  public int ad;
}
