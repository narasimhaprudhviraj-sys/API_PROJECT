package com.example.onlinecourse.dto;

public class DashboardStatsDTO {

    private long totalStudents;
    private long totalFaculty;
    private long totalBranches;
    private long totalMinorStreams;
    private long totalCourses;
    private long activeWindows;
    private long totalRegistrations;
    private long availableCourses;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(long totalStudents, long totalFaculty, long totalBranches,
                             long totalMinorStreams, long totalCourses, long activeWindows,
                             long totalRegistrations, long availableCourses) {
        this.totalStudents = totalStudents;
        this.totalFaculty = totalFaculty;
        this.totalBranches = totalBranches;
        this.totalMinorStreams = totalMinorStreams;
        this.totalCourses = totalCourses;
        this.activeWindows = activeWindows;
        this.totalRegistrations = totalRegistrations;
        this.availableCourses = availableCourses;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalFaculty() {
        return totalFaculty;
    }

    public void setTotalFaculty(long totalFaculty) {
        this.totalFaculty = totalFaculty;
    }

    public long getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(long totalBranches) {
        this.totalBranches = totalBranches;
    }

    public long getTotalMinorStreams() {
        return totalMinorStreams;
    }

    public void setTotalMinorStreams(long totalMinorStreams) {
        this.totalMinorStreams = totalMinorStreams;
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getActiveWindows() {
        return activeWindows;
    }

    public void setActiveWindows(long activeWindows) {
        this.activeWindows = activeWindows;
    }

    public long getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(long totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public long getAvailableCourses() {
        return availableCourses;
    }

    public void setAvailableCourses(long availableCourses) {
        this.availableCourses = availableCourses;
    }
}
