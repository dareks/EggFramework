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
package framework;

// TODO Add support for specifying HTTP headers
public class Response {

    String action;
    String redirect;
    String template;
    boolean partial;
    String text;
    byte[] bytes;
    Object singleObject;
    String contentType = "text/html; charset=utf-8";
    Integer bufferSize;
    int status = 200;

    public String getAction() {
        return action;
    }

    public boolean isForward() {
        return action != null;
    }

    public boolean isRedirect() {
        return redirect != null;
    }

    public String getRedirect() {
        return redirect;
    }

    public String getTemplate() {
        return template;
    }

    public boolean isPartial() {
        return partial;
    }

    public String getText() {
        return text;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getContentType() {
        return contentType;
    }

    public Response withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public Response withBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Response withStatus(int status) {
        this.status = status;
        return this;
    }

}
