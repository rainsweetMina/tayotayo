package kroryi.bus2.entity;


import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "route_station")
@NoArgsConstructor
@AllArgsConstructor
public class BusLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ID 자동 생성
    private String bsId;    // 정류소 ID
    private String bsNm;    // 정류소 명
    private int seq;     // 정류소 순서
    private String moveDir;    // 정역(정방향, 역방향) 구분
    private double xPos;    //x 좌표
    private double yPos;    //y 좌표

//    // 직접 모든 필드를 받는 생성자 추가
//    public BusLocation(String bsId, String bsNm, int seq, String moveDir, double xPos, double yPos) {
//        this.bsId = bsId;
//        this.bsNm = bsNm;
//        this.seq = seq;
//        this.moveDir = moveDir;
//        this.xPos = xPos;
//        this.yPos = yPos;
//    }



}
