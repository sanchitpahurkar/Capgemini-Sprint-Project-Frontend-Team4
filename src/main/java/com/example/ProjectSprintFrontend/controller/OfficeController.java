package com.example.ProjectSprintFrontend.controller;


import com.example.ProjectSprintFrontend.dto.EmployeePageResponse;
import com.example.ProjectSprintFrontend.dto.OfficeDTO;
import com.example.ProjectSprintFrontend.dto.OfficePageResponse;
import com.example.ProjectSprintFrontend.service.OfficeApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OfficeController {

    private final OfficeApiService officeApiService;

    public OfficeController(OfficeApiService officeApiService) {
        this.officeApiService = officeApiService;
    }

    @GetMapping("/offices")
    public String getOffices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String value,
            Model model
    ) {
        OfficePageResponse officePage = officeApiService.getOffices(page, size, field, value);

        model.addAttribute("officePage", officePage);
        model.addAttribute("offices", officePage.getOffices());
        model.addAttribute("size", size);
        model.addAttribute("field", field);
        model.addAttribute("value", value);

        return "offices";
    }

    @GetMapping("/offices/view/{officeCode}")
    public String viewOffice(@PathVariable String officeCode, Model model) {
        OfficeDTO office = officeApiService.getOfficeByCode(officeCode);
        model.addAttribute("office", office);
        return "office-details";
    }

    @GetMapping("/offices/update/{officeCode}")
    public String showUpdateForm(@PathVariable String officeCode, Model model) {
        OfficeDTO office = officeApiService.getOfficeByCode(officeCode);
        model.addAttribute("office", office);
        return "office-update";
    }

    @PostMapping("/offices/update/{officeCode}")
    public String updateOffice(
            @PathVariable String officeCode,
            @ModelAttribute OfficeDTO office,
            Model model
    ) {
        office.setOfficeCode(officeCode);

        String errorMessage = officeApiService.updateOffice(officeCode, office);

        if (errorMessage != null) {
            model.addAttribute("office", office);
            model.addAttribute("errorMessage", errorMessage);
            return "office-update";
        }

        return "redirect:/offices";
    }

    @GetMapping("/offices/{officeCode}/employees")
    public String viewEmployeesByOffice(
            @PathVariable String officeCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        EmployeePageResponse employeePage = officeApiService.getEmployeesByOfficeCode(officeCode, page, size);

        model.addAttribute("employeePage", employeePage);
        model.addAttribute("employees", employeePage.getEmployees());
        model.addAttribute("officeCode", officeCode);
        model.addAttribute("size", size);

        return "office-employees";
    }

    @GetMapping("/offices/add")
    public String showAddOfficeForm(Model model) {
        model.addAttribute("office", new OfficeDTO());
        return "office-add";
    }

    @PostMapping("/offices/add")
    public String addOffice(@ModelAttribute OfficeDTO office, Model model) {
        String errorMessage = officeApiService.createOffice(office);

        if (errorMessage != null) {
            model.addAttribute("office", office);
            model.addAttribute("errorMessage", errorMessage);
            return "office-add";
        }

        return "redirect:/offices";
    }
}