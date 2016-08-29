package lucky.com.preferenceannotion;

import com.lucky.PreferenceField;
import com.lucky.PreferenceItem;

/**
 * Created by yuchao on 8/26/16.
 */
@PreferenceItem
public class TrackPreference {
  @PreferenceField
  public int name;

  @PreferenceField
  public boolean hello;

  @PreferenceField
  public long world;

  @PreferenceField
  public String result;

}
