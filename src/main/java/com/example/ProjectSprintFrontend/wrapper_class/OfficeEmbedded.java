package com.example.ProjectSprintFrontend.wrapper_class;


import com.example.ProjectSprintFrontend.dto.OfficeDTO;

import java.util.List;

public class OfficeEmbedded {
    private List<OfficeDTO> offices;

    public List<OfficeDTO> getOffices() {
        return offices;
    }

    public void setOffices(List<OfficeDTO> offices) {
        this.offices = offices;
    }
}
