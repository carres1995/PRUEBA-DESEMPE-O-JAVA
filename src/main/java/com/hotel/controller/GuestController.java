package com.hotel.controller;

import com.hotel.model.Guest;
import com.hotel.service.GuestService;

import java.util.List;

/**
 * Controller for Guest management operations.
 */
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    public void handleSave(String firstName, String lastName, String documentNumber, String email, String phone) {
        Guest guest = new Guest();
        guest.setFirstName(firstName);
        guest.setLastName(lastName);
        guest.setDocumentNumber(documentNumber);
        guest.setEmail(email);
        guest.setPhone(phone);

        guestService.register(guest);
    }

    public void handleUpdate(Long id, String firstName, String lastName, String documentNumber, String email, String phone, boolean isActive) {
        Guest guest = new Guest();
        guest.setId(id);
        guest.setFirstName(firstName);
        guest.setLastName(lastName);
        guest.setDocumentNumber(documentNumber);
        guest.setEmail(email);
        guest.setPhone(phone);
        guest.setActive(isActive);

        guestService.update(guest);
    }

    public void handleDeactivate(Guest guest) {
        guestService.deactivate(guest);
    }

    public List<Guest> listAll() {
        return guestService.listAll();
    }
}
