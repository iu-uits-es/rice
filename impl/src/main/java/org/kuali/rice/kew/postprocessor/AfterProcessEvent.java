/*
 * Copyright 2008-2009 The Kuali Foundation
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
package org.kuali.rice.kew.postprocessor;


/**
 * Event sent to the postprocessor when the processor is ended
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class AfterProcessEvent implements IDocumentEvent {

	private static final long serialVersionUID = 2945081851810845704L;
	private Long routeHeaderId;
	private Long nodeInstanceId;
	private String appDocId;
	private boolean successfullyProcessed;

	public AfterProcessEvent(Long routeHeaderId, String appDocId, Long nodeInstanceId, boolean successfullyProcessed) {
		this.routeHeaderId = routeHeaderId;
		this.appDocId = appDocId;
		this.nodeInstanceId = nodeInstanceId;
		this.successfullyProcessed = successfullyProcessed;
	}
	
	public Long getNodeInstanceId() {
	    return nodeInstanceId;
	}

	public Long getRouteHeaderId() {
		return routeHeaderId;
	}

	public String getAppDocId() {
		return appDocId;
	}
	
    public boolean isSuccessfullyProcessed() {
        return this.successfullyProcessed;
    }

    public String getDocumentEventCode() {
        return IDocumentEvent.AFTER_PROCESS;
    }

}