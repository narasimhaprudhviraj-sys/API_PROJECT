package com.example.onlinecourse.service;

import com.example.onlinecourse.dto.MinorStreamDTO;
import java.util.List;

public interface MinorStreamService {
    List<MinorStreamDTO> getAllMinorStreams();
    List<MinorStreamDTO> getMinorStreamsByBranch(Long branchId);
    List<MinorStreamDTO> getActiveMinorStreamsByBranch(Long branchId);
    MinorStreamDTO getMinorStreamById(Long id);
    MinorStreamDTO createMinorStream(MinorStreamDTO dto);
    MinorStreamDTO updateMinorStream(Long id, MinorStreamDTO dto);
    void deleteMinorStream(Long id);
    void toggleMinorStreamStatus(Long id);
}
