package com.vanguard.assessment.service;

import com.vanguard.assessment.dto.AggregateGameSalesQueryResult;
import com.vanguard.assessment.dto.GameSalesDTO;
import com.vanguard.assessment.dto.GameSalesCriteria;
import com.vanguard.assessment.dto.GameSalesQueryResult;
import com.vanguard.assessment.dto.AggregatedGameSalesDTO;
import org.springframework.data.domain.Pageable;

public interface GameSalesService {

    GameSalesQueryResult<GameSalesDTO> getGameSales(GameSalesCriteria criteria, Pageable page);

    AggregateGameSalesQueryResult<AggregatedGameSalesDTO> getTotalSales(GameSalesCriteria criteria);

}
