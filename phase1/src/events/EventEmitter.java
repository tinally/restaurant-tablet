package events;

import services.framework.Service;
import services.framework.ServiceConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventEmitter extends Service {

  private Map<Class<? extends EventArgs>, List<RestaurantEventHandler<? extends EventArgs>>> eventHandlers;

  @ServiceConstructor
  public EventEmitter() {
    this.eventHandlers = new HashMap<>();
  }

  /**
   * Registers an event handler for a given event.
   * Every time an event is fired, the even handler will run.
   * @param eventHandler The event handler to run.
   * @param event The class of the event to run.
   * @param <T> The type of the event to register.
   * @see EventArgs
   */
  public <T extends EventArgs> void registerEventHandler(RestaurantEventHandler<T> eventHandler,
                                                         Class<T> event) {
    this.eventHandlers.putIfAbsent(event, new ArrayList<>());
    this.eventHandlers.get(event).add(eventHandler);
  }

  /**
   * Removes an event handler for a given event.
   * Every time an event is fired, the even handler will run.
   * @param eventHandler The event handler to run.
   * @param event The class of the event to run.
   * @param <T> The type of the event to register.
   * @see EventArgs
   */
  public <T extends EventArgs> void removeEventHandler(RestaurantEventHandler<T> eventHandler,
                                                       Class<T> event) {
    this.eventHandlers.putIfAbsent(event, new ArrayList<>());
    this.eventHandlers.get(event).remove(eventHandler);
  }

  /**
   * Fires the given event from the given sender, making all registered event handlers run.
   * @param eventArgs The arguments of the event to fire.
   * @param <T> The type of the event to fire.
   */
  @SuppressWarnings("unchecked")
  public <T extends EventArgs> void onEvent(T eventArgs) {
    for (RestaurantEventHandler<?> eventHandler : this.eventHandlers.get(eventArgs.getEventClass())) {
      if (!eventArgs.isCancelled() && eventHandler != null) {
        ((RestaurantEventHandler<T>) eventHandler).handle(eventArgs);
      }
    }
  }


}