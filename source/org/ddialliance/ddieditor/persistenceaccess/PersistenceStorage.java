package org.ddialliance.ddieditor.persistenceaccess;

import java.io.File;
import java.util.List;

import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQuery;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQueryResult;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLightLabelQueryResult;

public interface PersistenceStorage {

	/**
	 * Execute query against persistence storage
	 * 
	 * @param query
	 *            to execute
	 * @return list of results
	 * @throws Exception
	 */
	public List<String> query(String query) throws Exception;

	/**
	 * Execute an update query against persistence storage
	 * 
	 * @param update
	 *            query to execute
	 * @throws Exception
	 */
	public void updateQuery(String query) throws Exception;

	public boolean querySingleBoolean(String query) throws Exception;

	public String querySingleString(String query) throws Exception;

	public MaintainableLabelQueryResult queryMaintainableLabel(
			MaintainableLabelQuery maintainableLabelQuery,
			MaintainableLabelQueryResult maintainableLabelQueryResult)
			throws Exception;

	public MaintainableLightLabelQueryResult queryMaintainableLightLabel(
			MaintainableLabelQuery maintainableLabelQuery,
			MaintainableLightLabelQueryResult maintainableLightLabelQueryResult)
			throws Exception;

	/**
	 * Set the the working connection of the persistence manager
	 * 
	 * @param storage
	 * @throws Exception
	 */
	public void setWorkingConnection(StorageType storage) throws Exception;

	/**
	 * Retrieve the resource path expressed in XQuery by working resource
	 * 
	 * @param storage
	 * @return resource
	 * @throws Exception
	 */
	public String getResourcePath(StorageType storage, String resource)
			throws Exception;

	/**
	 * Retrieve the global resource path expressed in XQuery
	 * 
	 * @param storage
	 * @return global resource path
	 * @throws Exception
	 */
	public String getGlobalResourcePath(StorageType storage) throws Exception;

	/**
	 * Add a resource to a storage
	 * 
	 * @param obj
	 *            resource to add
	 * @throws Exception
	 */
	public void addResource(Object obj) throws Exception;

	/**
	 * Remove a resource from a storage
	 * 
	 * @param id
	 *            xml file to remove
	 * @throws Exception
	 */
	public void removeResource(String id) throws Exception;

	/**
	 * Retrieve ids of all resources
	 * 
	 * @return list of resource ids
	 * @throws Exception
	 */
	public List<String> getResources() throws Exception;

	/**
	 * Add a xml resource to storage
	 * 
	 * @param obj
	 *            storage to add
	 * @throws Exception
	 */
	public void addStorage(Object obj) throws Exception;

	/**
	 * Remove a storage
	 * 
	 * @param id
	 *            of storage to remove
	 * @throws Exception
	 */
	public void removeStorage(String id) throws Exception;

	/**
	 * Retrieve ids of all storages
	 * 
	 * @return list of resource ids
	 * @throws Exception
	 */
	public List<String> getStorages() throws Exception;

	/**
	 * Export a resource from storage to a file
	 * 
	 * @param id
	 *            of resource in storage
	 * @param file
	 *            in file system
	 * @throws Exception
	 */
	public void exportResource(String id, File file) throws Exception;

	/**
	 * Closes down storage
	 * 
	 * @throws Exception
	 */
	public void close() throws Exception;
	
	/**
	 * Perform housekeeping tasks
	 * @throws Exception
	 */
	public void houseKeeping() throws Exception;
}