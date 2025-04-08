package kroryi.bus2.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "bus_fare")
public class BusFare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bus_type")
    private String busType;
    // (General, Express, Direct)

    @Column(name = "pay_type")
    private String payType;

    @Column(name = "fare_adult")
    private Integer fareAdult;

    @Column(name = "fare_teen")
    private Integer fareTeen;

    @Column(name = "fare_child")
    private Integer fareChild;

    @Transient
    public String getBusTypeName() {
        return switch (busType) {
            case "G" -> "일반버스";
            case "E" -> "급행버스";
            case "D" -> "직행버스";
            default -> busType;
        };
    }

    @Transient
    public String getPayTypeName() {
        return switch (payType) {
            case "card" -> "카드";
            case "cash" -> "현금";
            default -> payType;
        };
    }

}
