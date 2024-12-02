package com.vanguard.assessment.service;

import com.vanguard.assessment.dto.ImportCsvResult;

import java.io.InputStream;

public interface GameDataService {

    ImportCsvResult importCsv(InputStream csvInputStream) throws Exception;

}
