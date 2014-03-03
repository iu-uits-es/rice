/*
 * Copyright 2005-2008 The Kuali Foundation
 * 
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.batch;

import java.io.IOException;
import java.io.InputStream;

/**
 * Xml document that is backed by an input stream
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class InputStreamXmlDoc extends BaseXmlDoc {

    private String name;
    private InputStream inputStream;
    
    public InputStreamXmlDoc(InputStream inputStream) {
        this("", inputStream);
    }
    
    public InputStreamXmlDoc(String name, InputStream inputStream) {
        super(null);
        this.name = name;
        this.inputStream = inputStream;
    }

    public String getName() {
        return name;
    }

    public InputStream getStream() throws IOException {
        return inputStream;
    }

}