package com.amortix.prime.service;

import com.amortix.prime.model.MonthlyRecoveryRun;

import java.io.InputStream;
import java.util.List;

public interface MonthlyRecoveryService {
    MonthlyRecoveryRun generateDemandSchedule(int month, int year, String operatorUser);
    MonthlyRecoveryRun findByRunCode(String runCode);
    List<MonthlyRecoveryRun> listAllRuns();

    String exportDemandScheduleAsText(String runCode);

    MonthlyRecoveryRun reconcilePayrollResponse(String runCode, InputStream responseFileStream, String operatorUser);
}
