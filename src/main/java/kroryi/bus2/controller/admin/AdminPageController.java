package kroryi.bus2.controller.admin;

import kroryi.bus2.dto.lost.FoundItemRequestDTO;
import kroryi.bus2.service.lost.FoundItemServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final FoundItemServiceImpl foundItemServiceImpl;

    public AdminPageController(FoundItemServiceImpl foundItemServiceImpl) {
        this.foundItemServiceImpl = foundItemServiceImpl;
    }
    // ✅ 광고 관리 페이지
    @GetMapping("/ad-manage")
    public String adManagePage() {
        return "admin/adManage"; // templates/admin/adManage.html
    }

    // 추후에 더 추가 가능

    @GetMapping("/notice")
    public String noticePage() {
        return "admin/notice"; // templates/admin/notice.h
    }


    // ✅ 습득물 등록/목록/매칭 관리 페이지
    @GetMapping("/found")
    public String foundPage(Model model) {
        model.addAttribute("foundItems", foundItemServiceImpl.getAllFoundItems());
        model.addAttribute("foundItemForm", new FoundItemRequestDTO());
        return "admin/found-list";
    }
    // ✅ qna 관리 페이지
    @GetMapping("/qna")
    public String qnaManagePage() {
        return "admin/qna"; // templates/admin/qna.html
    }





    // 관리자 페이지 레이아웃
    @GetMapping("/dashboard")
    public String AdminPageTest() {
        return "admin_page_test/dashboardTest";
    }


    // 스웨거 페이지 레이아웃
    @GetMapping("/swagger_page")
    public String SwaggerPageTest() {
        return "custom_swagger/customSwagger";
    }






    // 전체 노선 불러오는 페이지
    @GetMapping("/GetAllRoute")
    public String GetAllBusRoute() {
        return "bus/route/getAllRoute";
    }

    // 전체 정류장 불러오는 페이지
    @GetMapping("/GetAllBusStop")
    public String GetAllBusStop() {
        return "bus/bus_stop/getAllBusStop";
    }

    // 버스의 노선 추가 하는 페이지
    @GetMapping("/AddRouteStopLink")
    public String AddBusRoute() {
        return "bus/route/addRouteStopLink";
    }

    // 버스 노선 불러오는 페이지
    @GetMapping("/GetRouteStopLink")
    public String getBusRoute() {
        return "bus/route/getRouteStopLink";
    }

    // 노선 정보 불러오는 페이지
    @GetMapping("/UpdateRouteInfo")
    public String UpdateRoute() {
        return "bus/route/updateRouteInfo";
    }

    // 노선의 순서 변경하는 페이지
    @GetMapping("/UpdateRouteLink")
    public String UpdateRouteLink() {
        return "bus/route/updateRouteLinkSeq";
    }

    // 노선에 정류장 추가하는 페이지
    @GetMapping("/InsertStop")
    public String InsertStop() {
        return "bus/route/InsertStopIntoRouteLink";
    }



    // 정류장을 새로 추가하는 페이지
    @GetMapping("/AddBusStop")
    public String addStop() {
        return "bus/bus_stop/addBusStop";
    }

    // 정류장을 상세정보 보는 페이지
    @GetMapping("/GetBusStop")
    public String getStop() {
        return "bus/bus_stop/getBusStop";
    }

    // 정류장의 정보를 수정하는 페이지
    @GetMapping("/UpdateBusStop")
    public String updateStop() {
        return "bus/bus_stop/updateBusStop";
    }


    // 정류장의 정보를 수정하는 페이지
    @GetMapping("/path-settings")
    public String PathSettings() {
        return "bus/find-settings";
    }
}
