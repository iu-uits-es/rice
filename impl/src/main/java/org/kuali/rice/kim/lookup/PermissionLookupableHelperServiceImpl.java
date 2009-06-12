/*
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kim.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.util.MaxAgeSoftReference;
import org.kuali.rice.kim.bo.impl.PermissionImpl;
import org.kuali.rice.kim.bo.impl.RoleImpl;
import org.kuali.rice.kim.bo.role.impl.KimPermissionImpl;
import org.kuali.rice.kim.bo.role.impl.RolePermissionImpl;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.lookup.CollectionIncomplete;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.service.LookupService;

/**
 * This is a description of what this class does - bhargavp don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class PermissionLookupableHelperServiceImpl extends RoleMemberLookupableHelperServiceImpl {

	private static final long serialVersionUID = -3578448525862270477L;

	private static final Logger LOG = Logger.getLogger( PermissionLookupableHelperServiceImpl.class );
	
	private static LookupService lookupService;
	
	/**
	 * @see org.kuali.rice.kns.lookup.KualiLookupableHelperServiceImpl#getSearchResults(java.util.Map)
	 */
	@Override
	protected List<? extends BusinessObject> getMemberSearchResults(Map<String, String> searchCriteria, boolean unbounded) {
		Map<String, String> permissionSearchCriteria = buildSearchCriteria(searchCriteria);
		Map<String, String> roleSearchCriteria = buildRoleSearchCriteria(searchCriteria);
		boolean permissionCriteriaEmpty = permissionSearchCriteria==null || permissionSearchCriteria.isEmpty();
		boolean roleCriteriaEmpty = roleSearchCriteria==null || roleSearchCriteria.isEmpty();
		
		List<PermissionImpl> permissionSearchResultsCopy = new CollectionIncomplete<PermissionImpl>(new ArrayList<PermissionImpl>(), new Long(0));
		if(!permissionCriteriaEmpty && !roleCriteriaEmpty){
			permissionSearchResultsCopy = getCombinedSearchResults(permissionSearchCriteria, roleSearchCriteria, unbounded);
		} else if(permissionCriteriaEmpty && !roleCriteriaEmpty){
			permissionSearchResultsCopy = getPermissionsWithRoleSearchCriteria(roleSearchCriteria, unbounded);
		} else if(!permissionCriteriaEmpty && roleCriteriaEmpty){
			permissionSearchResultsCopy = getPermissionsWithPermissionSearchCriteria(permissionSearchCriteria, unbounded);
		} else if(permissionCriteriaEmpty && roleCriteriaEmpty){
			return getAllPermissions(unbounded);
		}
		return permissionSearchResultsCopy;
	}
	
	private List<PermissionImpl> getAllPermissions(boolean unbounded){
		List<PermissionImpl> permissions = searchPermissions(new HashMap<String, String>(), unbounded);
		for(PermissionImpl permission: permissions)
			populateAssignedToRoles(permission);
		return permissions;
	}
	
	private List<PermissionImpl> getCombinedSearchResults(
			Map<String, String> permissionSearchCriteria, Map<String, String> roleSearchCriteria, boolean unbounded){
		List<PermissionImpl> permissionSearchResults = searchPermissions(permissionSearchCriteria, unbounded);
		List<RoleImpl> roleSearchResults = searchRoles(roleSearchCriteria, unbounded);
		List<PermissionImpl> permissionsForRoleSearchResults = getPermissionsForRoleSearchResults(roleSearchResults, unbounded);
		List<PermissionImpl> matchedPermissions = new CollectionIncomplete<PermissionImpl>(
			new ArrayList<PermissionImpl>(), getActualSizeIfTruncated(permissionsForRoleSearchResults));
		if((permissionSearchResults!=null && !permissionSearchResults.isEmpty()) && 
				(permissionsForRoleSearchResults!=null && !permissionsForRoleSearchResults.isEmpty())){
			for(PermissionImpl permission: permissionSearchResults){
				for(PermissionImpl permissionFromRoleSearch: permissionsForRoleSearchResults){
					if(permissionFromRoleSearch.getPermissionId().equals(permission.getPermissionId()))
						matchedPermissions.add(permissionFromRoleSearch);
				}
			}
		}
		return matchedPermissions;
	}
	
	@SuppressWarnings("unchecked")
	private List<PermissionImpl> searchPermissions(Map<String, String> permissionSearchCriteria, boolean unbounded){
		return getPermissionsSearchResultsCopy((List<KimPermissionImpl>)getLookupService().findCollectionBySearchHelper(
				KimPermissionImpl.class, permissionSearchCriteria, unbounded));	
	}
	
	@SuppressWarnings("unchecked")
	private List<RoleImpl> searchRoles(Map<String, String> roleSearchCriteria, boolean unbounded){
		return (List<RoleImpl>)getLookupService().findCollectionBySearchHelper(
					RoleImpl.class, roleSearchCriteria, unbounded);
	}
	
	private List<PermissionImpl> getPermissionsWithRoleSearchCriteria(Map<String, String> roleSearchCriteria, boolean unbounded){
		return getPermissionsForRoleSearchResults(searchRoles(roleSearchCriteria, unbounded), unbounded);
	}

	private List<PermissionImpl> getPermissionsForRoleSearchResults(List<RoleImpl> roleSearchResults, boolean unbounded){
		Long actualSizeIfTruncated = getActualSizeIfTruncated(roleSearchResults);
		List<PermissionImpl> permissions = new ArrayList<PermissionImpl>();
		List<PermissionImpl> tempPermissions;
		List<String> collectedPermissionIds = new ArrayList<String>();
		Map<String, String> permissionCriteria;
		for(RoleImpl roleImpl: roleSearchResults){
			permissionCriteria = new HashMap<String, String>();
			permissionCriteria.put("rolePermissions.roleId", roleImpl.getRoleId());
			tempPermissions = searchPermissions(permissionCriteria, unbounded);
			actualSizeIfTruncated += getActualSizeIfTruncated(tempPermissions);
			for(PermissionImpl permission: tempPermissions){
				if(!collectedPermissionIds.contains(permission.getPermissionId())){
					populateAssignedToRoles(permission);
					collectedPermissionIds.add(permission.getPermissionId());
					permissions.add(permission);
				}
			}
		}
		return new CollectionIncomplete<PermissionImpl>(permissions, actualSizeIfTruncated);
	}

	private void populateAssignedToRoles(PermissionImpl permission){
		AttributeSet criteria;
		for(RolePermissionImpl rolePermission: permission.getRolePermissions()){
			if ( rolePermission.isActive() ) {
				criteria = new AttributeSet();
				criteria.put("roleId", rolePermission.getRoleId());
				permission.getAssignedToRoles().add((RoleImpl)getBusinessObjectService().findByPrimaryKey(RoleImpl.class, criteria));
			}
		}
	}
	
	/* Since most queries will only be on the template namespace and name, cache the results for 30 seconds
	 * so that queries against the details, which are done in memory, do not require repeated database trips.
	 */
    private static final HashMap<Map<String,String>,MaxAgeSoftReference<List<PermissionImpl>>> permResultCache = new HashMap<Map<String,String>, MaxAgeSoftReference<List<PermissionImpl>>>(); 
	private static final long PERM_CACHE_EXPIRE_SECONDS = 30L;
	
	private List<PermissionImpl> getPermissionsWithPermissionSearchCriteria(
			Map<String, String> permissionSearchCriteria, boolean unbounded){
		String detailCriteriaStr = permissionSearchCriteria.remove( DETAIL_CRITERIA );
		AttributeSet detailCriteria = parseDetailCriteria(detailCriteriaStr);

		MaxAgeSoftReference<List<PermissionImpl>> cachedResult = permResultCache.get(permissionSearchCriteria);
		List<PermissionImpl> permissions = null;
		if ( cachedResult == null || cachedResult.get() == null ) {
			permissions = searchPermissions(permissionSearchCriteria, unbounded);
			synchronized (permResultCache) {
				permResultCache.put(permissionSearchCriteria, new MaxAgeSoftReference<List<PermissionImpl>>( PERM_CACHE_EXPIRE_SECONDS, permissions ) ); 
			} 
		} else {
			permissions = cachedResult.get();
		}
		List<PermissionImpl> filteredPermissions = new CollectionIncomplete<PermissionImpl>(
				new ArrayList<PermissionImpl>(), getActualSizeIfTruncated(permissions)); 
		for(PermissionImpl perm: permissions){
			if ( detailCriteria.isEmpty() ) {
				filteredPermissions.add(perm);
				populateAssignedToRoles(perm);
			} else {
				if ( isMapSubset( perm.getDetails(), detailCriteria ) ) {
					filteredPermissions.add(perm);
					populateAssignedToRoles(perm);
				}
			}
		}
		return filteredPermissions;
	}
	
	private List<PermissionImpl> getPermissionsSearchResultsCopy(List<KimPermissionImpl> permissionSearchResults){
		List<PermissionImpl> permissionSearchResultsCopy = new CollectionIncomplete<PermissionImpl>(
			new ArrayList<PermissionImpl>(), getActualSizeIfTruncated(permissionSearchResults));
		for(KimPermissionImpl permissionImpl: permissionSearchResults){
			PermissionImpl permissionCopy = new PermissionImpl();
			try{
				PropertyUtils.copyProperties(permissionCopy, permissionImpl);
			} catch(Exception ex){
				LOG.error( "Unable to copy properties from KimPermissionImpl to PermissionImpl, skipping.", ex );
				continue;
			}
			permissionSearchResultsCopy.add(permissionCopy);
		}
		return permissionSearchResultsCopy;
	}

	/**
	 * @return the lookupService
	 */
	public LookupService getLookupService() {
		if ( lookupService == null ) {
			lookupService = KNSServiceLocator.getLookupService();
		}
		return lookupService;
	}


}