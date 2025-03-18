package kroryi.bus2.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusLocationDTO {
    private String bsId;    // 정류소 ID
    private String bsNm;    // 정류소 명
    private int seq;        // 정류소 순서
    private String moveDir; // 정역(정방향, 역방향) 구분
    private double xPos;    // x 좌표
    private double yPos;    // y 좌표

//    // 기본 생성자
//    public BusLocationDTO() {}
//
//    // 모든 필드를 포함한 생성자
//    public BusLocationDTO(String bsId, String bsNm, int seq, String moveDir, double xPos, double yPos) {
//        this.bsId = bsId;
//        this.bsNm = bsNm;
//        this.seq = seq;
//        this.moveDir = moveDir;
//        this.xPos = xPos;
//        this.yPos = yPos;
//    }
//
//    // Getter 메서드
//    public String getBsId() {
//        return bsId;
//    }
//
//    public String getBsNm() {
//        return bsNm;
//    }
//
//    public int getSeq() {
//        return seq;
//    }
//
//    public String getMoveDir() {
//        return moveDir;
//    }
//
//    public double getXPos() {
//        return xPos;
//    }
//
//    public double getYPos() {
//        return yPos;
//    }
//
//    // Setter 메서드
//    public void setBsId(String bsId) {
//        this.bsId = bsId;
//    }
//
//    public void setBsNm(String bsNm) {
//        this.bsNm = bsNm;
//    }
//
//    public void setSeq(int seq) {
//        this.seq = seq;
//    }
//
//    public void setMoveDir(String moveDir) {
//        this.moveDir = moveDir;
//    }
//
//    public void setXPos(double xPos) {
//        this.xPos = xPos;
//    }
//
//    public void setYPos(double yPos) {
//        this.yPos = yPos;
//    }
//
//    // toString() 메서드
//    @Override
//    public String toString() {
//        return "BusLocationDTO{" +
//                "bsId='" + bsId + '\'' +
//                ", bsNm='" + bsNm + '\'' +
//                ", seq=" + seq +
//                ", moveDir='" + moveDir + '\'' +
//                ", xPos=" + xPos +
//                ", yPos=" + yPos +
//                '}';
//    }
}
