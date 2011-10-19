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

/**
 * Run this class in order to run a web server with your application. To enable automatic reloading use JRebel (
 * {@link http ://www.zeroturnaround.com/jrebel/} )
 * 
 * @author Jacek Olszak
 */
public class Server {

    public static void main(String[] args) throws InterruptedException {
        ForkedJettyServer server = new ForkedJettyServer();
        System.setProperty(Config.MODE, "development");
        server.start();
    }
}
