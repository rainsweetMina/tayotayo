package kroryi.bus2.service.admin.lowfloorbus;

import kroryi.bus2.dto.lowfloorbus.CreateLowFloorBusRequestDTO;
import kroryi.bus2.dto.lowfloorbus.LowFloorBusResponseDTO;
import kroryi.bus2.dto.lowfloorbus.UpdateLowFloorBusRequestDTO;
import kroryi.bus2.entity.LowFloorBus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LowFloorBusService {
    LowFloorBusResponseDTO createLowFloorBus(CreateLowFloorBusRequestDTO dto, List<MultipartFile> files);
    LowFloorBusResponseDTO updateLowFloorBus(Long id, UpdateLowFloorBusRequestDTO dto, List<MultipartFile> files);
    void deleteLowFloorBus(Long id);
    List<LowFloorBusResponseDTO> getAllLowFloorBuses();
    LowFloorBusResponseDTO getLowFloorBusById(Long id);
    LowFloorBus findById(Long id);
} 