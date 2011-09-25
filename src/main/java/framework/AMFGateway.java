package framework;

import static framework.GlobalHelpers.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.Java15AmfMessageSerializer;
import flex.messaging.io.amf.MessageBody;

/**
 * Gateway is a place where Flash client connects and sends messages. Every Flash remoting action is executed through this gateway. This class can be compared to FrontController but it adds the
 * functionality of handling many incoming messages in a single HTTP request
 */
public class AMFGateway extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Java15AmfMessageSerializer serializer = new Java15AmfMessageSerializer();
        try {
            SerializationContext ctx = getSerializationContext();
            AmfMessageDeserializer deserializer = new AmfMessageDeserializer();
            InputStream is = req.getInputStream();
            boolean traceEnabled = Config.isTrue("amfGateway.trace");
            AmfTrace inTrace = traceEnabled ? new AmfTrace() : null;
            deserializer.initialize(ctx, is, inTrace);

            ActionMessage inputMessage = new ActionMessage();
            ActionContext actionContext = new ActionContext();
            deserializer.readMessage(inputMessage, actionContext);
            if (traceEnabled) {
                Loggers.CONTROLLER.info("{}", inTrace);
            }

            AmfTrace outTrace = traceEnabled ? new AmfTrace() : null;
            ActionMessage outputMessage = new ActionMessage();
            List<MessageBody> bodies = inputMessage.getBodies();
            for (MessageBody body : bodies) {
                String targetURI = body.getTargetURI();
                String responseURI = body.getResponseURI();
                req.setAttribute(ACTION_URI, targetURI);
                req.setAttribute(ACTION_DATA, body.getData());

                resp.setContentType("application/x-amf");

                serializer.initialize(ctx, resp.getOutputStream(), outTrace);

                // include to FrontController
                req.getRequestDispatcher(targetURI + ".html").include(req, resp);
                Object object = req.getAttribute(ACTION_RETURNED_OBJECT);

                outputMessage.addBody(new MessageBody(responseURI + "/onResult", targetURI, object));
                resp.getOutputStream().flush();
            }
            serializer.writeMessage(outputMessage);
            if (traceEnabled) {
                Loggers.CONTROLLER.info("{}", outTrace);
            }
        } catch (ClassNotFoundException e) {
            resp.sendError(400, e.getMessage());
            Loggers.CONTROLLER.error(e.getMessage(), e);
        }
        resp.getOutputStream().flush();
    }

    private static SerializationContext getSerializationContext() {
        SerializationContext serializationContext = new SerializationContext();
        serializationContext.enableSmallMessages = true;
        serializationContext.instantiateTypes = true;
        // use _remoteClass field
        serializationContext.supportRemoteClass = true;
        // false Legacy Flex 1.5 behavior was to return a java.util.Collection
        // for Array
        // ture New Flex 2+ behavior is to return Object[] for AS3 Array
        serializationContext.legacyCollection = false;

        serializationContext.legacyMap = false;
        // false Legacy flash.xml.XMLDocument Type
        // true New E4X XML Type
        serializationContext.legacyXMLDocument = false;

        // determines whether the constructed Document is name-space aware
        serializationContext.legacyXMLNamespaces = false;
        serializationContext.legacyThrowable = false;
        serializationContext.legacyBigNumbers = false;

        serializationContext.restoreReferences = false;
        serializationContext.logPropertyErrors = false;
        serializationContext.ignorePropertyErrors = true;
        return serializationContext;
    }

}
