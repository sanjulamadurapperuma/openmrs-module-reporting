/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reporting.report.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReportService API
 */
@Transactional
public interface ReportService extends OpenmrsService {
		
	/**
	 * @param uuid
	 * @return the ReportDesign with the given uuid
	 */
	@Transactional(readOnly = true)
	public ReportDesign getReportDesignByUuid(String uuid) throws APIException;
	
	
	/**
	 * Get the {@link ReportDesign} with the given id
	 * 
	 * @param id The Integer ReportDesign id
	 * @return the matching {@link ReportDesign} object
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public ReportDesign getReportDesign(Integer id) throws APIException;
	
	/**
	 * Return a list of {@link ReportDesign}s, optionally including those that are retired
	 * @param includeRetired if true, indicates that retired {@link ReportDesign}s should also be included
	 * @return a List<ReportDesign> object containing all of the {@link ReportDesign}s
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<ReportDesign> getAllReportDesigns(boolean includeRetired) throws APIException;
	
	/**
	 * Return a list of {@link ReportDesign}s for {@link ReportDefinition} that match the passed parameters
	 * Each input parameter can be null, restricting the returned results only if it is not null.  This allows you
	 * to retrieve all ReportDesigns by ReportDefinition, by RendererType, by retired status, or a combination of these
	 * criteria.
	 * @param reportDefinitionId if not null, only {@link ReportDesign}s for this {@link ReportDefinition} will be returned
	 * @param rendererType if not null, only {@link ReportDesign}s for this {@link ReportRenderer} type will be returned
	 * @param includeRetired if true, indicates that retired {@link ReportDesign}s should also be included
	 * @return a List<ReportDesign> object containing all of the {@link ReportDesign}s
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<ReportDesign> getReportDesigns(ReportDefinition reportDefinition, Class<? extends ReportRenderer> rendererType, 
											   boolean includeRetired) throws APIException;
	
	/**
	 * Save or update the given <code>ReportDesign</code> in the database. If this is a new
	 * ReportDesign, the returned ReportDesign will have a new
	 * {@link ReportDesign#getId()} inserted into it that was generated by the database
	 * 
	 * @param reportDesign The <code>ReportDesign</code> to save or update
	 * @throws APIException
	 */
	public ReportDesign saveReportDesign(ReportDesign reportDesign) throws APIException;
	
	/**
	 * Purges a <code>ReportDesign</code> from the database.
	 * @param reportDesign The <code>ReportDesign</code> to remove from the system
	 * @throws APIException
	 */
	public void purgeReportDesign(ReportDesign reportDesign);
	
	/**
	 * Returns a Collection<ReportRenderer> of all registered ReportRenderers
	 * 
	 * @return All registered report renderers
	 */
	@Transactional(readOnly = true)
	public Collection<ReportRenderer> getReportRenderers();
	
	/**
	 * Returns the preferred ReportRenderer for the given class name.
	 * 
	 * @param objectType
	 * @return	the preferred ReportRenderer for the given class name
	 */
	@Transactional(readOnly = true)
	public ReportRenderer getReportRenderer(String className);
	
	/**
	 * Returns the preferred ReportRenderer for the given object type.
	 * 
	 * @param objectType
	 * @return	the preferred ReportRenderer for the given object type
	 */
	@Transactional(readOnly = true)
	public ReportRenderer getPreferredReportRenderer(Class<Object> objectType);
	
	
	/**
	 * Returns a List of {@link RenderingMode}s that the passed {@link ReportDefinition} supports, in
	 * their preferred order
	 * 
	 * @return all rendering modes for the given schema, in their preferred order
	 */
	@Transactional(readOnly = true)
	public List<RenderingMode> getRenderingModes(ReportDefinition schema);


