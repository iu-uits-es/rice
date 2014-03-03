/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kim.bo.entity.impl;

import java.util.List;

import javax.persistence.MappedSuperclass;

import org.kuali.rice.kns.bo.DefaultableInactivateable;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

/**
 * This is a description of what this class does - jonathan don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@MappedSuperclass
public abstract class KimEntityDataBase extends PersistableBusinessObjectBase {


	protected DefaultableInactivateable getDefaultItem( List<? extends DefaultableInactivateable> list ) {
		// find the default entry
		for ( DefaultableInactivateable item : list ) {
			if ( item.isDefault() && item.isActive() ) {
				return item;
			}
		}
		// if no default, return the first
		if ( list.size() > 0 ) {
			return list.get( 0 );
		}
		// if neither, return null
		return null;		
	}
	
}