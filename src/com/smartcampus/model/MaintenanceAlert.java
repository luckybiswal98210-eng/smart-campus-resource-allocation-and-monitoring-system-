package com.smartcampus.model;

import java.sql.Date;

/**
 * Model representing an Equipment or Facility Maintenance Alert
 */
public class MaintenanceAlert {
    private int alertId;
    private int resourceId;
    private String issue;
    private String severity; // 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    private Date reportedDate;
    private boolean isResolved;

    private String resourceName;

    public MaintenanceAlert() {}

    public MaintenanceAlert(int alertId, int resourceId, String issue, String severity, Date reportedDate, boolean isResolved) {
        this.alertId = alertId;
        this.resourceId = resourceId;
        this.issue = issue;
        this.severity = severity;
        this.reportedDate = reportedDate;
        this.isResolved = isResolved;
    }

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public Date getReportedDate() { return reportedDate; }
    public void setReportedDate(Date reportedDate) { this.reportedDate = reportedDate; }

    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { isResolved = resolved; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
}