	/**
	 * <pre>
	 * Runs a report synchronously, blocking until the report is ready. This method populates the uuid
	 * field on the ReportRequest that is passed in, and adds the Request to the history.
	 * 
	 * If request specifies a WebRenderer, then the ReportDefinition will be evaluated, and the Report
	 * returned will contain the raw ReportData output, but no rendering will happen.
	 * 
	 * If request specifies a non-WebRenderer, the ReportDefinition will be evaluated <i>and</i> the
	 * data will be rendered, and the Report returned will include raw ReportData and a File.
	 * 
	 * Implementations of this service may choose to run the report directly, or to queue it,
	 * but if they queue it they should do so with HIGHEST priority.
	 * </pre>
	 * 
	 * @param request
	 * @return the result of running the report.
	 * @throws EvaluationException if the report could not be evaluated
	 * 
	 * @should set uuid on the request
	 * @should render the report if a plain renderer is specified
	 * @should not render the report if a web renderer is specified
	 */
	@Transactional(readOnly = true)
	public Report runReport(ReportRequest request) throws EvaluationException;
	
	
	/**
	 * Queues a report to be run asynchronously, returning immediately. The ReportRequest is assigned a uuid,
	 * which allows its result to be fetched later after the run has completed.
	 * 
	 * Once the run is completed, an entry is added to the history.
	 * 
	 * The service will respect the prioritization specified in the ReportRequests.
	 * 
	 * TODO add a callback-on-completion mechanism
	 *
	 * @param request
	 * @return the ReportRequest that was passed in, with its uuid populated.
	 */
	public ReportRequest queueReport(ReportRequest request);

	
	/**
	 * Get a history of ReportRequests that have been run in the past.
	 * 
	 * @return
	 */
	public List<ReportRequest> getCompletedReportRequests();
	
	
	/**
	 * Get all ReportRequests that are queued up to run.
	 * 
	 * @return
	 */
	public List<ReportRequest> getQueuedReportRequests();
	
	
	/**
	 * Get all ReportRequests that have been marked as 'Saved' by the user
	 * 
	 * @return
	 */
	public List<ReportRequest> getSavedReportRequests();
	
	
	/**
	 * Get the result of a previous report run.
	 * 
	 * The request is not re-run, so this method will return quickly, and may return null if no archived result is
	 * available.
	 * 
	 * @param request
	 * @return
	 */
	public Report getReport(ReportRequest request);
		
	
	/**
	 * Mark a report as 'Saved' so that it is not automatically deleted. Normally a daemon thread
	 * will clean out past report runs after some time has passed.
	 * 
	 * @param request
	 */
	public void archiveReportRequest(ReportRequest request); 
	

	/**
	 * Adds a ReportRequest to the history of run reports. You should probably not call this method. 
	 * 
	 * @param request
	 */
	public void addToHistory(ReportRequest request);

	
	/**
	 * Deletes the ReportRequest with the given uuid from the report history.
	 * Also deletes any associated files. 
	 * 
	 * @param uuid
	 */
	public void deleteFromHistory(String uuid);

	
	/**
	 * Finds a historic ReportRequest by its uuid.
	 * 
	 * @param uuid
	 * @return
	 */
	public ReportRequest getReportRequestByUuid(String uuid);

	
	/**
	 * Saves a ReportRequest, for example after editing its labels. 
	 * 
	 * @param req
	 */
	public void saveReportRequest(ReportRequest req);

	
	/**
	 * Finds a historic Report by its uuid.
	 * 
	 * @param uuid
	 * @return
	 */
	public Report getReportByUuid(String uuid);

	
	/**
	 * Finds the most recently run ReportRequest for each ReportDefinition
	 *
	 */
	public Map<ReportDefinition, ReportRequest> getLastReportRequestsByReport();

	/**
	 * Deletes report requests that are not saved, and are older than the value specified by
	 * {@link ReportingConstants#GLOBAL_PROPERTY_DELETE_REPORTS_AGE_IN_HOURS}
	 */
	public void deleteOldReportRequests();

	/**
	 * If there are any reports queued to be run, and we aren't running the maximum number of
	 * parallel reports, then start running the next queued report.
	 */
	public void maybeRunNextQueuedReport();
		
	/**
	 * @return an unmodifiable view of the reports currently being run
	 */
	public Collection<ReportRequest> getInProgress();

	/**
	 * Makes sure that the tasks for DeleteOldReports and RunQueuedReports are scheduled
	 */
	public void ensureScheduledTasksRunning();
	
}

