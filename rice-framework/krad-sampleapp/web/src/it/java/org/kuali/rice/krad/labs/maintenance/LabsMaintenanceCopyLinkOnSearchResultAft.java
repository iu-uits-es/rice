/**
 * Copyright 2005-2014 The Kuali Foundation
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
package org.kuali.rice.krad.labs.maintenance;

import org.junit.Test;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class LabsMaintenanceCopyLinkOnSearchResultAft extends LabsMaintenanceBase {

    /**
     * /kr-krad/kradsampleapp?viewId=KradMaintenanceSample-PageR2C3
     */
    public static final String BOOKMARK_URL = "/kr-krad/kradsampleapp?viewId=KradMaintenanceSample-PageR2C3";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
    	navigateToMaintenance("Maintenance Sample - Copy link on Search Results");
    }

    protected void testMaintenanceDefineControl() throws InterruptedException {
    	waitAndClickByLinkText("Travel Company Maintenance Sample - Copy from Search Result");
        waitAndClickButtonByText("Search");
        waitAndClickLinkContainingText("copy");
        waitForElementPresentByXpath("//div[@data-label='Company Name']");
        String companyNameValue=getTextByXpath("//div[@data-label='Company Name']");
        waitForElementPresentByXpath("//input[@name='document.newMaintainableObject.dataObject.travelCompanyName' and @value='"+companyNameValue+"']");
    }

    @Test
    public void testMaintenanceDefineControlBookmark() throws Exception {
    	testMaintenanceDefineControl();
        passed();
    }

    @Test
    public void testMaintenanceDefineControlNav() throws Exception {
    	testMaintenanceDefineControl();
        passed();
    }
}
