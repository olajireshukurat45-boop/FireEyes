package sms.com.sms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sms.com.sms.dto.DetectorDTO;
import sms.com.sms.service.GasDetectorService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gas-detectors")
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class RegisterGasDetector {
@Autowired
    private  GasDetectorService gasDetectorService;

    @Operation(summary = "Register a new gas detector")
    @PostMapping("/admin/register")
    public ResponseEntity<?> registerDetector(@RequestBody DetectorDTO dto) {
        try {
            DetectorDTO saved = gasDetectorService.create(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Smoke Detector registration failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Assign detector to user")
    @PostMapping("/user/assign")
    public ResponseEntity<String> assignDetector(
            @RequestParam String phonenumber,
            @RequestParam String macAddress) {
        gasDetectorService.assignDetectorToUser(phonenumber, macAddress);
        return ResponseEntity.ok("Successfully Linked");
    }

    @Operation(summary = "Get gas detector by MAC address")
    @GetMapping("/admin/getDetector")
    public ResponseEntity<DetectorDTO> getDetector(@RequestParam String macAddress) {
        return gasDetectorService.findByMac(macAddress)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all gas detectors (paginated and sorted)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "403", description = "Access Denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/admin/all")
    public ResponseEntity<List<DetectorDTO>> getAllGasDetectors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "macAddress") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DetectorDTO> pagedResult = gasDetectorService.getAllPaged(pageable);
        return ResponseEntity.ok(pagedResult.getContent());
    }

    @Operation(summary = "Update gas detector by MAC address")
    @PutMapping("/admin/update/{macAddress}")
    public ResponseEntity<?> updateDetector(@PathVariable String macAddress, @RequestBody DetectorDTO dto) {
        try {
            DetectorDTO updated = gasDetectorService.update(macAddress, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Operation(summary = "Delete gas detector by MAC address")
    @DeleteMapping("/admin/delete/{macAddress}")
    public ResponseEntity<?> deleteDetector(@PathVariable String macAddress) {
        gasDetectorService.delete(macAddress);
        return ResponseEntity.ok("Detector deleted successfully");
    }
     @PostMapping("/users/{macAddress}/notify")
public ResponseEntity<String> notifyUsers(
        @PathVariable String macAddress,
        @RequestBody Map<String, String> requestBody) {

    String message = requestBody.get("message");
    if (message == null || message.isEmpty()) {
        return ResponseEntity.badRequest().body("Message is required");
    }

    String result = gasDetectorService.notifyUsersByDetector(macAddress, message);
    return ResponseEntity.ok(result);
}

}
