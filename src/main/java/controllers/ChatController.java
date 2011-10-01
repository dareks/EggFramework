package controllers;

import static framework.GlobalHelpers.*;

import java.util.Date;
import java.util.List;

import services.Application;
import services.ChatService.ChatCallback;
import services.ChatService.Message;
import framework.AsyncForward;
import framework.Response;

/**
 * Sample use of asynchronous actions - create long running actions without
 */
public class ChatController {

    Application app;

    /**
     * Executed using AJAX to listen incoming messages (note the underscore in the begining - this means that this
     * action does not render the layout)
     */
    public AsyncForward _listen() {
        final AsyncForward forward = asyncForward();
        Date date = new Date(paramAsLong("date", new Date().getTime()));
        System.out.println("LISTEN " + date);
        app.chatService.register(date, new ChatCallback() {
            public void onMessages(List<Message> messages) {
                forward.attr("textMessages", messages); // sets the attribute which will be passed to _listenAsync
                                                        // action
                forward.resume(); // execute the _listenAsync action
            }
        });
        return forward; // gives Egg Framework a hint that this action did not finished yet
    }

    /**
     * Executed by framework when calling forward.resume(), that is, when message arrive
     */
    public Response _listenAsync() {
        return renderJSON(attr("textMessages")); // textMessages is used (message attribute is used by Egg Framework
                                                 // itself)
    }

    public Response send() {
        String message = param("message");
        app.chatService.send(message);
        return renderText("");
    }

}
