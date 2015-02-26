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

package edu.sampleu.kew;

import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;
import org.kuali.rice.testtools.selenium.WebDriverUtils;
import org.openqa.selenium.By;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fraferna on 10/15/14.
 */
public class RuleLookupAft extends WebDriverLegacyITBase {

    private static final String BOOKMARK_URL = "/kr/lookup.do?businessObjectClassName=org.kuali.rice.kew.rule.RuleBaseValues&returnLocation="
            + WebDriverUtils.getBaseUrlString()
            + "&hideReturnLink=true&docFormKey=88888888&methodToCall=start";
    private static final String INGESTER_URL = "/portal.do?channelTitle=XML%20Ingester&channelUrl="
            + WebDriverUtils.getBaseUrlString()
            + "/kew/../core/Ingester.do";

    @Override
    @Before
    public void testSetUp() {
        driver.get(INGESTER_URL);
        try {
            fileIngester(setUpFiles("KEW.Rule"));
        } catch (Exception e) {
            fail(e.getMessage() + "\n" + e.getStackTrace());
        }
        super.testSetUp();
    }

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
        waitAndClickMainMenu();
        waitAndClickByLinkText("Routing Rules");
    }

    @Test
    public void testOpeningRuleWithNoRuleTemplateNav () throws Exception {
        lookupAndOpenRuleWitNoTemplate();
        passed();
    }

    @Test
    public void testOpeningRuleWithNoRuleTemplateBookmark() throws Exception {
        lookupAndOpenRuleWitNoTemplate();
        passed();
    }

    private void lookupAndOpenRuleWitNoTemplate() throws Exception {
        String testWindowHandle = driver.getWindowHandle();
        selectFrameIframePortlet();
        waitAndType(By.id("description"), "Rule with no template");
        waitAndClickSearch();
    }

    protected List<File> setUpFiles(String path) throws Exception {
        List<File> fileUploadList = new ArrayList<File>();

        File dir = new File(path);

        if (dir != null && dir.listFiles().length > 0) {
            Integer i = 1;
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".xml") && !file.getName().equalsIgnoreCase("sample-app-config.xml")) {
                    fileUploadList.add(file);
                }
                i++;
            }
            Collections.sort(fileUploadList);
        } else {
            throw new Exception("----Resources not found----");
        }

        return fileUploadList;
    }

    /**
     * Performs Ingesting files to fileupload component and asserts succesful ingestion.
     *
     */
    private void fileIngester(List<File> fileToUpload) throws Exception {
        int cnt = 0;

        for (File file : fileToUpload) {
            String path = file.getAbsolutePath().toString();
            driver.findElement(By.name("file[" + cnt + "]")).sendKeys(path);
            cnt++;
        }

        waitAndClickById("imageField");
    }

}
