/**
 * 
 */
package oqube.bytes.events;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nono
 * 
 */
public class EventRecorder {

  private List<ClassFileIOEvent> events = new ArrayList<ClassFileIOEvent>();

  public void notify(ClassFileIOEvent event, int count) {
    events.add(event);
  }

  public List getEvents() {
    return events;
  }

}
