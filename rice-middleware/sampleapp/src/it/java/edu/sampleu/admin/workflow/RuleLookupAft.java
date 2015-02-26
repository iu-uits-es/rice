/*
 * Copyright 2006-2014 The Kuali Foundation
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

package edu.sampleu.admin.workflow;

import org.junit.Test;
import org.kuali.rice.testtools.selenium.AutomatedFunctionalTestUtils;
import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;
import org.kuali.rice.testtools.selenium.WebDriverUtils;
import org.openqa.selenium.By;

/**
 * Created by fraferna on 9/18/14.
 */
public class RuleLookupAft extends WebDriverLegacyITBase {

    public static final String BOOKMARK_URL =
            AutomatedFunctionalTestUtils.PORTAL
                    + "?channelTitle=Routing%20Rules&channelUrl="
                    + WebDriverUtils.getBaseUrlString()
                    + "/kr/lookup.do?businessObjectClassName=org.kuali.rice.kew.rule.RuleBaseValues&docFormKey=88888888&returnLocation="
                    + AutomatedFunctionalTestUtils.PORTAL_URL
                    + AutomatedFunctionalTestUtils.HIDE_RETURN_LINK
                    + AutomatedFunctionalTestUtils.SHOW_MAINTENANCE_LINKS;

    public static final String RULE_TEMPLATE_NAME = "TravelRequest-DestinationRouting";
    private static final String RULE_TEMPLATE_ATTRIBUTE_NAME = "Destination";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws InterruptedException {
        waitAndClickByLinkText("Routing Rules");
    }

    protected void testRuleLookupForTemplateAttributesAppearingInResults() throws Exception {
        selectFrameIframePortlet();
        waitAndClickSearch();
        Thread.sleep(2000);
        int defaultColumnCount = getResultColumnHeaderCount();
        // Click the button to search rule templates
        waitAndClick(By.xpath("//*[@id=\"lookup\"]/table/tbody/tr[2]/td/input"));
        waitAndTypeById("name", RULE_TEMPLATE_NAME);
        waitAndClickSearch();
        waitAndClickByLinkText("return value");
        Thread.sleep(2000);
        //Ensure that the attribute appears as a searchable field.
        assertTextPresent(RULE_TEMPLATE_ATTRIBUTE_NAME);
        waitAndClickSearch();
        Thread.sleep(2000);
        assertTrue(getResultColumnHeaderCount() > defaultColumnCount);
        // assert that the attribute appears as a column header.
        assertTrue(isElementPresentByLinkText(RULE_TEMPLATE_ATTRIBUTE_NAME));
    }

    private int getResultColumnHeaderCount() {
        return driver.findElement(By.xpath("//*[@id=\"row\"]/thead/tr")).findElements(By.tagName("th")).size();
    }

    @Test
    public void testRuleTemplateBookmark() throws Exception {
        testRuleLookupForTemplateAttributesAppearingInResults();
        passed();
    }

    @Test
    public void testRuleTemplateNav() throws Exception {
        testRuleLookupForTemplateAttributesAppearingInResults();
        passed();
    }

}
