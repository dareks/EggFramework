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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jci.stores.ResourceStore;

public class JarResourceStore implements ResourceStore {

    private String filename;

    public JarResourceStore(String filename) throws IOException {
        this.filename = filename;
    }

    public void write(String pResourceName, byte[] pResourceData) {
    }

    public synchronized byte[] read(String pResourceName) {
        JarFile jar = null;
        try {
            jar = new JarFile(URLDecoder.decode(filename, "utf-8"));
            JarEntry jarEntry = jar.getJarEntry(pResourceName);
            if (jarEntry != null) {

                InputStream inputStream = jar.getInputStream(jarEntry);
                try {
                    return IOUtils.toByteArray(inputStream);
                } finally {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            Loggers.RELOADER.error(e.getMessage(), e);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return null;
    }

    public void remove(String pResourceName) {
    }

}
