package com.unipi.findoctor.controllers;

import com.unipi.findoctor.constants.ControllerConstants;
import com.unipi.findoctor.dto.AppointmentDetailsDto;
import com.unipi.findoctor.dto.UserDto;
import com.unipi.findoctor.security.SecurityUtil;
import com.unipi.findoctor.services.AppointmentService;
import com.unipi.findoctor.services.DoctorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class RestApiController {
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final SecurityUtil securityUtil;

    @GetMapping("/available-time-slots")
    public ResponseEntity<Map<String, Boolean>> scheduleAppointment(@RequestParam(value = "date", required = true) String dateString,
                                                                    @RequestParam(value = "doctorUsername", required = true) String doctorUsername) {

        LocalDate date;

        // Check if date is in the correct format
        try {
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        // Check if the doctorUsername exists
        if (!doctorService.doctorExists(doctorUsername)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Boolean> time_slots = appointmentService.getDoctorAvailableTimeSlots(doctorUsername, date);

        // Return a response
        return ResponseEntity.ok(time_slots);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDetailsDto>> getAppointments(@RequestParam(value = "date") String dateString) {
        LocalDate date;

        // Check if date is in the correct format
        try {
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        UserDto userDto = securityUtil.getSessionUser();

        if (userDto == null || !userDto.getUserType().equals(ControllerConstants.USER_TYPE_DOCTOR)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<AppointmentDetailsDto> appointmentDetailDtos = appointmentService.fetchDoctorAppointments(userDto.getUsername(), date);

        return ResponseEntity.ok(appointmentDetailDtos);
    }

    @DeleteMapping("/appointments/delete/{id}")
    public ResponseEntity deleteAppointment(@PathVariable("id") String appointmentID) {

        Long id;

        // Check if id is Long
        try {
            id = Long.parseLong(appointmentID);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        UserDto userDto = securityUtil.getSessionUser();

        if (userDto == null || !userDto.getUserType().equals(ControllerConstants.USER_TYPE_DOCTOR)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            appointmentService.deleteById(id, userDto.getUsername());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
