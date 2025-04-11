package kroryi.bus2.entity;

import jakarta.persistence.*;
import kroryi.bus2.converter.StringListConverter;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "bus_company")
public class BusCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_address")
    private String companyAddress;

    @Column(name = "company_phone")
    private String companyPhone;

    @Convert(converter = StringListConverter.class)
    @Column(name = "company_route_no", columnDefinition = "TEXT")
    private List<String> companyRouteNo;
}
