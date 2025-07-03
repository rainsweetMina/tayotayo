package kroryi.bus2.service.admin.lowfloorbus;

import jakarta.persistence.EntityNotFoundException;
import kroryi.bus2.dto.lowfloorbus.CreateLowFloorBusRequestDTO;
import kroryi.bus2.dto.lowfloorbus.LowFloorBusResponseDTO;
import kroryi.bus2.dto.lowfloorbus.UpdateLowFloorBusRequestDTO;
import kroryi.bus2.entity.LowFloorBus;
import kroryi.bus2.entity.LowFloorBusFile;
import kroryi.bus2.repository.jpa.LowFloorBusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class LowFloorBusServiceImpl implements LowFloorBusService {

    private final LowFloorBusRepository lowFloorBusRepository;

    @Override
    @Transactional
    public LowFloorBusResponseDTO createLowFloorBus(CreateLowFloorBusRequestDTO dto, List<MultipartFile> files) {
        try {
            log.info("저상버스 대체 안내 생성 시작: {}", dto.getTitle());
            LowFloorBus lowFloorBus = new LowFloorBus(dto.getTitle(), dto.getAuthor(), dto.getContent());
            lowFloorBus.setTopNotice(dto.isTopNotice());
            
            // 먼저 LowFloorBus 엔티티를 저장하여 ID를 생성
            LowFloorBus savedLowFloorBus = lowFloorBusRepository.save(lowFloorBus);
            log.info("저상버스 대체 안내 기본 정보 저장 완료: ID={}", savedLowFloorBus.getId());
            
            // ID가 생성된 후 파일 처리
            if (files != null && !files.isEmpty()) {
                log.info("첨부파일 처리 시작: {} 개의 파일", files.size());
                List<LowFloorBusFile> lowFloorBusFiles = processFiles(files, savedLowFloorBus);
                lowFloorBusFiles.forEach(savedLowFloorBus::addFile);
                
                // 파일 정보가 추가된 엔티티를 다시 저장
                savedLowFloorBus = lowFloorBusRepository.save(savedLowFloorBus);
                log.info("첨부파일 처리 완료: {} 개의 파일 저장됨", lowFloorBusFiles.size());
            }
            
            return new LowFloorBusResponseDTO(savedLowFloorBus);
        } catch (Exception e) {
            log.error("저상버스 대체 안내 생성 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 접근 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public LowFloorBusResponseDTO updateLowFloorBus(Long id, UpdateLowFloorBusRequestDTO dto, List<MultipartFile> files) {
        try {
            log.info("저상버스 대체 안내 수정 시작: ID={}, 제목={}", id, dto.getTitle());
            LowFloorBus lowFloorBus = lowFloorBusRepository.findByIdWithFiles(id)
                    .orElseThrow(() -> new EntityNotFoundException("저상버스 대체 안내를 찾을 수 없습니다. ID: " + id));
            
            lowFloorBus.setTitle(dto.getTitle());
            lowFloorBus.setContent(dto.getContent());
            lowFloorBus.setTopNotice(dto.isTopNotice());
            
            if (files != null && !files.isEmpty()) {
                log.info("첨부파일 처리 시작: {} 개의 파일", files.size());
                List<LowFloorBusFile> lowFloorBusFiles = processFiles(files, lowFloorBus);
                lowFloorBus.updateFiles(lowFloorBusFiles);
                log.info("첨부파일 처리 완료");
            }
            
            LowFloorBus updatedLowFloorBus = lowFloorBusRepository.save(lowFloorBus);
            log.info("저상버스 대체 안내 수정 완료: ID={}", id);
            return new LowFloorBusResponseDTO(updatedLowFloorBus);
        } catch (EntityNotFoundException e) {
            log.error("저상버스 대체 안내 수정 실패: 엔티티 없음", e);
            throw e;
        } catch (Exception e) {
            log.error("저상버스 대체 안내 수정 중 오류 발생", e);
            throw new RuntimeException("데이터베이스 접근 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public void deleteLowFloorBus(Long id) {
        LowFloorBus lowFloorBus = lowFloorBusRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("저상버스 대체 안내를 찾을 수 없습니다. ID: " + id));
        
        lowFloorBusRepository.delete(lowFloorBus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowFloorBusResponseDTO> getAllLowFloorBuses() {
        // 탑공지가 먼저 오도록 정렬하여 결과 반환
        List<LowFloorBus> allLowFloorBuses = lowFloorBusRepository.findAll();
        
        // 탑공지 항목과 일반 항목으로 분리 후 각각 날짜순 정렬
        List<LowFloorBusResponseDTO> topNotices = allLowFloorBuses.stream()
                .filter(LowFloorBus::isTopNotice)
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .map(LowFloorBusResponseDTO::new)
                .collect(Collectors.toList());
                
        List<LowFloorBusResponseDTO> normalNotices = allLowFloorBuses.stream()
                .filter(lowFloorBus -> !lowFloorBus.isTopNotice())
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .map(LowFloorBusResponseDTO::new)
                .collect(Collectors.toList());
                
        // 탑공지를 먼저 두고 일반 공지를 뒤에 추가
        List<LowFloorBusResponseDTO> result = new ArrayList<>(topNotices);
        result.addAll(normalNotices);
        
        return result;
    }

    @Override
    @Transactional
    public LowFloorBusResponseDTO getLowFloorBusById(Long id) {
        LowFloorBus lowFloorBus = lowFloorBusRepository.findByIdWithFiles(id)
                .orElseThrow(() -> new EntityNotFoundException("저상버스 대체 안내를 찾을 수 없습니다. ID: " + id));
        
        lowFloorBus.increaseViewCount();
        lowFloorBusRepository.save(lowFloorBus);
        
        return new LowFloorBusResponseDTO(lowFloorBus);
    }

    @Override
    @Transactional(readOnly = true)
    public LowFloorBus findById(Long id) {
        return lowFloorBusRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("저상버스 대체 안내를 찾을 수 없습니다. ID: " + id));
    }
    
    @Override
    @Transactional
    public LowFloorBusResponseDTO toggleTopNotice(Long id, boolean topNotice) {
        LowFloorBus lowFloorBus = lowFloorBusRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("저상버스 대체 안내를 찾을 수 없습니다. ID: " + id));
                
        log.info("저상버스 대체 안내 탑공지 상태 변경: ID={}, 제목={}, 탑공지={} -> {}",
                id, lowFloorBus.getTitle(), lowFloorBus.isTopNotice(), topNotice);
                
        lowFloorBus.setTopNotice(topNotice);
        LowFloorBus updatedLowFloorBus = lowFloorBusRepository.save(lowFloorBus);
        
        log.info("저상버스 대체 안내 탑공지 상태 변경 완료: ID={}", id);
        return new LowFloorBusResponseDTO(updatedLowFloorBus);
    }
    
    private List<LowFloorBusFile> processFiles(List<MultipartFile> files, LowFloorBus lowFloorBus) {
        List<LowFloorBusFile> lowFloorBusFiles = new ArrayList<>();
        
        if (files == null || files.isEmpty()) {
            return lowFloorBusFiles;
        }
        
        // ID가 없으면 파일 처리를 진행하지 않음
        if (lowFloorBus.getId() == null) {
            log.error("LowFloorBus ID가 null입니다. 파일 처리를 건너뜁니다.");
            return lowFloorBusFiles;
        }
        
        Path uploadPath = Paths.get("uploads", "lowfloorbuses", String.valueOf(lowFloorBus.getId()));
        
        try {
            log.info("파일 업로드 경로 생성: {}", uploadPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성됨: {}", uploadPath);
            }
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    log.info("빈 파일 건너뜀");
                    continue;
                }
                
                String originalFileName = file.getOriginalFilename();
                String storedFileName = UUID.randomUUID() + "_" + originalFileName;
                Path filePath = uploadPath.resolve(storedFileName);
                
                log.info("파일 저장 시작: 원본명={}, 저장명={}", originalFileName, storedFileName);
                Files.copy(file.getInputStream(), filePath);
                log.info("파일 저장 완료: {}", filePath);
                
                LowFloorBusFile lowFloorBusFile = new LowFloorBusFile(
                        originalFileName,
                        storedFileName,
                        file.getContentType(),
                        file.getSize()
                );
                
                lowFloorBusFiles.add(lowFloorBusFile);
                log.info("파일 엔티티 생성됨: {}", lowFloorBusFile.getOriginalName());
            }
        } catch (IOException e) {
            log.error("파일 저장 중 IO 오류 발생", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("파일 처리 중 예상치 못한 오류 발생", e);
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
        }
        
        return lowFloorBusFiles;
    }
} 