package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.text.OrderedText;
import net.shoreline.client.api.event.Cancelable;
import net.shoreline.client.api.event.Event;

/**
 * @author Hypinohaizin
 * @since 2024/11/15 19:45
 */

@Cancelable
public class ChatTextEvent extends Event {

 private OrderedText text;

 public ChatTextEvent(OrderedText text) {
  this.text = text;
 }

 public void setText(OrderedText text) {
  this.text = text;
 }

 public OrderedText getText() {
  return text;
 }
}
