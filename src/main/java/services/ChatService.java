package services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * TODO Simplify this example <br />
 * TODO Limit number of messages held in memory
 */
public class ChatService {

    private List<Message> messages = Lists.newArrayList();
    private List<CallbackInfo> callbacks = Collections.synchronizedList(new ArrayList<CallbackInfo>());

    public ChatService() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    // take messages from last 10 seconds
                    List<Message> newMessages = getNewMessages();
                    if (newMessages.size() > 0) {
                        List<CallbackInfo> callbacksCopy = new ArrayList<CallbackInfo>(callbacks);
                        // iterate through callbacks and add new messages to them
                        for (Message message : newMessages) {
                            for (CallbackInfo callbackInfo : callbacksCopy) {
                                if (message.date.after(callbackInfo.dateFrom)) {
                                    callbackInfo.newMessages.add(message);
                                }
                            }
                        }
                        // run callbacks
                        for (CallbackInfo callbackInfo : callbacksCopy) {
                            if (callbackInfo.newMessages.size() > 0) {
                                callbackInfo.chatCallback.onMessages(callbackInfo.newMessages);
                                callbacks.remove(callbackInfo);
                            } else if (callbackInfo.dateFrom.before(thirtySecondsAgo())) {
                                callbacks.remove(callbackInfo);
                            }
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }

                }
            }

            private Date thirtySecondsAgo() {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, -30);
                return calendar.getTime();
            }

        });
        thread.start();
    }

    public synchronized void send(String message) {
        messages.add(new Message(message, new Date()));
    }

    private synchronized List<Message> getNewMessages() {
        return new ArrayList<Message>(messages);
    }

    public void register(Date dateFrom, ChatCallback callback) {
        this.callbacks.add(new CallbackInfo(dateFrom, callback));
    }

    public static interface ChatCallback {
        void onMessages(List<Message> messages);
    }

    public static class CallbackInfo {
        final ChatCallback chatCallback;
        final Date dateFrom;
        final List<Message> newMessages;

        public CallbackInfo(Date dateFrom, ChatCallback chatCallback) {
            this.chatCallback = chatCallback;
            this.dateFrom = dateFrom;
            this.newMessages = new ArrayList<Message>();
        }
    }

    public static class Message {
        public final String message;
        public final Date date;

        public Message(String message, Date date) {
            this.message = message;
            this.date = date;
        }
    }

}
