/*
 *   Copyright (C) 2011 Jacek Olszak
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package framework.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import framework.Message;

public class Errors {

    private Map<String, List<Message>> messages = Maps.newHashMap();

    public boolean hasErrors() {
        return messages.size() > 0;
    }

    public synchronized void add(Message msg) {
        add(null, msg);
    }

    public synchronized void add(String field, Message msg) {
        List<Message> list = messages.get(field);
        if (list == null) {
            list = Lists.newArrayList();
            messages.put(field, list);
        }
        list.add(msg);
    }

    public synchronized void add(String field, List<Message> msg) {
        List<Message> list = messages.get(field);
        if (list == null) {
            list = Lists.newArrayList();
            messages.put(field, list);
        }
        list.addAll(msg);
    }

    public Map<String, List<Message>> getMessagesMap() {
        return messages;
    }

    public List<Message> getMessages() {
        // TODO cache this method
        List<Message> msgs = Lists.newArrayList();
        Collection<List<Message>> values = messages.values();
        for (List<Message> list : values) {
            msgs.addAll(list);
        }
        return Collections.unmodifiableList(msgs);
    }

    public List<Message> getMessages(String field) {
        List<Message> list = messages.get(field);
        if (list == null) {
            return ImmutableList.of();
        }
        return list;
    }

    @Override
    public String toString() {
        return "Errors [messages=" + messages + "]";
    }

}
