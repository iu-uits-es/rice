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
package org.kuali.rice.coreservice.impl.style;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.rice.core.api.criteria.GenericQueryResults;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.coreservice.api.style.Style;
import org.kuali.rice.coreservice.api.style.StyleContract;
import org.kuali.rice.coreservice.api.style.StyleRepositoryService;
import org.kuali.rice.krad.data.DataObjectService;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link org.kuali.rice.coreservice.impl.style.StyleRepositoryServiceImpl}.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@RunWith(MockitoJUnitRunner.class)
public class StyleRepositoryServiceImplTest {
    @Mock private DataObjectService dataObjectService;

    @InjectMocks private StyleRepositoryServiceImpl styleRepositoryService = new StyleRepositoryServiceImpl();
    private static final String STYLE_ID = "1";
    private static final String NAME = "MyFirstStyle";
    private static final boolean ACTIVE = true;
    private static final String XML_CONTENT = "<my><awesome><xml-stylesheet/></awesome></my>";
    private static final Long VERSION_NUMBER = 1L;
    private static final String OBJECT_ID = UUID.randomUUID().toString();

    private static final Style style = createStyle();

    private StyleRepositoryService styleService = styleRepositoryService;

    @Test(expected = IllegalArgumentException.class)
    public void testGetStyle_nullStyleName() throws Exception{
        getStyleRepositoryService().getStyle(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStyle_emptyStyleName() throws Exception{
        getStyleRepositoryService().getStyle("");
        verify(dataObjectService,times(1)).findMatching(argThat(
                new ClassOrSubclassMatcher<StyleBo>(StyleBo.class)), any(
                QueryByCriteria.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStyle_blankStyleName() throws Exception{
        getStyleRepositoryService().getStyle(" ");
        verify(dataObjectService,times(1)).findMatching(argThat(
                new ClassOrSubclassMatcher<StyleBo>(StyleBo.class)), any(
                QueryByCriteria.class));
    }

    @Test
    public void testGetStyle_valid() throws Exception{
        setDataObjectServiceFetchStyle(style);
        Style styleFetched = getStyleRepositoryService().getStyle(NAME);
        assertTrue("Style fetched correctly",styleFetched != null &&
                        StringUtils.equals(styleFetched.getName(),style.getName()));
        verify(dataObjectService,times(1)).findMatching(argThat(
                new ClassOrSubclassMatcher<StyleBo>(StyleBo.class)), any(
                QueryByCriteria.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveStyle_nullStyle() throws Exception{
        getStyleRepositoryService().saveStyle(null);
    }

    @Test
    public void testSaveStyle_modify() throws Exception{
        StyleBo styleBo = StyleBo.from(style);
        Style.Builder builder = Style.Builder.create(styleBo);
        builder.setActive(false);
        Style modifiedStyle = builder.build();
        assertFalse("Style active field is false", modifiedStyle.isActive());
        setDataObjectServiceFetchStyle(style);

        setDataObjectServiceFetchStyle(modifiedStyle);
        when(dataObjectService.save(anyObject())).thenReturn(modifiedStyle);
        getStyleRepositoryService().saveStyle(modifiedStyle);

        Style modifiedStyleFetched = getStyleRepositoryService().getStyle(NAME);
        assertTrue("Style modified fetched",modifiedStyleFetched != null && !modifiedStyleFetched.isActive());

        verify(dataObjectService,times(2)).findMatching(argThat(
                new ClassOrSubclassMatcher<StyleBo>(StyleBo.class)), any(
                QueryByCriteria.class));
        verify(dataObjectService,times(2)).save(anyObject());

    }

    private void setDataObjectServiceFetchStyle(Style styleToSetup){
        List<StyleBo> styleBoList = new ArrayList<StyleBo>();
        styleBoList.add(StyleBo.from(styleToSetup));
        GenericQueryResults.Builder builder = GenericQueryResults.Builder.create();

        builder.setResults(styleBoList);
        when(dataObjectService.findMatching(argThat(new ClassOrSubclassMatcher<StyleBo>(StyleBo.class)), any(
                QueryByCriteria.class))).thenReturn(builder.build());
    }


    private static Style createStyle() {
        StyleContract styleContract = new StyleContract() {
            @Override
            public String getName() {
                return StyleRepositoryServiceImplTest.NAME;
            }

            @Override
            public String getXmlContent() {
                return StyleRepositoryServiceImplTest.XML_CONTENT;
            }

            @Override
            public String getObjectId() {
                return StyleRepositoryServiceImplTest.OBJECT_ID;
            }

            @Override
            public String getId() {
                return StyleRepositoryServiceImplTest.STYLE_ID;
            }

            @Override
            public boolean isActive() {
                return StyleRepositoryServiceImplTest.ACTIVE;
            }

            @Override
            public Long getVersionNumber() {
                return StyleRepositoryServiceImplTest.VERSION_NUMBER;
            }
        };
        return Style.Builder.create(styleContract).build();
    }

    public StyleRepositoryService getStyleRepositoryService(){
        return styleService;
    }

    public void setStyleRepositoryService(StyleRepositoryService styleService){
        this.styleService = styleService;
    }



    class ClassOrSubclassMatcher<T> implements ArgumentMatcher<Class<T>> {

        private final Class<T> targetClass;

        public ClassOrSubclassMatcher(Class<T> targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public boolean matches(Class<T> argument) {
            return argument != null && targetClass.isAssignableFrom(argument);
        }
    }

}
