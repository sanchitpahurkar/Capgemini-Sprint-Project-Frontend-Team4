package com.example.ProjectSprintFrontend.service;


import com.example.ProjectSprintFrontend.dto.EmployeeDTO;
import com.example.ProjectSprintFrontend.dto.EmployeePageResponse;
import com.example.ProjectSprintFrontend.dto.OfficeDTO;
import com.example.ProjectSprintFrontend.dto.OfficePageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class OfficeApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "http://localhost:8080/offices";
    private static final String PROJECTION = "officeList";

    public OfficeApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String createOffice(OfficeDTO office) {
        String url = BASE_URL;

        try {
            restTemplate.postForEntity(url, office, String.class);
            return null;
        } catch (HttpClientErrorException e) {
            return extractMeaningfulErrorMessage(e);
        } catch (Exception e) {
            return "Office creation failed due to an unexpected error.";
        }
    }

    public OfficePageResponse getOffices(int page, int size, String field, String value) {
        String url = buildUrl(page, size, field, value);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            JsonNode officesNode = root.path("_embedded").path("offices");
            JsonNode pageNode = root.path("page");

            List<OfficeDTO> offices = new ArrayList<>();
            if (officesNode.isArray()) {
                for (JsonNode node : officesNode) {
                    OfficeDTO office = objectMapper.treeToValue(node, OfficeDTO.class);
                    offices.add(office);
                }
            }

            OfficePageResponse pageResponse = new OfficePageResponse();
            pageResponse.setOffices(offices);

            int currentPage = pageNode.path("number").asInt();
            int totalPages = pageNode.path("totalPages").asInt();
            long totalElements = pageNode.path("totalElements").asLong();

            pageResponse.setCurrentPage(currentPage);
            pageResponse.setTotalPages(totalPages);
            pageResponse.setTotalElements(totalElements);
            pageResponse.setHasPrevious(currentPage > 0);
            pageResponse.setHasNext(currentPage < totalPages - 1);

            return pageResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch offices", e);
        }
    }

    private String buildUrl(int page, int size, String field, String value) {
        String encodedValue = value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);

        if (field == null || field.isBlank() || value == null || value.isBlank()) {
            return BASE_URL + "?projection=" + PROJECTION + "&page=" + page + "&size=" + size;
        }

        return switch (field) {
            case "city" -> BASE_URL + "/search/by-city?city=" + encodedValue
                    + "&projection=" + PROJECTION + "&page=" + page + "&size=" + size;

            case "state" -> BASE_URL + "/search/by-state?state=" + encodedValue
                    + "&projection=" + PROJECTION + "&page=" + page + "&size=" + size;

            case "country" -> BASE_URL + "/search/by-country?country=" + encodedValue
                    + "&projection=" + PROJECTION + "&page=" + page + "&size=" + size;

            case "territory" -> BASE_URL + "/search/by-territory?territory=" + encodedValue
                    + "&projection=" + PROJECTION + "&page=" + page + "&size=" + size;

            default -> BASE_URL + "?projection=" + PROJECTION + "&page=" + page + "&size=" + size;
        };
    }

    public OfficeDTO getOfficeByCode(String officeCode) {
        String url = BASE_URL + "/" + officeCode;

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            return objectMapper.treeToValue(root, OfficeDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch office with code: " + officeCode, e);
        }
    }

    public String updateOffice(String officeCode, OfficeDTO office) {
        String url = BASE_URL + "/" + officeCode;

        try {
            restTemplate.put(url, office);
            return null;
        } catch (HttpClientErrorException e) {
            return extractMeaningfulErrorMessage(e);
        } catch (Exception e) {
            return "Update failed due to an unexpected error.";
        }
    }

    private String extractMeaningfulErrorMessage(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            JsonNode root = objectMapper.readTree(responseBody);

            if (root.has("message") && !root.get("message").asText().isBlank()) {
                return root.get("message").asText();
            }

            if (root.has("errors") && root.get("errors").isArray() && !root.get("errors").isEmpty()) {
                JsonNode firstError = root.get("errors").get(0);

                if (firstError.has("defaultMessage")) {
                    return firstError.get("defaultMessage").asText();
                }

                if (firstError.has("message")) {
                    return firstError.get("message").asText();
                }
            }

            if (root.has("error") && !root.get("error").asText().isBlank()) {
                return root.get("error").asText();
            }

            HttpStatusCode statusCode = e.getStatusCode();
            return "Update failed with status: " + statusCode;

        } catch (Exception parseException) {
            return "Update failed: invalid data or constraint violation.";
        }
    }

    public EmployeePageResponse getEmployeesByOfficeCode(String officeCode, int page, int size) {
        String url = "http://localhost:8080/employees/search/by-office-code"
                + "?officeCode=" + officeCode
                + "&projection=employeeList"
                + "&page=" + page
                + "&size=" + size;

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            JsonNode employeesNode = root.path("_embedded").path("employees");
            JsonNode pageNode = root.path("page");

            List<EmployeeDTO> employees = new ArrayList<>();
            if (employeesNode.isArray()) {
                for (JsonNode node : employeesNode) {
                    EmployeeDTO employee = objectMapper.treeToValue(node, EmployeeDTO.class);
                    employees.add(employee);
                }
            }

            EmployeePageResponse pageResponse = new EmployeePageResponse();
            pageResponse.setEmployees(employees);

            int currentPage = pageNode.path("number").asInt();
            int totalPages = pageNode.path("totalPages").asInt();
            long totalElements = pageNode.path("totalElements").asLong();

            pageResponse.setCurrentPage(currentPage);
            pageResponse.setTotalPages(totalPages);
            pageResponse.setTotalElements(totalElements);
            pageResponse.setHasPrevious(currentPage > 0);
            pageResponse.setHasNext(currentPage < totalPages - 1);

            return pageResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch employees for office code: " + officeCode, e);
        }
    }
}