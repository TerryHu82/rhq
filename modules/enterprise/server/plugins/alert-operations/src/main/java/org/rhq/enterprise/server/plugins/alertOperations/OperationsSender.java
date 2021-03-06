/*
 * RHQ Management Platform
 * Copyright (C) 2005-2010 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.enterprise.server.plugins.alertOperations;

import org.rhq.core.domain.alert.Alert;
import org.rhq.core.domain.alert.notification.SenderResult;
import org.rhq.core.domain.auth.Subject;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.operation.JobId;
import org.rhq.core.domain.operation.OperationDefinition;
import org.rhq.core.domain.operation.OperationRequestStatus;
import org.rhq.core.domain.operation.bean.ResourceOperationSchedule;
import org.rhq.core.domain.resource.Resource;
import org.rhq.enterprise.server.plugin.pc.alert.AlertSender;
import org.rhq.enterprise.server.util.LookupUtil;

/**
 * Alert sender that triggers an operation on the resource
 * @author Heiko W. Rupp
 * @author Joseph Marques
 */
public class OperationsSender extends AlertSender {

    private static String getResourceOperationHistoryURL(int resourceId) {
        return "/coregui/CoreGUI.html#Resource/" + resourceId + "/Operations/History";
    }

    @Override
    public SenderResult send(Alert alert) {

        OperationInfo info = OperationInfo.load(alertParameters, extraParameters);
        if (info.error != null) {
            return SenderResult.getSimpleFailure(info.error);
        }

        Subject subject = LookupUtil.getSubjectManager().getOverlord(); // TODO get real subject for authz?

        OperationDefinition operation = info.getOperationDefinition();
        Configuration parameters = info.getArguments();

        Resource targetResource = null;
        try {
            targetResource = info.getTargetResource(alert);
        } catch (Throwable t) {
            String message = getResultMessage(info, "could not calculate which resources to execute the operation on: "
                + t.getMessage());
            return SenderResult.getSimpleFailure(message);
        }

        Configuration replacedParameters = null;
        try {
            if (parameters != null) {
                // the parameter-replaced configuration object will be persisted separately from the original
                replacedParameters = parameters.deepCopy(false);

                AlertTokenReplacer replacementEngine = new AlertTokenReplacer(alert, operation, targetResource);
                for (PropertySimple simpleProperty : replacedParameters.getSimpleProperties().values()) {
                    String temp = simpleProperty.getStringValue();
                    if (temp == null) {
                        continue; // do not process 'UNSET' properties
                    }
                    temp = replacementEngine.replaceTokens(temp);
                    simpleProperty.setStringValue(temp);
                }
            }
        } catch (Exception e) {
            String message = getResultMessage(info, "parameterized argument replacement failed with " + e.getMessage());
            return SenderResult.getSimpleFailure(message);
        }

        // Now fire off the operation with no delay and no repetition.
        try {
            String description = "Alert operation for " + alert.getAlertDefinition().getName();
            ResourceOperationSchedule schedule = LookupUtil.getOperationManager().scheduleResourceOperation(subject,
                targetResource.getId(), operation.getName(), 0, 0, 0, 0, replacedParameters, description);
            String message = getResultMessage(info, getHyperLinkForOperationSchedule(subject, targetResource.getId(),
                operation.getName(), schedule.getJobId()));
            return SenderResult.getSimpleDeffered(message);
        } catch (Throwable t) {
            String message = getResultMessage(info, "invocation failed with " + t.getMessage());
            return SenderResult.getSimpleFailure(message);
        }
    }

    /* 
     * if we actually execute the operation at the time of alert sending, we can report the
     * operation's failure/success accurately instead of just returning a "deferred" result
     */
    private String getHyperLinkForOperationSchedule(Subject subject, int resourceId, String operationName, JobId jobId) {
        /*
        OperationManagerLocal operationManager = LookupUtil.getOperationManager();

        ResourceOperationHistoryCriteria criteria = new ResourceOperationHistoryCriteria();
        criteria.addFilterJobId(jobId);
        criteria.addFilterResourceIds(resourceId);
        criteria.addFilterOperationName(operationName);
        criteria.addSortStartTime(PageOrdering.DESC); // put most recent at top of results
        criteria.setPaging(0, 1); // only return one result, in effect the latest

        int retries = 5;
        OperationHistory history = null;
        while (history == null && retries-- > 0) {
            try {
                Thread.sleep(2000);
                PageList<ResourceOperationHistory> histories = operationManager
                    .findResourceOperationHistoriesByCriteria(subject, criteria);
                if (histories.size() > 0 && histories.get(0).getStatus() != OperationRequestStatus.INPROGRESS) {
                    history = histories.get(0);
                }
            } catch (InterruptedException ie) {
                break; // stop if someone is interrupting
            } catch (Throwable t) {
                // getOperationHisotryByJobId throws exception instead of returning null if jobId not found
                continue; // loop and try again
            }
        }

        if (history == null) {
            return "Could not get result status, check operation history "
                + link("here", URL_RESOURCE_OPERATION_HISTORY + "?id=" + resourceId);
        }
        

        OperationRequestStatus status = history.getStatus();
        return "Status was " + decorate(status) + ", see operation details "
            + link("here", URL_RESOURCE_OPERATION_HISTORY_DETAILS + "?id=" + resourceId + "&opId=" + history.getId());
            */
        return "Check the corresponding " + link("operation history", getResourceOperationHistoryURL(resourceId))
            + " for more details.";
    }

    public String decorate(OperationRequestStatus status) {
        if (status == null) {
            return "<unknown>";
        }
        if (status == OperationRequestStatus.SUCCESS) {
            return "<span style=\"color: green; font-weight: bold;\">" + status + "</span>";
        } else if (status == OperationRequestStatus.FAILURE) {
            return "<span style=\"color: red; font-weight: bold;\">" + status + "</span>";
        } else if (status == OperationRequestStatus.CANCELED) {
            return "<span style=\"color: orange; font-weight: bold;\">" + status + "</span>";
        } else {
            return status.toString();
        }
    }

    private String link(String label, String url) {
        return "<a href=\"" + url + "\">" + label + "</a>";
    }

    private String getResultMessage(OperationInfo info, String details) {
        return "Executed " + info + "<br/>" + details;
    }

    @Override
    public String previewConfiguration() {
        OperationInfo info = OperationInfo.load(alertParameters, extraParameters);
        return info.toString();
    }
}
